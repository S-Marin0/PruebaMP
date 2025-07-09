package com.eventmaster.dao;

import com.eventmaster.model.pattern.factory.TipoEntrada; // La clase que define un tipo de entrada
import java.util.List;
import java.util.Optional;

public interface TipoEntradaDAO {

    // Los TiposEntrada suelen estar asociados a un Evento específico.
    // Por lo tanto, los métodos a menudo necesitarán un eventoId.

    /**
     * Guarda una definición de tipo de entrada para un evento.
     * Si el tipoEntrada ya tiene un ID y existe, la actualiza. Sino, la crea.
     * @param eventoId El ID del evento al que pertenece este tipo de entrada.
     * @param tipoEntrada La definición del tipo de entrada.
     * @return El TipoEntrada guardado (podría tener un ID asignado por la BBDD).
     */
    TipoEntrada save(String eventoId, TipoEntrada tipoEntrada);

    /**
     * Encuentra un tipo de entrada específico por su nombre/identificador DENTRO de un evento.
     * @param eventoId El ID del evento.
     * @param nombreTipoEntrada El nombre o identificador del tipo de entrada (ej. "General", "VIP").
     * @return Un Optional conteniendo el TipoEntrada si se encuentra.
     */
    Optional<TipoEntrada> findByEventoIdAndNombreTipo(String eventoId, String nombreTipoEntrada);

    /**
     * Encuentra un TipoEntrada por un ID único global (si las definiciones tienen IDs únicos).
     * @param tipoEntradaId El ID único de la definición del tipo de entrada.
     * @return Un Optional conteniendo el TipoEntrada.
     */
    Optional<TipoEntrada> findById(String tipoEntradaId);


    /**
     * Obtiene todas las definiciones de tipos de entrada para un evento específico.
     * @param eventoId El ID del evento.
     * @return Una lista de TipoEntrada para ese evento.
     */
    List<TipoEntrada> findAllByEventoId(String eventoId);

    /**
     * Elimina una definición de tipo de entrada de un evento.
     * @param eventoId El ID del evento.
     * @param nombreTipoEntrada El nombre o identificador del tipo de entrada a eliminar.
     */
    void deleteByEventoIdAndNombreTipo(String eventoId, String nombreTipoEntrada);

    /**
     * Elimina una definición de TipoEntrada por su ID único global.
     * @param tipoEntradaId El ID único de la definición del tipo de entrada.
     */
    void deleteById(String tipoEntradaId);

    /**
     * Actualiza la cantidad disponible de un tipo de entrada.
     * Este método es crucial y debe ser atómico.
     * @param eventoId El ID del evento.
     * @param nombreTipoEntrada El nombre del tipo de entrada.
     * @param nuevaCantidadDisponible La nueva cantidad disponible.
     * @return true si la actualización fue exitosa, false en caso contrario.
     */
    boolean actualizarCantidadDisponible(String eventoId, String nombreTipoEntrada, int nuevaCantidadDisponible);
}
