package com.eventmaster.model;

import java.time.LocalDateTime;

public class CodigoDescuento {
    private String codigo; // El código que el usuario ingresa
    private double porcentajeDescuento; // Ej: 0.15 para 15%
    private LocalDateTime fechaExpiracion;
    private int usosMaximos;
    private int usosActuales;
    private boolean activo;
    // Podría estar asociado a una promoción específica o ser independiente
    private Promocion promocionAsociada; // Opcional

    public CodigoDescuento(String codigo, double porcentajeDescuento, LocalDateTime fechaExpiracion, int usosMaximos) {
        this.codigo = codigo;
        this.porcentajeDescuento = porcentajeDescuento;
        this.fechaExpiracion = fechaExpiracion;
        this.usosMaximos = usosMaximos > 0 ? usosMaximos : Integer.MAX_VALUE; // 0 o negativo para usos ilimitados (dentro de la fecha)
        this.usosActuales = 0;
        this.activo = true;
    }

    // Getters
    public String getCodigo() {
        return codigo;
    }

    public double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public int getUsosMaximos() {
        return usosMaximos;
    }

    public int getUsosActuales() {
        return usosActuales;
    }

    public boolean isActivo() {
        return activo;
    }

    public Promocion getPromocionAsociada() {
        return promocionAsociada;
    }

    // Setters
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setPorcentajeDescuento(double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public void setUsosMaximos(int usosMaximos) {
        this.usosMaximos = usosMaximos > 0 ? usosMaximos : Integer.MAX_VALUE;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public void setPromocionAsociada(Promocion promocionAsociada) {
        this.promocionAsociada = promocionAsociada;
    }

    // Métodos de negocio
    public boolean esValido() {
        return activo &&
               (fechaExpiracion == null || LocalDateTime.now().isBefore(fechaExpiracion)) &&
               (usosActuales < usosMaximos);
    }

    public boolean aplicarDescuento() {
        if (esValido()) {
            usosActuales++;
            System.out.println("Código de descuento '" + codigo + "' aplicado. Usos restantes: " + (usosMaximos == Integer.MAX_VALUE ? "ilimitados" : usosMaximos - usosActuales) );
            return true;
        }
        System.out.println("Código de descuento '" + codigo + "' no es válido o ha expirado/alcanzado límite de usos.");
        return false;
    }

    public void incrementarUso() {
        if (usosActuales < usosMaximos) {
            usosActuales++;
        }
        if (usosActuales >= usosMaximos && usosMaximos != Integer.MAX_VALUE) {
            this.activo = false; // Desactivar si alcanza el límite de usos
        }
    }
}
