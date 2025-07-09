package com.eventmaster.model.pattern.factory;

import com.eventmaster.model.entity.Evento;
import java.util.Map;

// Interfaz Factory Method
public interface TipoEntradaFactory {
    Entrada crearEntrada(String idEntrada, TipoEntrada tipoDefinicion, Evento evento, double precioFinal, Map<String, Object> extras);
}
