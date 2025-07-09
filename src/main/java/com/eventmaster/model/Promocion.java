package com.eventmaster.model;

import java.time.LocalDateTime;

public class Promocion {
    private String id;
    private String descripcion;
    private double porcentajeDescuento; // Ejemplo: 0.1 para 10%
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private String tipoAplicable; // "Evento", "TipoEntrada", "UsuarioEspecifico"
    private String idAplicable; // ID del evento, tipo de entrada, o usuario al que aplica

    public Promocion(String id, String descripcion, double porcentajeDescuento, LocalDateTime fechaInicio, LocalDateTime fechaFin, String tipoAplicable, String idAplicable) {
        this.id = id;
        this.descripcion = descripcion;
        this.porcentajeDescuento = porcentajeDescuento;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tipoAplicable = tipoAplicable;
        this.idAplicable = idAplicable;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public String getTipoAplicable() {
        return tipoAplicable;
    }

    public String getIdAplicable() {
        return idAplicable;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setPorcentajeDescuento(double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public void setTipoAplicable(String tipoAplicable) {
        this.tipoAplicable = tipoAplicable;
    }

    public void setIdAplicable(String idAplicable) {
        this.idAplicable = idAplicable;
    }

    public boolean esValidaAhora() {
        LocalDateTime ahora = LocalDateTime.now();
        return (ahora.isEqual(fechaInicio) || ahora.isAfter(fechaInicio)) &&
               (ahora.isEqual(fechaFin) || ahora.isBefore(fechaFin));
    }

    // Podría tener métodos para verificar si aplica a un evento/entrada/usuario específico
    public boolean aplicaAEvento(String eventoId) {
        return "Evento".equalsIgnoreCase(tipoAplicable) && idAplicable.equals(eventoId);
    }

    public boolean aplicaATipoEntrada(String tipoEntradaNombre) {
        return "TipoEntrada".equalsIgnoreCase(tipoAplicable) && idAplicable.equalsIgnoreCase(tipoEntradaNombre);
    }
}
