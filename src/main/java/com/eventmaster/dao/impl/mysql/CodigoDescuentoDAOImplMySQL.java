package com.eventmaster.dao.impl.mysql;

import com.eventmaster.dao.CodigoDescuentoDAO;
import com.eventmaster.dao.PromocionDAO; // For fetching PromocionAsociada
import com.eventmaster.model.CodigoDescuento;
import com.eventmaster.model.Promocion;
import com.eventmaster.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
// CodigoDescuento model does not have an 'id' field, it uses 'codigo' string as its primary identifier.
// The DAO interface has findById, implying an internal ID might exist in DB.
// For this implementation, we'll assume 'codigo' (the string) is the PK in the DB table.
// If a separate UUID-based 'id' column is also present in DB, queries would need adjustment.

public class CodigoDescuentoDAOImplMySQL implements CodigoDescuentoDAO {

    // Table and column names
    private static final String TABLE_CODIGO_DESCUENTO = "codigo_descuento";
    // Assuming 'codigo_str' is the primary key string that users enter.
    // If there's also an internal auto-increment or UUID 'id', that needs to be added.
    // For now, 'codigo_str' is the main identifier from the model.
    private static final String COLUMN_CODIGO_STR = "codigo_str"; // The literal code string
    private static final String COLUMN_PORCENTAJE_DESCUENTO = "porcentaje_descuento";
    private static final String COLUMN_FECHA_EXPIRACION = "fecha_expiracion";
    private static final String COLUMN_USOS_MAXIMOS = "usos_maximos";
    private static final String COLUMN_USOS_ACTUALES = "usos_actuales";
    private static final String COLUMN_ACTIVO = "activo";
    private static final String COLUMN_PROMOCION_ASOCIADA_ID = "promocion_asociada_id"; // FK to Promocion table

    private PromocionDAO promocionDAO; // For fetching PromocionAsociada

    public CodigoDescuentoDAOImplMySQL(PromocionDAO promocionDAO) {
        this.promocionDAO = promocionDAO;
    }
    public CodigoDescuentoDAOImplMySQL() {
        // Default constructor, promocionDAO to be set via setter
    }
    public void setPromocionDAO(PromocionDAO promocionDAO) {
        this.promocionDAO = promocionDAO;
    }


    // SQL Queries
    // Using 'codigo_str' as the key from model.
    private static final String INSERT_CODIGO_DESCUENTO = "INSERT INTO " + TABLE_CODIGO_DESCUENTO +
            "(" + COLUMN_CODIGO_STR + ", " + COLUMN_PORCENTAJE_DESCUENTO + ", " + COLUMN_FECHA_EXPIRACION + ", " +
            COLUMN_USOS_MAXIMOS + ", " + COLUMN_USOS_ACTUALES + ", " + COLUMN_ACTIVO + ", " + COLUMN_PROMOCION_ASOCIADA_ID +
            ") VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_CODIGO_STR = "SELECT * FROM " + TABLE_CODIGO_DESCUENTO + " WHERE " + COLUMN_CODIGO_STR + " = ?";
    private static final String SELECT_ALL = "SELECT * FROM " + TABLE_CODIGO_DESCUENTO;
    private static final String SELECT_ALL_ACTIVOS = "SELECT * FROM " + TABLE_CODIGO_DESCUENTO +
            " WHERE " + COLUMN_ACTIVO + " = TRUE AND (" + COLUMN_FECHA_EXPIRACION + " IS NULL OR " + COLUMN_FECHA_EXPIRACION + " >= ?) AND " +
            COLUMN_USOS_ACTUALES + " < " + COLUMN_USOS_MAXIMOS;

    private static final String UPDATE_CODIGO_DESCUENTO = "UPDATE " + TABLE_CODIGO_DESCUENTO + " SET " +
            COLUMN_PORCENTAJE_DESCUENTO + " = ?, " + COLUMN_FECHA_EXPIRACION + " = ?, " +
            COLUMN_USOS_MAXIMOS + " = ?, " + COLUMN_USOS_ACTUALES + " = ?, " + COLUMN_ACTIVO + " = ?, " +
            COLUMN_PROMOCION_ASOCIADA_ID + " = ? " +
            "WHERE " + COLUMN_CODIGO_STR + " = ?";

    private static final String DELETE_BY_CODIGO_STR = "DELETE FROM " + TABLE_CODIGO_DESCUENTO + " WHERE " + COLUMN_CODIGO_STR + " = ?";
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM " + TABLE_CODIGO_DESCUENTO;


