package com.eventmaster.model.command;

import com.eventmaster.model.entity.Evento; // Ya estaba, pero es crucial
import com.eventmaster.model.entity.Organizador;
// ELIMINAR: import com.eventmaster.model.pattern.builder.EventoBuilder;
import com.eventmaster.service.EventoService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class EditarEventoCommand implements Command {
    private Evento eventoOriginal; // El evento a editar
    private Evento.EventoBuilder datosNuevosBuilder; // CAMBIO: Datos para la actualización
    private Organizador organizador; // Quien ejecuta la edición
    private EventoService eventoService; // Para persistir/notificar cambios
    private LocalDateTime tiempoEjecucion;

    // Para guardar el estado anterior y poder hacer undo
    private Map<String, Object> estadoAnterior;

    public EditarEventoCommand(Evento eventoOriginal, Evento.EventoBuilder datosNuevosBuilder, Organizador organizador, EventoService eventoService) { // CAMBIO
        this.eventoOriginal = eventoOriginal;
        this.datosNuevosBuilder = datosNuevosBuilder;
        this.organizador = organizador;
        this.eventoService = eventoService;
        this.estadoAnterior = new HashMap<>();
    }

    private void guardarEstadoAnterior() {
        estadoAnterior.put("nombre", eventoOriginal.getNombre());
        estadoAnterior.put("descripcion", eventoOriginal.getDescripcion());
        estadoAnterior.put("categoria", eventoOriginal.getCategoria());
        estadoAnterior.put("fechaHora", eventoOriginal.getFechaHora());
        estadoAnterior.put("lugar", eventoOriginal.getLugar());
        estadoAnterior.put("capacidadTotal", eventoOriginal.getCapacidadTotal());
        // Guardar más campos si son editables y relevantes para el undo
        // Por ejemplo, urlsImagenes, urlsVideos, tiposEntradaDisponibles (esto sería más complejo)
    }

    private void restaurarEstadoAnterior() {
        eventoOriginal.setNombre((String) estadoAnterior.get("nombre"));
        eventoOriginal.setDescripcion((String) estadoAnterior.get("descripcion"));
        eventoOriginal.setCategoria((String) estadoAnterior.get("categoria"));
        eventoOriginal.setFechaHora((LocalDateTime) estadoAnterior.get("fechaHora"));
        // La restauración de Lugar y Tipos de Entrada puede ser compleja si son objetos mutables
        // y no solo referencias. Aquí se asume una restauración simple.
        eventoOriginal.setLugar((com.eventmaster.model.entity.Lugar) estadoAnterior.get("lugar"));
        eventoOriginal.setCapacidadTotal((Integer) estadoAnterior.get("capacidadTotal"));
    }

    @Override
    public boolean execute() {
        this.tiempoEjecucion = LocalDateTime.now();
        if (eventoOriginal == null || !organizador.getEventosCreados().contains(eventoOriginal)) {
            System.err.println("Comando EditarEvento: Evento no válido o no pertenece al organizador.");
            return false;
        }

        guardarEstadoAnterior();

        try {
            // Aplicar los cambios desde el builder al evento original
            // El método editarEvento en Organizador podría hacer esto, o lo hacemos aquí.
            // Por simplicidad, lo hacemos aquí directamente sobre el eventoOriginal.
            if (datosNuevosBuilder.getNombre() != null) eventoOriginal.setNombre(datosNuevosBuilder.getNombre());
            if (datosNuevosBuilder.getDescripcion() != null) eventoOriginal.setDescripcion(datosNuevosBuilder.getDescripcion());
            if (datosNuevosBuilder.getCategoria() != null) eventoOriginal.setCategoria(datosNuevosBuilder.getCategoria());
            if (datosNuevosBuilder.getFechaHora() != null) eventoOriginal.setFechaHora(datosNuevosBuilder.getFechaHora());
            if (datosNuevosBuilder.getLugar() != null) eventoOriginal.setLugar(datosNuevosBuilder.getLugar());
            if (datosNuevosBuilder.getCapacidadTotal() > 0) eventoOriginal.setCapacidadTotal(datosNuevosBuilder.getCapacidadTotal());
            // Se podrían añadir más campos y lógica para actualizar imágenes, videos, tipos de entrada, etc.

            if (eventoService != null) {
                eventoService.actualizarEvento(eventoOriginal);
            }
            System.out.println("Comando EditarEvento: Evento '" + eventoOriginal.getNombre() + "' actualizado.");
            return true;
        } catch (Exception e) {
            System.err.println("Comando EditarEvento: Error durante la actualización - " + e.getMessage());
            // Intentar restaurar si falla a mitad
            restaurarEstadoAnterior(); // Podría no ser perfecto si el evento ya fue modificado parcialmente
            return false;
        }
    }

    @Override
    public boolean undo() {
        if (eventoOriginal == null || estadoAnterior.isEmpty()) {
            System.err.println("Comando EditarEvento: No se puede deshacer. Estado anterior no guardado o evento nulo.");
            return false;
        }

        restaurarEstadoAnterior();

        if (eventoService != null) {
            eventoService.actualizarEvento(eventoOriginal); // "Actualizar" al estado anterior
        }
        System.out.println("Comando EditarEvento: Deshacer edición del evento '" + eventoOriginal.getNombre() + "'.");
        estadoAnterior.clear(); // Limpiar para evitar múltiples undos incorrectos sin un redo
        return true;
    }

    @Override
    public String getDescription() {
        return "Editar Evento: " + (eventoOriginal != null ? eventoOriginal.getNombre() : "N/A");
    }

    @Override
    public LocalDateTime getTiempoEjecucion() {
        return tiempoEjecucion;
    }
}
