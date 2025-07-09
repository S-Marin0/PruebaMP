package com.eventmaster.model.pattern.observer;

public interface Subject {
    void registrarObserver(Observer observer);
    void eliminarObserver(Observer observer);
    void notificarObservers(String mensaje); // Notificación genérica
    // void notificarObserversCambioEvento(Evento evento, String detalleCambio); // Notificación específica
}
