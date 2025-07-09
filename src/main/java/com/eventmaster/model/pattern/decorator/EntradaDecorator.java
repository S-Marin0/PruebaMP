package com.eventmaster.model.pattern.decorator;

import com.eventmaster.model.pattern.factory.Entrada;
import com.eventmaster.model.entity.Evento;

// Decorador Abstracto
public abstract class EntradaDecorator implements Entrada {
    protected Entrada entradaDecorada;

    public EntradaDecorator(Entrada entradaDecorada) {
        this.entradaDecorada = entradaDecorada;
    }

    @Override
    public String getId() {
        return entradaDecorada.getId();
    }

    @Override
    public double getPrecio() {
        return entradaDecorada.getPrecio();
    }

    @Override
    public String getDescripcion() {
        return entradaDecorada.getDescripcion();
    }

    @Override
    public String getTipo() {
        return entradaDecorada.getTipo(); // El tipo base no cambia con el decorador
    }

    @Override
    public Evento getEventoAsociado() {
        return entradaDecorada.getEventoAsociado();
    }
}
