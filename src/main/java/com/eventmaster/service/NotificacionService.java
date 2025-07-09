package com.eventmaster.service;

import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.entity.Evento;

// Interfaz/Servicio placeholder para enviar notificaciones
// En una implementación real, podría ser una interfaz con implementaciones para Email, SMS, Push, etc.
// Y podría usar el SistemaNotificaciones (Singleton/Observer) internamente.
public class NotificacionService {

    public void enviarConfirmacionCompra(Usuario usuario, Compra compra) {
        System.out.println("NotificacionService: Enviando email de confirmación de compra a " + usuario.getEmail());
        System.out.println("  Detalles de la Compra ID: " + compra.getId());
        System.out.println("  Evento: " + compra.getEvento().getNombre());
        System.out.println("  Total Pagado: " + compra.getTotalPagado());
        // Lógica para formatear y enviar un email/SMS/push real.
    }

    public void notificarCambioEvento(Evento evento, String mensajeCambio, List<Usuario> asistentesAfectados) {
        System.out.println("NotificacionService: Notificando cambio en evento '" + evento.getNombre() + "': " + mensajeCambio);
        for (Usuario asistente : asistentesAfectados) {
            // Verificar preferencias de notificación del asistente
            // if (asistente.getConfiguracionNotificaciones().isRecibirNotificacionesCambiosEvento()) {
                System.out.println("  Enviando notificación a " + asistente.getEmail());
            // }
        }
    }

    public void notificarCancelacionEvento(Evento evento, List<Usuario> asistentesAfectados) {
        System.out.println("NotificacionService: Notificando cancelación del evento '" + evento.getNombre() + "'.");
        for (Usuario asistente : asistentesAfectados) {
             System.out.println("  Enviando notificación de cancelación y detalles de reembolso a " + asistente.getEmail());
        }
    }

    public void enviarRecomendacionEvento(Usuario usuario, Evento eventoRecomendado) {
        System.out.println("NotificacionService: Enviando recomendación de evento a " + usuario.getEmail());
        System.out.println("  Te podría interesar: " + eventoRecomendado.getNombre() + " el " + eventoRecomendado.getFechaHora());
    }

    public void enviarRecordatorioEvento(Usuario usuario, Evento evento) {
        System.out.println("NotificacionService: Enviando recordatorio del evento '" + evento.getNombre() + "' a " + usuario.getEmail());
    }

    // Otros métodos de notificación...
}
