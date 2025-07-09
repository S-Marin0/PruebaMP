package com.eventmaster.model.pattern.factory;

import com.eventmaster.model.entity.Evento;

// Producto Concreto
public class EntradaEarlyAccess extends EntradaBase {
    private int minutosAnticipacion;

    public EntradaEarlyAccess(String id, TipoEntrada tipoEntradaAsociado, Evento eventoAsociado, int minutosAnticipacion) {
        super(id, tipoEntradaAsociado, eventoAsociado);
        this.minutosAnticipacion = minutosAnticipacion;
    }

    public EntradaEarlyAccess(String id, TipoEntrada tipoEntradaAsociado, Evento eventoAsociado, double precioPagado, int minutosAnticipacion) {
        super(id, tipoEntradaAsociado, eventoAsociado, precioPagado);
        this.minutosAnticipacion = minutosAnticipacion;
    }

    public int getMinutosAnticipacion() {
        return minutosAnticipacion;
    }

    public void setMinutosAnticipacion(int minutosAnticipacion) {
        this.minutosAnticipacion = minutosAnticipacion;
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " (Early Access - " + minutosAnticipacion + " min antes)";
    }
}
