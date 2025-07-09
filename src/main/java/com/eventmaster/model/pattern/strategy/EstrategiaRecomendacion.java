package com.eventmaster.model.pattern.strategy;

import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Evento;
import java.util.List;

// Interfaz para el patrón Strategy
public interface EstrategiaRecomendacion {
    /**
     * Genera una lista de eventos recomendados para un usuario.
     * @param usuario El usuario para el cual generar recomendaciones.
     * @param todosLosEventos Una lista de todos los eventos disponibles en el sistema.
     * @param historialCompras (Opcional) Podría ser parte del Usuario o pasarse explícitamente.
     * @param preferencias (Opcional) Podría ser parte del Usuario o pasarse explícitamente.
     * @return Una lista de eventos recomendados.
     */
    List<Evento> recomendar(Usuario usuario, List<Evento> todosLosEventos);
    // Se podría refinar para pasar explícitamente el historial y preferencias si no están en Usuario,
    // o si se quiere desacoplar más. Por ahora, se asume que se pueden obtener del objeto Usuario.
}
