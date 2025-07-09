package com.eventmaster.model.pattern.proxy;

import com.eventmaster.model.entity.Usuario; // Para control de acceso

// Interfaz común para el RecursoReal y el Proxy
public interface RecursoMultimedia {
    String getUrlRecurso(); // Devuelve la URL o identificador del recurso
    byte[] mostrar(Usuario usuario) throws Exception; // Devuelve el contenido del recurso (ej. bytes de imagen/video)
                                           // Lanza excepción si el acceso es denegado o hay error.
    String getTipo(); // Ej: "imagen", "video"
    long getTamanoBytes(); // Tamaño del recurso, útil para el proxy (caching, etc.)
}
