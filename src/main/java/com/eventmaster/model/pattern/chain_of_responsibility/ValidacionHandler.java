package com.eventmaster.model.pattern.chain_of_responsibility;

import com.eventmaster.model.entity.Compra; // O una clase de contexto de compra más específica
import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.pattern.factory.TipoEntrada;


// Interfaz para el manejador en la cadena de responsabilidad
public interface ValidacionHandler {
    void setNext(ValidacionHandler nextHandler);

    /**
     * Procesa la validación.
     * @param usuario El usuario que realiza la compra.
     * @param evento El evento para el cual se compran las entradas.
     * @param tipoEntrada El tipo de entrada seleccionado.
     * @param cantidad La cantidad de entradas solicitadas.
     * @param contextoCompra Un objeto que puede llevar el estado de la compra a través de la cadena (ej. Compra).
     * @return true si la validación es exitosa y se puede continuar, false si falla.
     */
    boolean validar(Usuario usuario, Evento evento, TipoEntrada tipoEntrada, int cantidad, Compra contextoCompra);
}
