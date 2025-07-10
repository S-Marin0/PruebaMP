package com.eventmaster.model.command;

import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.entity.Usuario;
import com.eventmaster.service.ProcesadorReembolso; // CAMBIO
import com.eventmaster.service.NotificacionService;
// Asumimos que la devolución de entradas al stock se maneja si la compra se cancela completamente.
// Si es un reembolso parcial, las entradas podrían no devolverse.

import java.time.LocalDateTime;

public class ReembolsarCompraCommand implements Command {
    private Compra compraAReembolsar;
    private Usuario usuarioQueSolicita; // Puede ser el asistente o un admin
    private double montoReembolso; // Puede ser total o parcial
    private String motivoReembolso;

    private ProcesadorReembolso procesadorReembolso; // CAMBIO
    private NotificacionService notificacionService; // Para notificar al usuario
    private LocalDateTime tiempoEjecucion;

    private String estadoOriginalCompra; // Para el undo
    private double montoOriginalPagado; // Para el undo
    // private String idTransaccionReembolsoPasarela; // Para el undo, si fuera posible

    public ReembolsarCompraCommand(Compra compraAReembolsar, Usuario usuarioQueSolicita, double montoReembolso, String motivoReembolso,
                                   ProcesadorReembolso procesadorReembolso, NotificacionService notificacionService) { // CAMBIO
        this.compraAReembolsar = compraAReembolsar;
        this.usuarioQueSolicita = usuarioQueSolicita;
        this.montoReembolso = montoReembolso;
        this.motivoReembolso = motivoReembolso;
        this.procesadorReembolso = procesadorReembolso; // CAMBIO
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

        // ASUNCIÓN: compraAReembolsar tiene un método getIdTransaccionPasarela()
        String idTransaccionOriginalPasarela = compraAReembolsar.getIdTransaccionPasarela();
        if (idTransaccionOriginalPasarela == null || idTransaccionOriginalPasarela.trim().isEmpty()) {
            System.err.println("Comando ReembolsarCompra: No se puede procesar reembolso, falta ID de transacción de pasarela original en la compra ID: " + compraAReembolsar.getId());
            return false;
        }

        boolean reembolsoExitoso = procesadorReembolso.procesarSolicitudReembolso(
            compraAReembolsar,
            montoReembolso,
            motivoReembolso,
            idTransaccionOriginalPasarela
        );

        if (reembolsoExitoso) {
            // El estado de la compra ya se actualiza dentro de procesadorReembolso.procesarSolicitudReembolso
            // y las notificaciones también se podrían manejar allí o aquí.
            // Si procesarSolicitudReembolso ya hace la notificación, no es necesario aquí.
            // if (notificacionService != null) {
            //     // La notificación específica del resultado (monto final, etc.) es mejor desde ProcesadorReembolso
            //     System.out.println("Comando ReembolsarCompra: Notificación de reembolso gestionada por ProcesadorReembolso.");
            // }
            compraAReembolsar.agregarOperacionAlHistorial(this); // El comando se registra a sí mismo
            System.out.println("Comando ReembolsarCompra: Solicitud de reembolso para compra ID " + compraAReembolsar.getId() + " procesada con resultado: " + (reembolsoExitoso ? "Éxito" : "Fallo"));
            return true; // El comando se ejecutó, el resultado del reembolso está en el estado de la compra
        } else {
            System.err.println("Comando ReembolsarCompra: Fallo al procesar la solicitud de reembolso a través de ProcesadorReembolso para compra ID " + compraAReembolsar.getId());
            // El estado de la compra podría haberse quedado como FALLO_REEMBOLSO por ProcesadorReembolso
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
