package com.eventmaster.model.pattern.strategy;

import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Evento;
import com.eventmaster.service.EventoService; // Para obtener todos los eventos

import java.util.List;
import java.util.ArrayList;

// Contexto para el patrón Strategy
public class GestorRecomendacionesStrategy {
    private EstrategiaRecomendacion estrategiaActual;
    private EventoService eventoService; // Para obtener la lista de todos los eventos

    public GestorRecomendacionesStrategy(EventoService eventoService) {
        // Se podría establecer una estrategia por defecto o requerirla en el constructor
        this.eventoService = eventoService;
        this.estrategiaActual = new RecomendacionPorPopularidad(); // Estrategia por defecto
    }

    public GestorRecomendacionesStrategy(EstrategiaRecomendacion estrategiaInicial, EventoService eventoService) {
        this.estrategiaActual = estrategiaInicial;
        this.eventoService = eventoService;
    }

    public void setEstrategia(EstrategiaRecomendacion nuevaEstrategia) {
        System.out.println("GestorRecomendaciones: Cambiando estrategia de recomendación a " + nuevaEstrategia.getClass().getSimpleName());
        this.estrategiaActual = nuevaEstrategia;
    }

    public List<Evento> generarRecomendacionesParaUsuario(Usuario usuario) {
        if (estrategiaActual == null) {
            System.err.println("GestorRecomendaciones: No hay estrategia de recomendación establecida.");
            return new ArrayList<>();
        }
        if (eventoService == null) {
            System.err.println("GestorRecomendaciones: EventoService no configurado. No se pueden obtener eventos.");
            return new ArrayList<>();
        }

        List<Evento> todosLosEventos = eventoService.getAllEventos(); // Obtener todos los eventos
        return estrategiaActual.recomendar(usuario, todosLosEventos);
    }

    // Método de conveniencia para cambiar la estrategia basado en un string (podría usar una fábrica de estrategias)
    public void cambiarEstrategia(String tipoEstrategia) {
        if ("historial".equalsIgnoreCase(tipoEstrategia)) {
            setEstrategia(new RecomendacionPorHistorial());
        } else if ("popularidad".equalsIgnoreCase(tipoEstrategia)) {
            setEstrategia(new RecomendacionPorPopularidad());
        } else if ("preferencias".equalsIgnoreCase(tipoEstrategia)) {
            setEstrategia(new RecomendacionPorPreferencias());
        } else {
            System.err.println("GestorRecomendaciones: Tipo de estrategia '" + tipoEstrategia + "' no reconocido. Usando estrategia actual.");
        }
    }
}
