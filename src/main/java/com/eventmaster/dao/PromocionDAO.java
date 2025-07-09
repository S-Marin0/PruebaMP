package com.eventmaster.dao;

import com.eventmaster.model.Promocion;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PromocionDAO {
    Optional<Promocion> findById(String id);
    List<Promocion> findAll();
    List<Promocion> findActivas(LocalDateTime fechaActual); // Promociones válidas en la fecha dada
    List<Promocion> findByTipoAplicable(String tipoAplicable); // "Evento", "TipoEntrada"
    List<Promocion> findByTipoAplicableAndIdAplicable(String tipoAplicable, String idAplicable); // Ej. todas las promos para un EventoID específico

    Promocion save(Promocion promocion);
    void deleteById(String id);

    long count();
}
