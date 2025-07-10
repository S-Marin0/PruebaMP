package com.eventmaster.dao;

import com.eventmaster.model.entity.Lugar;
import java.util.List;
import java.util.Optional;

public interface LugarDAO {
    Optional<Lugar> findById(String id);
    List<Lugar> findAll();
    List<Lugar> findByNombreContaining(String nombre);
    List<Lugar> findByDireccionContaining(String direccionFragmento);
    // List<Lugar> findByCapacidadGreaterThanEqual(int capacidadMinima); // Ejemplo

    Lugar save(Lugar lugar);
    void deleteById(String id);
    void delete(Lugar lugar);

    long count();
}
