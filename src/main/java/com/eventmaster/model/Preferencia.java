package com.eventmaster.model;

// Placeholder para la entidad Preferencia de Usuario
// Podría ser más compleja, por ejemplo, con categorías, subcategorías, artistas, etc.
public class Preferencia {
    private String id;
    private String nombre; // Ej: "Música Rock", "Conferencias de Tecnología", "Teatro Clásico"
    private String tipo;   // Ej: "CategoriaEvento", "Artista", "GeneroMusical"

    public Preferencia(String id, String nombre, String tipo) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "Preferencia{" +
               "id='" + id + '\'' +
               ", nombre='" + nombre + '\'' +
               ", tipo='" + tipo + '\'' +
               '}';
    }
}
