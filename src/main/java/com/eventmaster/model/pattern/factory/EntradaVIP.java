package com.eventmaster.model.pattern.factory;

import com.eventmaster.model.entity.Evento;
import java.util.List;
import java.util.ArrayList;

// Producto Concreto
public class EntradaVIP extends EntradaBase {
    private List<String> beneficiosVIP;

    public EntradaVIP(String id, TipoEntrada tipoEntradaAsociado, Evento eventoAsociado) {
        super(id, tipoEntradaAsociado, eventoAsociado);
        this.beneficiosVIP = new ArrayList<>(tipoEntradaAsociado.getBeneficiosExtra()); // Copia los beneficios definidos en TipoEntrada
    }

    public EntradaVIP(String id, TipoEntrada tipoEntradaAsociado, Evento eventoAsociado, double precioPagado) {
        super(id, tipoEntradaAsociado, eventoAsociado, precioPagado);
        this.beneficiosVIP = new ArrayList<>(tipoEntradaAsociado.getBeneficiosExtra());
    }

    public List<String> getBeneficiosVIP() {
        return beneficiosVIP;
    }

    public void addBeneficioVIP(String beneficio) {
        this.beneficiosVIP.add(beneficio);
    }

    @Override
    public String getDescripcion() {
        return super.getDescripcion() + " (VIP). Beneficios: " + String.join(", ", beneficiosVIP);
    }
}
