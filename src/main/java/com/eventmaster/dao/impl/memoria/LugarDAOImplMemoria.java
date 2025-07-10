package com.eventmaster.dao.impl.memoria;

import com.eventmaster.dao.LugarDAO;
import com.eventmaster.model.entity.Lugar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class LugarDAOImplMemoria implements LugarDAO {

    private final Map<String, Lugar> lugaresMap = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public Optional<Lugar> findById(String id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(lugaresMap.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Lugar> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(lugaresMap.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Lugar> findByNombreContaining(String nombre) {
        lock.readLock().lock();
        try {
            String lowerNombre = nombre.toLowerCase();
            return lugaresMap.values().stream()
                    .filter(l -> l.getNombre().toLowerCase().contains(lowerNombre))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Lugar> findByDireccionContaining(String direccionFragmento) {
        lock.readLock().lock();
        try {
            String lowerDireccion = direccionFragmento.toLowerCase();
            return lugaresMap.values().stream()
                    .filter(l -> l.getDireccion() != null && l.getDireccion().toLowerCase().contains(lowerDireccion))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Lugar save(Lugar lugar) {
        lock.writeLock().lock();
        try {
            if (lugar == null) {
                throw new IllegalArgumentException("El lugar no puede ser nulo.");
            }
            if (lugar.getId() == null || lugar.getId().trim().isEmpty()) {
                lugar.setId(UUID.randomUUID().toString()); // Asignar ID si es nuevo
            }
            lugaresMap.put(lugar.getId(), lugar);
            return lugar;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteById(String id) {
        lock.writeLock().lock();
        try {
            lugaresMap.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void delete(Lugar lugar) {
        if (lugar != null && lugar.getId() != null) {
            deleteById(lugar.getId());
        }
    }

    @Override
    public long count() {
        lock.readLock().lock();
        try {
            return lugaresMap.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
