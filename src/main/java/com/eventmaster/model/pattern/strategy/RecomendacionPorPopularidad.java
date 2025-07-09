package com.eventmaster.model.pattern.strategy;

import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Evento;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

// Estrategia Concreta
public class RecomendacionPorPopularidad implements EstrategiaRecomendacion {

    @Override
    public List<Evento> recomendar(Usuario usuario, List<Evento> todosLosEventos) {
        System.out.println("RecomendacionPorPopularidad: Generando recomendaciones para " + usuario.getNombre());
        List<Evento> recomendaciones = new ArrayList<>();
        if (todosLosEventos == null || todosLosEventos.isEmpty()) {
            return recomendaciones;
        }

        // Filtrar eventos futuros y que estén publicados o en curso
        List<Evento> eventosCandidatos = todosLosEventos.stream()
            .filter(e -> e.getFechaHora().isAfter(java.time.LocalDateTime.now()) &&
                         ("Publicado".equals(e.getEstadoActual().getNombreEstado()) || "En Curso".equals(e.getEstadoActual().getNombreEstado())))
            .collect(Collectors.toList());

        // No recomendar eventos que el usuario ya haya comprado
        if (usuario.getHistorialCompras() != null && !usuario.getHistorialCompras().isEmpty()) {
            Set<String> eventosCompradosIds = usuario.getHistorialCompras().stream()
                                                 .map(compra -> compra.getEvento().getId())
                                                 .collect(Collectors.toSet());
            eventosCandidatos.removeIf(e -> eventosCompradosIds.contains(e.getId()));
        }


        // Ordenar por popularidad (ej. mayor número de entradas vendidas)
        // Si la capacidad es 0, podría dar problemas de división o ser un evento gratuito sin tracking de ventas así.
        // Daremos prioridad a los que tienen más entradas vendidas en términos absolutos.
        // Una métrica más sofisticada podría ser % de capacidad vendida.
        eventosCandidatos.sort(Comparator.comparingInt(Evento::getEntradasVendidas).reversed());

        int limiteRecomendaciones = 5;
        return eventosCandidatos.stream().limit(limiteRecomendaciones).collect(Collectors.toList());
    }
}
