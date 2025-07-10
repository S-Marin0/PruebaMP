package com.eventmaster.model.command;

import com.eventmaster.model.entity.Evento; // Ya estaba, pero es crucial para Evento.EventoBuilder
import com.eventmaster.model.entity.Organizador;
// ELIMINAR: import com.eventmaster.model.pattern.builder.EventoBuilder;
import com.eventmaster.service.EventoService;

import java.time.LocalDateTime;

public class CrearEventoCommand implements Command {
    private Organizador organizador;
    private Evento.EventoBuilder eventoBuilder; // CAMBIO
    private Evento eventoCreado;
    private EventoService eventoService;
    private LocalDateTime tiempoEjecucion;

    public CrearEventoCommand(Organizador organizador, Evento.EventoBuilder eventoBuilder, EventoService eventoService) { // CAMBIO
        this.organizador = organizador;
        this.eventoBuilder = eventoBuilder;
        this.eventoService = eventoService;
    }

    public CrearEventoCommand(Organizador organizador, Evento.EventoBuilder eventoBuilder) { // CAMBIO
        this(organizador, eventoBuilder, null);
    }

    @Override
    public boolean execute() {
        this.tiempoEjecucion = LocalDateTime.now();
        try {
            // El organizador es quien tiene la responsabilidad de usar el builder
            // y añadir el evento a su lista.
            this.eventoCreado = organizador.crearEvento(eventoBuilder);

            if (eventoCreado != null) {
                // Opcionalmente, usar un servicio para lógica adicional (persistir, notificar, etc.)
                if (eventoService != null) {
                    eventoService.registrarNuevoEvento(eventoCreado);
                }
                System.out.println("Comando CrearEvento: Evento '" + eventoCreado.getNombre() + "' creado exitosamente por " + organizador.getNombre());
                return true;
            } else {
                System.err.println("Comando CrearEvento: Fallo al crear el evento con el builder.");
                return false;
            }
        } catch (IllegalStateException e) {
            System.err.println("Comando CrearEvento: Error durante la ejecución - " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean undo() {
        if (eventoCreado != null && organizador.getEventosCreados().contains(eventoCreado)) {
            // Para deshacer, el organizador debe poder eliminar el evento.
            // Y el eventoService (si existe) debería poder des-registrar o marcar como eliminado.
            organizador.eliminarEvento(eventoCreado); // Asume que Organizador tiene un método para esto

            if (eventoService != null) {
                eventoService.eliminarEventoRegistrado(eventoCreado);
            }
            System.out.println("Comando CrearEvento: Deshacer creación del evento '" + eventoCreado.getNombre() + "'.");
            eventoCreado = null; // Limpiar la referencia
            return true;
        }
        System.err.println("Comando CrearEvento: No se puede deshacer. El evento no fue creado o ya fue eliminado.");
        return false;
    }

    @Override
    public String getDescription() {
        return "Crear Evento: " + (eventoBuilder != null ? eventoBuilder.getNombre() : "N/A");
    }

    @Override
    public LocalDateTime getTiempoEjecucion() {
        return tiempoEjecucion;
    }

    public Evento getEventoCreado() {
        return eventoCreado;
    }
}
