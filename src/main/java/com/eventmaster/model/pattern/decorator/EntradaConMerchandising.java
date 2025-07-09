package com.eventmaster.model.pattern.decorator;

import com.eventmaster.model.pattern.factory.Entrada;
import java.util.List;
import java.util.ArrayList;

// Decorador Concreto
public class EntradaConMerchandising extends EntradaDecorator {
    private List<String> itemsDeMerchandising;
    private double costoAdicionalMerchandising;

    public EntradaConMerchandising(Entrada entradaDecorada, List<String> itemsDeMerchandising, double costoAdicionalMerchandising) {
        super(entradaDecorada);
        this.itemsDeMerchandising = itemsDeMerchandising != null ? new ArrayList<>(itemsDeMerchandising) : new ArrayList<>();
        this.costoAdicionalMerchandising = costoAdicionalMerchandising;
    }

    public EntradaConMerchandising(Entrada entradaDecorada, String itemDeMerchandising, double costoAdicionalMerchandising) {
        super(entradaDecorada);
        this.itemsDeMerchandising = new ArrayList<>();
        if (itemDeMerchandising != null && !itemDeMerchandising.isEmpty()) {
            this.itemsDeMerchandising.add(itemDeMerchandising);
        }
        this.costoAdicionalMerchandising = costoAdicionalMerchandising;
    }


    @Override
    public double getPrecio() {
        return super.getPrecio() + costoAdicionalMerchandising;
    }

    @Override
    public String getDescripcion() {
        if (itemsDeMerchandising.isEmpty()) {
            return super.getDescripcion() + " (Merchandising incluido)";
        }
        return super.getDescripcion() + " (Incluye Merchandising: " + String.join(", ", itemsDeMerchandising) + ")";
    }

    public List<String> getItemsDeMerchandising() {
        return itemsDeMerchandising;
    }

    public void addItemDeMerchandising(String item) {
        if (item != null && !item.isEmpty()) {
            this.itemsDeMerchandising.add(item);
        }
    }

    public double getCostoAdicionalMerchandising() {
        return costoAdicionalMerchandising;
    }
}
