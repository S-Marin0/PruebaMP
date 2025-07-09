package com.eventmaster.model;

// Placeholder para la entidad Imagen
public class Imagen {
    private String id;
    private String url;
    private String descripcion; // Alt text o descripción

    public Imagen(String id, String url, String descripcion) {
        this.id = id;
        this.url = url;
        this.descripcion = descripcion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "Imagen{" +
               "id='" + id + '\'' +
               ", url='" + url + '\'' +
               ", descripcion='" + descripcion + '\'' +
               '}';
    }
}
