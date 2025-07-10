package com.eventmaster.dao;

import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.entity.Organizador; // Para buscar eventos por organizador
import com.eventmaster.model.entity.Lugar; // Para buscar eventos por lugar

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventoDAO {
    Optional<Evento> findById(String id);
    List<Evento> findAll();
    List<Evento> findByNombreContaining(String nombre);
    List<Evento> findByCategoria(String categoria);
    List<Evento> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);
    List<Evento> findByLugar(Lugar lugar); // O por ID de lugar: findByLugarId(String lugarId)
    List<Evento> findByOrganizador(Organizador organizador); // O por ID de organizador
    List<Evento> findEventosPublicados(); // Eventos con estado "Publicado"
    List<Evento> findEventosFuturos(); // Eventos cuya fecha aún no ha pasado

    Evento save(Evento evento); // Para crear o actualizar
    void deleteById(String id);
    void delete(Evento evento);

    long count();

    // Podríamos añadir métodos para actualizar campos específicos si es muy frecuente,
    // pero usualmente 'save' se encarga de la actualización completa del objeto.
    // void actualizarEstado(String eventoId, EstadoEvento nuevoEstado);
}
