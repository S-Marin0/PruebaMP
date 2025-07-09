package com.eventmaster.model.pattern.factory;

import com.eventmaster.model.entity.Evento;
import java.util.Map;
// import java.util.List;

// Concrete Factory
public class EntradaVIPFactory implements TipoEntradaFactory {
    @Override
    public Entrada crearEntrada(String idEntrada, TipoEntrada tipoDefinicion, Evento evento, double precioFinal, Map<String, Object> extras) {
        if (!tipoDefinicion.getNombreTipo().equalsIgnoreCase("VIP")) {
             System.err.println("Advertencia: EntradaVIPFactory usada para un tipo no VIP: " + tipoDefinicion.getNombreTipo());
        }
        // Los beneficios ya deberían estar en tipoDefinicion.getBeneficiosExtra()
        // Si se pasaran beneficios adicionales en 'extras', se podrían añadir aquí.
        // Ejemplo:
        // if (extras != null && extras.containsKey("beneficiosAdicionales")) {
        //     List<String> beneficiosAdicionales = (List<String>) extras.get("beneficiosAdicionales");
        //     // Lógica para agregar estos beneficios al tipoDefinicion o a la instancia de EntradaVIP
        // }
        return new EntradaVIP(idEntrada, tipoDefinicion, evento, precioFinal);
    }
}
