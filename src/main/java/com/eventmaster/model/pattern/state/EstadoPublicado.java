package com.eventmaster.model.pattern.state;

import com.eventmaster.model.entity.Evento;
import java.time.LocalDateTime;

public class EstadoPublicado implements EstadoEvento {
    private Evento evento;

    public EstadoPublicado(Evento evento) {
        this.evento = evento;
    }

    @Override
    public void publicar() {
        System.out.println("El evento '" + evento.getNombre() + "' ya está publicado.");
    }

    @Override
    public void cancelar() {
        System.out.println("Evento '" + evento.getNombre() + "' cancelado. Se notificará a los asistentes.");
        evento.setEstadoActual(new EstadoCancelado(evento));
        // Lógica para notificar a los asistentes que compraron entradas, procesar reembolsos, etc.
        // SistemaNotificaciones.getInstance().notificarCancelacionEvento(evento);
    }

    @Override
    public void iniciar() {
        if (LocalDateTime.now().isAfter(evento.getFechaHora()) || LocalDateTime.now().isEqual(evento.getFechaHora())) {
            System.out.println("Evento '" + evento.getNombre() + "' iniciado.");
            evento.setEstadoActual(new EstadoEnCurso(evento));
        } else {
            System.out.println("No se puede iniciar el evento '" + evento.getNombre() + "' antes de su fecha y hora programada.");
        }
    }

    @Override
    public void finalizar() {
        System.out.println("No se puede finalizar un evento que solo está publicado y no ha iniciado.");
    }

    @Override
    public String getNombreEstado() {
        return "Publicado";
    }
}
