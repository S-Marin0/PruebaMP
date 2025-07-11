package com.eventmaster.dao.impl.mysql;

import com.eventmaster.dao.PromocionDAO;
import com.eventmaster.model.Promocion;
import com.eventmaster.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PromocionDAOImplMySQL implements PromocionDAO {

    // Table and column names
    private static final String TABLE_PROMOCION = "promocion";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_DESCRIPCION = "descripcion";
    private static final String COLUMN_PORCENTAJE_DESCUENTO = "porcentaje_descuento";
    private static final String COLUMN_FECHA_INICIO = "fecha_inicio";
    private static final String COLUMN_FECHA_FIN = "fecha_fin";
    private static final String COLUMN_TIPO_APLICABLE = "tipo_aplicable"; // "Evento", "TipoEntrada"
    private static final String COLUMN_ID_APLICABLE = "id_aplicable";     // EventoID or TipoEntradaID/Nombre

    // SQL Queries
    private static final String INSERT_PROMOCION = "INSERT INTO " + TABLE_PROMOCION +
            "(" + COLUMN_ID + ", " + COLUMN_DESCRIPCION + ", " + COLUMN_PORCENTAJE_DESCUENTO + ", " +
            COLUMN_FECHA_INICIO + ", " + COLUMN_FECHA_FIN + ", " + COLUMN_TIPO_APLICABLE + ", " + COLUMN_ID_APLICABLE +
            ") VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID = "SELECT * FROM " + TABLE_PROMOCION + " WHERE " + COLUMN_ID + " = ?";
    private static final String SELECT_ALL = "SELECT * FROM " + TABLE_PROMOCION;
    private static final String SELECT_ACTIVAS = "SELECT * FROM " + TABLE_PROMOCION +
            " WHERE " + COLUMN_FECHA_INICIO + " <= ? AND " + COLUMN_FECHA_FIN + " >= ?";
    private static final String SELECT_BY_TIPO_APLICABLE = "SELECT * FROM " + TABLE_PROMOCION + " WHERE " + COLUMN_TIPO_APLICABLE + " = ?";
    private static final String SELECT_BY_TIPO_APLICABLE_AND_ID_APLICABLE = "SELECT * FROM " + TABLE_PROMOCION +
            " WHERE " + COLUMN_TIPO_APLICABLE + " = ? AND " + COLUMN_ID_APLICABLE + " = ?";

    private static final String UPDATE_PROMOCION = "UPDATE " + TABLE_PROMOCION + " SET " +
            COLUMN_DESCRIPCION + " = ?, " + COLUMN_PORCENTAJE_DESCUENTO + " = ?, " +
            COLUMN_FECHA_INICIO + " = ?, " + COLUMN_FECHA_FIN + " = ?, " +
            COLUMN_TIPO_APLICABLE + " = ?, " + COLUMN_ID_APLICABLE + " = ? " +
            "WHERE " + COLUMN_ID + " = ?";

    private static final String DELETE_BY_ID = "DELETE FROM " + TABLE_PROMOCION + " WHERE " + COLUMN_ID + " = ?";
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM " + TABLE_PROMOCION;


    @Override
    public Optional<Promocion> findById(String id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToPromocion(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Promocion by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Promocion> findAll() {
        List<Promocion> promociones = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                promociones.add(mapRowToPromocion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all Promociones: " + e.getMessage());
        }
        return promociones;
    }

    @Override
    public List<Promocion> findActivas(LocalDateTime fechaActual) {
        List<Promocion> promociones = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ACTIVAS)) {
            stmt.setTimestamp(1, Timestamp.valueOf(fechaActual));
            stmt.setTimestamp(2, Timestamp.valueOf(fechaActual));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    promociones.add(mapRowToPromocion(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding active Promociones: " + e.getMessage());
        }
        return promociones;
    }

    @Override
    public List<Promocion> findByTipoAplicable(String tipoAplicable) {
        List<Promocion> promociones = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_TIPO_APLICABLE)) {
            stmt.setString(1, tipoAplicable);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    promociones.add(mapRowToPromocion(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Promociones by tipoAplicable: " + e.getMessage());
        }
        return promociones;
    }

    @Override
    public List<Promocion> findByTipoAplicableAndIdAplicable(String tipoAplicable, String idAplicable) {
        List<Promocion> promociones = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_TIPO_APLICABLE_AND_ID_APLICABLE)) {
            stmt.setString(1, tipoAplicable);
            stmt.setString(2, idAplicable);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    promociones.add(mapRowToPromocion(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Promociones by tipoAplicable and idAplicable: " + e.getMessage());
        }
        return promociones;
    }


    @Override
    public Promocion save(Promocion promocion) {
        boolean isNew = promocion.getId() == null || findById(promocion.getId()).isEmpty();
        if (isNew && promocion.getId() == null) {
            promocion.setId(UUID.randomUUID().toString());
        }

        String sql = isNew ? INSERT_PROMOCION : UPDATE_PROMOCION;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (isNew) {
                stmt.setString(1, promocion.getId());
                stmt.setString(2, promocion.getDescripcion());
                stmt.setDouble(3, promocion.getPorcentajeDescuento());
                stmt.setTimestamp(4, Timestamp.valueOf(promocion.getFechaInicio()));
                stmt.setTimestamp(5, Timestamp.valueOf(promocion.getFechaFin()));
                stmt.setString(6, promocion.getTipoAplicable());
                stmt.setString(7, promocion.getIdAplicable());
            } else { // Update
                stmt.setString(1, promocion.getDescripcion());
                stmt.setDouble(2, promocion.getPorcentajeDescuento());
                stmt.setTimestamp(3, Timestamp.valueOf(promocion.getFechaInicio()));
                stmt.setTimestamp(4, Timestamp.valueOf(promocion.getFechaFin()));
                stmt.setString(5, promocion.getTipoAplicable());
                stmt.setString(6, promocion.getIdAplicable());
                stmt.setString(7, promocion.getId()); // WHERE id = ?
            }
            stmt.executeUpdate();
            return promocion;
        } catch (SQLException e) {
            System.err.println("Error saving Promocion: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteById(String id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_ID)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting Promocion by ID: " + e.getMessage());
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
            System.err.println("Error counting all Promociones: " + e.getMessage());
        }
        return 0;
    }

    private Promocion mapRowToPromocion(ResultSet rs) throws SQLException {
        String id = rs.getString(COLUMN_ID);
        String descripcion = rs.getString(COLUMN_DESCRIPCION);
        double porcentajeDescuento = rs.getDouble(COLUMN_PORCENTAJE_DESCUENTO);
        LocalDateTime fechaInicio = rs.getTimestamp(COLUMN_FECHA_INICIO).toLocalDateTime();
        LocalDateTime fechaFin = rs.getTimestamp(COLUMN_FECHA_FIN).toLocalDateTime();
        String tipoAplicable = rs.getString(COLUMN_TIPO_APLICABLE);
        String idAplicable = rs.getString(COLUMN_ID_APLICABLE);

        return new Promocion(id, descripcion, porcentajeDescuento, fechaInicio, fechaFin, tipoAplicable, idAplicable);
    }
}
