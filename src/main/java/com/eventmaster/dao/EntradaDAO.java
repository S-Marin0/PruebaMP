package com.eventmaster.dao;

import com.eventmaster.model.pattern.factory.Entrada; // La interfaz que representa una entrada individual
import com.eventmaster.model.entity.Compra; // Para buscar entradas por compra
import com.eventmaster.model.entity.Evento; // Para buscar entradas por evento

import java.util.List;
import java.util.Optional;

public interface EntradaDAO {

    /**
     * Guarda una instancia de entrada vendida.
     * @param entrada La instancia de Entrada a guardar.
     * @return La Entrada guardada (podría tener un ID asignado por la BBDD si no lo tenía).
     */
    Entrada save(Entrada entrada);

    /**
     * Guarda una lista de instancias de entrada (ej. en una compra múltiple).
     * @param entradas Lista de Entradas a guardar.
     * @return La lista de Entradas guardadas.
     */
    List<Entrada> saveAll(List<Entrada> entradas);

    /**
     * Encuentra una entrada por su ID único.
     * @param idEntrada El ID de la instancia de entrada.
     * @return Un Optional conteniendo la Entrada si se encuentra.
     */
    Optional<Entrada> findById(String idEntrada);

    /**
     * Encuentra todas las entradas asociadas a una Compra específica.
     * @param compraId El ID de la Compra.
     * @return Una lista de Entradas.
     */
    List<Entrada> findAllByCompraId(String compraId);

    /**
     * Encuentra todas las entradas vendidas para un Evento específico.
     * Útil para reportes o verificar asistentes.
     * @param eventoId El ID del Evento.
     * @return Una lista de Entradas.
     */
    List<Entrada> findAllByEventoId(String eventoId);

    /**
     * Encuentra todas las entradas vendidas para un Evento específico y un tipo de entrada específico.
     * @param eventoId El ID del Evento.
     * @param nombreTipoEntrada El nombre del tipo de entrada (ej. "VIP", "General").
     * @return Una lista de Entradas.
     */
    List<Entrada> findAllByEventoIdAndTipoEntrada(String eventoId, String nombreTipoEntrada);


    /**
     * Elimina una instancia de entrada (ej. si una compra es cancelada y la entrada invalidada).
     * @param idEntrada El ID de la entrada a eliminar.
     */
    void deleteById(String idEntrada);

    /**
     * Elimina todas las entradas asociadas a una compra.
     * @param compraId El ID de la compra.
     */
    void deleteAllByCompraId(String compraId);

    /**
     * Cuenta el número de entradas vendidas para un evento.
     * @param eventoId El ID del evento.
     * @return El número de entradas vendidas.
     */
    long countByEventoId(String eventoId);

    /**
     * Cuenta el número de entradas vendidas para un evento y tipo específico.
     * @param eventoId El ID del evento.
     * @param nombreTipoEntrada El nombre del tipo de entrada.
     * @return El número de entradas vendidas de ese tipo para ese evento.
     */
    long countByEventoIdAndTipoEntrada(String eventoId, String nombreTipoEntrada);

}
