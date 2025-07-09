package com.eventmaster.model.pattern.state;

import com.eventmaster.model.entity.Evento;

public class EstadoEnCurso implements EstadoEvento {
    private Evento evento;

    public EstadoEnCurso(Evento evento) {
        this.evento = evento;
    }

    @Override
    public void publicar() {
        System.out.println("No se puede publicar un evento que ya está en curso.");
    }

    @Override
    public void cancelar() {
        System.out.println("No se puede cancelar un evento que ya está en curso. Considerar finalizarlo prematuramente si es necesario.");
        // Podría implementarse una cancelación de emergencia, pero usualmente un evento en curso no se "cancela" en el sentido de reembolso total.
    }

    @Override
    public void iniciar() {
        System.out.println("El evento '" + evento.getNombre() + "' ya está en curso.");
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
