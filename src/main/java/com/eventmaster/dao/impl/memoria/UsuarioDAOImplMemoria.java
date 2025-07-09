package com.eventmaster.dao.impl.memoria;

import com.eventmaster.dao.UsuarioDAO;
import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Asistente;
import com.eventmaster.model.entity.Organizador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UsuarioDAOImplMemoria implements UsuarioDAO {

    private final Map<String, Usuario> usuariosMap = new HashMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public Optional<Usuario> findById(String id) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(usuariosMap.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        lock.readLock().lock();
        try {
            return usuariosMap.values().stream()
                    .filter(u -> u.getEmail().equalsIgnoreCase(email))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<Usuario> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(usuariosMap.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Usuario save(Usuario usuario) {
        lock.writeLock().lock();
        try {
            if (usuario == null) {
                throw new IllegalArgumentException("El usuario no puede ser nulo.");
            }
            if (usuario.getId() == null || usuario.getId().trim().isEmpty()) {
                usuario.setId(UUID.randomUUID().toString()); // Asignar ID si es nuevo
            }
            // Verificar si ya existe un usuario con ese email pero diferente ID (para evitar duplicados de email)
            findByEmail(usuario.getEmail()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(usuario.getId())) {
                    throw new IllegalArgumentException("Ya existe un usuario con el email: " + usuario.getEmail());
                }
            });

            usuariosMap.put(usuario.getId(), usuario);
            return usuario;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void deleteById(String id) {
        lock.writeLock().lock();
        try {
            usuariosMap.remove(id);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public long count() {
        lock.readLock().lock();
        try {
            return usuariosMap.size();
        } finally {
            lock.readLock().unlock();
        }
    }
}
