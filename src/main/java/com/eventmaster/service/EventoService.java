package com.eventmaster.service;

import com.eventmaster.model.entity.Evento;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Servicio placeholder para gestionar eventos (ej. persistencia, notificaciones complejas)
// En una aplicación real, esto interactuaría con una capa DAO y posiblemente otros servicios.
public class EventoService {

    private static List<Evento> repositorioDeEventos = new ArrayList<>(); // Simulación de un repositorio en memoria

    public EventoService() {
        // Constructor
    }

    public void registrarNuevoEvento(Evento evento) {
        if (evento != null && !repositorioDeEventos.stream().anyMatch(e -> e.getId().equals(evento.getId()))) {
            repositorioDeEventos.add(evento);
            System.out.println("EventoService: Evento '" + evento.getNombre() + "' registrado en el sistema.");
            // Aquí podría ir lógica de notificación a un sistema de búsqueda, administradores, etc.
        } else if (evento != null) {
            System.out.println("EventoService: Evento '" + evento.getNombre() + "' ya estaba registrado o es nulo.");
        }
    }

    public void eliminarEventoRegistrado(Evento evento) {
        if (evento != null) {
            boolean removed = repositorioDeEventos.removeIf(e -> e.getId().equals(evento.getId()));
            if (removed) {
                System.out.println("EventoService: Evento '" + evento.getNombre() + "' eliminado del sistema.");
            } else {
                System.out.println("EventoService: No se encontró el evento '" + evento.getNombre() + "' para eliminar.");
            }
        }
    }

    public Optional<Evento> findEventoById(String id) {
        return repositorioDeEventos.stream().filter(e -> e.getId().equals(id)).findFirst();
    }

    public List<Evento> getAllEventos() {
        return new ArrayList<>(repositorioDeEventos); // Devuelve una copia para evitar modificaciones externas
    }

    public void actualizarEvento(Evento eventoActualizado) {
        Optional<Evento> eventoOpt = findEventoById(eventoActualizado.getId());
        if (eventoOpt.isPresent()) {
            Evento eventoExistente = eventoOpt.get();
            // Remover el viejo y añadir el actualizado (simplista, en un ORM sería un update)
            repositorioDeEventos.remove(eventoExistente);
            repositorioDeEventos.add(eventoActualizado);
            System.out.println("EventoService: Evento '" + eventoActualizado.getNombre() + "' actualizado.");
        } else {
            System.out.println("EventoService: No se pudo actualizar, evento '" + eventoActualizado.getNombre() + "' no encontrado.");
        }
    }

    // Otros métodos que podrían ser útiles:
    // buscarEventosPorCriterio(Filtro filtro)
    // obtenerEventosPorOrganizador(Organizador organizador)
    // obtenerEventosPorLugar(Lugar lugar)
}
