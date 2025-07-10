package com.eventmaster.model.facade;

import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.pattern.factory.TipoEntrada;
import com.eventmaster.model.pattern.factory.Entrada;
import com.eventmaster.model.pattern.factory.TipoEntradaFactory;
import com.eventmaster.model.pattern.chain_of_responsibility.ValidacionHandler;
import com.eventmaster.service.EventoService;
import com.eventmaster.service.UsuarioService; // Asumiendo un servicio de usuario para actualizar historial
import com.eventmaster.service.ProcesadorPago; // CAMBIO: Interfaz en lugar de clase concreta PagoService
import com.eventmaster.service.NotificacionService; // Para enviar notificaciones

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap; // Importación añadida
import java.util.UUID;

public class ProcesoCompraFacade {
    private ValidacionHandler cadenaValidacion; // Inicio de la cadena de responsabilidad
    private EventoService eventoService;
    private UsuarioService usuarioService;
    private ProcesadorPago procesadorPago; // CAMBIO: Tipo de campo
    private NotificacionService notificacionService;
    private Map<String, TipoEntradaFactory> entradaFactories; // Para crear diferentes tipos de entrada

    public ProcesoCompraFacade(ValidacionHandler cadenaValidacion,
                               EventoService eventoService,
                               UsuarioService usuarioService,
                               ProcesadorPago procesadorPago, // CAMBIO: Tipo de parámetro
                               NotificacionService notificacionService,
                               Map<String, TipoEntradaFactory> entradaFactories) {
        this.cadenaValidacion = cadenaValidacion;
        this.eventoService = eventoService;
        this.usuarioService = usuarioService;
        this.procesadorPago = procesadorPago; // CAMBIO: Asignación
        this.notificacionService = notificacionService;
        this.entradaFactories = entradaFactories;
    }

