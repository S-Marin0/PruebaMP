package com.eventmaster.model.pattern.factory;

import com.eventmaster.model.entity.Evento; // Necesario para asociar la entrada a un evento

// Clase base concreta para una instancia de Entrada vendida (ConcreteComponent para Decorator)
public class EntradaBase implements Entrada {
    protected String id; // ID único de esta entrada específica
    protected TipoEntrada tipoEntradaAsociado; // Definición del tipo de entrada (General, VIP)
    protected Evento eventoAsociado; // Evento al que pertenece esta entrada
    protected double precioPagado; // Precio final pagado, podría incluir descuentos/cargos

    public EntradaBase(String id, TipoEntrada tipoEntradaAsociado, Evento eventoAsociado, double precioPagado) {
        this.id = id;
        this.tipoEntradaAsociado = tipoEntradaAsociado;
        this.eventoAsociado = eventoAsociado;
        this.precioPagado = precioPagado;
    }

    public EntradaBase(String id, TipoEntrada tipoEntradaAsociado, Evento eventoAsociado) {
        this.id = id;
        this.tipoEntradaAsociado = tipoEntradaAsociado;
        this.eventoAsociado = eventoAsociado;
        this.precioPagado = tipoEntradaAsociado.getPrecioBase(); // Por defecto, el precio base del tipo
    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public double getPrecio() {
        return precioPagado;
    }

    @Override
    public String getDescripcion() {
        return "Entrada para el evento: " + eventoAsociado.getNombre() +
               " - Tipo: " + tipoEntradaAsociado.getNombreTipo();
    }

    @Override
    public String getTipo() {
        return tipoEntradaAsociado.getNombreTipo();
    }

    @Override
    public Evento getEventoAsociado() {
        return eventoAsociado;
    }

    public TipoEntrada getTipoEntradaAsociado() {
        return tipoEntradaAsociado;
    }

    public void setPrecioPagado(double precioPagado){
        this.precioPagado = precioPagado;
    }
}
