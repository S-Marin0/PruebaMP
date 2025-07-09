package com.eventmaster.model.pattern.factory;

// Interfaz base para todas las entradas (Componente para Decorator)
public interface Entrada {
    double getPrecio();
    String getDescripcion();
    String getTipo(); // Devuelve el nombre del tipo de entrada base (ej. "General", "VIP")
    String getId(); // Identificador único de esta instancia de entrada
    Evento getEventoAsociado(); // Evento al que pertenece esta entrada
    // Podría tener más métodos como validar, transferir, etc.
}
