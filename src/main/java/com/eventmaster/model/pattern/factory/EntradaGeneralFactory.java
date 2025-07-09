package com.eventmaster.model.pattern.factory;

import com.eventmaster.model.entity.Evento;
import java.util.Map;

// Concrete Factory
public class EntradaGeneralFactory implements TipoEntradaFactory {
    @Override
    public Entrada crearEntrada(String idEntrada, TipoEntrada tipoDefinicion, Evento evento, double precioFinal, Map<String, Object> extras) {
        if (!tipoDefinicion.getNombreTipo().equalsIgnoreCase("General")) {
            // Podríamos lanzar una excepción o manejarlo de otra forma si el tipo no coincide
            System.err.println("Advertencia: EntradaGeneralFactory usada para un tipo no General: " + tipoDefinicion.getNombreTipo());
        }
        return new EntradaGeneral(idEntrada, tipoDefinicion, evento, precioFinal);
    }
}
