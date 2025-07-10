package com.eventmaster.model.pattern.state;

import com.eventmaster.model.entity.Evento;

public class EstadoEnCurso implements EstadoEvento {
    private Evento evento;

    public EstadoEnCurso(Evento evento) {
        this.evento = evento;
    }

    @Override
    public void publicar() {
        throw new IllegalStateException("No se puede publicar un evento que ya está en curso.");
    }

    @Override
    public void cancelar() {
        // Según la política actual, no se puede cancelar un evento en curso.
        // Si se quisiera permitir una cancelación de emergencia, se cambiaría la lógica aquí.
        // Por ahora, lanzamos excepción para mantener consistencia.
        throw new IllegalStateException("No se puede cancelar un evento que ya está en curso. Considere finalizarlo.");
    }

    @Override
    public void iniciar() {
        // System.out.println("El evento '" + evento.getNombre() + "' ya está en curso."); // Idempotente, no lanzar error
    }

    @Override
    public void finalizar() {
        System.out.println("Evento '" + evento.getNombre() + "' finalizado.");
        evento.setEstadoActual(new EstadoFinalizado(evento));
        // Lógica para estadísticas finales, feedback, etc.
    }

    @Override
    public String getNombreEstado() {
        return "En Curso";
    }
}
