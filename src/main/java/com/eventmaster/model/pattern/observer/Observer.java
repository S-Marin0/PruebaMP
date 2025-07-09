package com.eventmaster.model.pattern.observer;

import com.eventmaster.model.entity.Evento; // Para pasar el contexto del evento

public interface Observer {
    // void actualizar(String mensaje); // Versión genérica
    void actualizar(Evento evento, String mensaje); // Versión más específica para cambios de evento
    String getObserverId(); // Para poder remover observadores específicos
}
