package com.eventmaster.model.pattern.state;

import com.eventmaster.model.entity.Evento;

public class EstadoFinalizado implements EstadoEvento {
    private Evento evento;

    public EstadoFinalizado(Evento evento) {
        this.evento = evento;
    }

    @Override
    public void publicar() {
        throw new IllegalStateException("No se puede publicar un evento que ya ha finalizado.");
    }

    @Override
    public void cancelar() {
        throw new IllegalStateException("No se puede cancelar un evento que ya ha finalizado.");
    }

    @Override
    public void iniciar() {
        throw new IllegalStateException("No se puede iniciar un evento que ya ha finalizado.");
    }

    @Override
    public void finalizar() {
        // System.out.println("El evento '" + evento.getNombre() + "' ya está finalizado."); // Idempotente, no lanzar error
    }

    @Override
    public String getNombreEstado() {
        return "Finalizado";
    }
}
