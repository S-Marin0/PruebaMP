package com.eventmaster.service;

import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Compra;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Servicio placeholder para gestionar usuarios
public class UsuarioService {

    private static Map<String, Usuario> repositorioDeUsuarios = new HashMap<>(); // Simulación

    public void registrarUsuario(Usuario usuario) {
        if (usuario != null && !repositorioDeUsuarios.containsKey(usuario.getId())) {
            repositorioDeUsuarios.put(usuario.getId(), usuario);
            System.out.println("UsuarioService: Usuario '" + usuario.getNombre() + "' registrado.");
        }
    }

    public Optional<Usuario> findUsuarioById(String id) {
        return Optional.ofNullable(repositorioDeUsuarios.get(id));
    }

    public void agregarCompraAlHistorial(Usuario usuario, Compra compra) {
        if (usuario != null && compra != null) {
            // En un sistema real, se buscaría el usuario por ID y se actualizaría.
            Usuario u = repositorioDeUsuarios.get(usuario.getId());
            if (u != null) {
                u.addCompraAlHistorial(compra); // Asume que Usuario tiene este método
                System.out.println("UsuarioService: Compra ID " + compra.getId() + " añadida al historial de " + u.getNombre());
            } else {
                 System.err.println("UsuarioService: No se pudo añadir compra al historial, usuario no encontrado: " + usuario.getId());
            }
        }
    }

    // Otros métodos: actualizarUsuario, eliminarUsuario, buscarPorEmail, etc.
}
