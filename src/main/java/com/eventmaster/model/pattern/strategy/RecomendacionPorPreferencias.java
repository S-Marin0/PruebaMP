package com.eventmaster.model.pattern.strategy;

import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.entity.Asistente;
import com.eventmaster.model.Preferencia; // Asumiendo que Asistente tiene una lista de Preferencia

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Collections;

// Estrategia Concreta
public class RecomendacionPorPreferencias implements EstrategiaRecomendacion {

    @Override
    public List<Evento> recomendar(Usuario usuario, List<Evento> todosLosEventos) {
        System.out.println("RecomendacionPorPreferencias: Generando recomendaciones para " + usuario.getNombre());
        List<Evento> recomendaciones = new ArrayList<>();
        if (!(usuario instanceof Asistente) || todosLosEventos == null || todosLosEventos.isEmpty()) {
            return recomendaciones;
        }

        Asistente asistente = (Asistente) usuario;
        List<Preferencia> preferenciasUsuario = asistente.getPreferenciasDetalladas(); // Usar las detalladas
        // O la lista de strings de la clase base: List<String> preferenciasNombres = asistente.getPreferencias();


        if (preferenciasUsuario == null || preferenciasUsuario.isEmpty()) {
            System.out.println("RecomendacionPorPreferencias: El usuario no tiene preferencias definidas.");
            // Podría delegar a otra estrategia o devolver vacío.
            return recomendaciones;
        }

        Set<String> categoriasPreferidas = new HashSet<>();
        // Set<String> artistasPreferidos = new HashSet<>(); // Si tuviéramos esta info

        for (Preferencia pref : preferenciasUsuario) {
            // Asumimos que el nombre de la preferencia puede ser directamente una categoría de evento
            // o se necesita una lógica para mapear preferencias a categorías/tags de eventos.
            // Por simplicidad, usaremos el nombre de la preferencia como si fuera una categoría.
            if ("CategoriaEvento".equalsIgnoreCase(pref.getTipo()) || pref.getTipo() == null) { // Si el tipo es nulo, asumimos que es categoría
                 categoriasPreferidas.add(pref.getNombre().toLowerCase());
            }
            // else if ("Artista".equalsIgnoreCase(pref.getTipo())) {
            //    artistasPreferidos.add(pref.getNombre().toLowerCase());
            // }
        }

        if (categoriasPreferidas.isEmpty() /* && artistasPreferidos.isEmpty() */) {
             System.out.println("RecomendacionPorPreferencias: No se pudieron extraer categorías de las preferencias.");
            return recomendaciones;
        }

        Set<String> eventosCompradosIds = new HashSet<>();
        if (asistente.getHistorialCompras() != null) {
             eventosCompradosIds = asistente.getHistorialCompras().stream()
                                                 .map(compra -> compra.getEvento().getId())
                                                 .collect(Collectors.toSet());
        }

        for (Evento eventoActual : todosLosEventos) {
            if (eventosCompradosIds.contains(eventoActual.getId()) ||
                eventoActual.getFechaHora().isBefore(java.time.LocalDateTime.now()) ||
                !"Publicado".equals(eventoActual.getEstadoActual().getNombreEstado())) { // Solo eventos publicados y futuros
                continue;
            }

            boolean categoriaCoincide = eventoActual.getCategoria() != null &&
                                        categoriasPreferidas.contains(eventoActual.getCategoria().toLowerCase());

            // boolean artistaCoincide = false; // Lógica si se rastrean artistas en eventos

            if (categoriaCoincide /* || artistaCoincide */) {
                recomendaciones.add(eventoActual);
            }
        }

        Collections.sort(recomendaciones, (e1, e2) -> e1.getFechaHora().compareTo(e2.getFechaHora()));

        int limiteRecomendaciones = 5;
        return recomendaciones.stream().limit(limiteRecomendaciones).collect(Collectors.toList());
    }
}
