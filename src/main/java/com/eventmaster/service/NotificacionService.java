package com.eventmaster.service;

import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.entity.Asistente; // Necesario para NotificacionConfig
import com.eventmaster.model.NotificacionConfig; // Necesario para NotificacionConfig
import java.util.List; // Importación que faltaba


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
        System.out.println("NotificacionService: Intentando notificar cambio en evento '" + evento.getNombre() + "': " + mensajeCambio);
        for (Usuario usuario : asistentesAfectados) {
            if (usuario instanceof Asistente) {
                Asistente asistente = (Asistente) usuario;
                NotificacionConfig config = asistente.getConfiguracionNotificaciones();
                if (config != null && config.isRecibirNotificacionesCambiosEventoComprado()) {
                    System.out.println("  Enviando notificación de cambio a " + asistente.getEmail());
                    // Lógica de envío real
                } else {
                    System.out.println("  Usuario " + asistente.getEmail() + " ha optado por NO recibir notificaciones de cambios en eventos.");
                }
            } else {
                // Comportamiento para no Asistentes
                System.out.println("  Enviando notificación de cambio a " + usuario.getEmail() + " (usuario no es Asistente o config no aplica).");
            }
        }
    }

    public void notificarCancelacionEvento(Evento evento, List<Usuario> asistentesAfectados) {
        // Cancelaciones son generalmente importantes y no opcionales.
        System.out.println("NotificacionService: Notificando cancelación del evento '" + evento.getNombre() + "'.");
        for (Usuario usuario : asistentesAfectados) {
             System.out.println("  Enviando notificación de cancelación y detalles de reembolso a " + usuario.getEmail());
        }
    }

    public void enviarRecomendacionEvento(Usuario usuario, Evento eventoRecomendado) {
        if (usuario instanceof Asistente) {
            Asistente asistente = (Asistente) usuario;
            NotificacionConfig config = asistente.getConfiguracionNotificaciones();
            if (config != null && config.isRecibirRecomendacionesPersonalizadas()) {
                System.out.println("NotificacionService: Enviando recomendación de evento a " + usuario.getEmail());
                System.out.println("  Te podría interesar: " + eventoRecomendado.getNombre() + " el " + eventoRecomendado.getFechaHora());
                // Lógica de envío real
            } else {
                 System.out.println("NotificacionService: Usuario " + usuario.getEmail() + " ha optado por NO recibir recomendaciones.");
            }
        } else {
            System.out.println("NotificacionService: Enviando recomendación de evento a " + usuario.getEmail() + " (usuario no es Asistente o config no aplica).");
            System.out.println("  Te podría interesar: " + eventoRecomendado.getNombre() + " el " + eventoRecomendado.getFechaHora());
        }
    }

    public void enviarRecordatorioEvento(Usuario usuario, Evento evento) {
        if (usuario instanceof Asistente) {
            Asistente asistente = (Asistente) usuario;
            NotificacionConfig config = asistente.getConfiguracionNotificaciones();
            if (config != null && config.isRecibirRecordatoriosEventosComprados()) {
                System.out.println("NotificacionService: Enviando recordatorio del evento '" + evento.getNombre() + "' a " + usuario.getEmail());
                // Lógica de envío real
            } else {
                System.out.println("NotificacionService: Usuario " + usuario.getEmail() + " ha optado por NO recibir recordatorios de eventos.");
            }
        } else {
            System.out.println("NotificacionService: Enviando recordatorio del evento '" + evento.getNombre() + "' a " + usuario.getEmail() + " (usuario no es Asistente o config no aplica).");
        }
    }

    // Otros métodos de notificación...
}
