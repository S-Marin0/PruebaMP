package com.eventmaster.dao.impl.mysql;

import com.eventmaster.dao.LugarDAO;
import com.eventmaster.model.entity.Lugar;
// Import ComponenteLugar and Seccion if they need to be persisted/retrieved by this DAO specifically
// For now, assuming Lugar's subcomponentes are handled at service layer or are simple enough not to need separate DB mapping here
import com.eventmaster.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
// For tiposEventosAdmitidos and reservasPorFranja, if storing as JSON or serialized string
// import com.google.gson.Gson; // Example if using Gson for JSON
// import com.google.gson.reflect.TypeToken; // Example for list/map deserialization

public class LugarDAOImplMySQL implements LugarDAO {

    private static final String TABLE_LUGAR = "lugar";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NOMBRE = "nombre";
    private static final String COLUMN_DIRECCION = "direccion";
    // For complex fields like List<String> or Map<String, String>
    // Option 1: Separate tables (e.g., lugar_tipos_eventos, lugar_reservas) - Normalized, relational
    // Option 2: Store as JSON string or delimited string in a TEXT column - Denormalized, simpler for some cases
    private static final String COLUMN_TIPOS_EVENTOS_ADMITIDOS = "tipos_eventos_admitidos"; // Assuming TEXT storing JSON array
    private static final String COLUMN_RESERVAS_POR_FRANJA = "reservas_por_franja"; // Assuming TEXT storing JSON map

    // SQL Queries
    private static final String INSERT_LUGAR = "INSERT INTO " + TABLE_LUGAR +
            "(" + COLUMN_ID + ", " + COLUMN_NOMBRE + ", " + COLUMN_DIRECCION + ", " +
            COLUMN_TIPOS_EVENTOS_ADMITIDOS + ", " + COLUMN_RESERVAS_POR_FRANJA + ") VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID = "SELECT * FROM " + TABLE_LUGAR + " WHERE " + COLUMN_ID + " = ?";
    private static final String SELECT_ALL = "SELECT * FROM " + TABLE_LUGAR;
    private static final String UPDATE_LUGAR = "UPDATE " + TABLE_LUGAR + " SET " +
            COLUMN_NOMBRE + " = ?, " + COLUMN_DIRECCION + " = ?, " +
            COLUMN_TIPOS_EVENTOS_ADMITIDOS + " = ?, " + COLUMN_RESERVAS_POR_FRANJA + " = ? WHERE " + COLUMN_ID + " = ?";
    private static final String DELETE_BY_ID = "DELETE FROM " + TABLE_LUGAR + " WHERE " + COLUMN_ID + " = ?";
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM " + TABLE_LUGAR;
    private static final String SELECT_BY_NOMBRE_CONTAINING = "SELECT * FROM " + TABLE_LUGAR + " WHERE " + COLUMN_NOMBRE + " LIKE ?";
    private static final String SELECT_BY_DIRECCION_CONTAINING = "SELECT * FROM " + TABLE_LUGAR + " WHERE " + COLUMN_DIRECCION + " LIKE ?";

    // private final Gson gson = new Gson(); // If using Gson for JSON serialization

