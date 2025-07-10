package com.eventmaster.dao.impl.memoria;

import com.eventmaster.dao.CompraDAO;
import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Evento;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class CompraDAOImplMemoria implements CompraDAO {

    private final Map<String, Compra> comprasMap = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public Optional<Compra> findById(String id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(comprasMap.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Compra> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(comprasMap.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Compra> findByUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) return new ArrayList<>();
        return findByUsuarioId(usuario.getId());
    }

    @Override
    public List<Compra> findByUsuarioId(String usuarioId) {
        lock.readLock().lock();
        try {
            if (usuarioId == null) return new ArrayList<>();
            return comprasMap.values().stream()
                    .filter(c -> c.getUsuario() != null && usuarioId.equals(c.getUsuario().getId()))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Compra> findByEvento(Evento evento) {
        if (evento == null || evento.getId() == null) return new ArrayList<>();
        return findByEventoId(evento.getId());
    }

    @Override
    public List<Compra> findByEventoId(String eventoId) {
        lock.readLock().lock();
        try {
            if (eventoId == null) return new ArrayList<>();
            return comprasMap.values().stream()
                    .filter(c -> c.getEvento() != null && eventoId.equals(c.getEvento().getId()))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Compra> findByFechaCompraBetween(LocalDateTime inicio, LocalDateTime fin) {
        lock.readLock().lock();
        try {
            return comprasMap.values().stream()
                    .filter(c -> c.getFechaCompra() != null &&
                                 !c.getFechaCompra().isBefore(inicio) &&
                                 !c.getFechaCompra().isAfter(fin))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Compra> findByEstadoCompra(String estado) {
        lock.readLock().lock();
        try {
            if (estado == null) return new ArrayList<>();
            return comprasMap.values().stream()
                    .filter(c -> estado.equalsIgnoreCase(c.getEstadoCompra()))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Compra save(Compra compra) {
        lock.writeLock().lock();
        try {
            if (compra == null) {
                throw new IllegalArgumentException("La compra no puede ser nula.");
            }
            if (compra.getId() == null || compra.getId().trim().isEmpty()) {
                compra.setId(UUID.randomUUID().toString()); // Asignar ID si es nueva
            }
            comprasMap.put(compra.getId(), compra);
            return compra;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteById(String id) {
        lock.writeLock().lock();
        try {
            comprasMap.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(Compra compra) {
        if (compra != null && compra.getId() != null) {
            deleteById(compra.getId());
        }
    }

    @Override
    public long count() {
        lock.readLock().lock();
        try {
            return comprasMap.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public long countByUsuarioId(String usuarioId) {
        lock.readLock().lock();
        try {
            if (usuarioId == null) return 0;
            return comprasMap.values().stream()
                    .filter(c -> c.getUsuario() != null && usuarioId.equals(c.getUsuario().getId()))
                    .count();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public long countByEventoId(String eventoId) {
        lock.readLock().lock();
        try {
            if (eventoId == null) return 0;
            return comprasMap.values().stream()
                    .filter(c -> c.getEvento() != null && eventoId.equals(c.getEvento().getId()))
                    .count();
        } finally {
            lock.readLock().unlock();
        }
    }
}
