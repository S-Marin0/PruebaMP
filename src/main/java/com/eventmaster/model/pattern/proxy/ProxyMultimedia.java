package com.eventmaster.model.pattern.proxy;

import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Organizador; // Para control de acceso específico
import com.eventmaster.service.ControlAccesoService; // Servicio para verificar permisos

import java.util.Map;
import java.util.HashMap;

// El Proxy que controla el acceso al RecursoReal
public class ProxyMultimedia implements RecursoMultimedia {
    private RecursoReal recursoReal; // Referencia al objeto real
    private String urlRecurso;
    private String tipoRecurso;
    private long tamanoEstimadoBytes;

    // Cache simple en memoria (podría ser más sofisticada, ej. LRUCache)
    private static Map<String, byte[]> cacheContenido = new HashMap<>();
    private ControlAccesoService controlAccesoService;

    public ProxyMultimedia(String urlRecurso, String tipoRecurso, long tamanoEstimadoBytes, ControlAccesoService controlAccesoService) {
        this.urlRecurso = urlRecurso;
        this.tipoRecurso = tipoRecurso;
        this.tamanoEstimadoBytes = tamanoEstimadoBytes;
        this.controlAccesoService = controlAccesoService;
        // El RecursoReal se crea solo cuando es necesario (Lazy Initialization)
    }

    public ProxyMultimedia(String urlRecurso, String tipoRecurso, ControlAccesoService controlAccesoService) {
        this(urlRecurso, tipoRecurso, 0, controlAccesoService);
    }


    private boolean verificarPermiso(Usuario usuario) {
        if (usuario == null) { // Acceso anónimo
            // Permitir acceso a recursos públicos, denegar a protegidos
            // Aquí se necesitaría una forma de saber si el recurso es público.
            // Por ahora, denegamos si no hay usuario para recursos que podrían ser privados.
            System.out.println("ProxyMultimedia: Acceso anónimo. Verificando si el recurso es público...");
            // return controlAccesoService.esRecursoPublico(this.urlRecurso); // Asumiendo este método
            return false; // Denegar por defecto para anónimos si no es explícitamente público
        }

        // Lógica de control de acceso basada en el tipo de usuario o permisos específicos
        // Ejemplo: Solo organizadores pueden ver ciertos reportes o videos de "cómo hacer"
        if ("video_tutorial_organizador".equals(tipoRecurso)) {
            return usuario instanceof Organizador && controlAccesoService.tienePermiso(usuario, "VER_TUTORIAL_ORGANIZADOR");
        }

        // Para imágenes promocionales de eventos, usualmente son públicas una vez el evento está publicado.
        // El controlAccesoService podría tener lógica más fina.
        return controlAccesoService.tienePermiso(usuario, "VER_MULTIMEDIA_EVENTO", urlRecurso);
    }

    @Override
    public String getUrlRecurso() {
        return urlRecurso;
    }

    @Override
    public byte[] mostrar(Usuario usuario) throws Exception {
        System.out.println("ProxyMultimedia: Solicitud para mostrar " + urlRecurso + " por usuario " + (usuario != null ? usuario.getNombre() : "anónimo"));

        if (!verificarPermiso(usuario)) {
            System.err.println("ProxyMultimedia: Acceso denegado para " + urlRecurso + " por usuario " + (usuario != null ? usuario.getNombre() : "anónimo"));
            throw new SecurityException("Acceso denegado al recurso multimedia: " + urlRecurso);
        }
        System.out.println("ProxyMultimedia: Permiso concedido.");

        // Verificar caché
        if (cacheContenido.containsKey(urlRecurso)) {
            System.out.println("ProxyMultimedia: Recurso encontrado en caché: " + urlRecurso);
            return cacheContenido.get(urlRecurso);
        }

        // Si no está en caché, crear RecursoReal (Lazy Initialization) y cargar
        if (recursoReal == null) {
            System.out.println("ProxyMultimedia: Creando instancia de RecursoReal para " + urlRecurso);
            recursoReal = new RecursoReal(urlRecurso, tipoRecurso, tamanoEstimadoBytes);
        }

        byte[] contenido = recursoReal.mostrar(usuario); // El RecursoReal carga si es necesario

        // Añadir al caché si el contenido es válido y no es demasiado grande (política de caché simple)
        if (contenido != null && contenido.length > 0 && contenido.length < 5 * 1024 * 1024) { // Cachear si < 5MB
            cacheContenido.put(urlRecurso, contenido);
            System.out.println("ProxyMultimedia: Recurso " + urlRecurso + " añadido a la caché.");
        }

        return contenido;
    }

    @Override
    public String getTipo() {
        return tipoRecurso;
    }

    @Override
    public long getTamanoBytes() {
        if (recursoReal != null) {
            return recursoReal.getTamanoBytes(); // Obtener el tamaño real si el recurso ya fue cargado
        }
        return tamanoEstimadoBytes; // Devolver el estimado si no se ha cargado
    }

    public static void limpiarCache() {
        cacheContenido.clear();
        System.out.println("ProxyMultimedia: Caché de contenido limpiada.");
    }
}
