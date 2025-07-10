package com.eventmaster.model.entity;

import com.eventmaster.model.NotificacionConfig;
import com.eventmaster.model.Preferencia;
import com.eventmaster.model.command.Command;
import com.eventmaster.model.command.CommandInvoker;
import com.eventmaster.model.pattern.observer.Observer; // Importar Observer

import java.util.ArrayList;
import java.util.List;

public class Asistente extends Usuario implements Observer { // Implementar Observer
    private List<Preferencia> preferenciasDetalladas; // Más detallado que la lista de strings en Usuario
    private NotificacionConfig configuracionNotificaciones;
    private CommandInvoker commandInvoker; // Para gestionar historial de operaciones de compra, etc.

    public Asistente(String id, String nombre, String email, String password) {
        super(id, nombre, email, password);
        this.preferenciasDetalladas = new ArrayList<>();
        this.configuracionNotificaciones = new NotificacionConfig(id); // Pasar ID de usuario a la config
        this.commandInvoker = new CommandInvoker(10); // Historial de 10 comandos
    }

    // Getters y Setters
    public List<Preferencia> getPreferenciasDetalladas() {
        return preferenciasDetalladas;
    }

    public void setPreferenciasDetalladas(List<Preferencia> preferenciasDetalladas) {
        this.preferenciasDetalladas = preferenciasDetalladas;
    }

    public void addPreferenciaDetallada(Preferencia preferencia) {
        this.preferenciasDetalladas.add(preferencia);
        // También podríamos actualizar la lista de strings genérica en la clase base Usuario
        super.addPreferencia(preferencia.getNombre());
    }

    public NotificacionConfig getConfiguracionNotificaciones() {
        return configuracionNotificaciones;
    }

    public void setConfiguracionNotificaciones(NotificacionConfig configuracionNotificaciones) {
        this.configuracionNotificaciones = configuracionNotificaciones;
    }

    public CommandInvoker getCommandInvoker() {
        return commandInvoker;
    }

    // Métodos específicos del Asistente
    public List<Evento> buscarEvento(/* Filtro filtro */) {
        // Lógica para buscar eventos basada en un filtro
        // Esta es una simulación, necesitaría acceso a un servicio o DAO de eventos
        System.out.println("Buscando eventos...");
        return new ArrayList<>(); // Devuelve lista vacía por ahora
    }

    public Compra comprarEntradas(Evento evento, String tipoEntradaNombre, int cantidad /*, MetodoPago metodoPago */) {
        // Lógica para iniciar el proceso de compra.
        // Esto se integrará con el Command 'ComprarEntradaCommand' y 'ProcesoCompraFacade'
        // Aquí se crearía y ejecutaría un ComprarEntradaCommand
        System.out.println(getNombre() + " está intentando comprar " + cantidad + " entradas de tipo " + tipoEntradaNombre + " para el evento: " + evento.getNombre());

        // Simulación de creación de una compra
        // Compra compra = new Compra(this.getId() + "_" + evento.getId() + "_" + System.currentTimeMillis(), this, evento);
        // Aquí se añadirían las entradas a la compra, se procesaría el pago, etc.
        // Por ahora, devolvemos null o una compra dummy.
        return null;
    }

    public boolean cancelarCompra(Compra compra) {
        // Lógica para cancelar una compra, podría usar un CancelarCompraCommand
        System.out.println("Intentando cancelar la compra: " + compra.getId());
        // compra.cancelarCompra(); // Suponiendo que la compra tiene este método
        return true; // Simulado
    }

    public boolean solicitarReembolso(Compra compra, String motivo) {
        // Lógica para solicitar un reembolso, podría usar un ReembolsarCompraCommand
        System.out.println("Solicitando reembolso para la compra: " + compra.getId() + " por motivo: " + motivo);
        // compra.solicitarReembolso(); // Suponiendo que la compra tiene este método
        return true; // Simulado
    }

    public void verEntradasCompradas() {
        System.out.println("Entradas compradas por " + getNombre() + ":");
        if (getHistorialCompras() != null) {
            for (Compra compra : getHistorialCompras()) {
                System.out.println("- Evento: " + compra.getEvento().getNombre() + ", Fecha de compra: " + compra.getFechaCompra() + ", Estado: " + compra.getEstadoCompra());
            }
        }
    }

    public void verHistorialOperaciones() {
        System.out.println("Historial de operaciones para " + getNombre() + ":");
        if (commandInvoker != null && commandInvoker.obtenerHistorial() != null) {
            for (Command comando : commandInvoker.obtenerHistorial()) {
                System.out.println("- " + comando.getDescription() + " (Ejecutado en: " + comando.getTiempoEjecucion() + ")");
            }
        }
    }

    public void actualizarPreferencias(List<Preferencia> nuevasPreferencias) {
        this.setPreferenciasDetalladas(nuevasPreferencias);
        // Actualizar también las preferencias genéricas en la clase base
        List<String> nombresPreferencias = new ArrayList<>();
        for (Preferencia p : nuevasPreferencias) {
            nombresPreferencias.add(p.getNombre());
        }
        super.setPreferencias(nombresPreferencias);
        System.out.println("Preferencias de " + getNombre() + " actualizadas.");
    }


    @Override
    public List<Evento> obtenerRecomendaciones() {
        // Lógica para obtener recomendaciones basadas en this.preferenciasDetalladas y this.historialCompras
        // Esto se integraría con el GestorRecomendacionesStrategy
        System.out.println("Generando recomendaciones para " + getNombre() + "...");
        // Ejemplo:
        // EstrategiaRecomendacion estrategia = gestorRecomendaciones.getEstrategiaParaUsuario(this);
        // return estrategia.recomendar(this, todosLosEventosDisponibles);
        return new ArrayList<>(); // Devuelve lista vacía por ahora
    }

    // --- Implementación de Observer ---
    @Override
    public void actualizar(Evento evento, String mensaje) {
        // Verificar configuración de notificaciones del asistente
        if (evento == null) { // Notificación general del sistema
            if (configuracionNotificaciones.isRecibirNotificacionesNuevosEventosImportantes()) { // CAMBIO AQUÍ
                 recibirNotificacion("Notificación General del Sistema: " + mensaje);
            }
        } else { // Notificación específica de un evento
            // Podríamos verificar si el asistente está interesado en este evento específico
            // o si es un cambio a un evento al que ya compró entradas.
            boolean esEventoComprado = getHistorialCompras().stream().anyMatch(c -> c.getEvento().getId().equals(evento.getId()) && "COMPLETADA".equals(c.getEstadoCompra()));

            if (esEventoComprado && configuracionNotificaciones.isRecibirNotificacionesCambiosEventoComprado()) { // Asumiendo que este es el campo correcto para cambios
                 recibirNotificacion("Actualización sobre el evento '" + evento.getNombre() + "': " + mensaje);
            } else if (!esEventoComprado && configuracionNotificaciones.isRecibirRecomendacionesPersonalizadas() && mensaje.toLowerCase().contains("nuevo") /*o similar*/) {
                // Si es un nuevo evento y el usuario quiere recomendaciones, podría ser relevante.
                // Esta lógica puede ser más sofisticada.
                recibirNotificacion("Información sobre evento '" + evento.getNombre() + "': " + mensaje);
            }
        }
    }

    @Override
    public String getObserverId() {
        return getId(); // Usar el ID del Usuario como ID del Observer
    }
}
