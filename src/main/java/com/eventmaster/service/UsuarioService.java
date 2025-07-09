package com.eventmaster.service;

import com.eventmaster.dao.UsuarioDAO;
import com.eventmaster.dao.CompraDAO; // Para cuando se agrega una compra al historial
import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Compra;

import java.util.List;
import java.util.Optional;

public class UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final CompraDAO compraDAO; // Podría ser necesario si el historial de compras se gestiona extensamente aquí

    public UsuarioService(UsuarioDAO usuarioDAO, CompraDAO compraDAO) {
        this.usuarioDAO = usuarioDAO;
        this.compraDAO = compraDAO;
    }

    public Usuario registrarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser nulo.");
        }
        // Validar si ya existe un usuario con el mismo email
        Optional<Usuario> existenteConEmail = usuarioDAO.findByEmail(usuario.getEmail());
        if (existenteConEmail.isPresent() && (usuario.getId() == null || !existenteConEmail.get().getId().equals(usuario.getId()))) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con el email: " + usuario.getEmail());
        }

        Usuario usuarioGuardado = usuarioDAO.save(usuario);
        System.out.println("UsuarioService: Usuario '" + usuarioGuardado.getNombre() + "' registrado/actualizado con ID: " + usuarioGuardado.getId());
        return usuarioGuardado;
    }

    public Optional<Usuario> findUsuarioById(String id) {
        Optional<Usuario> usuarioOpt = usuarioDAO.findById(id);
        // Cargar historial de compras si es necesario aquí, o dejar que la capa de presentación/controlador lo haga
        usuarioOpt.ifPresent(this::cargarHistorialComprasParaUsuario);
        return usuarioOpt;
    }

    public Optional<Usuario> findUsuarioByEmail(String email) {
        Optional<Usuario> usuarioOpt = usuarioDAO.findByEmail(email);
        usuarioOpt.ifPresent(this::cargarHistorialComprasParaUsuario);
        return usuarioOpt;
    }

    public List<Usuario> getAllUsuarios() {
        List<Usuario> usuarios = usuarioDAO.findAll();
        usuarios.forEach(this::cargarHistorialComprasParaUsuario);
        return usuarios;
    }

    public void eliminarUsuario(String id) {
        // Considerar lógica adicional:
        // - ¿Qué pasa con los eventos creados por un organizador si se elimina?
        // - ¿Qué pasa con las compras realizadas por un asistente? (¿Anonimizar? ¿Borrado en cascada?)
        // Por ahora, solo elimina el usuario.
        usuarioDAO.deleteById(id);
        System.out.println("UsuarioService: Usuario con ID " + id + " eliminado.");
    }

    public void agregarCompraAlHistorial(Usuario usuario, Compra compra) {
        if (usuario == null || usuario.getId() == null || compra == null) {
            System.err.println("UsuarioService: Usuario o compra nulos, no se puede añadir al historial.");
            return;
        }
        // El objeto Compra ya debería tener al Usuario asociado y estar guardado por CompraDAO.
        // Aquí, solo nos aseguramos de que la referencia en el objeto Usuario (si la mantenemos en memoria) esté actualizada.
        // Y si el historial de compras del Usuario se carga bajo demanda, esta llamada podría no ser estrictamente necesaria
        // si la Compra ya está correctamente persistida con el usuarioId.

        // Si el objeto Usuario en memoria mantiene una lista de compras:
        Optional<Usuario> usuarioOpt = usuarioDAO.findById(usuario.getId()); // Recargar para asegurar estado actual
        if (usuarioOpt.isPresent()) {
            Usuario u = usuarioOpt.get();
            // Si la lista de compras no está ya cargada, cargarla.
            if (u.getHistorialCompras() == null || u.getHistorialCompras().isEmpty()) {
                cargarHistorialComprasParaUsuario(u);
            }
            // Añadir si no está presente (evitar duplicados si ya se cargó y añadió)
            if(u.getHistorialCompras().stream().noneMatch(c -> c.getId().equals(compra.getId()))){
                 u.addCompraAlHistorial(compra);
            }
            // No es necesario un usuarioDAO.save(u) aquí si el historial de compras
            // es una relación en la BBDD gestionada por CompraDAO (compra tiene usuario_id).
            // Si Usuario tuviera una colección de IDs de Compra, entonces sí se necesitaría actualizar Usuario.
            System.out.println("UsuarioService: Compra ID " + compra.getId() + " asociada al historial en memoria de " + u.getNombre());
        } else {
             System.err.println("UsuarioService: No se pudo añadir compra al historial, usuario no encontrado: " + usuario.getId());
        }
    }

    private void cargarHistorialComprasParaUsuario(Usuario usuario) {
        if (usuario != null && usuario.getId() != null) {
            // Limpiar historial actual para evitar duplicados si se llama múltiples veces
            // o asegurarse de que la lista en Usuario maneje esto.
            // usuario.getHistorialCompras().clear(); // Si se va a reemplazar siempre.

            List<Compra> historial = compraDAO.findByUsuarioId(usuario.getId());
            // Cargar las entradas para cada compra
            historial.forEach(compra -> {
                // Aquí se podría llamar a un EntradaService o EntradaDAO para cargar las entradas de la compra.
                // Por ahora, lo dejamos así, asumiendo que Compra podría tenerlas o se cargan después.
            });
            usuario.setHistorialCompras(historial);
        }
    }

    public Usuario autenticarUsuario(String email, String password) {
        Optional<Usuario> usuarioOpt = findUsuarioByEmail(email);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            // En una implementación real, se compararía un hash de la contraseña.
            if (usuario.getPassword().equals(password)) { // Simplificado
                System.out.println("UsuarioService: Usuario " + email + " autenticado correctamente.");
                return usuario;
            } else {
                System.out.println("UsuarioService: Contraseña incorrecta para " + email);
            }
        } else {
            System.out.println("UsuarioService: Usuario con email " + email + " no encontrado.");
        }
        return null;
    }

    // Otros métodos: actualizarPerfil, cambiarPassword, gestionarPreferencias (que usarían UsuarioDAO)
}
