package com.eventmaster.model.pattern.builder;

// CAMBIO: Asegurar importación de Evento
import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.entity.Lugar;
import com.eventmaster.model.entity.Organizador;
import com.eventmaster.model.pattern.factory.TipoEntrada;
// No necesitamos las fábricas de TipoEntrada aquí, solo la definición de TipoEntrada
// ELIMINAR: import com.eventmaster.model.pattern.factory.TipoEntradaFactory;
// ELIMINAR: import com.eventmaster.model.pattern.factory.EntradaGeneralFactory;
// ELIMINAR: import com.eventmaster.model.pattern.factory.EntradaVIPFactory;


import java.time.LocalDateTime;
// ELIMINAR: import java.util.HashMap; (No usado directamente)
// ELIMINAR: import java.util.Map; (No usado directamente)

// Clase Director (Opcional) para construir tipos comunes de Eventos.
// No es estrictamente necesario si el cliente usa el Builder directamente,
// pero puede ser útil para encapsular lógicas de construcción complejas o recurrentes.
public class EventoDirector {

    // Ejemplo de cómo podría usarse un director para un tipo específico de evento.
    // El builder se pasa como argumento.
    // El tipo de 'builder' ya estaba correcto como Evento.EventoBuilder en la implementación anterior.
    public void construirEventoConciertoRock(Evento.EventoBuilder builder, String nombre, Organizador organizador, Lugar lugar, LocalDateTime fechaHora) {
        // El 'nombre', 'organizador', 'lugar', 'fechaHora' se asumen ya establecidos en el 'builder'
        // por quien lo instanció antes de pasarlo al director.
        // Por lo tanto, no llamamos a builder.setNombre(nombre) aquí.
        builder
            // Los siguientes ya están implícitos en el constructor del builder que recibe EventoDirector
            // .setOrganizador(organizador)
            // .setLugar(lugar)
            // .setFechaHora(fechaHora)
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
        // El 'nombre', 'organizador', 'lugar', 'fechaHora' se asumen ya establecidos en el 'builder'.
        builder
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
