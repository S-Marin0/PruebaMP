package com.eventmaster.model.pattern.decorator;

import com.eventmaster.model.pattern.factory.Entrada;

// Decorador Concreto
public class EntradaConDescuento extends EntradaDecorator {
    private double porcentajeDescuento; // Ej: 0.10 para 10% de descuento
    private String motivoDescuento;

    public EntradaConDescuento(Entrada entradaDecorada, double porcentajeDescuento, String motivoDescuento) {
        super(entradaDecorada);
        if (porcentajeDescuento < 0 || porcentajeDescuento > 1) {
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 1.");
        }
        this.porcentajeDescuento = porcentajeDescuento;
        this.motivoDescuento = motivoDescuento;
    }

    @Override
    public double getPrecio() {
        double precioOriginal = super.getPrecio();
        return precioOriginal * (1 - porcentajeDescuento);
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " (Descuento Aplicado: " + (porcentajeDescuento * 100) + "% por " + motivoDescuento + ")";
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public String getMotivoDescuento() {
        return motivoDescuento;
    }
}
