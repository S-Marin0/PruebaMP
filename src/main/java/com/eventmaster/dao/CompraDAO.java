package com.eventmaster.dao;

import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.entity.Usuario; // Para buscar compras por usuario
import com.eventmaster.model.entity.Evento;  // Para buscar compras por evento

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CompraDAO {
    Optional<Compra> findById(String id);
    List<Compra> findAll();
    List<Compra> findByUsuario(Usuario usuario); // O por usuarioId
    List<Compra> findByUsuarioId(String usuarioId);
    List<Compra> findByEvento(Evento evento);   // O por eventoId
    List<Compra> findByEventoId(String eventoId);
    List<Compra> findByFechaCompraBetween(LocalDateTime inicio, LocalDateTime fin);
    List<Compra> findByEstadoCompra(String estado); // Ej. "COMPLETADA", "PENDIENTE_PAGO"

    Compra save(Compra compra); // Para crear o actualizar
    void deleteById(String id);
    void delete(Compra compra);

    long count();
    long countByUsuarioId(String usuarioId);
    long countByEventoId(String eventoId);
}
