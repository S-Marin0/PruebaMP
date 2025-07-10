package com.eventmaster.dao.impl.memoria;

import com.eventmaster.dao.PromocionDAO;
import com.eventmaster.model.Promocion;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class PromocionDAOImplMemoria implements PromocionDAO {

    private final Map<String, Promocion> promocionesMap = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public Optional<Promocion> findById(String id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(promocionesMap.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Promocion> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(promocionesMap.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Promocion> findActivas(LocalDateTime fechaActual) {
        lock.readLock().lock();
        try {
            return promocionesMap.values().stream()
                    .filter(p -> p.esValidaAhora()) // Usamos el método de la entidad Promocion
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Promocion> findByTipoAplicable(String tipoAplicable) {
        lock.readLock().lock();
        try {
            return promocionesMap.values().stream()
                    .filter(p -> p.getTipoAplicable() != null && p.getTipoAplicable().equalsIgnoreCase(tipoAplicable))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Promocion> findByTipoAplicableAndIdAplicable(String tipoAplicable, String idAplicable) {
        lock.readLock().lock();
        try {
            return promocionesMap.values().stream()
                    .filter(p -> p.getTipoAplicable() != null && p.getTipoAplicable().equalsIgnoreCase(tipoAplicable) &&
                                 p.getIdAplicable() != null && p.getIdAplicable().equals(idAplicable))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Promocion save(Promocion promocion) {
        lock.writeLock().lock();
        try {
            if (promocion == null) {
                throw new IllegalArgumentException("La promoción no puede ser nula.");
            }
            if (promocion.getId() == null || promocion.getId().trim().isEmpty()) {
                promocion.setId(UUID.randomUUID().toString());
            }
            promocionesMap.put(promocion.getId(), promocion);
            return promocion;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteById(String id) {
        lock.writeLock().lock();
        try {
            promocionesMap.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public long count() {
        lock.readLock().lock();
        try {
            return promocionesMap.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
