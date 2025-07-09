package com.eventmaster.model.pattern.template_method;

import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.pattern.factory.TipoEntrada;
import com.eventmaster.model.pattern.factory.Entrada;
// Asumimos que los servicios necesarios son inyectados o accesibles
import com.eventmaster.service.PagoService;
import com.eventmaster.service.NotificacionService;
import com.eventmaster.service.EventoService;
import com.eventmaster.service.UsuarioService;


import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class ProcesoCompraTemplate {

    // Servicios que podrían ser necesarios para las subclases o el template
    protected PagoService pagoService;
    protected NotificacionService notificacionService;
    protected EventoService eventoService;
    protected UsuarioService usuarioService;

    public ProcesoCompraTemplate(PagoService pagoService, NotificacionService notificacionService, EventoService eventoService, UsuarioService usuarioService) {
        this.pagoService = pagoService;
        this.notificacionService = notificacionService;
        this.eventoService = eventoService;
        this.usuarioService = usuarioService;
    }

    /**
     * El método plantilla que define el esqueleto del algoritmo de compra.
     * Los pasos específicos son implementados por las subclases o son hooks.
     */
    public final Compra realizarCompraCompleta(Usuario usuario, Evento evento, TipoEntrada tipoEntrada, int cantidad, Map<String, Object> detallesPago, String codigoDescuento) {
        System.out.println("\n--- Template Method: Iniciando Proceso de Compra Completa ---");

        Compra compra = iniciarCompra(usuario, evento, tipoEntrada, cantidad);
        if (compra == null) {
            System.err.println("Template Method: Fallo al iniciar la compra.");
            return null;
        }

        if (!validarDisponibilidad(compra, tipoEntrada, cantidad)) {
            System.err.println("Template Method: Validación de disponibilidad fallida.");
            compra.setEstadoCompra("FALLO_DISPONIBILIDAD");
            return compra; // Devolver la compra con el estado de error
        }
        System.out.println("Template Method: Disponibilidad OK.");

        aplicarPromocionesSiCorresponde(compra, codigoDescuento, tipoEntrada);
        System.out.println("Template Method: Promociones aplicadas (si hubo). Total actual: " + compra.getTotalPagado());


        if (!procesarPago(compra, detallesPago)) {
            System.err.println("Template Method: Procesamiento de pago fallido.");
            compra.setEstadoCompra("FALLO_PAGO");
            return compra;
        }
        compra.setEstadoCompra("PAGADA"); // Marcar como pagada
        System.out.println("Template Method: Pago procesado exitosamente.");

        List<Entrada> entradasGeneradas = generarEntradas(compra, tipoEntrada, cantidad);
        if (entradasGeneradas == null || entradasGeneradas.isEmpty()) {
            System.err.println("Template Method: Fallo al generar las entradas. Se intentará revertir el pago.");
            // Lógica para revertir el pago (puede ser compleja)
            pagoService.procesarReembolso(usuario, compra.getId(), compra.getTotalPagado()); // Simulación
            compra.setEstadoCompra("FALLO_GENERACION_ENTRADAS_PAGO_REVERTIDO");
            return compra;
        }
        entradasGeneradas.forEach(compra::agregarEntrada);
        System.out.println("Template Method: Entradas generadas.");

        actualizarStockEvento(evento, tipoEntrada, cantidad);
        System.out.println("Template Method: Stock del evento actualizado.");

        confirmarYRegistrarCompra(usuario, compra);
        System.out.println("Template Method: Compra confirmada y registrada.");

        notificarUsuario(compra);
        System.out.println("Template Method: Usuario notificado.");

        finalizarCompraHook(compra); // Hook opcional para pasos finales específicos
        System.out.println("--- Template Method: Proceso de Compra Completa Finalizado ---");
        return compra;
    }

    // Pasos abstractos o con implementación por defecto (hooks)

    /**
     * Inicia el objeto Compra con datos básicos.
     */
    protected Compra iniciarCompra(Usuario usuario, Evento evento, TipoEntrada tipoEntrada, int cantidad) {
        Compra compra = new Compra(UUID.randomUUID().toString(), usuario, evento);
        // Calcular un total provisional inicial, se puede ajustar después con promociones
        compra.setTotalPagado(tipoEntrada.getPrecioBase() * cantidad);
        return compra;
    }

    /**
     * Valida la disponibilidad de entradas. Puede ser sobrescrito.
     * Este es un HOOK, tiene una implementación por defecto pero puede ser extendido.
     */
    protected boolean validarDisponibilidad(Compra compra, TipoEntrada tipoEntrada, int cantidad) {
        Evento evento = compra.getEvento();
        if (evento.getEntradasVendidas() + cantidad > evento.getCapacidadTotal()) {
            return false;
        }
        return tipoEntrada.revisarDisponibilidad(cantidad);
    }

    /**
     * Aplica promociones o códigos de descuento.
     * Este es un HOOK.
     */
    protected abstract void aplicarPromocionesSiCorresponde(Compra compra, String codigoDescuento, TipoEntrada tipoEntrada);

    /**
     * Procesa el pago.
     * Este es un paso concreto que usa un servicio.
     */
    protected boolean procesarPago(Compra compra, Map<String, Object> detallesPago) {
        return pagoService.procesarPago(compra.getUsuario(), compra.getTotalPagado(), detallesPago);
    }

    /**
     * Genera las instancias de Entrada.
     * Este es un paso ABSTRACTO que debe ser implementado por subclases (para usar las fábricas correctas).
     */
    protected abstract List<Entrada> generarEntradas(Compra compra, TipoEntrada tipoEntrada, int cantidad);

    /**
     * Actualiza el stock del evento.
     */
    protected void actualizarStockEvento(Evento evento, TipoEntrada tipoEntrada, int cantidad) {
        evento.venderEntradas(tipoEntrada.getNombreTipo(), cantidad);
        if (eventoService != null) {
            eventoService.actualizarEvento(evento);
        }
    }

    /**
     * Confirma la compra y la registra en el historial del usuario.
     */
    protected void confirmarYRegistrarCompra(Usuario usuario, Compra compra) {
        compra.setEstadoCompra("COMPLETADA");
        compra.setFechaCompra(LocalDateTime.now());
        if (usuarioService != null) {
            usuarioService.agregarCompraAlHistorial(usuario, compra);
        }
    }


    /**
     * Notifica al usuario sobre la compra.
     * Este es un HOOK.
     */
    protected void notificarUsuario(Compra compra) {
        if (notificacionService != null) {
            notificacionService.enviarConfirmacionCompra(compra.getUsuario(), compra);
        }
    }

    /**
     * Hook opcional para pasos adicionales al final del proceso.
     */
    protected void finalizarCompraHook(Compra compra) {
        // Por defecto no hace nada. Las subclases pueden implementarlo.
        System.out.println("Template Method Hook: finalizarCompraHook para compra ID " + compra.getId());
    }
}
