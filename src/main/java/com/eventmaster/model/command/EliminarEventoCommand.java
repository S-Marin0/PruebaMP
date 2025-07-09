package com.eventmaster.model.command;

import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.entity.Organizador;
import com.eventmaster.model.pattern.state.EstadoEvento; // Para guardar el estado anterior
import com.eventmaster.service.EventoService;

import java.time.LocalDateTime;

public class EliminarEventoCommand implements Command {
    private Evento eventoAEliminar;
    private Organizador organizador;
    private EventoService eventoService;
    private LocalDateTime tiempoEjecucion;

    // Para el undo, necesitamos recrear o restaurar el evento
    private Evento eventoEliminadoBackup; // Guardamos una copia o los datos para recrearlo
    private EstadoEvento estadoOriginalEvento; // Si la "eliminación" es un cambio de estado

    public EliminarEventoCommand(Evento eventoAEliminar, Organizador organizador, EventoService eventoService) {
        this.eventoAEliminar = eventoAEliminar;
        this.organizador = organizador;
        this.eventoService = eventoService;
    }

    @Override
    public boolean execute() {
        this.tiempoEjecucion = LocalDateTime.now();
        if (eventoAEliminar == null || !organizador.getEventosCreados().contains(eventoAEliminar)) {
            System.err.println("Comando EliminarEvento: Evento no válido o no pertenece al organizador.");
            return false;
        }

        // Guardar estado para un posible undo.
        // La forma de "backup" depende de si es una eliminación lógica (cambio de estado) o física.
        // Si es física, necesitaríamos una copia profunda o todos sus datos.
        // Si es lógica (ej. cambiar estado a 'Cancelado' o 'Archivado'), guardamos el estado anterior.
        this.estadoOriginalEvento = eventoAEliminar.getEstadoActual();
        // this.eventoEliminadoBackup = crearCopiaProfunda(eventoAEliminar); // Para eliminación física

        try {
            // Opción 1: Eliminación lógica (cambio de estado)
            // eventoAEliminar.cancelar(); // O un nuevo estado "Archivado"
            // System.out.println("Comando EliminarEvento: Evento '" + eventoAEliminar.getNombre() + "' marcado como cancelado/archivado.");

            // Opción 2: Eliminación física de la lista del organizador y del servicio
            organizador.eliminarEvento(eventoAEliminar); // Este método debería quitarlo de la lista eventosCreados
             if (eventoService != null) {
                eventoService.eliminarEventoRegistrado(eventoAEliminar);
            }
            System.out.println("Comando EliminarEvento: Evento '" + eventoAEliminar.getNombre() + "' eliminado.");
            this.eventoEliminadoBackup = eventoAEliminar; // Guardamos la referencia para el undo (si es eliminación física)


            return true;
        } catch (Exception e) {
            System.err.println("Comando EliminarEvento: Error durante la eliminación - " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean undo() {
        if (eventoEliminadoBackup == null) {
            System.err.println("Comando EliminarEvento: No se puede deshacer. No hay backup del evento eliminado.");
            return false;
        }

        // Opción 1: Si fue eliminación lógica (cambio de estado)
        // eventoEliminadoBackup.setEstadoActual(estadoOriginalEvento);
        // System.out.println("Comando EliminarEvento: Deshacer eliminación lógica del evento '" + eventoEliminadoBackup.getNombre() + "'. Estado restaurado.");
        // if (eventoService != null) {
        //    eventoService.actualizarEvento(eventoEliminadoBackup);
        // }

        // Opción 2: Si fue eliminación física
        // Volver a añadir el evento a la lista del organizador y al servicio
        organizador.addEventoCreado(eventoEliminadoBackup); // Asumiendo que Organizador tiene este método
        if (eventoService != null) {
            eventoService.registrarNuevoEvento(eventoEliminadoBackup); // O un método "restaurarEvento"
        }
        // Si el estado fue modificado antes de la eliminación física (ej. a Cancelado), restaurarlo.
        if(estadoOriginalEvento != null && eventoEliminadoBackup.getEstadoActual().getClass() != estadoOriginalEvento.getClass()){
            eventoEliminadoBackup.setEstadoActual(estadoOriginalEvento);
        }

        System.out.println("Comando EliminarEvento: Deshacer eliminación del evento '" + eventoEliminadoBackup.getNombre() + "'. Evento restaurado.");
        eventoEliminadoBackup = null; // Limpiar backup para evitar múltiples undos incorrectos
        return true;
    }

    @Override
    public String getDescription() {
        return "Eliminar Evento: " + (eventoAEliminar != null ? eventoAEliminar.getNombre() : (eventoEliminadoBackup != null ? eventoEliminadoBackup.getNombre() : "N/A"));
    }

    @Override
    public LocalDateTime getTiempoEjecucion() {
        return tiempoEjecucion;
    }
}
