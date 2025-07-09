package com.eventmaster.model.pattern.state;

// Interfaz para el patrón State
public interface EstadoEvento {
    void publicar();
    void cancelar();
    void iniciar();
    void finalizar();
    String getNombreEstado();
}
