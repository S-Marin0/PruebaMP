package com.eventmaster.dao;

import com.eventmaster.model.CodigoDescuento;
import java.util.List;
import java.util.Optional;

public interface CodigoDescuentoDAO {
    Optional<CodigoDescuento> findByCodigo(String codigo); // El código literal que ingresa el usuario
    Optional<CodigoDescuento> findById(String id); // Si los códigos tienen un ID interno además del código literal
    List<CodigoDescuento> findAll();
    List<CodigoDescuento> findAllActivos(); // Códigos que no han expirado y tienen usos disponibles

    CodigoDescuento save(CodigoDescuento codigoDescuento); // Para crear o actualizar (ej. incrementar usosActuales)
    void deleteById(String id);
    void deleteByCodigo(String codigo);

    long count();
}
