package com.eventmaster.model.singleton;

import com.eventmaster.model.pattern.observer.Observer;
import com.eventmaster.model.pattern.observer.Subject;
import com.eventmaster.model.entity.Asistente; // Necesario para acceder a NotificacionConfig
import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.entity.Usuario; // Para enviar notificaciones directas
import com.eventmaster.model.entity.Compra;  // Para notificaciones de compra
import com.eventmaster.model.NotificacionConfig; // Necesario para acceder a las preferencias

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

// Singleton y también implementa Subject para notificaciones de cambios de eventos generales
public class SistemaNotificaciones implements Subject {

    private static volatile SistemaNotificaciones instancia; // volatile para asegurar visibilidad en multihilo

    // Lista de observers para cambios generales en eventos (ej. nuevo evento importante, cancelación masiva)
    // Los observers específicos de un evento (ej. asistentes a ESE evento) se manejarían de otra forma,
    // quizás el propio Evento siendo un Subject, o un mapa aquí: Map<String eventoId, List<Observer>>
    private List<Observer> observersGenerales;

    // Mapa para observadores específicos de un evento (eventoId -> lista de observers)
    private Map<String, List<Observer>> observersPorEvento;


    // Constructor privado para Singleton
    private SistemaNotificaciones() {
        this.observersGenerales = new ArrayList<>();
        this.observersPorEvento = new HashMap<>();
        if (instancia != null) {
            // Prevenir creación por reflexión si ya existe
            throw new IllegalStateException("Ya existe una instancia de SistemaNotificaciones. Use getInstance().");
        }
    }

    public static SistemaNotificaciones getInstance() {
        // Doble checkeo de bloqueo para eficiencia y seguridad en multihilo
        if (instancia == null) {
            synchronized (SistemaNotificaciones.class) {
                if (instancia == null) {
                    instancia = new SistemaNotificaciones();
                }
            }
        }
        return instancia;
    }

    // --- Implementación de Subject (para observadores generales) ---
    @Override
    public void registrarObserver(Observer observer) {
        if (observer != null && !observersGenerales.contains(observer)) {
            observersGenerales.add(observer);
            System.out.println("SistemaNotificaciones: Observer general '" + observer.getObserverId() + "' registrado.");
        }
    }

    @Override
    public void eliminarObserver(Observer observer) {
        observersGenerales.remove(observer);
        System.out.println("SistemaNotificaciones: Observer general '" + observer.getObserverId() + "' eliminado.");
    }

    /**
     * Notifica a todos los observers generales.
     * El mensaje es genérico. Para notificaciones específicas de evento, usar notificarObserversDeEvento.
     */
    @Override
    public void notificarObservers(String mensaje) {
        System.out.println("SistemaNotificaciones: Notificando a " + observersGenerales.size() + " observers generales: " + mensaje);
        for (Observer observer : new ArrayList<>(observersGenerales)) { // Iterar sobre copia para evitar ConcurrentModificationException
            observer.actualizar(null, mensaje); // Pasa null como evento ya que es una notificación general
        }
    }

    // --- Métodos para observadores específicos de Eventos ---
    public void registrarObserverParaEvento(String eventoId, Observer observer) {
        observersPorEvento.computeIfAbsent(eventoId, k -> new ArrayList<>());
        if (observer != null && !observersPorEvento.get(eventoId).contains(observer)) {
            observersPorEvento.get(eventoId).add(observer);
            System.out.println("SistemaNotificaciones: Observer '" + observer.getObserverId() + "' registrado para evento ID: " + eventoId);
        }
    }

    public void eliminarObserverDeEvento(String eventoId, Observer observer) {
        if (observersPorEvento.containsKey(eventoId)) {
            observersPorEvento.get(eventoId).remove(observer);
            System.out.println("SistemaNotificaciones: Observer '" + observer.getObserverId() + "' eliminado del evento ID: " + eventoId);
            if (observersPorEvento.get(eventoId).isEmpty()) {
                observersPorEvento.remove(eventoId); // Limpiar si no quedan observers
            }
        }
    }

    public void notificarObserversDeEvento(Evento evento, String mensaje) {
        String eventoId = evento.getId();
        if (observersPorEvento.containsKey(eventoId)) {
            List<Observer> observersDelEvento = observersPorEvento.get(eventoId);
            System.out.println("SistemaNotificaciones: Notificando a " + observersDelEvento.size() + " observers del evento '" + evento.getNombre() + "': " + mensaje);
            for (Observer observer : new ArrayList<>(observersDelEvento)) { // Iterar sobre copia
                observer.actualizar(evento, mensaje);
            }
        }
    }


    // --- Métodos de notificación específicos (usados por otros servicios/lógica) ---
    // Estos no son parte del patrón Observer directamente, sino funcionalidades del Singleton.

