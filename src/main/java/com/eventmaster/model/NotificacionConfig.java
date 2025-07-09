package com.eventmaster.model;

// Placeholder para la configuración de notificaciones de un usuario
public class NotificacionConfig {
    private String usuarioId; // Para vincular con el Usuario
    private boolean recibirNotificacionesNuevosEventosImportantes; // Más específico
    private boolean recibirRecordatoriosEventosComprados;
    private boolean recibirNotificacionesCambiosEventoComprado; // Cambios de fecha, lugar, cancelación
    private boolean recibirRecomendacionesPersonalizadas;
    private String metodoPreferido; // "Email", "Push", "SMS"

    public NotificacionConfig(String usuarioId) {
        this.usuarioId = usuarioId;
        // Valores por defecto
        this.recibirNotificacionesNuevosEventosImportantes = true;
        this.recibirRecordatoriosEventosComprados = true;
        this.recibirNotificacionesCambiosEventoComprado = true;
        this.recibirRecomendacionesPersonalizadas = true;
        this.metodoPreferido = "Email";
    }

    public NotificacionConfig(String usuarioId, boolean recibirNuevosImportantes, boolean recibirRecordatorios,
                              boolean recibirCambiosComprado, boolean recibirRecomendaciones, String metodo) {
        this.usuarioId = usuarioId;
        this.recibirNotificacionesNuevosEventosImportantes = recibirNuevosImportantes;
        this.recibirRecordatoriosEventosComprados = recibirRecordatorios;
        this.recibirNotificacionesCambiosEventoComprado = recibirCambiosComprado;
        this.recibirRecomendacionesPersonalizadas = recibirRecomendaciones;
        this.metodoPreferido = metodo;
    }

    // Getters y Setters
    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public boolean isRecibirNotificacionesNuevosEventosImportantes() {
        return recibirNotificacionesNuevosEventosImportantes;
    }

    public void setRecibirNotificacionesNuevosEventosImportantes(boolean recibirNotificacionesNuevosEventosImportantes) {
        this.recibirNotificacionesNuevosEventosImportantes = recibirNotificacionesNuevosEventosImportantes;
    }

    public boolean isRecibirRecordatoriosEventosComprados() {
        return recibirRecordatoriosEventosComprados;
    }

    public void setRecibirRecordatoriosEventosComprados(boolean recibirRecordatoriosEventosComprados) {
        this.recibirRecordatoriosEventosComprados = recibirRecordatoriosEventosComprados;
    }

    public boolean isRecibirNotificacionesCambiosEventoComprado() {
        return recibirNotificacionesCambiosEventoComprado;
    }

    public void setRecibirNotificacionesCambiosEventoComprado(boolean recibirNotificacionesCambiosEventoComprado) {
        this.recibirNotificacionesCambiosEventoComprado = recibirNotificacionesCambiosEventoComprado;
    }

    public boolean isRecibirRecomendacionesPersonalizadas() {
        return recibirRecomendacionesPersonalizadas;
    }

    public void setRecibirRecomendacionesPersonalizadas(boolean recibirRecomendacionesPersonalizadas) {
        this.recibirRecomendacionesPersonalizadas = recibirRecomendacionesPersonalizadas;
    }

    public String getMetodoPreferido() {
        return metodoPreferido;
    }

    public void setMetodoPreferido(String metodoPreferido) {
        this.metodoPreferido = metodoPreferido;
    }
}
