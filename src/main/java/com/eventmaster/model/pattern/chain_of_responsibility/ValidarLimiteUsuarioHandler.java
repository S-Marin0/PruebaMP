package com.eventmaster.model.pattern.chain_of_responsibility;

import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.pattern.factory.TipoEntrada;
import com.eventmaster.model.pattern.factory.Entrada; // Para revisar el historial de compras

public class ValidarLimiteUsuarioHandler implements ValidacionHandler {
    private ValidacionHandler nextHandler;

    @Override
    public void setNext(ValidacionHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public boolean validar(Usuario usuario, Evento evento, TipoEntrada tipoEntrada, int cantidad, Compra contextoCompra) {
        System.out.println("ValidarLimiteUsuarioHandler: Verificando límite de compra por usuario para '" + tipoEntrada.getNombreTipo() + "'.");

        int limitePorUsuario = tipoEntrada.getLimiteCompraPorUsuario();
        if (limitePorUsuario == Integer.MAX_VALUE || limitePorUsuario <=0) { // Sin límite o mal configurado (se interpreta como sin límite)
            System.out.println("ValidarLimiteUsuarioHandler: Sin límite de compra por usuario para este tipo de entrada.");
            if (nextHandler != null) {
                return nextHandler.validar(usuario, evento, tipoEntrada, cantidad, contextoCompra);
            }
            return true;
        }

        long entradasYaCompradasPorUsuarioParaEsteEventoYTipo = 0;
        if (usuario.getHistorialCompras() != null) {
            for (Compra compraAnterior : usuario.getHistorialCompras()) {
                // Considerar solo compras completadas y para el mismo evento
                if (compraAnterior.getEvento().getId().equals(evento.getId()) &&
                    "COMPLETADA".equals(compraAnterior.getEstadoCompra())) {
                    for (Entrada entrada : compraAnterior.getEntradasCompradas()) {
                        // Asumimos que EntradaBase o un decorador tiene acceso al TipoEntrada original
                        if (entrada instanceof com.eventmaster.model.pattern.factory.EntradaBase) {
                            com.eventmaster.model.pattern.factory.EntradaBase eb = (com.eventmaster.model.pattern.factory.EntradaBase) entrada;
                            if (eb.getTipoEntradaAsociado().getNombreTipo().equals(tipoEntrada.getNombreTipo())) {
                                entradasYaCompradasPorUsuarioParaEsteEventoYTipo++;
                            }
                        }
                        // Si hay decoradores, se necesitaría una forma de llegar al TipoEntrada del componente base.
                        // Por simplicidad, aquí solo consideramos EntradaBase.
                    }
                }
            }
        }

        if (entradasYaCompradasPorUsuarioParaEsteEventoYTipo + cantidad > limitePorUsuario) {
            System.err.println("Validación fallida: El usuario '" + usuario.getNombre() +
                               "' excedería el límite de compra (" + limitePorUsuario +
                               ") para el tipo de entrada '" + tipoEntrada.getNombreTipo() +
                               "'. Ya compradas: " + entradasYaCompradasPorUsuarioParaEsteEventoYTipo +
                               ", Solicitando: " + cantidad);
            // contextoCompra.addError("Se excede el límite de compra por usuario para este tipo de entrada.");
            return false;
        }

        System.out.println("ValidarLimiteUsuarioHandler: Límite de compra por usuario OK.");
        if (nextHandler != null) {
            return nextHandler.validar(usuario, evento, tipoEntrada, cantidad, contextoCompra);
        }
        return true;
    }
}
