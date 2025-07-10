package com.eventmaster.model.command;

import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Evento;
import com.eventmaster.service.EventoService; // Para actualizar la disponibilidad de entradas
import com.eventmaster.service.ProcesadorPago; // CAMBIO
import com.eventmaster.service.ProcesadorPago.ResultadoPago; // CAMBIO
import com.eventmaster.service.NotificacionService; // Para notificar al usuario

import java.time.LocalDateTime;

public class CancelarCompraCommand implements Command {
    private Compra compraACancelar;
    private Usuario usuarioQueCancela; // Podría ser el asistente o un administrador
    private String motivoCancelacion;

    private EventoService eventoService;
    private ProcesadorPago procesadorPago; // CAMBIO
    private NotificacionService notificacionService;
    private LocalDateTime tiempoEjecucion;

    private String estadoOriginalCompra; // Para el undo
    private int cantidadEntradasDevueltas; // Para el undo
    private String idTransaccionPasarelaOriginal; // Para el undo del reembolso, si fuera posible

    public CancelarCompraCommand(Compra compraACancelar, Usuario usuarioQueCancela, String motivoCancelacion,
                                 EventoService eventoService, ProcesadorPago procesadorPago, NotificacionService notificacionService) { // CAMBIO
        this.compraACancelar = compraACancelar;
        this.usuarioQueCancela = usuarioQueCancela; // Importante para verificar permisos y notificar
        this.motivoCancelacion = motivoCancelacion;
        this.eventoService = eventoService;
        this.procesadorPago = procesadorPago; // CAMBIO
        this.notificacionService = notificacionService;
    }

    @Override
    public boolean execute() {
        this.tiempoEjecucion = LocalDateTime.now();
        if (compraACancelar == null) {
            System.err.println("Comando CancelarCompra: La compra a cancelar es nula.");
            return false;
        }

        System.out.println("Comando CancelarCompra: Ejecutando para compra ID " + compraACancelar.getId() + " por usuario " + usuarioQueCancela.getNombre());

        if (!"COMPLETADA".equals(compraACancelar.getEstadoCompra()) && !"PAGADA_PENDIENTE_CONFIRMACION".equals(compraACancelar.getEstadoCompra())) {
            System.err.println("Comando CancelarCompra: No se puede cancelar una compra que no está completada o pendiente de confirmación. Estado actual: " + compraACancelar.getEstadoCompra());
            return false;
        }

        this.estadoOriginalCompra = compraACancelar.getEstadoCompra();
        this.cantidadEntradasDevueltas = compraACancelar.getEntradasCompradas().size();

        // Lógica de cancelación:
        // 1. Devolver entradas al stock del evento
        Evento evento = compraACancelar.getEvento();
        compraACancelar.getEntradasCompradas().forEach(entrada -> {
            if (evento.getTiposEntradaDisponibles().containsKey(entrada.getTipo())) {
                 evento.devolverEntradas(entrada.getTipo(), 1);
            }
        });
        if (eventoService != null) {
            eventoService.actualizarEvento(evento); // Persistir cambios en la disponibilidad
        }
        System.out.println("Comando CancelarCompra: Entradas devueltas al stock.");

        // 2. Procesar reembolso si la compra estaba "COMPLETADA" (y si las políticas lo permiten)
        boolean reembolsoProcesado = false;
        if ("COMPLETADA".equals(this.estadoOriginalCompra)) {
            // Aquí iría la lógica de si aplica reembolso o no (políticas de cancelación, tiempo, etc.)
            // Por ahora, asumimos que sí aplica si estaba completada.
            if (procesadorPago != null) {
                // ASUNCIÓN: compraACancelar tiene un método getIdTransaccionPasarela()
                // Este ID se debió guardar en la Compra cuando el pago original se procesó.
                String idTransaccionOriginal = compraACancelar.getIdTransaccionPasarela(); // Necesitamos este método en Compra
                if (idTransaccionOriginal == null || idTransaccionOriginal.trim().isEmpty()) {
                    System.err.println("Comando CancelarCompra: No se puede procesar reembolso, falta ID de transacción de pasarela original en la compra.");
                    // Considerar esto como un fallo parcial o completo de la cancelación.
                } else {
                    this.idTransaccionPasarelaOriginal = idTransaccionOriginal; // Guardar para posible undo (complejo)
                    ResultadoPago resultadoReembolso = procesadorPago.procesarReembolso(idTransaccionOriginal, compraACancelar.getTotalPagado());
                    reembolsoProcesado = resultadoReembolso.isExito();
                    if(reembolsoProcesado) {
                        System.out.println("Comando CancelarCompra: Reembolso procesado. ID Pasarela Reembolso: " + resultadoReembolso.getIdTransaccionPasarela());
                    } else {
                        System.err.println("Comando CancelarCompra: Fallo al procesar el reembolso ("+ resultadoReembolso.getMensaje() +"). La cancelación continuará pero el reembolso debe manejarse manualmente.");
                        // Dependiendo de la política, se podría detener la cancelación aquí.
                    }
                }
            }
        }

        // 3. Actualizar estado de la compra
        compraACancelar.setEstadoCompra(reembolsoProcesado ? "REEMBOLSADA_POR_CANCELACION" : "CANCELADA");
        // En un sistema real, el UsuarioService podría actualizar la compra en el historial del usuario.
        // usuarioService.actualizarCompraEnHistorial(usuarioQueCancela, compraACancelar);

        // 4. Notificar al usuario
        if (notificacionService != null) {
            // notificacionService.notificarCancelacionCompra(usuarioQueCancela, compraACancelar, motivoCancelacion, reembolsoProcesado);
             System.out.println("Comando CancelarCompra: Notificación de cancelación enviada (simulada).");
        }

        compraACancelar.agregarOperacionAlHistorial(this);
        System.out.println("Comando CancelarCompra: Compra ID " + compraACancelar.getId() + " cancelada exitosamente.");
        return true;
    }

