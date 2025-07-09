package com.eventmaster.dao.impl.memoria;

import com.eventmaster.dao.CodigoDescuentoDAO;
import com.eventmaster.model.CodigoDescuento;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID; // Si los códigos tuvieran un ID interno único además del código string
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class CodigoDescuentoDAOImplMemoria implements CodigoDescuentoDAO {

    // Usaremos el string del código como clave principal para búsquedas rápidas
    private final Map<String, CodigoDescuento> codigosMap = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public Optional<CodigoDescuento> findByCodigo(String codigo) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(codigosMap.get(codigo));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<CodigoDescuento> findById(String id) {
        // Esto asume que el 'id' es el mismo que el 'codigo' string.
        // Si tuvieran un ID interno separado (ej. UUID), necesitaríamos otro mapa o filtrar.
        lock.readLock().lock();
        try {
            // Si el ID es diferente del código string, tendríamos que iterar:
            // return codigosMap.values().stream().filter(cd -> id.equals(cd.getIdInterno())).findFirst();
            // Por ahora, asumimos que el ID que se busca aquí es el código string.
            return Optional.ofNullable(codigosMap.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<CodigoDescuento> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(codigosMap.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<CodigoDescuento> findAllActivos() {
        lock.readLock().lock();
        try {
            return codigosMap.values().stream()
                    .filter(CodigoDescuento::esValido) // Usa el método de la entidad
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public CodigoDescuento save(CodigoDescuento codigoDescuento) {
        lock.writeLock().lock();
        try {
            if (codigoDescuento == null || codigoDescuento.getCodigo() == null || codigoDescuento.getCodigo().trim().isEmpty()) {
                throw new IllegalArgumentException("El código de descuento y su string de código no pueden ser nulos o vacíos.");
            }
            // Si tuviera un ID interno separado:
            // if (codigoDescuento.getId() == null) { codigoDescuento.setId(UUID.randomUUID().toString()); }
            codigosMap.put(codigoDescuento.getCodigo(), codigoDescuento);
            return codigoDescuento;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteById(String id) {
        // Asumiendo que el 'id' es el código string.
        deleteByCodigo(id);
    }

    @Override
    public void deleteByCodigo(String codigo) {
        lock.writeLock().lock();
        try {
            codigosMap.remove(codigo);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public long count() {
        lock.readLock().lock();
        try {
            return codigosMap.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
