package com.eventmaster.model.entity;

import com.eventmaster.model.pattern.composite.ComponenteLugar;

// Asiento es un Leaf en el patrón Composite
public class Asiento implements ComponenteLugar {
    private String idAsiento; // Ejemplo: "Fila A, Asiento 10"
    private boolean disponible;
    // Podría tener un precio específico o pertenecer a una categoría de precio dentro de una sección

    public Asiento(String idAsiento) {
        this.idAsiento = idAsiento;
        this.disponible = true; // Por defecto disponible
    }

    @Override
    public String getNombre() {
        return idAsiento;
    }

    public void setNombre(String idAsiento) {
        this.idAsiento = idAsiento;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public int obtenerCapacidadTotal() {
        return 1; // Un asiento tiene capacidad para 1 persona
    }

    // Métodos agregar, remover, getHijo no son aplicables a un Leaf
    @Override
    public void agregar(ComponenteLugar componente) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("No se puede agregar a un asiento (Leaf).");
    }

    @Override
    public void remover(ComponenteLugar componente) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("No se puede remover de un asiento (Leaf).");
    }

    @Override
    public ComponenteLugar getHijo(int i) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Un asiento (Leaf) no tiene hijos.");
    }
}
