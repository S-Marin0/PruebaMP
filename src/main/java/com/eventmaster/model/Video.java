package com.eventmaster.model;

// Placeholder para la entidad Video
public class Video {
    private String id;
    private String url; // URL al video (ej. YouTube, Vimeo, o self-hosted)
    private String titulo;
    private String descripcion;

    public Video(String id, String url, String titulo, String descripcion) {
        this.id = id;
        this.url = url;
        this.titulo = titulo;
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "Video{" +
               "id='" + id + '\'' +
               ", url='" + url + '\'' +
               ", titulo='" + titulo + '\'' +
               '}';
    }
}
