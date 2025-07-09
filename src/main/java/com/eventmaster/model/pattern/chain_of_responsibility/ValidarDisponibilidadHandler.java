package com.eventmaster.model.pattern.chain_of_responsibility;

import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.pattern.factory.TipoEntrada;

public class ValidarDisponibilidadHandler implements ValidacionHandler {
    private ValidacionHandler nextHandler;

    @Override
    public void setNext(ValidacionHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public boolean validar(Usuario usuario, Evento evento, TipoEntrada tipoEntrada, int cantidad, Compra contextoCompra) {
        System.out.println("ValidarDisponibilidadHandler: Verificando disponibilidad para " + cantidad + " entradas de tipo '" + tipoEntrada.getNombreTipo() + "' para el evento '" + evento.getNombre() + "'.");

        // Validar contra la capacidad general del evento
        if (evento.getEntradasVendidas() + cantidad > evento.getCapacidadTotal()) {
            System.err.println("Validación fallida: La cantidad solicitada excede la capacidad total restante del evento.");
            // Podríamos añadir un mensaje al contextoCompra si es necesario
            // contextoCompra.addError("La cantidad solicitada excede la capacidad total restante del evento.");
            return false;
        }

        // Validar contra la disponibilidad específica del tipo de entrada
        if (!tipoEntrada.revisarDisponibilidad(cantidad)) {
            System.err.println("Validación fallida: No hay suficientes entradas disponibles del tipo '" + tipoEntrada.getNombreTipo() + "'. Solicitadas: " + cantidad + ", Disponibles: " + tipoEntrada.getCantidadDisponible());
            // contextoCompra.addError("No hay suficientes entradas disponibles del tipo '" + tipoEntrada.getNombreTipo() + "'.");
            return false;
        }

        System.out.println("ValidarDisponibilidadHandler: Disponibilidad OK.");
        if (nextHandler != null) {
            return nextHandler.validar(usuario, evento, tipoEntrada, cantidad, contextoCompra);
        }
        return true;
    }
}
