package com.eventmaster.model.pattern.chain_of_responsibility;

import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.pattern.factory.TipoEntrada;
import com.eventmaster.model.CodigoDescuento; // Asumiendo que el código de descuento se pasa o se obtiene del contextoCompra
// import com.eventmaster.service.PromocionService; // Podría usarse un servicio para validar códigos

public class ValidarPromocionHandler implements ValidacionHandler {
    private ValidacionHandler nextHandler;
    // private PromocionService promocionService; // Opcional

    // public ValidarPromocionHandler(PromocionService promocionService) {
    //     this.promocionService = promocionService;
    // }

    public ValidarPromocionHandler() {
        // Constructor sin servicio por ahora
    }

    @Override
    public void setNext(ValidacionHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    @Override
    public boolean validar(Usuario usuario, Evento evento, TipoEntrada tipoEntrada, int cantidad, Compra contextoCompra) {
        System.out.println("ValidarPromocionHandler: Verificando códigos de descuento o promociones aplicables.");

        // Lógica para aplicar promociones automáticas o validar un código de descuento
        // Esto es una simplificación. En un caso real, se obtendría el código del contextoCompra
        // o de una solicitud.

        // Ejemplo: Si el contextoCompra tiene un código de descuento seteado
        // String codigoIngresado = contextoCompra.getCodigoDescuentoIngresado();
        // if (codigoIngresado != null && !codigoIngresado.isEmpty()) {
        //     CodigoDescuento codigo = promocionService.validarYObtenerCodigo(codigoIngresado);
        //     if (codigo != null && codigo.esValido()) {
        //         // Aplicar el descuento al contextoCompra o a las entradas
        //         double descuento = codigo.getPorcentajeDescuento();
        //         // contextoCompra.aplicarDescuento(descuento, "Código: " + codigoIngresado);
        //         System.out.println("ValidarPromocionHandler: Código de descuento '" + codigoIngresado + "' válido y aplicado.");
        //         codigo.incrementarUso(); // Marcar como usado
        //     } else {
        //         System.err.println("Validación fallida: Código de descuento '" + codigoIngresado + "' no es válido o ha expirado.");
        //         // contextoCompra.addError("El código de descuento ingresado no es válido o ha expirado.");
        //         return false; // Podría ser opcional fallar toda la compra por un código inválido
        //     }
        // }

        // Lógica para promociones automáticas (ej. descuento por compra anticipada, etc.)
        // if (evento.tienePromocionAutomaticaAplicable(tipoEntrada, cantidad, usuario)) {
        //     // contextoCompra.aplicarPromocion(evento.getPromocionAplicable());
        //     System.out.println("ValidarPromocionHandler: Promoción automática aplicada.");
        // }


        System.out.println("ValidarPromocionHandler: Validación de promociones/descuentos completada (simulada).");
        if (nextHandler != null) {
            return nextHandler.validar(usuario, evento, tipoEntrada, cantidad, contextoCompra);
        }
        return true;
    }
}
