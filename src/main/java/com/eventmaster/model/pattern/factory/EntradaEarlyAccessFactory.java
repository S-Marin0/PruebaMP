package com.eventmaster.model.pattern.factory;

import com.eventmaster.model.entity.Evento;
import java.util.Map;

// Concrete Factory
public class EntradaEarlyAccessFactory implements TipoEntradaFactory {
    @Override
    public Entrada crearEntrada(String idEntrada, TipoEntrada tipoDefinicion, Evento evento, double precioFinal, Map<String, Object> extras) {
        if (!tipoDefinicion.getNombreTipo().toLowerCase().contains("early")) { // Permite "Early Access", "Early Bird", etc.
            System.err.println("Advertencia: EntradaEarlyAccessFactory usada para un tipo no Early Access: " + tipoDefinicion.getNombreTipo());
        }

        int minutosAnticipacion = 0; // Valor por defecto
        if (extras != null && extras.containsKey("minutosAnticipacion")) {
            try {
                minutosAnticipacion = (Integer) extras.get("minutosAnticipacion");
            } catch (ClassCastException e) {
                System.err.println("Error al castear minutosAnticipacion: " + e.getMessage());
            }
        } else {
            // Podríamos tener una configuración por defecto para Early Access si no se especifica
            // Por ejemplo, buscar en tipoDefinicion si tiene algún campo para esto.
        }

        return new EntradaEarlyAccess(idEntrada, tipoDefinicion, evento, precioFinal, minutosAnticipacion);
    }
}