    public void enviarConfirmacionCompra(Usuario usuario, Compra compra) {
        // Las confirmaciones de compra suelen ser importantes y no opcionales.
        // Si se quisiera hacer opcional, se añadiría una verificación de preferencias aquí.
        System.out.println("SistemaNotificaciones (Singleton): Enviando email de confirmación de compra a " + usuario.getEmail());
        System.out.println("  Detalles de la Compra ID: " + compra.getId() + " para evento " + compra.getEvento().getNombre());
        // Aquí iría la lógica real de envío (email, SMS, etc.)
        // Podría usar un servicio de mensajería externo.
    }

    public void enviarRecordatorioEvento(Usuario usuario, Evento evento) {
        if (usuario instanceof Asistente) {
            Asistente asistente = (Asistente) usuario;
            NotificacionConfig config = asistente.getConfiguracionNotificaciones();
            if (config != null && config.isRecibirRecordatoriosEventosComprados()) {
                System.out.println("SistemaNotificaciones (Singleton): Enviando recordatorio para evento '" + evento.getNombre() + "' a " + usuario.getEmail());
                // Lógica de envío real aquí
            } else {
                System.out.println("SistemaNotificaciones (Singleton): Usuario " + usuario.getEmail() + " ha optado por NO recibir recordatorios de eventos.");
            }
        } else {
            // Comportamiento para usuarios no Asistentes o sin configuración (si aplica)
             System.out.println("SistemaNotificaciones (Singleton): Enviando recordatorio para evento '" + evento.getNombre() + "' a " + usuario.getEmail() + " (usuario no es Asistente o config no aplica).");
        }
    }

    public void enviarNotificacionCambioEvento(Usuario usuario, Evento evento, String mensajeCambio) {
        if (usuario instanceof Asistente) {
            Asistente asistente = (Asistente) usuario;
            NotificacionConfig config = asistente.getConfiguracionNotificaciones();
            if (config != null && config.isRecibirNotificacionesCambiosEventoComprado()) {
                System.out.println("SistemaNotificaciones (Singleton): Enviando notificación de cambio para evento '" + evento.getNombre() + "' a " + usuario.getEmail() + ". Mensaje: " + mensajeCambio);
                // Lógica de envío real aquí
            } else {
                System.out.println("SistemaNotificaciones (Singleton): Usuario " + usuario.getEmail() + " ha optado por NO recibir notificaciones de cambios en eventos comprados.");
            }
        } else {
             System.out.println("SistemaNotificaciones (Singleton): Enviando notificación de cambio para evento '" + evento.getNombre() + "' a " + usuario.getEmail() + " (usuario no es Asistente o config no aplica). Mensaje: " + mensajeCambio);
        }
    }

    public void enviarRecomendacion(Usuario usuario, Evento eventoRecomendado) {
        if (usuario instanceof Asistente) {
            Asistente asistente = (Asistente) usuario;
            NotificacionConfig config = asistente.getConfiguracionNotificaciones();
            if (config != null && config.isRecibirRecomendacionesPersonalizadas()) {
                System.out.println("SistemaNotificaciones (Singleton): Enviando recomendación de evento '" + eventoRecomendado.getNombre() + "' a " + usuario.getEmail());
                // Lógica de envío real aquí
            } else {
                System.out.println("SistemaNotificaciones (Singleton): Usuario " + usuario.getEmail() + " ha optado por NO recibir recomendaciones personalizadas.");
            }
        } else {
            // Comportamiento para usuarios no Asistentes o sin configuración
            System.out.println("SistemaNotificaciones (Singleton): Enviando recomendación de evento '" + eventoRecomendado.getNombre() + "' a " + usuario.getEmail() + " (usuario no es Asistente o config no aplica).");
        }
    }

    public void enviarNotificacionNuevoEventoImportante(Usuario usuario, Evento evento) {
        if (usuario instanceof Asistente) {
            Asistente asistente = (Asistente) usuario;
            NotificacionConfig config = asistente.getConfiguracionNotificaciones();
            if (config != null && config.isRecibirNotificacionesNuevosEventosImportantes()) {
                System.out.println("SistemaNotificaciones (Singleton): Enviando notificación de nuevo evento importante '" + evento.getNombre() + "' a " + usuario.getEmail());
                // Lógica de envío real aquí
            } else {
                System.out.println("SistemaNotificaciones (Singleton): Usuario " + usuario.getEmail() + " ha optado por NO recibir notificaciones de nuevos eventos importantes.");
            }
        } else {
            System.out.println("SistemaNotificaciones (Singleton): Enviando notificación de nuevo evento importante '" + evento.getNombre() + "' a " + usuario.getEmail() + " (usuario no es Asistente o config no aplica).");
        }
    }


    // Método para proteger de la clonación (parte de la implementación robusta de Singleton)
    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("La clonación de esta clase Singleton no está permitida.");
    }

    // Método para proteger de la deserialización (parte de la implementación robusta de Singleton)
    // Esto asegura que si la instancia es serializada y luego deserializada, se devuelva la misma instancia.
    protected Object readResolve() {
        return getInstance();
    }
}