    /**
     * Método principal de la fachada para ejecutar todo el proceso de compra.
     * @param usuario El usuario que realiza la compra.
     * @param eventoId El ID del evento.
     * @param tipoEntradaNombre El nombre del tipo de entrada deseado.
     * @param cantidad La cantidad de entradas.
     * @param detallesPago Detalles para el procesamiento del pago (ej. número de tarjeta, etc.).
     * @param codigoDescuento Opcional, un código de descuento a aplicar.
     * @param aplicarMercancia Booleano que indica si se seleccionó la opción de mercancía.
     * @param aplicarDescuento Booleano que indica si se seleccionó la opción de descuento.
     * @return Un objeto Compra si el proceso es exitoso, null en caso contrario.
     */
    public Compra ejecutarProcesoCompra(Usuario usuario,
                                      String eventoId,
                                      String tipoEntradaNombre,
                                      int cantidad,
                                      Map<String, Object> detallesPago,
                                      String codigoDescuento,
                                      boolean aplicarMercancia,
                                      boolean aplicarDescuento) {
        System.out.println("\n--- Iniciando Proceso de Compra (Facade) ---");
        System.out.println("Aplicar Mercancia: " + aplicarMercancia + ", Aplicar Descuento: " + aplicarDescuento);

        Evento evento = eventoService.findEventoById(eventoId)
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado con ID: " + eventoId));

        TipoEntrada tipoEntradaDef = evento.getTiposEntradaDisponibles().get(tipoEntradaNombre);
        if (tipoEntradaDef == null) {
            throw new IllegalArgumentException("Tipo de entrada '" + tipoEntradaNombre + "' no encontrado para el evento '" + evento.getNombre() + "'.");
        }

        // 1. Crear un objeto Compra (contexto)
        Compra nuevaCompra = new Compra(UUID.randomUUID().toString(), usuario, evento);
        // nuevaCompra.setCodigoDescuentoIngresado(codigoDescuento); // Si Compra tiene este campo

        // 1.B Calcular Precio Unitario Final con Decoradores (antes de validaciones)
        double precioUnitarioCalculado = tipoEntradaDef.getPrecioBase();
        String descripcionEntradaDecorada = tipoEntradaDef.getNombreTipo();

        if (aplicarMercancia && tipoEntradaDef.isOfreceMercanciaOpcional()) {
            precioUnitarioCalculado += tipoEntradaDef.getPrecioAdicionalMercancia();
            descripcionEntradaDecorada += " + " + tipoEntradaDef.getDescripcionMercancia();
             System.out.println("ProcesoCompraFacade: Mercancía aplicada. Nuevo precio unitario: " + precioUnitarioCalculado);
        }
        if (aplicarDescuento && tipoEntradaDef.isOfreceDescuentoOpcional()) {
            // Aquí se podría añadir lógica para verificar límites de uso del descuento si existieran
            precioUnitarioCalculado -= tipoEntradaDef.getMontoDescuentoFijo();
            // Corregido: usar getDescripcionDescuento()
            descripcionEntradaDecorada += " (Descuento: " + (tipoEntradaDef.getDescripcionDescuento() != null ? tipoEntradaDef.getDescripcionDescuento() : "Promocional") + " aplicado)";
            System.out.println("ProcesoCompraFacade: Descuento aplicado. Nuevo precio unitario: " + precioUnitarioCalculado);
        }
        if (precioUnitarioCalculado < 0) {
            precioUnitarioCalculado = 0; // El precio no puede ser negativo
        }

        double totalAPagar = precioUnitarioCalculado * cantidad;
        nuevaCompra.setTotalPagado(totalAPagar); // Establecer el total calculado con decoradores
        System.out.println("ProcesoCompraFacade: Precio unitario final con decoradores: " + precioUnitarioCalculado + ", Total a pagar: " + totalAPagar);

        // 2. Ejecutar Cadena de Validación
        // La cadena de validación podría necesitar el precio final si alguna validación depende de él.
        if (!cadenaValidacion.validar(usuario, evento, tipoEntradaDef, cantidad, nuevaCompra)) {
            System.err.println("ProcesoCompraFacade: Validación fallida. Motivo: " + nuevaCompra.getEstadoCompra());
            return null;
        }
        System.out.println("ProcesoCompraFacade: Validaciones superadas.");

        // Si la cadena de validación aplicara promociones que alteran el total, se recalcularía aquí.
        // totalAPagar = nuevaCompra.getTotalPagado(); // Actualizar si la cadena lo modificó.

        // 3. Procesar Pago
        String idTransaccionApp = UUID.randomUUID().toString(); // ID único para esta operación de compra en nuestra app
        ProcesadorPago.ResultadoPago resultadoPago = procesadorPago.procesarPago(usuario, totalAPagar, detallesPago, idTransaccionApp);
        boolean pagoExitoso = resultadoPago.isExito();

        if(pagoExitoso && resultadoPago.getIdTransaccionPasarela() != null) {
            nuevaCompra.setIdTransaccionPasarela(resultadoPago.getIdTransaccionPasarela());
            System.out.println("ProcesoCompraFacade: ID de transacción de pasarela ("+ resultadoPago.getIdTransaccionPasarela() +") guardado en la compra.");
        }

        if (!pagoExitoso) {
            System.err.println("ProcesoCompraFacade: Fallo en el procesamiento del pago. Motivo: " + resultadoPago.getMensaje());
            nuevaCompra.setEstadoCompra("PAGO_FALLIDO");
            return null;
        }
        System.out.println("ProcesoCompraFacade: Pago procesado exitosamente.");
        nuevaCompra.setEstadoCompra("PAGADA_PENDIENTE_CONFIRMACION");

        // 4. Generar Entradas (instancias) y actualizar disponibilidad
        TipoEntradaFactory entradaFactory = entradaFactories.get(tipoEntradaDef.getNombreTipo());
        if (entradaFactory == null) {
            // Usar una fábrica por defecto o lanzar error
            System.err.println("ProcesoCompraFacade: No se encontró una fábrica para el tipo de entrada: " + tipoEntradaDef.getNombreTipo() + ". Usando fábrica general por defecto.");
            entradaFactory = entradaFactories.getOrDefault("General", entradaFactories.values().stream().findFirst().orElse(null));
            if(entradaFactory == null) throw new IllegalStateException("No hay fábricas de entradas configuradas.");
        }

        List<Entrada> entradasGeneradas = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            String idUnicoEntrada = UUID.randomUUID().toString();
            // Los 'extras' para la fábrica podrían venir de tipoEntradaDef o de la solicitud
            Map<String, Object> extrasParaEntrada = new HashMap<>();
            if (tipoEntradaDef.getNombreTipo().toLowerCase().contains("early")) {
                extrasParaEntrada.put("minutosAnticipacion", 60); // Ejemplo
            }
            // El precio pasado a la fábrica de EntradaBase debería ser el precio DESPUÉS de decoradores,
            // o la EntradaBase toma el precio original y los decoradores lo ajustan.
            // Optaremos por: EntradaBase toma el precio original del TipoEntrada, y los decoradores lo modifican.
            // El precioFinalUnitario que se usa para la fábrica debe ser el precio base del tipo de entrada.
            // El precio de la entrada decorada se usará para el total de la compra.

            Entrada entradaBase = entradaFactory.crearEntrada(idUnicoEntrada, tipoEntradaDef, evento, tipoEntradaDef.getPrecioBase(), extrasParaEntrada);
            Entrada entradaDecorada = entradaBase;

            if (aplicarMercancia && tipoEntradaDef.isOfreceMercanciaOpcional()) {
                entradaDecorada = new com.eventmaster.model.pattern.decorator.EntradaConMerchandising(
                    entradaDecorada,
                    tipoEntradaDef.getDescripcionMercancia(),
                    tipoEntradaDef.getPrecioAdicionalMercancia()
                );
            }
            if (aplicarDescuento && tipoEntradaDef.isOfreceDescuentoOpcional()) {
                entradaDecorada = new com.eventmaster.model.pattern.decorator.EntradaConDescuento(
                    entradaDecorada,
                    (tipoEntradaDef.getDescripcionDescuento() != null ? tipoEntradaDef.getDescripcionDescuento() : "Descuento Aplicado"), // Corregido y con fallback
                    tipoEntradaDef.getMontoDescuentoFijo()
                );
            }

            // Verificar que el precio de la entrada decorada coincida con el precioUnitarioConDecoradores
            if (Math.abs(entradaDecorada.getPrecio() - precioUnitarioCalculado) > 0.001) { // Comparación de doubles con tolerancia
                 System.err.println("ALERTA: Discrepancia de precios. Precio unitario calculado: " + precioUnitarioCalculado +
                                   ", Precio entrada decorada: " + entradaDecorada.getPrecio());
                 // Considerar manejo de esta discrepancia, podría ser un error de lógica.
            }

            entradasGeneradas.add(entradaDecorada);
            nuevaCompra.agregarEntrada(entradaDecorada);
        }
        System.out.println("ProcesoCompraFacade: " + cantidad + " entradas generadas (con decoradores aplicados).");

