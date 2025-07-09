package com.eventmaster.dao.impl.memoria;

import com.eventmaster.dao.EntradaDAO;
import com.eventmaster.model.pattern.factory.Entrada; // Interfaz

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class EntradaDAOImplMemoria implements EntradaDAO {

    private final Map<String, Entrada> entradasMap = new HashMap<>(); // idEntrada -> Entrada
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public Entrada save(Entrada entrada) {
        lock.writeLock().lock();
        try {
            if (entrada == null) {
                throw new IllegalArgumentException("La entrada no puede ser nula.");
            }
            // Asumimos que la entrada ya tiene un ID asignado por quien la crea (ej. ComprarEntradaCommand o Facade)
            // Si no, se podría generar aquí:
            // if (entrada.getId() == null || entrada.getId().trim().isEmpty()) {
            //    // Necesitaríamos un setId en la interfaz Entrada o castear al tipo concreto.
            //    // Esto es problemático con la interfaz. Es mejor que el ID venga ya asignado.
            // }
            if (entrada.getId() == null) {
                 throw new IllegalArgumentException("La entrada debe tener un ID antes de guardarla.");
            }
            entradasMap.put(entrada.getId(), entrada);
            return entrada;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<Entrada> saveAll(List<Entrada> entradas) {
        lock.writeLock().lock();
        try {
            if (entradas == null) {
                throw new IllegalArgumentException("La lista de entradas no puede ser nula.");
            }
            List<Entrada> entradasGuardadas = new ArrayList<>();
            for (Entrada entrada : entradas) {
                if (entrada.getId() == null) {
                    throw new IllegalArgumentException("Todas las entradas deben tener un ID antes de guardarlas.");
                }
                entradasMap.put(entrada.getId(), entrada);
                entradasGuardadas.add(entrada);
            }
            return entradasGuardadas;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<Entrada> findById(String idEntrada) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(entradasMap.get(idEntrada));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Entrada> findAllByCompraId(String compraId) {
        lock.readLock().lock();
        try {
            if (compraId == null) return new ArrayList<>();
            // Para esto, necesitaríamos que la Entrada tenga una referencia a su CompraId.
            // La interfaz Entrada no lo tiene. Asumiremos que la Compra tiene una lista de Ids de Entrada,
            // o que la Entrada (su implementación) tiene un getCompraId().
            // Esto es una limitación de la interfaz Entrada actual para esta query.
            // Simulación: si EntradaBase tuviera getCompraId()
            return entradasMap.values().stream()
                // .filter(e -> e instanceof com.eventmaster.model.pattern.factory.EntradaBase &&
                //              compraId.equals(((com.eventmaster.model.pattern.factory.EntradaBase)e).getCompraId()))
                // Como no lo tiene, no podemos filtrar así directamente sobre la interfaz.
                // Esta query sería más fácil si Compra almacenara los IDs de sus entradas.
                // O si el servicio que llama a esto ya tiene la Compra y obtiene sus entradas.
                .filter(e -> {
                    // Lógica conceptual: si la entrada está asociada a una compra,
                    // y esa compra tiene el ID buscado. Esto es difícil de implementar
                    // sin modificar la interfaz Entrada o tener más contexto.
                    // Por ahora, devolveremos vacío o una lista filtrada si Entrada tuviera un getCompra().
                    return false; // Placeholder, esta query no es directamente implementable con el modelo actual de Entrada.
                                  // Se necesitaría que Entrada tuviera una forma de obtener su compraId.
                })
                .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Entrada> findAllByEventoId(String eventoId) {
        lock.readLock().lock();
        try {
            if (eventoId == null) return new ArrayList<>();
            return entradasMap.values().stream()
                    .filter(e -> e.getEventoAsociado() != null && eventoId.equals(e.getEventoAsociado().getId()))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Entrada> findAllByEventoIdAndTipoEntrada(String eventoId, String nombreTipoEntrada) {
        lock.readLock().lock();
        try {
            if (eventoId == null || nombreTipoEntrada == null) return new ArrayList<>();
            return entradasMap.values().stream()
                    .filter(e -> e.getEventoAsociado() != null &&
                                 eventoId.equals(e.getEventoAsociado().getId()) &&
                                 nombreTipoEntrada.equalsIgnoreCase(e.getTipo()))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void deleteById(String idEntrada) {
        lock.writeLock().lock();
        try {
            entradasMap.remove(idEntrada);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteAllByCompraId(String compraId) {
        lock.writeLock().lock();
        try {
            if (compraId == null) return;
            // Similar a findByCompraId, necesitamos una forma de identificar las entradas por compraId.
            // Por ahora, esta operación no se puede implementar eficientemente sin cambiar el modelo de Entrada.
            // entradasMap.values().removeIf(e -> compraId.equals(e.getCompraId())); // Si existiera e.getCompraId()
            System.out.println("EntradaDAOImplMemoria: deleteAllByCompraId no implementado eficientemente sin getCompraId() en Entrada.");
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public long countByEventoId(String eventoId) {
        lock.readLock().lock();
        try {
            if (eventoId == null) return 0;
            return entradasMap.values().stream()
                    .filter(e -> e.getEventoAsociado() != null && eventoId.equals(e.getEventoAsociado().getId()))
                    .count();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public long countByEventoIdAndTipoEntrada(String eventoId, String nombreTipoEntrada) {
        lock.readLock().lock();
        try {
            if (eventoId == null || nombreTipoEntrada == null) return 0;
            return entradasMap.values().stream()
                    .filter(e -> e.getEventoAsociado() != null &&
                                 eventoId.equals(e.getEventoAsociado().getId()) &&
                                 nombreTipoEntrada.equalsIgnoreCase(e.getTipo()))
                    .count();
        } finally {
            lock.readLock().unlock();
        }
    }
}
