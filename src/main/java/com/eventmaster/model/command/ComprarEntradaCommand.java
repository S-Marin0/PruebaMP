package com.eventmaster.model.command;

import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.facade.ProcesoCompraFacade; // El facade hará el trabajo pesado

import java.time.LocalDateTime;
import java.util.Map;

public class ComprarEntradaCommand implements Command {
    private Usuario usuario;
    private String eventoId;
    private String tipoEntradaNombre;
    private int cantidad;
    private Map<String, Object> detallesPago;
    private String codigoDescuento; // Opcional

    private ProcesoCompraFacade procesoCompraFacade;
    private Compra compraRealizada; // Para el undo y para obtener el resultado
    private LocalDateTime tiempoEjecucion;

    public ComprarEntradaCommand(Usuario usuario, String eventoId, String tipoEntradaNombre, int cantidad,
                                 Map<String, Object> detallesPago, String codigoDescuento,
                                 ProcesoCompraFacade procesoCompraFacade) {
        this.usuario = usuario;
        this.eventoId = eventoId;
        this.tipoEntradaNombre = tipoEntradaNombre;
        this.cantidad = cantidad;
        this.detallesPago = detallesPago;
        this.codigoDescuento = codigoDescuento;
        this.procesoCompraFacade = procesoCompraFacade;
    }

    public ComprarEntradaCommand(Usuario usuario, String eventoId, String tipoEntradaNombre, int cantidad,
                                 Map<String, Object> detallesPago, ProcesoCompraFacade procesoCompraFacade) {
        this(usuario, eventoId, tipoEntradaNombre, cantidad, detallesPago, null, procesoCompraFacade);
    }


    @Override
    public boolean execute() {
        this.tiempoEjecucion = LocalDateTime.now();
        System.out.println("Comando ComprarEntrada: Ejecutando para usuario " + usuario.getNombre() + ", Evento ID: " + eventoId + ", Tipo: " + tipoEntradaNombre + ", Cant: " + cantidad);
        try {
            this.compraRealizada = procesoCompraFacade.ejecutarProcesoCompra(
                usuario, eventoId, tipoEntradaNombre, cantidad, detallesPago, codigoDescuento
            );
            return this.compraRealizada != null;
        } catch (IllegalArgumentException e) {
            System.err.println("Comando ComprarEntrada: Error de validación - " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Comando ComprarEntrada: Error inesperado durante la ejecución - " + e.getMessage());
            // e.printStackTrace(); // Para depuración
            return false;
        }
    }

    @Override
    public boolean undo() {
        if (compraRealizada == null || !"COMPLETADA".equals(compraRealizada.getEstadoCompra())) {
            System.err.println("Comando ComprarEntrada: No se puede deshacer. La compra no fue completada o no existe.");
            return false;
        }

        System.out.println("Comando ComprarEntrada: Intentando deshacer compra ID: " + compraRealizada.getId());
        // La lógica de deshacer una compra es compleja:
        // 1. Reembolsar el pago (si aplica, y si el PagoService lo soporta).
        // 2. Devolver las entradas al stock del evento/tipo de entrada.
        // 3. Eliminar la compra del historial del usuario.
        // 4. Cambiar el estado de la compra a "REEMBOLSADA" o "CANCELADA_POR_UNDO".

        // Esto es una simplificación. En un sistema real, se necesitaría un CancelarCompraCommand
        // o una lógica de reembolso más robusta invocada aquí.
        // Por ahora, simularemos la cancelación y devolución.

        Evento evento = compraRealizada.getEvento();
        // Devolver cada entrada al tipo correspondiente en el evento
        compraRealizada.getEntradasCompradas().forEach(entrada -> {
            // Necesitamos el nombre del TipoEntrada original para devolverlo correctamente
            // Asumiendo que `entrada.getTipo()` devuelve el nombre del TipoEntrada original
            if (evento.getTiposEntradaDisponibles().containsKey(entrada.getTipo())) {
                 evento.devolverEntradas(entrada.getTipo(), 1); // Devuelve de a una
            }
        });

        // Actualizar el evento en el servicio (si es necesario para persistir cambios en entradasVendidas)
        // procesoCompraFacade.getEventoService().actualizarEvento(evento); // Asumiendo acceso al EventoService

        // Cambiar estado de la compra
        compraRealizada.setEstadoCompra("CANCELADA_POR_UNDO");
        // Eliminar del historial del usuario (o marcarla como cancelada)
        // procesoCompraFacade.getUsuarioService().removerCompraDelHistorial(usuario, compraRealizada); // Asumiendo acceso y método

        // Simular reembolso
        // procesoCompraFacade.getPagoService().procesarReembolso(usuario, compraRealizada.getId(), compraRealizada.getTotalPagado());

        System.out.println("Comando ComprarEntrada: Compra ID " + compraRealizada.getId() + " deshecha (simulado).");
        // compraRealizada = null; // Para evitar múltiples undos sin un redo
        return true;
    }

    @Override
    public String getDescription() {
        return "Comprar Entradas: Evento ID " + eventoId + ", Tipo " + tipoEntradaNombre + ", Cantidad " + cantidad +
               (compraRealizada != null ? " (Compra ID: " + compraRealizada.getId() + ")" : "");
    }

    @Override
    public LocalDateTime getTiempoEjecucion() {
        return tiempoEjecucion;
    }

    public Compra getCompraRealizada() {
        return compraRealizada;
    }
}
