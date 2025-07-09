package com.eventmaster.model.pattern.state;

import com.eventmaster.model.entity.Evento;

public class EstadoFinalizado implements EstadoEvento {
    private Evento evento;

    public EstadoFinalizado(Evento evento) {
        this.evento = evento;
    }

    @Override
    public void publicar() {
        System.out.println("No se puede publicar un evento que ya ha finalizado.");
    }

    @Override
    public void cancelar() {
        System.out.println("No se puede cancelar un evento que ya ha finalizado.");
    }

    @Override
    public void iniciar() {
        System.out.println("No se puede iniciar un evento que ya ha finalizado.");
    }

    @Override
    public void finalizar() {
        System.out.println("El evento '" + evento.getNombre() + "' ya está finalizado.");
    }

    @Override
    public String getNombreEstado() {
        return "Finalizado";
    }
}
