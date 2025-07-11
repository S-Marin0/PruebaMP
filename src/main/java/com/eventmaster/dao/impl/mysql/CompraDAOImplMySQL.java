package com.eventmaster.dao.impl.mysql;

import com.eventmaster.dao.CompraDAO;
import com.eventmaster.dao.UsuarioDAO; // To fetch Usuario
import com.eventmaster.dao.EventoDAO;  // To fetch Evento
// EntradaDAO might be needed if we were to save/load Entrada instances as part of Compra persistence
// However, Compra.entradasCompradas is a List<Entrada>. Individual Entrada instances are saved by EntradaDAO.
// This CompraDAO will store compra metadata. The link between Compra and its Entradas is via Compra_ID on Entrada table.
import com.eventmaster.model.entity.Compra;
import com.eventmaster.model.entity.Usuario;
import com.eventmaster.model.entity.Evento;
// import com.eventmaster.model.pattern.factory.Entrada; // Not directly mapped here, but related
import com.eventmaster.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CompraDAOImplMySQL implements CompraDAO {

    // Table and column names
    private static final String TABLE_COMPRA = "compra";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USUARIO_ID = "usuario_id";
    private static final String COLUMN_EVENTO_ID = "evento_id"; // Evento primarily associated with the purchase
    private static final String COLUMN_TOTAL_PAGADO = "total_pagado";
    private static final String COLUMN_FECHA_COMPRA = "fecha_compra";
    private static final String COLUMN_ESTADO_COMPRA = "estado_compra";
    private static final String COLUMN_ID_TRANSACCION_PASARELA = "id_transaccion_pasarela";

    // DAOs for related entities
    private UsuarioDAO usuarioDAO;
    private EventoDAO eventoDAO;
    // private EntradaDAO entradaDAO; // Needed if loading entradasCompradas list here

    // Constructor for dependency injection
    public CompraDAOImplMySQL(UsuarioDAO usuarioDAO, EventoDAO eventoDAO) {
        this.usuarioDAO = usuarioDAO;
        this.eventoDAO = eventoDAO;
    }
     public CompraDAOImplMySQL() {
        // Default constructor, dependencies to be set via setters
    }

    public void setUsuarioDAO(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public void setEventoDAO(EventoDAO eventoDAO) {
        this.eventoDAO = eventoDAO;
    }

    // SQL Queries
    private static final String INSERT_COMPRA = "INSERT INTO " + TABLE_COMPRA +
            "(" + COLUMN_ID + ", " + COLUMN_USUARIO_ID + ", " + COLUMN_EVENTO_ID + ", " + COLUMN_TOTAL_PAGADO + ", " +
            COLUMN_FECHA_COMPRA + ", " + COLUMN_ESTADO_COMPRA + ", " + COLUMN_ID_TRANSACCION_PASARELA +
            ") VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID = "SELECT * FROM " + TABLE_COMPRA + " WHERE " + COLUMN_ID + " = ?";
    private static final String SELECT_ALL = "SELECT * FROM " + TABLE_COMPRA;
    private static final String SELECT_BY_USUARIO_ID = "SELECT * FROM " + TABLE_COMPRA + " WHERE " + COLUMN_USUARIO_ID + " = ?";
    private static final String SELECT_BY_EVENTO_ID = "SELECT * FROM " + TABLE_COMPRA + " WHERE " + COLUMN_EVENTO_ID + " = ?"; // For finding all purchases related to one event
    private static final String SELECT_BY_FECHA_BETWEEN = "SELECT * FROM " + TABLE_COMPRA + " WHERE " + COLUMN_FECHA_COMPRA + " BETWEEN ? AND ?";
    private static final String SELECT_BY_ESTADO = "SELECT * FROM " + TABLE_COMPRA + " WHERE " + COLUMN_ESTADO_COMPRA + " = ?";

    private static final String UPDATE_COMPRA = "UPDATE " + TABLE_COMPRA + " SET " +
            COLUMN_USUARIO_ID + " = ?, " + COLUMN_EVENTO_ID + " = ?, " + COLUMN_TOTAL_PAGADO + " = ?, " +
            COLUMN_FECHA_COMPRA + " = ?, " + COLUMN_ESTADO_COMPRA + " = ?, " + COLUMN_ID_TRANSACCION_PASARELA + " = ? " +
            "WHERE " + COLUMN_ID + " = ?";

    private static final String DELETE_BY_ID = "DELETE FROM " + TABLE_COMPRA + " WHERE " + COLUMN_ID + " = ?";
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM " + TABLE_COMPRA;
    private static final String COUNT_BY_USUARIO_ID = "SELECT COUNT(*) FROM " + TABLE_COMPRA + " WHERE " + COLUMN_USUARIO_ID + " = ?";
    private static final String COUNT_BY_EVENTO_ID = "SELECT COUNT(*) FROM " + TABLE_COMPRA + " WHERE " + COLUMN_EVENTO_ID + " = ?";


    @Override
    public Optional<Compra> findById(String id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCompra(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Compra by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Compra> findAll() {
        List<Compra> compras = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                compras.add(mapRowToCompra(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all Compras: " + e.getMessage());
        }
        return compras;
    }

    @Override
    public List<Compra> findByUsuario(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) return new ArrayList<>();
        return findByUsuarioId(usuario.getId());
    }

    @Override
    public List<Compra> findByUsuarioId(String usuarioId) {
        List<Compra> compras = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_USUARIO_ID)) {
            stmt.setString(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    compras.add(mapRowToCompra(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Compras by UsuarioID: " + e.getMessage());
        }
        return compras;
    }

    @Override
    public List<Compra> findByEvento(Evento evento) {
        if (evento == null || evento.getId() == null) return new ArrayList<>();
        return findByEventoId(evento.getId());
    }

    @Override
    public List<Compra> findByEventoId(String eventoId) {
        List<Compra> compras = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_EVENTO_ID)) {
            stmt.setString(1, eventoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    compras.add(mapRowToCompra(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Compras by EventoID: " + e.getMessage());
        }
        return compras;
    }

    @Override
    public List<Compra> findByFechaCompraBetween(LocalDateTime inicio, LocalDateTime fin) {
        List<Compra> compras = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_FECHA_BETWEEN)) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fin));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    compras.add(mapRowToCompra(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Compras by fecha: " + e.getMessage());
        }
        return compras;
    }

    @Override
    public List<Compra> findByEstadoCompra(String estado) {
        List<Compra> compras = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ESTADO)) {
            stmt.setString(1, estado);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    compras.add(mapRowToCompra(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Compras by estado: " + e.getMessage());
        }
        return compras;
    }

    @Override
    public Compra save(Compra compra) {
        if (compra.getUsuario() == null || compra.getUsuario().getId() == null) {
             System.err.println("Error: Compra must have a valid Usuario with an ID.");
             return null;
        }
        if (compra.getEvento() == null || compra.getEvento().getId() == null) {
             System.err.println("Error: Compra must have a valid Evento with an ID.");
             return null;
        }

        boolean isNew = compra.getId() == null || findById(compra.getId()).isEmpty();
        if (isNew && compra.getId() == null) {
            compra.setId(UUID.randomUUID().toString());
        }

        String sql = isNew ? INSERT_COMPRA : UPDATE_COMPRA;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (isNew) {
                stmt.setString(1, compra.getId());
                stmt.setString(2, compra.getUsuario().getId());
                stmt.setString(3, compra.getEvento().getId());
                stmt.setDouble(4, compra.getTotalPagado());
                stmt.setTimestamp(5, Timestamp.valueOf(compra.getFechaCompra()));
                stmt.setString(6, compra.getEstadoCompra());
                stmt.setString(7, compra.getIdTransaccionPasarela());
            } else { // Update
                stmt.setString(1, compra.getUsuario().getId());
                stmt.setString(2, compra.getEvento().getId());
                stmt.setDouble(3, compra.getTotalPagado());
                stmt.setTimestamp(4, Timestamp.valueOf(compra.getFechaCompra()));
                stmt.setString(5, compra.getEstadoCompra());
                stmt.setString(6, compra.getIdTransaccionPasarela());
                stmt.setString(7, compra.getId()); // WHERE id = ?
            }
            stmt.executeUpdate();
            // Note: The entradasCompradas list within the Compra object is not persisted by this DAO.
            // Individual Entrada instances are saved by EntradaDAO and linked via compra_id.
            return compra;
        } catch (SQLException e) {
            System.err.println("Error saving Compra: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteById(String id) {
        // Consider implications: deleting a Compra might require deleting associated Entrada records
        // or handling them based on business rules (e.g., cascade delete in DB or manual deletion via EntradaDAO).
        // This DAO only deletes the Compra record itself.
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_ID)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting Compra by ID: " + e.getMessage());
        }
    }

    @Override
    public void delete(Compra compra) {
        if (compra != null && compra.getId() != null) {
            deleteById(compra.getId());
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
            System.err.println("Error counting all Compras: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public long countByUsuarioId(String usuarioId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_BY_USUARIO_ID)) {
            stmt.setString(1, usuarioId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting Compras by UsuarioID: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public long countByEventoId(String eventoId) {
         try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_BY_EVENTO_ID)) {
            stmt.setString(1, eventoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting Compras by EventoID: " + e.getMessage());
        }
        return 0;
    }

    private Compra mapRowToCompra(ResultSet rs) throws SQLException {
        String id = rs.getString(COLUMN_ID);
        String usuarioId = rs.getString(COLUMN_USUARIO_ID);
        String eventoId = rs.getString(COLUMN_EVENTO_ID);
        double totalPagado = rs.getDouble(COLUMN_TOTAL_PAGADO);
        LocalDateTime fechaCompra = rs.getTimestamp(COLUMN_FECHA_COMPRA).toLocalDateTime();
        String estadoCompra = rs.getString(COLUMN_ESTADO_COMPRA);
        String idTransaccionPasarela = rs.getString(COLUMN_ID_TRANSACCION_PASARELA);

        if (this.usuarioDAO == null || this.eventoDAO == null) {
            throw new IllegalStateException("UsuarioDAO or EventoDAO not initialized in CompraDAOImplMySQL.");
        }

        Usuario usuario = usuarioDAO.findById(usuarioId)
                .orElseThrow(() -> new SQLException("Usuario not found for ID: " + usuarioId + " for Compra ID: " + id));
        Evento evento = eventoDAO.findById(eventoId)
                .orElseThrow(() -> new SQLException("Evento not found for ID: " + eventoId + " for Compra ID: " + id));

        Compra compra = new Compra(id, usuario, evento);
        compra.setTotalPagado(totalPagado);
        compra.setFechaCompra(fechaCompra);
        compra.setEstadoCompra(estadoCompra);
        compra.setIdTransaccionPasarela(idTransaccionPasarela);

        // The list Compra.entradasCompradas is NOT populated here.
        // To populate it, we would need EntradaDAO and a method like entradaDAO.findAllByCompraId(id).
        // This is often handled at the service layer to avoid circular dependencies or overly complex DAOs.
        // For example:
        // if (this.entradaDAO != null) {
        //     List<Entrada> entradas = this.entradaDAO.findAllByCompraId(id);
        //     entradas.forEach(compra::agregarEntrada);
        // }
        // The historialOperaciones list is also not populated from the DB by this basic DAO.

        return compra;
    }
}
