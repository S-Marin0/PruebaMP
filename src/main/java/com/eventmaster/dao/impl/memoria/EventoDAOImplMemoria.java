package com.eventmaster.dao.impl.memoria;

import com.eventmaster.dao.EventoDAO;
import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.entity.Lugar;
import com.eventmaster.model.entity.Organizador;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class EventoDAOImplMemoria implements EventoDAO {

    private final Map<String, Evento> eventosMap = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public Optional<Evento> findById(String id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(eventosMap.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Evento> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(eventosMap.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Evento> findByNombreContaining(String nombre) {
        lock.readLock().lock();
        try {
            String lowerNombre = nombre.toLowerCase();
            return eventosMap.values().stream()
                    .filter(e -> e.getNombre().toLowerCase().contains(lowerNombre))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Evento> findByCategoria(String categoria) {
        lock.readLock().lock();
        try {
            return eventosMap.values().stream()
                    .filter(e -> e.getCategoria() != null && e.getCategoria().equalsIgnoreCase(categoria))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Evento> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin) {
        lock.readLock().lock();
        try {
            return eventosMap.values().stream()
                    .filter(e -> e.getFechaHora() != null &&
                                 !e.getFechaHora().isBefore(inicio) &&
                                 !e.getFechaHora().isAfter(fin))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Evento> findByLugar(Lugar lugar) {
        lock.readLock().lock();
        try {
            if (lugar == null || lugar.getId() == null) return new ArrayList<>();
            return eventosMap.values().stream()
                    .filter(e -> e.getLugar() != null && lugar.getId().equals(e.getLugar().getId()))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Evento> findByOrganizador(Organizador organizador) {
        lock.readLock().lock();
        try {
            if (organizador == null || organizador.getId() == null) return new ArrayList<>();
            return eventosMap.values().stream()
                    .filter(e -> e.getOrganizador() != null && organizador.getId().equals(e.getOrganizador().getId()))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Evento> findEventosPublicados() {
        lock.readLock().lock();
        try {
            return eventosMap.values().stream()
                .filter(e -> e.getEstadoActual() != null && "Publicado".equalsIgnoreCase(e.getEstadoActual().getNombreEstado()))
                .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Evento> findEventosFuturos() {
        lock.readLock().lock();
        try {
            LocalDateTime ahora = LocalDateTime.now();
            return eventosMap.values().stream()
                .filter(e -> e.getFechaHora() != null && e.getFechaHora().isAfter(ahora))
                .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }


    @Override
    public Evento save(Evento evento) {
        lock.writeLock().lock();
        try {
            if (evento == null) {
                throw new IllegalArgumentException("El evento no puede ser nulo.");
            }
            if (evento.getId() == null || evento.getId().trim().isEmpty()) {
                evento.setId(UUID.randomUUID().toString()); // Asignar ID si es nuevo
            }
            eventosMap.put(evento.getId(), evento);
            return evento;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteById(String id) {
        lock.writeLock().lock();
        try {
            eventosMap.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(Evento evento) {
        if (evento != null && evento.getId() != null) {
            deleteById(evento.getId());
        }
    }

    @Override
    public long count() {
        lock.readLock().lock();
        try {
            return eventosMap.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
