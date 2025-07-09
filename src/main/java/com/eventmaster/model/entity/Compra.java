package com.eventmaster.model.entity;

import com.eventmaster.model.command.Command; // Para el historial de operaciones sobre la compra
import com.eventmaster.model.pattern.factory.Entrada; // Interfaz Entrada

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Placeholder para la entidad Compra
public class Compra {
    private String id;
    private Usuario usuario; // El asistente que realizó la compra
    private Evento evento;
    private List<Entrada> entradasCompradas; // Lista de instancias de Entrada (podrían ser decoradas)
    private double totalPagado;
    private LocalDateTime fechaCompra;
    private String estadoCompra; // Ej: "PENDIENTE_PAGO", "COMPLETADA", "CANCELADA", "REEMBOLSADA"
    private List<Command> historialOperaciones; // Para registrar comandos como CancelarCompraCommand, ReembolsarCommand

    public Compra(String id, Usuario usuario, Evento evento) {
        this.id = id;
        this.usuario = usuario;
        this.evento = evento;
        this.entradasCompradas = new ArrayList<>();
        this.fechaCompra = LocalDateTime.now();
        this.estadoCompra = "PENDIENTE_PAGO"; // Estado inicial
        this.historialOperaciones = new ArrayList<>();
    }

    // Getters
    public String getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Evento getEvento() {
        return evento;
    }

    public List<Entrada> getEntradasCompradas() {
        return entradasCompradas;
    }

    public double getTotalPagado() {
        return totalPagado;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public String getEstadoCompra() {
        return estadoCompra;
    }

    public List<Command> getHistorialOperaciones() {
        return historialOperaciones;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
    }

    public void setTotalPagado(double totalPagado) {
        this.totalPagado = totalPagado;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public void setEstadoCompra(String estadoCompra) {
        this.estadoCompra = estadoCompra;
        // Aquí se podría añadir lógica para notificar cambios de estado si es necesario
    }

    // Métodos de negocio
    public void agregarEntrada(Entrada entrada) {
        this.entradasCompradas.add(entrada);
        // Recalcular total si es necesario, aunque usualmente el total se fija al final del proceso de compra.
    }

    public void agregarOperacionAlHistorial(Command operacion) {
        this.historialOperaciones.add(operacion);
    }

    public void generarComprobante() {
        System.out.println("Generando comprobante para la compra ID: " + id);
        System.out.println("Usuario: " + usuario.getNombre());
        System.out.println("Evento: " + evento.getNombre());
        System.out.println("Entradas:");
        for (Entrada entrada : entradasCompradas) {
            System.out.println("  - ID: " + entrada.getId() + ", Tipo: " + entrada.getTipo() + ", Precio: " + entrada.getPrecio());
            System.out.println("    Descripción: " + entrada.getDescripcion());
        }
        System.out.println("Total Pagado: " + totalPagado);
        System.out.println("Fecha de Compra: " + fechaCompra);
        System.out.println("Estado: " + estadoCompra);
    }

    public boolean solicitarReembolso() {
        // Lógica para solicitar un reembolso. Esto podría involucrar un Command.
        // Cambiar estado, notificar, etc.
        if ("COMPLETADA".equals(this.estadoCompra)) {
            System.out.println("Solicitud de reembolso iniciada para la compra " + this.id);
            // this.setEstadoCompra("PENDIENTE_REEMBOLSO");
            return true;
        }
        System.out.println("No se puede solicitar reembolso para una compra en estado: " + this.estadoCompra);
        return false;
    }

    public boolean cancelarCompra() {
        // Lógica para cancelar una compra. Esto podría involucrar un Command.
        // Cambiar estado, liberar entradas, notificar, etc.
         if ("PENDIENTE_PAGO".equals(this.estadoCompra) || "COMPLETADA".equals(this.estadoCompra) /*y cumple políticas de cancelación*/) {
            System.out.println("Compra " + this.id + " cancelada.");
            String estadoAnterior = this.estadoCompra;
            this.setEstadoCompra("CANCELADA");
            // Aquí se debería invocar la lógica para devolver las entradas al evento
            for(Entrada e : this.entradasCompradas){
                // Asumiendo que cada entrada sabe a qué TipoEntrada pertenece para devolverla correctamente
                // Esto es una simplificación. El TipoEntrada original debería ser accesible.
                // evento.devolverEntradas(e.getTipo(), 1); // Esto es conceptual.
            }
            // Si estaba completada y se cancela, podría implicar un reembolso.
            if("COMPLETADA".equals(estadoAnterior)){
                // Lógica de reembolso
            }
            return true;
        }
        System.out.println("No se puede cancelar una compra en estado: " + this.estadoCompra);
        return false;
    }


    @Override
    public String toString() {
        return "Compra{" +
               "id='" + id + '\'' +
               ", usuario=" + (usuario != null ? usuario.getNombre() : "N/A") +
               ", evento=" + (evento != null ? evento.getNombre() : "N/A") +
               ", totalPagado=" + totalPagado +
               ", estadoCompra='" + estadoCompra + '\'' +
               '}';
    }
}
