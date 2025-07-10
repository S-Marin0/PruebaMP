package com.eventmaster.dao;

import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Asistente;
import com.eventmaster.model.entity.Organizador;

import java.util.List;
import java.util.Optional;

public interface UsuarioDAO {

    // Métodos generales para Usuario
    Optional<Usuario> findById(String id);
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findAll();
    Usuario save(Usuario usuario); // Puede ser para crear o actualizar
    void deleteById(String id);

    // Métodos específicos si se necesita diferenciar el tipo al guardar o buscar
    // Aunque save(Usuario usuario) podría manejar polimorfismo con instanceof en la implementación.

    // Ejemplo de métodos más específicos (podrían no ser necesarios si los generales son suficientes)
    // Optional<Asistente> findAsistenteById(String id);
    // Optional<Organizador> findOrganizadorById(String id);
    // Asistente saveAsistente(Asistente asistente);
    // Organizador saveOrganizador(Organizador organizador);

    // Contar usuarios (ejemplo de otro tipo de método)
    long count();
}
