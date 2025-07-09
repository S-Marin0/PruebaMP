package com.eventmaster.model.pattern.state;

import com.eventmaster.model.entity.Evento;

public class EstadoCancelado implements EstadoEvento {
    private Evento evento;

    public EstadoCancelado(Evento evento) {
        this.evento = evento;
    }

    @Override
    public void publicar() {
        System.out.println("No se puede publicar un evento que ha sido cancelado. Considere crear un nuevo evento.");
    }

    @Override
    public void cancelar() {
        System.out.println("El evento '" + evento.getNombre() + "' ya está cancelado.");
    }

    @Override
    public void iniciar() {
        System.out.println("No se puede iniciar un evento que ha sido cancelado.");
    }

    @Override
    public void finalizar() {
        System.out.println("No se puede finalizar un evento que ha sido cancelado.");
    }

    @Override
    public String getNombreEstado() {
        return "Cancelado";
    }
}
