package com.eventmaster.model.pattern.composite;

import java.time.LocalDateTime;

public interface ComponenteLugar {
    String getNombre();
    int obtenerCapacidadTotal(); // Capacidad total del componente (puede ser sumada en composites)

    // Métodos relacionados con la disponibilidad y reserva (podrían ser más complejos)
    // boolean estaDisponible(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin);
    // boolean reservar(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin);
    // void liberarReserva(LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin);

    void agregar(ComponenteLugar componente) throws UnsupportedOperationException;
    void remover(ComponenteLugar componente) throws UnsupportedOperationException;
    ComponenteLugar getHijo(int i) throws UnsupportedOperationException;
}
