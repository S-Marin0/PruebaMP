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
     * @return Un objeto Compra si el proceso es exitoso, null en caso contrario.
     */
    public Compra ejecutarProcesoCompra(Usuario usuario, String eventoId, String tipoEntradaNombre, int cantidad, Map<String, Object> detallesPago, String codigoDescuento) {
        System.out.println("\n--- Iniciando Proceso de Compra (Facade) ---");
        Evento evento = eventoService.findEventoById(eventoId)
            .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado con ID: " + eventoId));

        TipoEntrada tipoEntradaDef = evento.getTiposEntradaDisponibles().get(tipoEntradaNombre);
        if (tipoEntradaDef == null) {
            throw new IllegalArgumentException("Tipo de entrada '" + tipoEntradaNombre + "' no encontrado para el evento '" + evento.getNombre() + "'.");
        }

        // 1. Crear un objeto Compra (contexto)
        Compra nuevaCompra = new Compra(UUID.randomUUID().toString(), usuario, evento);
        // nuevaCompra.setCodigoDescuentoIngresado(codigoDescuento); // Si Compra tiene este campo

        // 2. Ejecutar Cadena de Validación
        if (!cadenaValidacion.validar(usuario, evento, tipoEntradaDef, cantidad, nuevaCompra)) {
            System.err.println("ProcesoCompraFacade: Validación fallida. Motivo: " + nuevaCompra.getEstadoCompra()); // Asumiendo que el estado de compra refleja el error
            return null;
        }
        System.out.println("ProcesoCompraFacade: Validaciones superadas.");

        // 3. Calcular Precio Total (podría estar dentro de la validación de promoción o aquí)
        double precioUnitario = tipoEntradaDef.getPrecioBase();
        // Aquí se aplicarían descuentos de `nuevaCompra` si la cadena de validación los modificó.
        // double precioFinalUnitario = aplicarDescuentosSiExisten(precioUnitario, nuevaCompra);
        double precioFinalUnitario = precioUnitario; // Simplificado por ahora
        double totalAPagar = precioFinalUnitario * cantidad;
        nuevaCompra.setTotalPagado(totalAPagar); // Provisional, se confirma después del pago

        System.out.println("ProcesoCompraFacade: Precio total calculado: " + totalAPagar);

        // 4. Procesar Pago
        String idTransaccionApp = UUID.randomUUID().toString(); // ID único para esta operación de compra en nuestra app
        ProcesadorPago.ResultadoPago resultadoPago = procesadorPago.procesarPago(usuario, totalAPagar, detallesPago, idTransaccionApp);
        boolean pagoExitoso = resultadoPago.isExito();

        if(pagoExitoso && resultadoPago.getIdTransaccionPasarela() != null) { // GUARDAR ID DE TRANSACCIÓN DE PASARELA
            nuevaCompra.setIdTransaccionPasarela(resultadoPago.getIdTransaccionPasarela());
            System.out.println("ProcesoCompraFacade: ID de transacción de pasarela ("+ resultadoPago.getIdTransaccionPasarela() +") guardado en la compra.");
        }

        if (!pagoExitoso) {
            System.err.println("ProcesoCompraFacade: Fallo en el procesamiento del pago. Motivo: " + resultadoPago.getMensaje());
            nuevaCompra.setEstadoCompra("PAGO_FALLIDO");
            // Considerar si se debe notificar al usuario aquí
            return null;
        }
        System.out.println("ProcesoCompraFacade: Pago procesado exitosamente.");
        nuevaCompra.setEstadoCompra("PAGADA_PENDIENTE_CONFIRMACION"); // Estado intermedio

        // 5. Generar Entradas (instancias) y actualizar disponibilidad
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
            Entrada entrada = entradaFactory.crearEntrada(idUnicoEntrada, tipoEntradaDef, evento, precioFinalUnitario, extrasParaEntrada);
            // Aquí se podrían aplicar decoradores si es necesario, basados en la compra o promoción
            // ej. if (nuevaCompra.tieneMerchandising()) entrada = new EntradaConMerchandising(entrada, ...);
            entradasGeneradas.add(entrada);
            nuevaCompra.agregarEntrada(entrada);
        }
        System.out.println("ProcesoCompraFacade: " + cantidad + " entradas generadas.");

        // Actualizar disponibilidad en el evento y tipo de entrada
        // Esto debería ser una operación atómica o transaccional con el pago.
        evento.venderEntradas(tipoEntradaNombre, cantidad); // Este método ya actualiza tipoEntradaDef internamente
        eventoService.actualizarEvento(evento); // Guardar cambios en el evento (entradasVendidas)

        // 6. Confirmar Compra
        nuevaCompra.setEstadoCompra("COMPLETADA");
        nuevaCompra.setFechaCompra(java.time.LocalDateTime.now()); // Actualizar fecha a la de confirmación
        usuarioService.agregarCompraAlHistorial(usuario, nuevaCompra);
        System.out.println("ProcesoCompraFacade: Compra ID " + nuevaCompra.getId() + " completada y registrada.");

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
