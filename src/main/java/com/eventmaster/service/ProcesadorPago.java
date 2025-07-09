package com.eventmaster.service;

import com.eventmaster.model.entity.Usuario;
import java.util.Map;

// Interfaz para el procesamiento de pagos
public interface ProcesadorPago {

    /**
     * Procesa un pago.
     * @param usuario El usuario que realiza el pago.
     * @param monto El monto a pagar.
     * @param detallesPago Mapa con los detalles del método de pago (ej. numTarjeta, cvv, etc.).
     * @param idTransaccion Un identificador único para esta transacción de pago.
     * @return Un objeto ResultadoPago que indica el éxito y puede contener un ID de transacción de la pasarela.
     */
    ResultadoPago procesarPago(Usuario usuario, double monto, Map<String, Object> detallesPago, String idTransaccion);

    /**
     * Procesa un reembolso.
     * @param idTransaccionOriginal El ID de la transacción de pago original que se va a reembolsar.
     * @param montoReembolso El monto a reembolsar.
     * @return Un objeto ResultadoPago que indica el éxito del reembolso.
     */
    ResultadoPago procesarReembolso(String idTransaccionOriginal, double montoReembolso);

    // Clase interna o externa para encapsular el resultado de una operación de pago/reembolso
    public static class ResultadoPago {
        private boolean exito;
        private String mensaje;
        private String idTransaccionPasarela; // ID devuelto por la pasarela de pago

        public ResultadoPago(boolean exito, String mensaje, String idTransaccionPasarela) {
            this.exito = exito;
            this.mensaje = mensaje;
            this.idTransaccionPasarela = idTransaccionPasarela;
        }

        public ResultadoPago(boolean exito, String mensaje) {
            this(exito, mensaje, null);
        }

        public boolean isExito() {
            return exito;
        }

        public String getMensaje() {
            return mensaje;
        }

        public String getIdTransaccionPasarela() {
            return idTransaccionPasarela;
        }
    }
}
