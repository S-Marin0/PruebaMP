package com.eventmaster.model.pattern.builder;

import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.entity.Lugar;
import com.eventmaster.model.entity.Organizador;
import com.eventmaster.model.pattern.factory.TipoEntrada; // Asumiendo que esta es la clase/interfaz base para TipoEntrada
import com.eventmaster.model.pattern.factory.TipoEntradaFactory; // Interfaz de fábrica de entradas
import com.eventmaster.model.pattern.factory.EntradaGeneralFactory; // Fábrica concreta
import com.eventmaster.model.pattern.factory.EntradaVIPFactory; // Fábrica concreta


import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Clase Director (Opcional) para construir tipos comunes de Eventos.
// No es estrictamente necesario si el cliente usa el Builder directamente,
// pero puede ser útil para encapsular lógicas de construcción complejas o recurrentes.
public class EventoDirector {

    // Ejemplo de cómo podría usarse un director para un tipo específico de evento.
    // El builder se pasa como argumento.
    public void construirEventoConciertoRock(Evento.EventoBuilder builder, String nombre, Organizador organizador, Lugar lugar, LocalDateTime fechaHora) {
        builder
            .setNombre(nombre)
            // .setOrganizador(organizador) // El builder ya lo recibe en su constructor
            // .setLugar(lugar)             // El builder ya lo recibe en su constructor
            // .setFechaHora(fechaHora)     // El builder ya lo recibe en su constructor
            .setCategoria("Concierto de Rock")
            .setDescripcion("¡Prepárate para una noche de rock inolvidable!")
            .addImagenUrl("default_rock_concert_image.jpg")
            .setCapacidadTotal(lugar.obtenerCapacidadTotal() > 0 ? lugar.obtenerCapacidadTotal() : 500); // Capacidad por defecto o del lugar

        // Añadir tipos de entrada comunes para un concierto de rock
        TipoEntrada generalDef = new TipoEntrada("General", 50.00, (int)(builder.getCapacidadTotal() * 0.8), 10);
        TipoEntrada vipDef = new TipoEntrada("VIP", 150.00, (int)(builder.getCapacidadTotal() * 0.2), 5);
        vipDef.addBeneficioExtra("Acceso backstage");
        vipDef.addBeneficioExtra("Bebida de cortesía");

        builder.addTipoEntrada(generalDef.getNombreTipo(), generalDef);
        builder.addTipoEntrada(vipDef.getNombreTipo(), vipDef);

        // El método build() se llamaría fuera del director, sobre el builder configurado.
        // return builder.build(); // Opcionalmente el director podría retornar el objeto construido.
    }

    public void construirEventoConferenciaTech(Evento.EventoBuilder builder, String nombre, Organizador organizador, Lugar lugar, LocalDateTime fechaHora) {
        builder
            .setNombre(nombre)
            .setCategoria("Conferencia de Tecnología")
            .setDescripcion("Explora las últimas tendencias en tecnología e innovación.")
            .addImagenUrl("default_tech_conference_image.jpg")
            .setCapacidadTotal(lugar.obtenerCapacidadTotal() > 0 ? lugar.obtenerCapacidadTotal() : 200);

        TipoEntrada standardDef = new TipoEntrada("Standard", 100.00, (int)(builder.getCapacidadTotal() * 0.9), 0); // Sin límite por usuario
        TipoEntrada premiumDef = new TipoEntrada("Premium", 250.00, (int)(builder.getCapacidadTotal() * 0.1), 2);
        premiumDef.addBeneficioExtra("Acceso a talleres exclusivos");
        premiumDef.addBeneficioExtra("Material de la conferencia");

        builder.addTipoEntrada(standardDef.getNombreTipo(), standardDef);
        builder.addTipoEntrada(premiumDef.getNombreTipo(), premiumDef);
    }

    // Podríamos tener más métodos para otros tipos de eventos...
}
