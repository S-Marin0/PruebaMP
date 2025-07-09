package com.eventmaster.service;

import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Organizador;
import com.eventmaster.model.entity.Asistente;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// Servicio placeholder para la lógica de control de acceso
public class ControlAccesoService {

    // Simulación de roles y permisos
    private static final String ROL_ORGANIZADOR = "ORGANIZADOR";
    private static final String ROL_ASISTENTE = "ASISTENTE";
    private static final String ROL_ADMIN = "ADMINISTRADOR"; // Rol adicional

    private static final String PERMISO_VER_MULTIMEDIA_PUBLICA = "VER_MULTIMEDIA_PUBLICA";
    private static final String PERMISO_VER_MULTIMEDIA_EVENTO_PROPIO = "VER_MULTIMEDIA_EVENTO_PROPIO"; // Organizador para sus eventos
    private static final String PERMISO_VER_TUTORIAL_ORGANIZADOR = "VER_TUTORIAL_ORGANIZADOR";
    private static final String PERMISO_ACCESO_TOTAL_MULTIMEDIA = "ACCESO_TOTAL_MULTIMEDIA"; // Para Admin


    // En un sistema real, esto vendría de una base de datos o configuración de roles/permisos
    private Set<String> getPermisosPorRol(String rol) {
        Set<String> permisos = new HashSet<>();
        if (ROL_ASISTENTE.equals(rol)) {
            permisos.add(PERMISO_VER_MULTIMEDIA_PUBLICA);
        } else if (ROL_ORGANIZADOR.equals(rol)) {
            permisos.add(PERMISO_VER_MULTIMEDIA_PUBLICA);
            permisos.add(PERMISO_VER_MULTIMEDIA_EVENTO_PROPIO);
            permisos.add(PERMISO_VER_TUTORIAL_ORGANIZADOR);
        } else if (ROL_ADMIN.equals(rol)) {
            permisos.add(PERMISO_ACCESO_TOTAL_MULTIMEDIA);
            // Implícitamente tiene todos los demás o se añaden explícitamente
            permisos.addAll(getPermisosPorRol(ROL_ORGANIZADOR));
            permisos.addAll(getPermisosPorRol(ROL_ASISTENTE));
        }
        return permisos;
    }

    private String getRolUsuario(Usuario usuario) {
        if (usuario instanceof Organizador) { // Asumiendo que Admin podría ser un tipo de Organizador o un tipo aparte
            // if (usuario.isAdmin()) return ROL_ADMIN; // Si hay un flag de admin
            return ROL_ORGANIZADOR;
        } else if (usuario instanceof Asistente) {
            return ROL_ASISTENTE;
        }
        return "ANONIMO"; // O un rol por defecto
    }


    public boolean tienePermiso(Usuario usuario, String permisoRequerido) {
        if (usuario == null) return false; // Anónimo no tiene permisos específicos por defecto (salvo públicos)

        String rol = getRolUsuario(usuario);
        Set<String> permisosDelRol = getPermisosPorRol(rol);

        if (permisosDelRol.contains(PERMISO_ACCESO_TOTAL_MULTIMEDIA)) return true; // Admin tiene acceso a todo

        return permisosDelRol.contains(permisoRequerido);
    }

    /**
     * Verifica el permiso para un recurso específico, potencialmente considerando el propietario.
     * @param usuario El usuario que solicita acceso.
     * @param permisoRequerido El tipo de permiso general (ej. "VER_MULTIMEDIA_EVENTO").
     * @param idRecurso El ID del recurso (ej. URL de imagen, ID de evento asociado).
     * @return true si tiene permiso, false en caso contrario.
     */
    public boolean tienePermiso(Usuario usuario, String permisoRequerido, String idRecurso) {
         if (usuario == null && PERMISO_VER_MULTIMEDIA_PUBLICA.equals(permisoRequerido)) {
             // Lógica para determinar si idRecurso es público (ej. consultando el evento asociado)
             // return esRecursoPublico(idRecurso);
             return true; // Simulación: toda multimedia de evento es pública si se pide con este permiso
         }
         if (usuario == null) return false;

        String rol = getRolUsuario(usuario);
        Set<String> permisosDelRol = getPermisosPorRol(rol);

        if (permisosDelRol.contains(PERMISO_ACCESO_TOTAL_MULTIMEDIA)) return true;

        if (permisosDelRol.contains(permisoRequerido)) {
            // Lógica adicional si el permiso depende del recurso
            if (PERMISO_VER_MULTIMEDIA_EVENTO_PROPIO.equals(permisoRequerido) && usuario instanceof Organizador) {
                Organizador org = (Organizador) usuario;
                // Necesitaríamos una forma de saber a qué evento pertenece idRecurso
                // y si ese evento fue creado por 'org'.
                // Esto es complejo sin acceso a EventoService aquí o pasando el Evento.
                // Por ahora, si es organizador y pide este permiso, se lo damos (simplificación).
                System.out.println("ControlAccesoService: Permiso VER_MULTIMEDIA_EVENTO_PROPIO concedido (simulado para Organizador).");
                return true;
            }
            // Para otros permisos que no dependen del recurso específico más allá del tipo general
            return true;
        }
        return false;
    }

    public boolean esRecursoPublico(String idRecurso) {
        // Lógica para determinar si un recurso (ej. imagen de un evento publicado) es público.
        // Podría consultar EventoService para ver el estado del evento al que pertenece la imagen.
        // Por ahora, simulamos que ciertos patrones de URL son públicos.
        if (idRecurso.contains("public_")) {
            return true;
        }
        // O si es una imagen de un evento que está en estado "Publicado".
        // Evento eventoAsociado = eventoService.findEventoPorUrlRecurso(idRecurso);
        // return eventoAsociado != null && "Publicado".equals(eventoAsociado.getEstadoActual().getNombreEstado());
        return false; // Por defecto no es público si no cumple una condición explícita
    }
}
