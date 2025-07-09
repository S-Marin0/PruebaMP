package com.eventmaster.model.entity;

import com.eventmaster.model.pattern.builder.EventoBuilder;
import com.eventmaster.model.pattern.state.EstadoEvento;
import com.eventmaster.model.pattern.state.EstadoBorrador; // Estado inicial por defecto
import com.eventmaster.model.pattern.factory.TipoEntrada; // Asumiendo que TipoEntrada es la interfaz/clase base para los tipos de entrada

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Evento {
    private String id;
    private String nombre;
    private String descripcion;
    private String categoria; // Ejemplo: Concierto, Conferencia, Deporte, Teatro
    private LocalDateTime fechaHora;
    private Lugar lugar;
    private Organizador organizador;
    private int capacidadTotal;
    private int entradasVendidas;
    private EstadoEvento estadoActual;
    private List<String> urlsImagenes; // URLs a imágenes promocionales
    private List<String> urlsVideos;   // URLs a videos promocionales
    private Map<String, TipoEntrada> tiposEntradaDisponibles; // nombre del tipo de entrada -> objeto TipoEntrada

    // Constructor privado para ser usado por el Builder
    private Evento(EventoBuilder builder) {
        this.id = builder.getId(); // Asumiendo que el ID se puede generar o setear en el builder
        this.nombre = builder.getNombre();
        this.descripcion = builder.getDescripcion();
        this.categoria = builder.getCategoria();
        this.fechaHora = builder.getFechaHora();
        this.lugar = builder.getLugar();
        this.organizador = builder.getOrganizador();
        this.capacidadTotal = builder.getCapacidadTotal();
        this.entradasVendidas = 0; // Inicialmente no hay entradas vendidas
        this.estadoActual = builder.getEstadoActual() != null ? builder.getEstadoActual() : new EstadoBorrador(this); // Estado inicial
        this.urlsImagenes = builder.getUrlsImagenes() != null ? builder.getUrlsImagenes() : new ArrayList<>();
        this.urlsVideos = builder.getUrlsVideos() != null ? builder.getUrlsVideos() : new ArrayList<>();
        this.tiposEntradaDisponibles = builder.getTiposEntradaDisponibles() != null ? builder.getTiposEntradaDisponibles() : new HashMap<>();
        if (this.lugar != null && this.capacidadTotal == 0) {
            this.capacidadTotal = this.lugar.obtenerCapacidadTotal(); // Tomar capacidad del lugar si no se especifica
        }
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public Lugar getLugar() {
        return lugar;
    }

    public Organizador getOrganizador() {
        return organizador;
    }

    public int getCapacidadTotal() {
        return capacidadTotal;
    }

    public int getEntradasVendidas() {
        return entradasVendidas;
    }

    public EstadoEvento getEstadoActual() {
        return estadoActual;
    }

    public List<String> getUrlsImagenes() {
        return urlsImagenes;
    }

    public List<String> getUrlsVideos() {
        return urlsVideos;
    }

    public Map<String, TipoEntrada> getTiposEntradaDisponibles() {
        return tiposEntradaDisponibles;
    }

    // Setters (algunos pueden ser manejados por el estado o ser inmutables después de la creación)
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public void setLugar(Lugar lugar) {
        this.lugar = lugar;
        // Si la capacidad del evento no fue fijada explícitamente, tomar la del nuevo lugar
        if (this.capacidadTotal == 0 && lugar != null) {
            this.capacidadTotal = lugar.obtenerCapacidadTotal();
        }
    }

    public void setOrganizador(Organizador organizador) {
        this.organizador = organizador;
    }

    public void setCapacidadTotal(int capacidadTotal) {
        this.capacidadTotal = capacidadTotal;
    }

    public void setEstadoActual(EstadoEvento estadoActual) {
        this.estadoActual = estadoActual;
    }

    // Métodos de negocio
    public void agregarTipoEntrada(String nombreTipo, TipoEntrada tipoEntrada) {
        this.tiposEntradaDisponibles.put(nombreTipo, tipoEntrada);
    }

    public TipoEntrada getTipoEntrada(String nombreTipo) {
        return this.tiposEntradaDisponibles.get(nombreTipo);
    }

    public boolean venderEntradas(String tipoEntradaNombre, int cantidad) {
        TipoEntrada tipoEntrada = tiposEntradaDisponibles.get(tipoEntradaNombre);
        if (tipoEntrada == null) {
            System.out.println("Error: Tipo de entrada '" + tipoEntradaNombre + "' no existe para este evento.");
            return false;
        }

        if (entradasVendidas + cantidad <= capacidadTotal && tipoEntrada.revisarDisponibilidad(cantidad)) {
            tipoEntrada.reducirDisponibilidad(cantidad);
            entradasVendidas += cantidad;
            System.out.println(cantidad + " entradas de tipo '" + tipoEntradaNombre + "' vendidas para " + nombre);
            return true;
        }
        System.out.println("No hay suficientes entradas disponibles o se excede la capacidad del evento.");
        return false;
    }

    public void devolverEntradas(String tipoEntradaNombre, int cantidad) {
        TipoEntrada tipoEntrada = tiposEntradaDisponibles.get(tipoEntradaNombre);
        if (tipoEntrada != null) {
            tipoEntrada.aumentarDisponibilidad(cantidad); // Asumiendo que TipoEntrada tiene este método
            entradasVendidas -= cantidad;
            if (entradasVendidas < 0) entradasVendidas = 0;
            System.out.println(cantidad + " entradas de tipo '" + tipoEntradaNombre + "' devueltas para " + nombre);
        }
    }

    public int obtenerDisponibilidadTotal() {
        return capacidadTotal - entradasVendidas;
    }

    // Métodos delegados al Estado (Patrón State)
    public void publicar() {
        estadoActual.publicar();
    }

    public void cancelar() {
        estadoActual.cancelar();
    }

    public void iniciar() {
        estadoActual.iniciar();
    }

    public void finalizar() {
        estadoActual.finalizar();
    }

    public void setId(String id) {
        this.id = id;
    }

    // Builder estático anidado
    public static class EventoBuilder {
        private String id; // Opcional, podría generarse automáticamente
        private String nombre;
        private String descripcion;
        private String categoria;
        private LocalDateTime fechaHora;
        private Lugar lugar;
        private Organizador organizador;
        private int capacidadTotal; // Si es 0, se tomará del lugar
        private EstadoEvento estadoActual; // Opcional, por defecto EstadoBorrador
        private List<String> urlsImagenes = new ArrayList<>();
        private List<String> urlsVideos = new ArrayList<>();
        private Map<String, TipoEntrada> tiposEntradaDisponibles = new HashMap<>();

        public EventoBuilder(String nombre, Organizador organizador, Lugar lugar, LocalDateTime fechaHora) {
            this.nombre = nombre;
            this.organizador = organizador;
            this.lugar = lugar;
            this.fechaHora = fechaHora;
        }

        public EventoBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public EventoBuilder setDescripcion(String descripcion) {
            this.descripcion = descripcion;
            return this;
        }

        public EventoBuilder setCategoria(String categoria) {
            this.categoria = categoria;
            return this;
        }

        public EventoBuilder setCapacidadTotal(int capacidadTotal) {
            this.capacidadTotal = capacidadTotal;
            return this;
        }

        public EventoBuilder setEstadoActual(EstadoEvento estadoActual) {
            this.estadoActual = estadoActual;
            return this;
        }

        public EventoBuilder addImagenUrl(String url) {
            this.urlsImagenes.add(url);
            return this;
        }

        public EventoBuilder addVideoUrl(String url) {
            this.urlsVideos.add(url);
            return this;
        }

        public EventoBuilder addTipoEntrada(String nombreTipo, TipoEntrada tipoEntrada) {
            this.tiposEntradaDisponibles.put(nombreTipo, tipoEntrada);
            return this;
        }

        // Getters para que el constructor de Evento pueda acceder a los valores
        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public String getDescripcion() { return descripcion; }
        public String getCategoria() { return categoria; }
        public LocalDateTime getFechaHora() { return fechaHora; }
        public Lugar getLugar() { return lugar; }
        public Organizador getOrganizador() { return organizador; }
        public int getCapacidadTotal() { return capacidadTotal; }
        public EstadoEvento getEstadoActual() { return estadoActual; }
        public List<String> getUrlsImagenes() { return urlsImagenes; }
        public List<String> getUrlsVideos() { return urlsVideos; }
        public Map<String, TipoEntrada> getTiposEntradaDisponibles() { return tiposEntradaDisponibles; }

        public Evento build() {
            Evento evento = new Evento(this);
            // Validaciones adicionales si son necesarias
            if (evento.nombre == null || evento.nombre.trim().isEmpty()) {
                throw new IllegalStateException("El nombre del evento no puede ser vacío.");
            }
            if (evento.organizador == null) {
                throw new IllegalStateException("El evento debe tener un organizador.");
            }
            if (evento.lugar == null) {
                throw new IllegalStateException("El evento debe tener un lugar.");
            }
            if (evento.fechaHora == null) {
                throw new IllegalStateException("El evento debe tener una fecha y hora.");
            }
            return evento;
        }
    }
}
