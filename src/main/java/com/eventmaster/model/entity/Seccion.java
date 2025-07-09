package com.eventmaster.model.entity;

import com.eventmaster.model.pattern.composite.ComponenteLugar;
import java.util.ArrayList;
import java.util.List;

// Seccion es un Composite que puede contener Asientos u otras Secciones
public class Seccion implements ComponenteLugar {
    private String nombre;
    private List<ComponenteLugar> asientosOsubSecciones; // Puede contener Asientos (Leaf) u otras Secciones (Composite)

    public Seccion(String nombre) {
        this.nombre = nombre;
        this.asientosOsubSecciones = new ArrayList<>();
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public void agregar(ComponenteLugar componente) {
        asientosOsubSecciones.add(componente);
    }

    @Override
    public void remover(ComponenteLugar componente) {
        asientosOsubSecciones.remove(componente);
    }

    @Override
    public ComponenteLugar getHijo(int i) {
        return asientosOsubSecciones.get(i);
    }

    @Override
    public int obtenerCapacidadTotal() {
        int capacidadTotal = 0;
        for (ComponenteLugar componente : asientosOsubSecciones) {
            capacidadTotal += componente.obtenerCapacidadTotal();
        }
        return capacidadTotal;
    }

    public List<ComponenteLugar> getAsientosOsubSecciones() {
        return asientosOsubSecciones;
    }
}
