package com.eventmaster.model.pattern.decorator;

import com.eventmaster.model.pattern.factory.Entrada;

// Decorador Concreto
public class EntradaConDescuento extends EntradaDecorator {
    private double montoDescuento; // Monto fijo a descontar
    private String descripcionDescuento;

    public EntradaConDescuento(Entrada entradaDecorada, String descripcionDescuento, double montoDescuento) {
        super(entradaDecorada);
        if (montoDescuento < 0) {
            // Opcionalmente, permitir descuentos negativos si eso tuviera sentido (ej. una tarifa)
            // Por ahora, asumimos que un descuento siempre reduce o mantiene el precio.
            // O podría ser que un montoDescuento negativo sea un cargo, pero el nombre "Descuento" no encaja.
            // Mejor asegurar que el monto sea positivo o cero.
            throw new IllegalArgumentException("El monto del descuento no puede ser negativo.");
        }
        this.montoDescuento = montoDescuento;
        this.descripcionDescuento = descripcionDescuento;
    }

    @Override
    public double getPrecio() {
        double precioOriginal = super.getPrecio();
        double precioConDescuento = precioOriginal - this.montoDescuento;
        return Math.max(0, precioConDescuento); // Asegurar que el precio no sea negativo
    }

    @Override
    public String getDescripcion() {
        // Usar java.text.NumberFormat para formatear el monto como moneda podría ser mejor aquí.
        return super.getDescripcion() + " (Descuento: " + this.descripcionDescuento + " -€" + String.format("%.2f", this.montoDescuento) + ")";
    }

    public double getMontoDescuento() {
        return montoDescuento;
    }

    public String getDescripcionDescuento() { // Cambiado de getMotivoDescuento
        return descripcionDescuento;
    }
}
