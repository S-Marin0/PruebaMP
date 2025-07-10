package com.eventmaster.service;

import com.eventmaster.dao.EventoDAO;
import com.eventmaster.dao.TipoEntradaDAO;
import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.entity.Lugar;
import com.eventmaster.model.entity.Organizador;
import com.eventmaster.model.pattern.factory.TipoEntrada; // Definición del tipo de entrada

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;

public class EventoService {

    private final EventoDAO eventoDAO;
    private final TipoEntradaDAO tipoEntradaDAO; // Para gestionar las definiciones de TipoEntrada asociadas

    public EventoService(EventoDAO eventoDAO, TipoEntradaDAO tipoEntradaDAO) {
        this.eventoDAO = eventoDAO;
        this.tipoEntradaDAO = tipoEntradaDAO;
    }

    public Evento registrarNuevoEvento(Evento evento) {
        if (evento == null) {
            throw new IllegalArgumentException("El evento no puede ser nulo.");
        }
        // Aquí se podrían añadir validaciones de negocio antes de guardar
        Evento eventoGuardado = eventoDAO.save(evento); // Guarda el evento principal
        System.out.println("EventoService: Evento '" + eventoGuardado.getNombre() + "' registrado en el sistema con ID: " + eventoGuardado.getId());

        // Guardar las definiciones de TipoEntrada asociadas al evento
        if (evento.getTiposEntradaDisponibles() != null && !evento.getTiposEntradaDisponibles().isEmpty()) { // Modificación: verificar si no está vacío
            for (Map.Entry<String, TipoEntrada> entry : evento.getTiposEntradaDisponibles().entrySet()) {
                // Asumiendo que TipoEntradaDAO.save asocia el TipoEntrada con el eventoId.
                tipoEntradaDAO.save(eventoGuardado.getId(), entry.getValue());
            }
            System.out.println("EventoService: Tipos de entrada para '" + eventoGuardado.getNombre() + "' guardados.");
        } else {
            System.out.println("EventoService: Evento '" + eventoGuardado.getNombre() + "' registrado sin tipos de entrada definidos.");
        }
        return eventoGuardado;
    }

    public void eliminarEventoRegistrado(Evento evento) {
        if (evento != null && evento.getId() != null) {
            // Eliminar primero las definiciones de TipoEntrada asociadas (dependencia)
            // O la BBDD podría tener ON DELETE CASCADE
            List<TipoEntrada> tipos = tipoEntradaDAO.findAllByEventoId(evento.getId());
            for(TipoEntrada te : tipos) {
                // Asumiendo que TipoEntrada (definición) no tiene un ID global propio en el modelo actual,
                // lo eliminamos por eventoId y nombreTipo. Si tuviera ID global, sería por ese ID.
                tipoEntradaDAO.deleteByEventoIdAndNombreTipo(evento.getId(), te.getNombreTipo());
            }
            eventoDAO.deleteById(evento.getId());
            System.out.println("EventoService: Evento '" + evento.getNombre() + "' y sus tipos de entrada eliminados del sistema.");
        } else {
            System.out.println("EventoService: Evento nulo o sin ID, no se puede eliminar.");
        }
    }

    public Optional<Evento> findEventoById(String id) {
        Optional<Evento> eventoOpt = eventoDAO.findById(id);
        eventoOpt.ifPresent(this::cargarTiposDeEntradaParaEvento);
        return eventoOpt;
    }

    public List<Evento> getAllEventos() {
        List<Evento> eventos = eventoDAO.findAll();
        eventos.forEach(this::cargarTiposDeEntradaParaEvento);
        return eventos;
    }

    public Evento actualizarEvento(Evento eventoActualizado) {
        if (eventoActualizado == null || eventoActualizado.getId() == null) {
            throw new IllegalArgumentException("Evento a actualizar no puede ser nulo o no tener ID.");
        }
        // Guardar el evento principal
        Evento eventoGuardado = eventoDAO.save(eventoActualizado);

        // Actualizar/guardar las definiciones de TipoEntrada asociadas
        // Esto podría ser complejo: ¿qué pasa si se eliminan tipos de entrada, se añaden nuevos, o se modifican existentes?
        // Una estrategia simple es eliminar los viejos y guardar los nuevos.
        // O una lógica más fina de comparación.
        // Por ahora, asumimos que el mapa en eventoActualizado.getTiposEntradaDisponibles() es la verdad actual.
        // Primero, podríamos eliminar todos los TipoEntrada existentes para este eventoId (si la interfaz lo permite fácilmente)
        // o cargarlos y compararlos.
        // Para la simulación, vamos a guardar/actualizar cada uno.

        // Lógica actual:
        if (eventoGuardado.getTiposEntradaDisponibles() != null) {
            for (Map.Entry<String, TipoEntrada> entry : eventoGuardado.getTiposEntradaDisponibles().entrySet()) {
                tipoEntradaDAO.save(eventoGuardado.getId(), entry.getValue());
            }
            System.out.println("EventoService: Tipos de entrada para '" + eventoGuardado.getNombre() + "' (re)guardados/actualizados.");
        } else {
            System.out.println("EventoService: Evento '" + eventoGuardado.getNombre() + "' actualizado, no se procesaron tipos de entrada (mapa nulo o vacío en el objeto evento).");
        }
        return eventoGuardado;
    }