    @Override
    public boolean undo() {
        // Deshacer una cancelación es complejo. Implicaría:
        // 1. Volver a cobrar al usuario (si hubo reembolso).
        // 2. Volver a quitar las entradas del stock.
        // 3. Restaurar el estado original de la compra.
        // Esto usualmente no se implementa directamente como un "undo" simple,
        // sino que el usuario tendría que realizar una nueva compra.
        // Por simplicidad, aquí solo restauraremos el estado y la disponibilidad (sin rehacer el pago).
        System.out.println("Comando CancelarCompra: Intentando deshacer cancelación de compra ID " + compraACancelar.getId());
        if (compraACancelar == null || estadoOriginalCompra == null) {
            System.err.println("Comando CancelarCompra: No se puede deshacer. No hay estado original o compra es nula.");
            return false;
        }

        if (!"CANCELADA".equals(compraACancelar.getEstadoCompra()) && !"REEMBOLSADA".equals(compraACancelar.getEstadoCompra())) {
            System.err.println("Comando CancelarCompra: La compra no está en un estado que permita deshacer la cancelación.");
            return false;
        }

        // 1. Volver a "vender" las entradas (reducir disponibilidad)
        Evento evento = compraACancelar.getEvento();
        boolean stockSuficienteParaRestaurar = true;
        for(com.eventmaster.model.pattern.factory.Entrada entrada : compraACancelar.getEntradasCompradas()){
            if(!evento.getTiposEntradaDisponibles().get(entrada.getTipo()).revisarDisponibilidad(1)){
                stockSuficienteParaRestaurar = false;
                break;
            }
        }

        if(stockSuficienteParaRestaurar){
            compraACancelar.getEntradasCompradas().forEach(entrada -> {
                evento.venderEntradas(entrada.getTipo(), 1);
            });
            if (eventoService != null) {
                eventoService.actualizarEvento(evento);
            }
            System.out.println("Comando CancelarCompra (Undo): Entradas re-asignadas de la disponibilidad.");
        } else {
            System.err.println("Comando CancelarCompra (Undo): No hay suficiente stock para restaurar las entradas. El undo no puede completarse de forma segura.");
            return false;
        }

        // 2. Restaurar estado de la compra
        compraACancelar.setEstadoCompra(estadoOriginalCompra);
        System.out.println("Comando CancelarCompra (Undo): Estado de la compra restaurado a " + estadoOriginalCompra);

        // 3. (Simulado) Revertir reembolso - En la realidad esto sería complejo.
        if ("REEMBOLSADA".equals(compraACancelar.getEstadoCompra())) { // Si antes estaba reembolsada y ahora se restaura el estado original
             System.out.println("Comando CancelarCompra (Undo): Lógica de reversión de reembolso (simulada). Se necesitaría volver a cobrar.");
        }

        // Quitar este comando del historial de la compra (si se añade)
        // compraACancelar.getHistorialOperaciones().remove(this);

        System.out.println("Comando CancelarCompra: Deshacer cancelación de compra ID " + compraACancelar.getId() + " completado (parcialmente simulado).");
        return true;
    }

    @Override
    public String getDescription() {
        return "Cancelar Compra: ID " + (compraACancelar != null ? compraACancelar.getId() : "N/A") + ", Motivo: " + motivoCancelacion;
    }

    @Override
    public LocalDateTime getTiempoEjecucion() {
        return tiempoEjecucion;
    }
}