    @Override
    public Optional<CodigoDescuento> findByCodigo(String codigo) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_CODIGO_STR)) {
            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToCodigoDescuento(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding CodigoDescuento by codigo_str: " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * The CodigoDescuento model uses the 'codigo' string as its main identifier.
     * If 'id' in the DAO interface refers to this string, then this method is equivalent to findByCodigo.
     * If 'id' refers to a separate internal DB ID (e.g., UUID or auto-increment), this method needs
     * a different query and the table needs an 'id' column.
     * For this implementation, assuming 'id' refers to the 'codigo' string.
     */
    @Override
    public Optional<CodigoDescuento> findById(String id) {
        return findByCodigo(id);
    }

    @Override
    public List<CodigoDescuento> findAll() {
        List<CodigoDescuento> codigos = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                codigos.add(mapRowToCodigoDescuento(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all CodigoDescuentos: " + e.getMessage());
        }
        return codigos;
    }

    @Override
    public List<CodigoDescuento> findAllActivos() {
        List<CodigoDescuento> codigos = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_ACTIVOS)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    codigos.add(mapRowToCodigoDescuento(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding active CodigoDescuentos: " + e.getMessage());
        }
        return codigos;
    }

    @Override
    public CodigoDescuento save(CodigoDescuento codigoDescuento) {
        // Assumes 'codigo' (the string) is the primary key and unique.
        // Insert or Update logic based on existence of 'codigoDescuento.getCodigo()'
        Optional<CodigoDescuento> existing = findByCodigo(codigoDescuento.getCodigo());
        boolean isNew = !existing.isPresent();

        String sql = isNew ? INSERT_CODIGO_DESCUENTO : UPDATE_CODIGO_DESCUENTO;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String promocionId = null;
            if (codigoDescuento.getPromocionAsociada() != null) {
                promocionId = codigoDescuento.getPromocionAsociada().getId();
            }

            if (isNew) {
                stmt.setString(1, codigoDescuento.getCodigo());
                stmt.setDouble(2, codigoDescuento.getPorcentajeDescuento());
                stmt.setTimestamp(3, codigoDescuento.getFechaExpiracion() != null ? Timestamp.valueOf(codigoDescuento.getFechaExpiracion()) : null);
                stmt.setInt(4, codigoDescuento.getUsosMaximos());
                stmt.setInt(5, codigoDescuento.getUsosActuales());
                stmt.setBoolean(6, codigoDescuento.isActivo());
                stmt.setString(7, promocionId);
            } else { // Update
                stmt.setDouble(1, codigoDescuento.getPorcentajeDescuento());
                stmt.setTimestamp(2, codigoDescuento.getFechaExpiracion() != null ? Timestamp.valueOf(codigoDescuento.getFechaExpiracion()) : null);
                stmt.setInt(3, codigoDescuento.getUsosMaximos());
                stmt.setInt(4, codigoDescuento.getUsosActuales());
                stmt.setBoolean(5, codigoDescuento.isActivo());
                stmt.setString(6, promocionId);
                stmt.setString(7, codigoDescuento.getCodigo()); // WHERE codigo_str = ?
            }
            stmt.executeUpdate();
            return codigoDescuento;
        } catch (SQLException e) {
            System.err.println("Error saving CodigoDescuento: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deleteById(String id) {
        // Assuming 'id' is the 'codigo' string.
        deleteByCodigo(id);
    }

    @Override
    public void deleteByCodigo(String codigo) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_CODIGO_STR)) {
            stmt.setString(1, codigo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting CodigoDescuento by codigo_str: " + e.getMessage());
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
            System.err.println("Error counting all CodigoDescuentos: " + e.getMessage());
        }
        return 0;
    }

    private CodigoDescuento mapRowToCodigoDescuento(ResultSet rs) throws SQLException {
        String codigoStr = rs.getString(COLUMN_CODIGO_STR);
        double porcentajeDescuento = rs.getDouble(COLUMN_PORCENTAJE_DESCUENTO);
        Timestamp fechaExpiracionTS = rs.getTimestamp(COLUMN_FECHA_EXPIRACION);
        LocalDateTime fechaExpiracion = (fechaExpiracionTS != null) ? fechaExpiracionTS.toLocalDateTime() : null;
        int usosMaximos = rs.getInt(COLUMN_USOS_MAXIMOS);
        int usosActuales = rs.getInt(COLUMN_USOS_ACTUALES);
        boolean activo = rs.getBoolean(COLUMN_ACTIVO);
        String promocionId = rs.getString(COLUMN_PROMOCION_ASOCIADA_ID);

        CodigoDescuento codigoDescuento = new CodigoDescuento(codigoStr, porcentajeDescuento, fechaExpiracion, usosMaximos);
        // Model constructor initializes usosActuales to 0 and activo to true.
        // We need to set them from DB values.
        try {
            // Reflection or dedicated setters would be cleaner.
            // For now, direct field manipulation or simple setters if they existed.
            // Let's assume CodigoDescuento needs setters for these for ORM.
            // Or, the constructor needs to be more flexible.
            // To avoid modifying model now, we'll set what we can.
            // A simple way for usosActuales:
            for(int i=0; i < usosActuales; i++) {
                codigoDescuento.incrementarUso(); // This also handles deactivating if max uses reached
            }
            // If 'activo' from DB is false, and incrementing uses didn't make it false, set it.
            if (!activo && codigoDescuento.isActivo()) {
                 codigoDescuento.setActivo(false);
            }
            // This way of setting usosActuales is not ideal as `incrementarUso` might have side effects.
            // A direct setter `setUsosActuales(int n)` would be better.
            // For now, the above is a workaround.

        } catch (Exception e) {
            // Should not happen with current model, but good practice if reflection were used.
            System.err.println("Error setting usosActuales/activo reflectively: " + e.getMessage());
        }
        // Correctly set 'activo' state from DB if it differs from calculated
        if (codigoDescuento.isActivo() != activo) {
            codigoDescuento.setActivo(activo);
        }


        if (promocionId != null && this.promocionDAO != null) {
            Optional<Promocion> promoOpt = this.promocionDAO.findById(promocionId);
            promoOpt.ifPresent(codigoDescuento::setPromocionAsociada);
            // If promoOpt is empty, promocionAsociada remains null, which is fine.
        } else if (promocionId != null && this.promocionDAO == null) {
            System.err.println("Warning: PromocionDAO not injected in CodigoDescuentoDAOImplMySQL. Cannot fetch PromocionAsociada for ID: " + promocionId);
        }


        return codigoDescuento;
    }
}
