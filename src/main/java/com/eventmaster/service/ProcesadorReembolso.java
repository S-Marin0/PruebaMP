package com.eventmaster.service;

import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Evento; // Para devolver entradas
import com.eventmaster.service.ProcesadorPago.ResultadoPago; // Para usar el resultado

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.HashMap;

// Clase específica para la lógica de negocio de los reembolsos.
// Utiliza un ProcesadorPago para la interacción real con la pasarela.
public class ProcesadorReembolso {

    private ProcesadorPago procesadorPagoService; // Servicio de pasarela de pago
    private EventoService eventoService; // Para devolver entradas al stock
    private NotificacionService notificacionService; // Para notificar

    // Políticas de reembolso (ejemplos)
    private Map<String, Double> tarifasComisionReembolso; // motivo -> % comisión
    private int diasLimiteReembolsoTotal; // Días antes del evento para reembolso total

    public ProcesadorReembolso(ProcesadorPago procesadorPagoService, EventoService eventoService, NotificacionService notificacionService) {
        this.procesadorPagoService = procesadorPagoService;
        this.eventoService = eventoService;
        this.notificacionService = notificacionService;
        this.tarifasComisionReembolso = new HashMap<>();
        // Configuración por defecto de políticas
        this.diasLimiteReembolsoTotal = 7; // Reembolso total hasta 7 días antes del evento
        this.tarifasComisionReembolso.put("CANCELACION_USUARIO_TARDE", 0.10); // 10% si cancela tarde
    }

    public void configurarTarifaComision(String motivo, double tarifa) {
        this.tarifasComisionReembolso.put(motivo.toUpperCase(), tarifa);
    }

    public void setDiasLimiteReembolsoTotal(int dias) {
        this.diasLimiteReembolsoTotal = dias;
    }

    /**
     * Valida si una compra es elegible para reembolso según las políticas.
     * @param compra La compra a verificar.
     * @param fechaSolicitud La fecha en que se solicita el reembolso.
     * @return true si es elegible, false en caso contrario.
     */
    public boolean validarElegibilidadReembolso(Compra compra, LocalDateTime fechaSolicitud) {
        if (!"COMPLETADA".equals(compra.getEstadoCompra())) {
            System.out.println("ProcesadorReembolso: Compra " + compra.getId() + " no está 'COMPLETADA', no elegible para reembolso automático.");
            return false; // Solo compras completadas son elegibles (podría variar)
        }

        LocalDateTime fechaEvento = compra.getEvento().getFechaHora();
        long diasHastaEvento = ChronoUnit.DAYS.between(fechaSolicitud, fechaEvento);

        if (diasHastaEvento >= diasLimiteReembolsoTotal) {
            System.out.println("ProcesadorReembolso: Compra " + compra.getId() + " elegible para reembolso total (días hasta evento: " + diasHastaEvento + ").");
            return true;
        } else if (diasHastaEvento >= 0) { // Aún no ha pasado el evento
            System.out.println("ProcesadorReembolso: Compra " + compra.getId() + " podría ser elegible para reembolso parcial o sujeto a comisión (días hasta evento: " + diasHastaEvento + ").");
            // Aquí podrían aplicarse otras reglas (ej. no reembolsable X días antes)
            return true; // Por ahora, permitimos si el evento no ha pasado
        }

        System.out.println("ProcesadorReembolso: Evento para compra " + compra.getId() + " ya ha pasado o está muy próximo. No elegible para reembolso automático.");
        return false;
    }

    /**
     * Calcula el monto a reembolsar después de aplicar comisiones.
     * @param compra La compra original.
     * @param montoOriginalReembolso El monto que se quiere reembolsar (puede ser parcial o total).
     * @param motivo El motivo de la cancelación/reembolso.
     * @return El monto final a reembolsar.
     */
    public double calcularMontoFinalReembolso(Compra compra, double montoOriginalReembolso, String motivo) {
        double montoFinal = montoOriginalReembolso;
        LocalDateTime fechaEvento = compra.getEvento().getFechaHora();
        long diasHastaEvento = ChronoUnit.DAYS.between(LocalDateTime.now(), fechaEvento);

        if (diasHastaEvento < diasLimiteReembolsoTotal && diasHastaEvento >=0) {
            double comision = tarifasComisionReembolso.getOrDefault(motivo.toUpperCase(),
                                 tarifasComisionReembolso.getOrDefault("CANCELACION_USUARIO_TARDE", 0.0));
            montoFinal = montoOriginalReembolso * (1 - comision);
            System.out.println("ProcesadorReembolso: Comisión del " + (comision*100) + "% aplicada por " + motivo + ". Monto a reembolsar: " + montoFinal);
        }
        return Math.max(0, montoFinal); // Asegurar que no sea negativo
    }

