package com.eventmaster.model.pattern.factory.impl;

import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.pattern.factory.Entrada;

public class EntradaImpl implements Entrada {
    private String id;
    private Evento eventoAsociado;
    private String tipo; // e.g., "General", "VIP"
    private double precio;
    private String descripcion;
    private String compraId; // Added to associate with a Compra

    public EntradaImpl(String id, Evento eventoAsociado, String tipo, double precio, String descripcion) {
        this.id = id;
        this.eventoAsociado = eventoAsociado;
        this.tipo = tipo;
        this.precio = precio;
        this.descripcion = descripcion;
    }

    // Overloaded constructor or setter for compraId
    public EntradaImpl(String id, Evento eventoAsociado, String tipo, double precio, String descripcion, String compraId) {
        this(id, eventoAsociado, tipo, precio, descripcion);
        this.compraId = compraId;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Evento getEventoAsociado() {
        return eventoAsociado;
    }

    public void setEventoAsociado(Evento eventoAsociado) {
        this.eventoAsociado = eventoAsociado;
    }

    @Override
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    @Override
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Getter and Setter for compraId
    public String getCompraId() {
        return compraId;
    }

    public void setCompraId(String compraId) {
        this.compraId = compraId;
    }

    @Override
    public String toString() {
        return "EntradaImpl{" +
               "id='" + id + '\'' +
               ", eventoId=" + (eventoAsociado != null ? eventoAsociado.getId() : "null") +
               ", tipo='" + tipo + '\'' +
               ", precio=" + precio +
               ", compraId='" + compraId + '\'' +
               '}';
    }
}
