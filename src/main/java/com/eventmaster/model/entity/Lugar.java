package com.eventmaster.model.entity;

import com.eventmaster.model.pattern.composite.ComponenteLugar;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Lugar es un Composite que puede contener Secciones u otros ComponenteLugar
public class Lugar implements ComponenteLugar {
    private String id;
    private String nombre;
    private String direccion;
    private List<String> tiposEventosAdmitidos; // Ej: "Concierto", "Conferencia"
    private List<ComponenteLugar> subcomponentes; // Secciones, áreas específicas, etc.
    // Mapa para reservas: Key podría ser LocalDateTime de inicio, Value podría ser un objeto ReservaDetalle o simplemente el ID del evento.
    // Para simplificar, usaremos String para la franja (ej. "2024-12-25-MAÑANA") y el ID del evento.
    private Map<String, String> reservasPorFranja; // franja -> eventoId

    public Lugar(String id, String nombre, String direccion) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.tiposEventosAdmitidos = new ArrayList<>();
        this.subcomponentes = new ArrayList<>();
        this.reservasPorFranja = new HashMap<>();
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public List<String> getTiposEventosAdmitidos() {
        return tiposEventosAdmitidos;
    }

    public void addTipoEventoAdmitido(String tipoEvento) {
        this.tiposEventosAdmitidos.add(tipoEvento);
    }

    public List<ComponenteLugar> getSubcomponentes() {
        return subcomponentes;
    }

    // Métodos del patrón Composite
    @Override
    public void agregar(ComponenteLugar componente) {
        subcomponentes.add(componente);
    }

    @Override
    public void remover(ComponenteLugar componente) {
        subcomponentes.remove(componente);
    }

    @Override
    public ComponenteLugar getHijo(int i) {
        return subcomponentes.get(i);
    }

    @Override
    public int obtenerCapacidadTotal() {
        int capacidadTotal = 0;
        if (subcomponentes.isEmpty()) {
            // Si un Lugar no tiene subcomponentes explícitos, podría tener una capacidad base.
            // Para este modelo, asumiremos que si no hay subcomponentes, su capacidad es 0
            // o debe ser seteada explícitamente. O, si tiene asientos directamente (no modelado así aun).
            // Por ahora, si no hay subcomponentes, la capacidad viene de ellos.
            // Si quisiéramos una capacidad propia del Lugar (ej. un campo abierto), se añadiría aquí.
        } else {
            for (ComponenteLugar componente : subcomponentes) {
                capacidadTotal += componente.obtenerCapacidadTotal();
            }
        }
        return capacidadTotal;
    }

    // Gestión de Reservas (simplificado)
    // Una franja podría ser "MAÑANA", "TARDE", "NOCHE" para una fecha específica.
    // Key formato: "YYYY-MM-DD-FRANJA" (ej. "2024-12-25-TARDE")
    public boolean reservarFranja(LocalDateTime fecha, String franja, String eventoId) {
        String claveReserva = fecha.toLocalDate().toString() + "-" + franja.toUpperCase();
        if (!reservasPorFranja.containsKey(claveReserva)) {
            reservasPorFranja.put(claveReserva, eventoId);
            System.out.println("Lugar '" + nombre + "' reservado para el evento '" + eventoId + "' en la franja " + franja + " del " + fecha.toLocalDate());
            return true;
        }
        System.out.println("Lugar '" + nombre + "' ya está reservado para la franja " + franja + " del " + fecha.toLocalDate());
        return false;
    }

    public boolean estaDisponible(LocalDateTime fecha, String franja) {
        String claveReserva = fecha.toLocalDate().toString() + "-" + franja.toUpperCase();
        return !reservasPorFranja.containsKey(claveReserva);
    }

    public void liberarFranja(LocalDateTime fecha, String franja) {
         String claveReserva = fecha.toLocalDate().toString() + "-" + franja.toUpperCase();
         if (reservasPorFranja.containsKey(claveReserva)) {
             reservasPorFranja.remove(claveReserva);
             System.out.println("Reserva para el lugar '" + nombre + "' en la franja " + franja + " del " + fecha.toLocalDate() + " liberada.");
         } else {
             System.out.println("No había reserva para el lugar '" + nombre + "' en la franja " + franja + " del " + fecha.toLocalDate() + ".");
         }
    }

    public Map<String, String> getReservasPorFranja() {
        return reservasPorFranja;
    }
}
