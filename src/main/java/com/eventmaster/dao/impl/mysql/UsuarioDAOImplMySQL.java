package com.eventmaster.dao.impl.mysql;

import com.eventmaster.dao.UsuarioDAO;
import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Asistente; // Required for specific type handling
import com.eventmaster.model.entity.Organizador; // Required for specific type handling
import com.eventmaster.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID; // For generating IDs if not auto-increment

public class UsuarioDAOImplMySQL implements UsuarioDAO {

    // Table and column names
    private static final String TABLE_USUARIO = "usuario";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NOMBRE = "nombre";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_TIPO_USUARIO = "tipo_usuario"; // To distinguish Asistente/Organizador

    // SQL Queries
    private static final String INSERT_USUARIO = "INSERT INTO " + TABLE_USUARIO +
            "(" + COLUMN_ID + ", " + COLUMN_NOMBRE + ", " + COLUMN_EMAIL + ", " + COLUMN_PASSWORD + ", " + COLUMN_TIPO_USUARIO + ") VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM " + TABLE_USUARIO + " WHERE " + COLUMN_ID + " = ?";
    private static final String SELECT_BY_EMAIL = "SELECT * FROM " + TABLE_USUARIO + " WHERE " + COLUMN_EMAIL + " = ?";
    private static final String SELECT_ALL = "SELECT * FROM " + TABLE_USUARIO;
    private static final String UPDATE_USUARIO = "UPDATE " + TABLE_USUARIO + " SET " +
            COLUMN_NOMBRE + " = ?, " + COLUMN_EMAIL + " = ?, " + COLUMN_PASSWORD + " = ?, " + COLUMN_TIPO_USUARIO + " = ? WHERE " + COLUMN_ID + " = ?";
    private static final String DELETE_BY_ID = "DELETE FROM " + TABLE_USUARIO + " WHERE " + COLUMN_ID + " = ?";
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM " + TABLE_USUARIO;

    @Override
    public Optional<Usuario> findById(String id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            // Log error (e.g., using a logger)
            System.err.println("Error finding Usuario by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Usuario> findByEmail(String email) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EMAIL)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUsuario(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Usuario by email: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Usuario> findAll() {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                usuarios.add(mapRowToUsuario(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all Usuarios: " + e.getMessage());
        }
        return usuarios;
    }

    @Override
    public Usuario save(Usuario usuario) {
        // Determine if it's an insert or update
        if (usuario.getId() == null || findById(usuario.getId()).isEmpty()) {
            // Insert new usuario
            if (usuario.getId() == null) {
                 usuario.setId(UUID.randomUUID().toString()); // Generate new ID
            }
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(INSERT_USUARIO)) {
                stmt.setString(1, usuario.getId());
                stmt.setString(2, usuario.getNombre());
                stmt.setString(3, usuario.getEmail());
                stmt.setString(4, usuario.getPassword()); // Store password hash in real app
                stmt.setString(5, getTipoUsuarioString(usuario));
                stmt.executeUpdate();
                return usuario;
            } catch (SQLException e) {
                System.err.println("Error inserting Usuario: " + e.getMessage());
                // Handle error, maybe throw custom exception
                return null; // Or throw exception
            }
        } else {
            // Update existing usuario
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(UPDATE_USUARIO)) {
                stmt.setString(1, usuario.getNombre());
                stmt.setString(2, usuario.getEmail());
                stmt.setString(3, usuario.getPassword());
                stmt.setString(4, getTipoUsuarioString(usuario));
                stmt.setString(5, usuario.getId());
                stmt.executeUpdate();
                return usuario;
            } catch (SQLException e) {
                System.err.println("Error updating Usuario: " + e.getMessage());
                return null; // Or throw exception
            }
        }
    }

    @Override
    public void deleteById(String id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_ID)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting Usuario by ID: " + e.getMessage());
        }
    }

    @Override
    public long count() {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting Usuarios: " + e.getMessage());
        }
        return 0;
    }

    private Usuario mapRowToUsuario(ResultSet rs) throws SQLException {
        String id = rs.getString(COLUMN_ID);
        String nombre = rs.getString(COLUMN_NOMBRE);
        String email = rs.getString(COLUMN_EMAIL);
        String password = rs.getString(COLUMN_PASSWORD);
        String tipoUsuario = rs.getString(COLUMN_TIPO_USUARIO);

        Usuario usuario;
        if ("ASISTENTE".equalsIgnoreCase(tipoUsuario)) {
            // Assuming Asistente has a constructor like this or setters
            usuario = new Asistente(id, nombre, email, password);
        } else if ("ORGANIZADOR".equalsIgnoreCase(tipoUsuario)) {
            // Assuming Organizador has a constructor like this or setters
            // If Organizador has specific fields from DB, fetch them here
            usuario = new Organizador(id, nombre, email, password);
        } else {
            // Handle unknown type or throw an error
            // For now, defaulting to a generic user might be problematic if Usuario is abstract
            // Throwing an exception or logging an error is better.
            throw new SQLException("Unknown user type in database: " + tipoUsuario);
        }
        // Note: Preferences and HistorialCompras are not mapped here as they are likely in separate tables.
        // This DAO focuses on the 'usuario' table. Related entities would need their own DAOs and service methods to link them.
        return usuario;
    }

    private String getTipoUsuarioString(Usuario usuario) {
        if (usuario instanceof Asistente) {
            return "ASISTENTE";
        } else if (usuario instanceof Organizador) {
            return "ORGANIZADOR";
        }
        return "DESCONOCIDO"; // Or throw an IllegalArgumentException
    }
}
