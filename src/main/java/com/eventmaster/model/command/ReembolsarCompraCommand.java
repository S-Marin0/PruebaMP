package com.eventmaster.model.command;

import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.entity.Usuario;
import com.eventmaster.service.PagoService;
import com.eventmaster.service.NotificacionService;
// Asumimos que la devolución de entradas al stock se maneja si la compra se cancela completamente.
// Si es un reembolso parcial, las entradas podrían no devolverse.

import java.time.LocalDateTime;

public class ReembolsarCompraCommand implements Command {
    private Compra compraAReembolsar;
    private Usuario usuarioQueSolicita; // Puede ser el asistente o un admin
    private double montoReembolso; // Puede ser total o parcial
    private String motivoReembolso;

    private PagoService pagoService;
    private NotificacionService notificacionService; // Para notificar al usuario
    private LocalDateTime tiempoEjecucion;

    private String estadoOriginalCompra; // Para el undo
    private double montoOriginalPagado; // Para el undo

    public ReembolsarCompraCommand(Compra compraAReembolsar, Usuario usuarioQueSolicita, double montoReembolso, String motivoReembolso,
                                   PagoService pagoService, NotificacionService notificacionService) {
        this.compraAReembolsar = compraAReembolsar;
        this.usuarioQueSolicita = usuarioQueSolicita;
        this.montoReembolso = montoReembolso;
        this.motivoReembolso = motivoReembolso;
        this.pagoService = pagoService;
        this.notificacionService = notificacionService;
    }

    @Override
    public boolean execute() {
        this.tiempoEjecucion = LocalDateTime.now();
        if (compraAReembolsar == null) {
            System.err.println("Comando ReembolsarCompra: La compra a reembolsar es nula.");
            return false;
        }
        if (montoReembolso <= 0 || montoReembolso > compraAReembolsar.getTotalPagado()) {
            System.err.println("Comando ReembolsarCompra: Monto de reembolso inválido (" + montoReembolso +
                               "). Debe ser mayor que 0 y no exceder el total pagado (" + compraAReembolsar.getTotalPagado() + ").");
            return false;
        }

        System.out.println("Comando ReembolsarCompra: Ejecutando para compra ID " + compraAReembolsar.getId() +
                           ", Monto: " + montoReembolso + ", por usuario " + usuarioQueSolicita.getNombre());

        // Solo se pueden reembolsar compras completadas o quizás canceladas si el pago se hizo.
        if (!"COMPLETADA".equals(compraAReembolsar.getEstadoCompra())) {
            System.err.println("Comando ReembolsarCompra: Solo se pueden reembolsar compras 'COMPLETADA'. Estado actual: " + compraAReembolsar.getEstadoCompra());
            return false;
        }

        this.estadoOriginalCompra = compraAReembolsar.getEstadoCompra();
        this.montoOriginalPagado = compraAReembolsar.getTotalPagado();


        boolean reembolsoExitoso = pagoService.procesarReembolso(
            compraAReembolsar.getUsuario(),
            compraAReembolsar.getId(),
            montoReembolso
        );

        if (reembolsoExitoso) {
            // Actualizar estado de la compra
            if (montoReembolso == compraAReembolsar.getTotalPagado()) {
                compraAReembolsar.setEstadoCompra("REEMBOLSADA_TOTAL");
                // Si es reembolso total, se podrían devolver las entradas al stock (similar a CancelarCompraCommand)
                // Aquí se omitiría esa lógica para no duplicar con CancelarCompraCommand,
                // asumiendo que un reembolso total implica una cancelación previa o simultánea.
            } else {
                compraAReembolsar.setEstadoCompra("REEMBOLSADA_PARCIAL");
                // Actualizar el total pagado reflejando el reembolso parcial
                // compraAReembolsar.setTotalPagado(compraAReembolsar.getTotalPagado() - montoReembolso); NO, el total pagado original no cambia.
                // Se podría tener un campo "montoReembolsado" en Compra.
            }

            System.out.println("Comando ReembolsarCompra: Reembolso de " + montoReembolso + " procesado para compra ID " + compraAReembolsar.getId());

            if (notificacionService != null) {
                // notificacionService.notificarReembolsoProcesado(compraAReembolsar.getUsuario(), compraAReembolsar, montoReembolso, motivoReembolso);
                System.out.println("Comando ReembolsarCompra: Notificación de reembolso enviada (simulada).");
            }
            compraAReembolsar.agregarOperacionAlHistorial(this);
            return true;
        } else {
            System.err.println("Comando ReembolsarCompra: Fallo al procesar el reembolso a través del servicio de pago.");
            return false;
        }
    }

    @Override
    public boolean undo() {
        // Deshacer un reembolso es muy problemático y usualmente no se permite o requiere intervención manual.
        // Implicaría volver a cobrar al usuario.
        System.err.println("Comando ReembolsarCompra: La operación de deshacer reembolso no está soportada de forma automática.");
        System.err.println("  Se necesitaría volver a cobrar al usuario: " + compraAReembolsar.getUsuario().getNombre() + " el monto de " + montoReembolso);
        // Si se quisiera implementar, se restauraría el estado de la compra y se intentaría un nuevo cobro.
        // compraAReembolsar.setEstadoCompra(estadoOriginalCompra);
        // pagoService.procesarPago(compraAReembolsar.getUsuario(), montoReembolso, detallesPagoOriginales); // Necesitaríamos detallesPagoOriginales
        return false;
    }

    @Override
    public String getDescription() {
        return "Reembolsar Compra: ID " + (compraAReembolsar != null ? compraAReembolsar.getId() : "N/A") +
               ", Monto: " + montoReembolso + ", Motivo: " + motivoReembolso;
    }

    @Override
    public LocalDateTime getTiempoEjecucion() {
        return tiempoEjecucion;
    }
}
