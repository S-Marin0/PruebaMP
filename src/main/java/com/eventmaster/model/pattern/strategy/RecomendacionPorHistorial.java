package com.eventmaster.model.pattern.strategy;

import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.entity.Asistente; // Para acceder a preferencias detalladas si es necesario

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Collections;

// Estrategia Concreta
public class RecomendacionPorHistorial implements EstrategiaRecomendacion {

    @Override
    public List<Evento> recomendar(Usuario usuario, List<Evento> todosLosEventos) {
        System.out.println("RecomendacionPorHistorial: Generando recomendaciones para " + usuario.getNombre());
        List<Evento> recomendaciones = new ArrayList<>();
        if (!(usuario instanceof Asistente) || todosLosEventos == null || todosLosEventos.isEmpty()) {
            return recomendaciones; // Solo para asistentes y si hay eventos para recomendar
        }

        Asistente asistente = (Asistente) usuario;
        List<Compra> historial = asistente.getHistorialCompras();

        if (historial == null || historial.isEmpty()) {
            System.out.println("RecomendacionPorHistorial: El usuario no tiene historial de compras.");
            // Podría delegar a otra estrategia o devolver vacío.
            return recomendaciones;
        }

        // 1. Analizar categorías y organizadores de eventos pasados
        Set<String> categoriasPasadas = new HashSet<>();
        Set<String> organizadoresPasados = new HashSet<>();
        Set<String> eventosCompradosIds = new HashSet<>();

        for (Compra compra : historial) {
            if ("COMPLETADA".equals(compra.getEstadoCompra()) || "PAGADA_PENDIENTE_CONFIRMACION".equals(compra.getEstadoCompra())) { // Considerar compras válidas
                Evento eventoComprado = compra.getEvento();
                if (eventoComprado != null) {
                    eventosCompradosIds.add(eventoComprado.getId());
                    if (eventoComprado.getCategoria() != null && !eventoComprado.getCategoria().isEmpty()) {
                        categoriasPasadas.add(eventoComprado.getCategoria().toLowerCase());
                    }
                    if (eventoComprado.getOrganizador() != null && eventoComprado.getOrganizador().getNombre() != null) {
                        organizadoresPasados.add(eventoComprado.getOrganizador().getNombre().toLowerCase());
                    }
                }
            }
        }

        if (categoriasPasadas.isEmpty() && organizadoresPasados.isEmpty()) {
            System.out.println("RecomendacionPorHistorial: No se pudieron extraer categorías u organizadores del historial.");
            return recomendaciones;
        }

        // 2. Filtrar eventos futuros que coincidan
        for (Evento eventoActual : todosLosEventos) {
            // No recomendar eventos ya comprados o pasados
            if (eventosCompradosIds.contains(eventoActual.getId()) ||
                eventoActual.getFechaHora().isBefore(java.time.LocalDateTime.now())) {
                continue;
            }

            boolean categoriaCoincide = eventoActual.getCategoria() != null && categoriasPasadas.contains(eventoActual.getCategoria().toLowerCase());
            boolean organizadorCoincide = eventoActual.getOrganizador() != null && eventoActual.getOrganizador().getNombre() != null &&
                                         organizadoresPasados.contains(eventoActual.getOrganizador().getNombre().toLowerCase());

            if (categoriaCoincide || organizadorCoincide) {
                // Se podría añadir un sistema de puntuación aquí (ej. +2 si coincide categoría, +1 si coincide organizador)
                recomendaciones.add(eventoActual);
            }
        }

        // 3. Ordenar (opcional, por fecha o relevancia) y limitar cantidad
        Collections.sort(recomendaciones, (e1, e2) -> e1.getFechaHora().compareTo(e2.getFechaHora())); // Por fecha más próxima

        int limiteRecomendaciones = 5; // Limitar a 5 recomendaciones
        return recomendaciones.stream().limit(limiteRecomendaciones).collect(Collectors.toList());
    }
}