        // Actualizar disponibilidad en el evento y tipo de entrada
        // Esto debería ser una operación atómica o transaccional con el pago.
        evento.venderEntradas(tipoEntradaNombre, cantidad); // Este método ya actualiza tipoEntradaDef internamente
        eventoService.actualizarEvento(evento); // Guardar cambios en el evento (entradasVendidas)

        // 6. Confirmar y Guardar Compra
        nuevaCompra.setEstadoCompra("COMPLETADA");
        nuevaCompra.setFechaCompra(java.time.LocalDateTime.now()); // Actualizar fecha a la de confirmación

        this.compraDAO.save(nuevaCompra); // Guardar la compra en el DAO
        System.out.println("ProcesoCompraFacade: Compra ID " + nuevaCompra.getId() + " guardada en CompraDAO.");

        usuarioService.agregarCompraAlHistorial(usuario, nuevaCompra); // Esto actualiza el objeto Usuario en memoria
        System.out.println("ProcesoCompraFacade: Compra ID " + nuevaCompra.getId() + " procesada para historial de usuario.");

        // 7. Notificar al Usuario
        notificacionService.enviarConfirmacionCompra(usuario, nuevaCompra);
        System.out.println("ProcesoCompraFacade: Notificación de confirmación de compra enviada.");
        System.out.println("--- Proceso de Compra Finalizado (Facade) ---");

        nuevaCompra.generarComprobante(); // Mostrar comprobante (simulado)

        return nuevaCompra;
    }

    // Método helper (ejemplo, no implementado completamente)
    // private double aplicarDescuentosSiExisten(double precioOriginal, Compra compraContexto) {
    //     if (compraContexto.getDescuentoAplicado() > 0) {
    //         return precioOriginal * (1 - compraContexto.getDescuentoAplicado());
    //     }
    //     return precioOriginal;
    // }
}