    // Método para cargar las definiciones de TipoEntrada en un objeto Evento
    private void cargarTiposDeEntradaParaEvento(Evento evento) {
        if (evento != null && evento.getId() != null) {
            List<TipoEntrada> tipos = tipoEntradaDAO.findAllByEventoId(evento.getId());

            if (evento.getTiposEntradaDisponibles() == null) {
                 // No debería pasar si el constructor de Evento y el builder lo inicializan.
            } else {
                 evento.getTiposEntradaDisponibles().clear(); // Limpiar para asegurar que solo tenemos los del DAO
            }

            for (TipoEntrada te : tipos) {
                evento.agregarTipoEntrada(te.getNombreTipo(), te); // Asumiendo que Evento tiene este método
            }
            System.out.println("EventoService: Cargados " + tipos.size() + " tipos de entrada para evento ID " + evento.getId());
        }
    }

    // Métodos de búsqueda delegados al DAO (y luego cargan tipos de entrada)
    public List<Evento> findEventosPorNombre(String nombre) {
        List<Evento> eventos = eventoDAO.findByNombreContaining(nombre);
        eventos.forEach(this::cargarTiposDeEntradaParaEvento);
        return eventos;
    }

    public List<Evento> findEventosPorCategoria(String categoria) {
        List<Evento> eventos = eventoDAO.findByCategoria(categoria);
        eventos.forEach(this::cargarTiposDeEntradaParaEvento);
        return eventos;
    }

    public List<Evento> findEventosPorOrganizador(Organizador organizador) {
        List<Evento> eventos = eventoDAO.findByOrganizador(organizador);
        eventos.forEach(this::cargarTiposDeEntradaParaEvento);
        return eventos;
    }

    public List<Evento> findEventosPublicadosYFuturos() {
        // Combinar lógica o añadir un método específico al DAO si es una consulta común
        LocalDateTime ahora = LocalDateTime.now();
        List<Evento> eventos = eventoDAO.findAll().stream()
            .filter(e -> e.getFechaHora() != null && e.getFechaHora().isAfter(ahora) &&
                         e.getEstadoActual() != null && "Publicado".equalsIgnoreCase(e.getEstadoActual().getNombreEstado()))
            .collect(Collectors.toList());
        eventos.forEach(this::cargarTiposDeEntradaParaEvento);
        return eventos;
    }

    /**
     * Vende un número de entradas para un tipo específico de un evento.
     * Esta lógica ahora reside principalmente en el Evento y su TipoEntrada,
     * pero el servicio puede orquestar la persistencia del cambio.
     * @return true si la venta fue exitosa y se persistió, false en caso contrario.
     */
    public boolean procesarVentaEntradas(String eventoId, String nombreTipoEntrada, int cantidad) {
        Optional<Evento> eventoOpt = findEventoById(eventoId); // Esto ya carga los tipos de entrada
        if (eventoOpt.isPresent()) {
            Evento evento = eventoOpt.get();
            boolean ventaExitosaEnModelo = evento.venderEntradas(nombreTipoEntrada, cantidad); // Lógica en el modelo

            if (ventaExitosaEnModelo) {
                // Persistir los cambios en el Evento (entradasVendidas)
                eventoDAO.save(evento);
                // Persistir los cambios en TipoEntrada (cantidadDisponible)
                TipoEntrada tipoEntradaAfectado = evento.getTipoEntrada(nombreTipoEntrada);
                if (tipoEntradaAfectado != null) {
                    tipoEntradaDAO.save(eventoId, tipoEntradaAfectado); // El save actualiza si ya existe
                    // O un método más específico: tipoEntradaDAO.actualizarCantidadDisponible(eventoId, nombreTipoEntrada, tipoEntradaAfectado.getCantidadDisponible());
                }
                System.out.println("EventoService: Venta procesada y persistida para evento " + evento.getNombre());
                return true;
            } else {
                System.out.println("EventoService: Venta fallida en el modelo para evento " + evento.getNombre());
                return false;
            }
        }
        System.out.println("EventoService: Evento no encontrado para procesar venta, ID: " + eventoId);
        return false;
    }
}
