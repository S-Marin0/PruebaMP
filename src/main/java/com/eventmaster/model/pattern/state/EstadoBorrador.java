package com.eventmaster.model.pattern.state;

import com.eventmaster.model.entity.Evento;

public class EstadoBorrador implements EstadoEvento {
    private Evento evento;

    public EstadoBorrador(Evento evento) {
        this.evento = evento;
    }

    @Override
    public void publicar() {
        System.out.println("Evento '" + evento.getNombre() + "' publicado. Ahora está visible para los asistentes.");
        evento.setEstadoActual(new EstadoPublicado(evento));
        // Aquí se podría notificar a observadores o al mediador si es necesario
    }

    @Override
    public void cancelar() {
        System.out.println("Evento '" + evento.getNombre() + "' en estado borrador ha sido cancelado (o eliminado).");
        evento.setEstadoActual(new EstadoCancelado(evento));
        // Lógica adicional, como eliminarlo de la lista de borradores del organizador si es necesario.
    }

    @Override
    public void iniciar() {
        throw new IllegalStateException("No se puede iniciar un evento que está en estado borrador. Primero debe publicarse.");
    }

    @Override
    public void finalizar() {
        throw new IllegalStateException("No se puede finalizar un evento que está en estado borrador.");
    }

    @Override
    public String getNombreEstado() {
        return "Borrador";
    }
}
