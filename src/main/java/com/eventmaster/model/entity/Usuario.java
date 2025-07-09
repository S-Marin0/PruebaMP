package com.eventmaster.model.entity;

import java.util.List;
import java.util.ArrayList;

public abstract class Usuario {
    private String id;
    private String nombre;
    private String email;
    private String password; // Considerar almacenar un hash en lugar de texto plano
    private List<String> preferencias; // Podría ser una lista de IDs de categorías o tags
    private List<Compra> historialCompras;

    public Usuario(String id, String nombre, String email, String password) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.preferencias = new ArrayList<>();
        this.historialCompras = new ArrayList<>();
    }

    // Getters y Setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getPreferencias() {
        return preferencias;
    }

    public void setPreferencias(List<String> preferencias) {
        this.preferencias = preferencias;
    }

    public void addPreferencia(String preferencia) {
        this.preferencias.add(preferencia);
    }

    public List<Compra> getHistorialCompras() {
        return historialCompras;
    }

    public void setHistorialCompras(List<Compra> historialCompras) {
        this.historialCompras = historialCompras;
    }

    public void addCompraAlHistorial(Compra compra) {
        this.historialCompras.add(compra);
    }

    // Métodos abstractos o comunes
    public boolean autenticar(String password) {
        // Lógica de autenticación (comparar hash de password)
        return this.password.equals(password); // Simplificado
    }

    public abstract List<Evento> obtenerRecomendaciones(); // Implementación específica en Asistente

    public void recibirNotificacion(String mensaje) {
        System.out.println("Notificación para " + nombre + ": " + mensaje);
    }
}
