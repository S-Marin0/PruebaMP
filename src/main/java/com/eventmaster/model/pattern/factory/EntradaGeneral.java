package com.eventmaster.model.pattern.factory;

import com.eventmaster.model.entity.Evento;

// Producto Concreto
public class EntradaGeneral extends EntradaBase {

    public EntradaGeneral(String id, TipoEntrada tipoEntradaAsociado, Evento eventoAsociado) {
        super(id, tipoEntradaAsociado, eventoAsociado);
    }

    public EntradaGeneral(String id, TipoEntrada tipoEntradaAsociado, Evento eventoAsociado, double precioPagado) {
        super(id, tipoEntradaAsociado, eventoAsociado, precioPagado);
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " (Acceso General)";
    }
}
