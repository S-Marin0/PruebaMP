package com.eventmaster.service;

import com.eventmaster.model.entity.Usuario;
import java.util.Map;
import java.util.UUID;

// Implementación de ProcesadorPago
public class PagoServiceImpl implements ProcesadorPago {

    @Override
    public ResultadoPago procesarPago(Usuario usuario, double monto, Map<String, Object> detallesPago, String idTransaccionApp) {
        System.out.println("PagoServiceImpl: Procesando pago de " + monto +
                           " para el usuario " + usuario.getNombre() +
                           " (App Tx ID: " + idTransaccionApp + ")");

        // Lógica de interacción con una pasarela de pago real (Stripe, PayPal, etc.)
        // Por ahora, simulamos.
        if (monto > 0) {
            // Simular una interacción con la pasarela
            String idPasarela = "PASARELA_" + UUID.randomUUID().toString();
            System.out.println("PagoServiceImpl: Pago aprobado por pasarela (simulado). ID Pasarela: " + idPasarela);
            return new ResultadoPago(true, "Pago procesado exitosamente.", idPasarela);
        }
        System.err.println("PagoServiceImpl: Monto de pago inválido: " + monto);
        return new ResultadoPago(false, "Monto de pago inválido: " + monto);
    }

    @Override
    public ResultadoPago procesarReembolso(String idTransaccionOriginalPasarela, double montoReembolso) {
        System.out.println("PagoServiceImpl: Procesando reembolso de " + montoReembolso +
                           " para transacción original de pasarela: " + idTransaccionOriginalPasarela);

        // Lógica de interacción con la pasarela de pago para procesar el reembolso.
        if (montoReembolso > 0 && idTransaccionOriginalPasarela != null && !idTransaccionOriginalPasarela.isEmpty()) {
            String idReembolsoPasarela = "REEMBOLSO_PASARELA_" + UUID.randomUUID().toString();
            System.out.println("PagoServiceImpl: Reembolso aprobado por pasarela (simulado). ID Reembolso Pasarela: " + idReembolsoPasarela);
            return new ResultadoPago(true, "Reembolso procesado exitosamente.", idReembolsoPasarela);
        }
        String errorMsg = "";
        if (montoReembolso <= 0) errorMsg += "Monto de reembolso inválido. ";
        if (idTransaccionOriginalPasarela == null || idTransaccionOriginalPasarela.isEmpty()) errorMsg += "ID de transacción original no proporcionado. ";

        System.err.println("PagoServiceImpl: Fallo en el reembolso. " + errorMsg);
        return new ResultadoPago(false, "Fallo en el reembolso: " + errorMsg.trim());
    }

    // El PagoService que tenías antes tenía métodos que tomaban Usuario y compraId para reembolso.
    // La interfaz ProcesadorPago se centra en el ID de transacción de la pasarela.
    // Podemos mantener un método de conveniencia si es necesario, pero la interfaz es más genérica.

    /**
     * Método de conveniencia para procesar un reembolso usando datos de la app.
     * Este método necesitaría obtener el ID de transacción de la pasarela asociado a la compra.
     * @param usuario El usuario al que se le reembolsa.
     * @param idCompraApp El ID de la compra en la aplicación.
     * @param monto El monto a reembolsar.
     * @param idTransaccionPasarelaOriginal El ID de la transacción original en la pasarela de pago.
     * @return Resultado del reembolso.
     */
    public ResultadoPago procesarReembolsoConDatosApp(Usuario usuario, String idCompraApp, double monto, String idTransaccionPasarelaOriginal) {
        System.out.println("PagoServiceImpl (Convenience): Solicitando reembolso para compra App ID: " + idCompraApp +
                           ", Usuario: " + usuario.getNombre() + ", Monto: " + monto);
        if(idTransaccionPasarelaOriginal == null || idTransaccionPasarelaOriginal.isEmpty()){
            System.err.println("PagoServiceImpl (Convenience): No se puede procesar reembolso sin ID de transacción de pasarela original para compra " + idCompraApp);
            return new ResultadoPago(false, "Falta ID de transacción de pasarela original.");
        }
        return procesarReembolso(idTransaccionPasarelaOriginal, monto);
    }
}
