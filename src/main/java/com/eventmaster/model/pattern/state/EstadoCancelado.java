package com.eventmaster.model.pattern.state;

import com.eventmaster.model.entity.Evento;

public class EstadoCancelado implements EstadoEvento {
    private Evento evento;

    public EstadoCancelado(Evento evento) {
        this.evento = evento;
    }

    @Override
    public void publicar() {
        throw new IllegalStateException("No se puede publicar un evento que ha sido cancelado. Considere crear un nuevo evento.");
    }

    @Override
    public void cancelar() {
        // System.out.println("El evento '" + evento.getNombre() + "' ya está cancelado."); // Opcional: lanzar excepción o permitirlo como idempotente
        throw new IllegalStateException("El evento '" + evento.getNombre() + "' ya está cancelado.");
    }

    @Override
    public void iniciar() {
        throw new IllegalStateException("No se puede iniciar un evento que ha sido cancelado.");
    }

    @Override
    public void finalizar() {
        throw new IllegalStateException("No se puede finalizar un evento que ha sido cancelado.");
    }

    @Override
    public String getNombreEstado() {
        return "Cancelado";
    }
}
