package com.eventmaster.model.entity;

// CAMBIO: Importar Evento para poder referenciar Evento.EventoBuilder
import com.eventmaster.model.entity.Evento;
// ELIMINAR: import com.eventmaster.model.pattern.builder.EventoBuilder;
import java.util.ArrayList;
import java.util.List;

public class Organizador extends Usuario {
    private String infoContacto;
    private List<Evento> eventosCreados;

    public Organizador(String id, String nombre, String email, String password, String infoContacto) {
        super(id, nombre, email, password);
        this.infoContacto = infoContacto;
        this.eventosCreados = new ArrayList<>();
    }

    // Getters y Setters
    public String getInfoContacto() {
        return infoContacto;
    }

    public void setInfoContacto(String infoContacto) {
        this.infoContacto = infoContacto;
    }

    public List<Evento> getEventosCreados() {
        return eventosCreados;
    }

    public void addEventoCreado(Evento evento) {
        this.eventosCreados.add(evento);
    }

    // Métodos específicos del Organizador
    public Evento crearEvento(Evento.EventoBuilder builder) { // CAMBIO: Usar Evento.EventoBuilder
        // El builder ya debería tener seteado el organizador
        // Aquí se podría añadir lógica adicional si es necesario antes de construir
        Evento evento = builder.build();
        addEventoCreado(evento);
        return evento;
    }

    public void editarEvento(Evento evento, Evento.EventoBuilder builder) { // CAMBIO: Usar Evento.EventoBuilder
        // Lógica para editar un evento existente usando un builder
        // Esto es conceptual, la implementación real podría variar
        if (eventosCreados.contains(evento)) {
            // Actualizar los campos del evento original con los del builder
            // Asumiendo que EventoBuilder tiene getters para todos estos campos
            if(builder.getNombre() != null) evento.setNombre(builder.getNombre());
            if(builder.getDescripcion() != null) evento.setDescripcion(builder.getDescripcion());
            if(builder.getFechaHora() != null) evento.setFechaHora(builder.getFechaHora()); // Corregido a getFechaHora
            if(builder.getLugar() != null) evento.setLugar(builder.getLugar());
            if(builder.getCategoria() != null) evento.setCategoria(builder.getCategoria());
            if(builder.getCapacidadTotal() > 0) evento.setCapacidadTotal(builder.getCapacidadTotal());
            // Para imágenes, videos, tipos de entrada, la actualización sería más compleja
            // y podría implicar limpiar listas y añadir nuevos elementos del builder.
            System.out.println("Evento " + evento.getNombre() + " editado.");
        } else {
            System.out.println("Error: El evento no pertenece a este organizador.");
        }
    }

    public void eliminarEvento(Evento evento) {
        if (eventosCreados.remove(evento)) {
            System.out.println("Evento " + evento.getNombre() + " eliminado.");
            // Aquí también se podría cambiar el estado del evento a cancelado o archivado
            // evento.setEstado(new EstadoCancelado()); // Asumiendo que el evento tiene setEstado
        } else {
            System.out.println("Error: No se pudo eliminar el evento.");
        }
    }

    public String verEstadisticas(Evento evento) {
        // Lógica para generar y devolver estadísticas de un evento
        if (eventosCreados.contains(evento)) {
            return "Estadísticas para " + evento.getNombre() + ": Entradas vendidas: " + evento.getEntradasVendidas() +
                   ", Capacidad: " + evento.getCapacidadTotal(); // Simplificado
        }
        return "No se pueden mostrar estadísticas para este evento.";
    }

    @Override
    public List<Evento> obtenerRecomendaciones() {
        // Los organizadores generalmente no reciben recomendaciones de eventos para asistir
        System.out.println("Los organizadores no reciben recomendaciones de eventos de la misma forma que los asistentes.");
        return new ArrayList<>();
    }
}