    /**
     * Procesa la solicitud de reembolso completa.
     * @param compra La compra a reembolsar.
     * @param montoAReembolsar El monto específico a reembolsar (puede ser parcial).
     * @param motivo El motivo del reembolso.
     * @param idTransaccionPasarelaOriginal El ID de la transacción original en la pasarela.
     * @return true si el reembolso (incluyendo la lógica de negocio) fue exitoso.
     */
    public boolean procesarSolicitudReembolso(Compra compra, double montoAReembolsar, String motivo, String idTransaccionPasarelaOriginal) {
        System.out.println("ProcesadorReembolso: Iniciando procesamiento de reembolso para compra ID " + compra.getId() + ", Monto: " + montoAReembolsar);

        if (!validarElegibilidadReembolso(compra, LocalDateTime.now())) {
            System.err.println("ProcesadorReembolso: La compra no es elegible para reembolso según las políticas.");
            // notificacionService.notificarRechazoReembolso(compra.getUsuario(), compra, "No elegible según políticas.");
            return false;
        }

        double montoFinalAReembolsar = calcularMontoFinalReembolso(compra, montoAReembolsar, motivo);

        if (montoFinalAReembolsar <= 0 && montoAReembolsar > 0) {
             System.out.println("ProcesadorReembolso: El monto final a reembolsar es 0 o menos después de comisiones. No se procesa reembolso en pasarela.");
             // Aún así, se podría marcar la compra como 'REEMBOLSADA_CERO' o similar si se cancelan las entradas.
             compra.setEstadoCompra("REEMBOLSADA_CERO_POR_COMISION");
             // Lógica para devolver entradas si es reembolso total
             if (montoAReembolsar == compra.getTotalPagado()) {
                devolverEntradasAlStock(compra);
             }
             // notificacionService.notificarReembolsoProcesado(compra.getUsuario(), compra, 0, motivo + " (Comisión aplicada completa)");
             return true; // Se considera "exitoso" en términos de proceso, aunque el monto sea cero.
        }

        if (montoFinalAReembolsar <= 0 && montoAReembolsar <= 0){
            System.err.println("ProcesadorReembolso: Monto a reembolsar es cero o negativo. No se procesa.");
            return false;
        }


        ResultadoPago resultadoPasarela = procesadorPagoService.procesarReembolso(idTransaccionPasarelaOriginal, montoFinalAReembolsar);

        if (resultadoPasarela.isExito()) {
            System.out.println("ProcesadorReembolso: Reembolso de " + montoFinalAReembolsar + " exitoso en pasarela. ID Pasarela: " + resultadoPasarela.getIdTransaccionPasarela());

            // Actualizar estado de la compra
            if (montoAReembolsar == compra.getTotalPagado()) { // Si el monto solicitado originalmente era el total
                compra.setEstadoCompra("REEMBOLSADA_TOTAL");
                devolverEntradasAlStock(compra);
            } else {
                compra.setEstadoCompra("REEMBOLSADA_PARCIAL");
                // Si es parcial, las entradas usualmente no se devuelven a menos que se especifique
            }
            // compra.setMontoReembolsado(montoFinalAReembolsar); // Si Compra tiene este campo
            // compra.setTransaccionReembolsoId(resultadoPasarela.getIdTransaccionPasarela());

            if (notificacionService != null) {
                // notificacionService.notificarReembolsoExitoso(compra.getUsuario(), compra, montoFinalAReembolsar, motivo);
                System.out.println("ProcesadorReembolso: Notificación de reembolso exitoso enviada (simulada).");
            }
            return true;
        } else {
            System.err.println("ProcesadorReembolso: Fallo el reembolso en la pasarela de pago. Mensaje: " + resultadoPasarela.getMensaje());
            // compra.setEstadoCompra("FALLO_REEMBOLSO");
            // notificacionService.notificarFalloReembolso(compra.getUsuario(), compra, resultadoPasarela.getMensaje());
            return false;
        }
    }

    private void devolverEntradasAlStock(Compra compra) {
        Evento evento = compra.getEvento();
        if (evento != null && eventoService != null) {
            compra.getEntradasCompradas().forEach(entrada -> {
                // Asumiendo que entrada.getTipo() devuelve el nombre del TipoEntrada original
                if (evento.getTiposEntradaDisponibles().containsKey(entrada.getTipo())) {
                     evento.devolverEntradas(entrada.getTipo(), 1); // Devuelve de a una
                }
            });
            eventoService.actualizarEvento(evento);
            System.out.println("ProcesadorReembolso: Entradas de la compra " + compra.getId() + " devueltas al stock.");
        }
    }
}