    @Override
    public Optional<Lugar> findById(String id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToLugar(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Lugar by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Lugar> findAll() {
        List<Lugar> lugares = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lugares.add(mapRowToLugar(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all Lugares: " + e.getMessage());
        }
        return lugares;
    }

    @Override
    public Lugar save(Lugar lugar) {
        boolean isNew = lugar.getId() == null || findById(lugar.getId()).isEmpty();
        if (isNew && lugar.getId() == null) {
            lugar.setId(UUID.randomUUID().toString());
        }

        // Serialize complex fields (tiposEventosAdmitidos, reservasPorFranja)
        // For simplicity, this example assumes they are stored as plain strings.
        // In a real scenario, use JSON or separate tables.
        // String tiposEventosJson = gson.toJson(lugar.getTiposEventosAdmitidos());
        // String reservasJson = gson.toJson(lugar.getReservasPorFranja());
        // For now, let's assume these fields are not persisted or handled differently.
        // This DAO will only handle basic fields until JSON/serialization strategy is confirmed.

        String sql = isNew ? INSERT_LUGAR : UPDATE_LUGAR;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (isNew) {
                stmt.setString(1, lugar.getId());
                stmt.setString(2, lugar.getNombre());
                stmt.setString(3, lugar.getDireccion());
                // Placeholder for complex fields - assuming schema might not have them yet or they are handled differently
                stmt.setString(4, null); // tiposEventosAdmitidos
                stmt.setString(5, null); // reservasPorFranja
            } else { // Update
                stmt.setString(1, lugar.getNombre());
                stmt.setString(2, lugar.getDireccion());
                stmt.setString(3, null); // tiposEventosAdmitidos
                stmt.setString(4, null); // reservasPorFranja
                stmt.setString(5, lugar.getId());
            }
            stmt.executeUpdate();
            return lugar;
        } catch (SQLException e) {
            System.err.println("Error saving Lugar: " + e.getMessage());
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
            System.err.println("Error deleting Lugar by ID: " + e.getMessage());
        }
    }

    @Override
    public void delete(Lugar lugar) {
        if (lugar != null && lugar.getId() != null) {
            deleteById(lugar.getId());
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
            System.err.println("Error counting Lugares: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public List<Lugar> findByNombreContaining(String nombre) {
        List<Lugar> lugares = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_NOMBRE_CONTAINING)) {
            stmt.setString(1, "%" + nombre + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lugares.add(mapRowToLugar(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Lugares by nombre: " + e.getMessage());
        }
        return lugares;
    }

    @Override
    public List<Lugar> findByDireccionContaining(String direccionFragmento) {
        List<Lugar> lugares = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_DIRECCION_CONTAINING)) {
            stmt.setString(1, "%" + direccionFragmento + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lugares.add(mapRowToLugar(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Lugares by direccion: " + e.getMessage());
        }
        return lugares;
    }

    private Lugar mapRowToLugar(ResultSet rs) throws SQLException {
        String id = rs.getString(COLUMN_ID);
        String nombre = rs.getString(COLUMN_NOMBRE);
        String direccion = rs.getString(COLUMN_DIRECCION);
        // String tiposEventosJson = rs.getString(COLUMN_TIPOS_EVENTOS_ADMITIDOS);
        // String reservasJson = rs.getString(COLUMN_RESERVAS_POR_FRANJA);

        Lugar lugar = new Lugar(id, nombre, direccion);

        // Deserialize complex fields if they were stored (e.g. as JSON)
        // Type listType = new TypeToken<ArrayList<String>>() {}.getType();
        // List<String> tiposEventos = gson.fromJson(tiposEventosJson, listType);
        // if (tiposEventos != null) {
        //     tiposEventos.forEach(lugar::addTipoEventoAdmitido);
        // }

        // Type mapType = new TypeToken<HashMap<String, String>>() {}.getType();
        // Map<String, String> reservas = gson.fromJson(reservasJson, mapType);
        // if (reservas != null) {
        //     // Assuming Lugar has a setter for the whole map or individual additions
        //     // lugar.setReservasPorFranja(reservas); // If a setter exists
        //     reservas.forEach((key, value) -> {
        //         // Need to parse key "YYYY-MM-DD-FRANJA" back to components for lugar.reservarFranja
        //         // This part is complex and depends on how reservations are managed internally
        //     });
        // }

        // Subcomponentes (Composite pattern) are not directly mapped here.
        // This would require a more complex setup, possibly another table for parent-child relationships
        // if subcomponentes are also Lugar instances or different types stored in their own tables.
        // For now, subcomponentes list will be empty when loaded from DB by this basic DAO.

        return lugar;
    }
}
