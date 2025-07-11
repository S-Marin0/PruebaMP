package com.eventmaster.dao.impl.mysql;

import com.eventmaster.dao.EntradaDAO;
import com.eventmaster.dao.EventoDAO; // To fetch Evento for EventoAsociado
import com.eventmaster.model.entity.Evento; // For EventoAsociado
import com.eventmaster.model.pattern.factory.Entrada;
// A concrete implementation of Entrada will be needed for instantiation
import com.eventmaster.model.pattern.factory.impl.EntradaImpl; // Assuming a generic implementation
import com.eventmaster.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EntradaDAOImplMySQL implements EntradaDAO {

    // Table and column names for sold entries
    private static final String TABLE_ENTRADA_VENDIDA = "entrada_vendida";
    private static final String COLUMN_ID = "id"; // Unique ID for this sold entry instance
    private static final String COLUMN_EVENTO_ID = "evento_id"; // FK to Evento
    private static final String COLUMN_COMPRA_ID = "compra_id"; // FK to Compra
    private static final String COLUMN_TIPO_ENTRADA_NOMBRE = "tipo_entrada_nombre"; // e.g., "General", "VIP"
    private static final String COLUMN_PRECIO_FINAL = "precio_final"; // Price at which it was sold
    private static final String COLUMN_DESCRIPCION_FINAL = "descripcion_final"; // Description at time of sale (could include decorator info)
    // We might also need a reference to the TipoEntrada definition ID if applicable:
    // private static final String COLUMN_TIPO_ENTRADA_DEF_ID = "tipo_entrada_definicion_id";


    // SQL Queries
    private static final String INSERT_ENTRADA = "INSERT INTO " + TABLE_ENTRADA_VENDIDA +
            "(" + COLUMN_ID + ", " + COLUMN_EVENTO_ID + ", " + COLUMN_COMPRA_ID + ", " + COLUMN_TIPO_ENTRADA_NOMBRE + ", " +
            COLUMN_PRECIO_FINAL + ", " + COLUMN_DESCRIPCION_FINAL + ") VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID = "SELECT * FROM " + TABLE_ENTRADA_VENDIDA + " WHERE " + COLUMN_ID + " = ?";
    private static final String SELECT_ALL_BY_COMPRA_ID = "SELECT * FROM " + TABLE_ENTRADA_VENDIDA + " WHERE " + COLUMN_COMPRA_ID + " = ?";
    private static final String SELECT_ALL_BY_EVENTO_ID = "SELECT * FROM " + TABLE_ENTRADA_VENDIDA + " WHERE " + COLUMN_EVENTO_ID + " = ?";
    private static final String SELECT_ALL_BY_EVENTO_ID_AND_TIPO = "SELECT * FROM " + TABLE_ENTRADA_VENDIDA +
            " WHERE " + COLUMN_EVENTO_ID + " = ? AND " + COLUMN_TIPO_ENTRADA_NOMBRE + " = ?";
    private static final String DELETE_BY_ID = "DELETE FROM " + TABLE_ENTRADA_VENDIDA + " WHERE " + COLUMN_ID + " = ?";
    private static final String DELETE_ALL_BY_COMPRA_ID = "DELETE FROM " + TABLE_ENTRADA_VENDIDA + " WHERE " + COLUMN_COMPRA_ID + " = ?";
    private static final String COUNT_BY_EVENTO_ID = "SELECT COUNT(*) FROM " + TABLE_ENTRADA_VENDIDA + " WHERE " + COLUMN_EVENTO_ID + " = ?";
    private static final String COUNT_BY_EVENTO_ID_AND_TIPO = "SELECT COUNT(*) FROM " + TABLE_ENTRADA_VENDIDA +
            " WHERE " + COLUMN_EVENTO_ID + " = ? AND " + COLUMN_TIPO_ENTRADA_NOMBRE + " = ?";

    private EventoDAO eventoDAO; // Required to fetch Evento for Entrada.getEventoAsociado()

    // Constructor for dependency injection
    public EntradaDAOImplMySQL(EventoDAO eventoDAO) {
        this.eventoDAO = eventoDAO;
    }
     public EntradaDAOImplMySQL() {
        // This constructor might be used by AppContextListener if EventoDAO is set via setter.
        // Ensure EventoDAO is set before use.
    }

    public void setEventoDAO(EventoDAO eventoDAO) {
        this.eventoDAO = eventoDAO;
    }


    @Override
    public Entrada save(Entrada entrada) {
        if (entrada.getId() == null) {
            // If Entrada is an interface, its implementations must provide a way to set ID,
            // or we assume UUID is generated here and not set back if interface is immutable.
            // For EntradaImpl, we might need a setId method.
            // For now, assume ID is generated if not present, but not set back on the passed 'entrada' object
            // if it's purely an interface. This is problematic.
            // A better approach: ensure 'entrada' has its ID before calling save, or 'save' returns a new instance.
            // Let's assume the 'entrada' object should have its ID set by the service layer before calling DAO.
            if (entrada.getId() == null) {
                 // This path is risky if the caller expects the ID to be on the original object.
                 // Let's assume `EntradaImpl` (or whatever concrete class) allows ID to be set,
                 // or the service layer pre-assigns IDs.
                 // For this example, we'll proceed as if ID is managed by the caller or is on EntradaImpl.
                 // If entrada.getId() is null, it implies a new UUID should be used for the DB.
                 // The `entrada` object itself might not get this new UUID if it's an immutable interface implementation.
                 System.err.println("Warning: Entrada passed to save() has null ID. A new ID will be used for DB, but may not be reflected in the passed object instance if it's immutable.");
            }
        }
        String entradaId = (entrada.getId() != null) ? entrada.getId() : UUID.randomUUID().toString();
        String eventoId = (entrada.getEventoAsociado() != null) ? entrada.getEventoAsociado().getId() : null;

        // compraId needs to be on the Entrada object, or passed differently.
        // The Entrada interface doesn't have getCompraId().
        // We get it from EntradaImpl if the object is an instance of it.
        String compraId = null;
        if (entrada instanceof EntradaImpl) {
            compraId = ((EntradaImpl) entrada).getCompraId();
        }
        if (compraId == null) {
            // System.err.println("Warning: Compra ID is null for Entrada ID: " + entrada.getId() + ". This might be an issue depending on schema (compra_id nullable or not).");
            // Allow null compraId to proceed; DB constraint will catch if it's an issue.
        }


        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_ENTRADA)) {
            stmt.setString(1, entradaId);
            stmt.setString(2, eventoId);
            stmt.setString(3, compraId); // This needs to be correctly sourced
            stmt.setString(4, entrada.getTipo());
            stmt.setDouble(5, entrada.getPrecio());
            stmt.setString(6, entrada.getDescripcion());

            stmt.executeUpdate();
            // If ID was generated here, the original 'entrada' object is not updated.
            // The caller must be aware or use the returned object if it were a new instance with ID.
            // Since Entrada is an interface, we return the same instance.
            return entrada;
        } catch (SQLException e) {
            System.err.println("Error saving Entrada: " + e.getMessage());
            e.printStackTrace();
            return null; // Or throw custom exception
        }
    }

    @Override
    public List<Entrada> saveAll(List<Entrada> entradas) {
        // Using a batch update could be more efficient if supported and needed.
        // For now, iterate and save individually.
        List<Entrada> savedEntradas = new ArrayList<>();
        for (Entrada entrada : entradas) {
            Entrada saved = save(entrada);
            if (saved != null) {
                savedEntradas.add(saved);
            } else {
                // Handle error for individual save: maybe collect errors or stop.
                System.err.println("Failed to save one of the entradas in batch for evento: " + (entrada.getEventoAsociado() != null ? entrada.getEventoAsociado().getId() : "N/A"));
            }
        }
        return savedEntradas;
    }

    @Override
    public Optional<Entrada> findById(String idEntrada) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setString(1, idEntrada);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToEntrada(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Entrada by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Entrada> findAllByCompraId(String compraId) {
        List<Entrada> entradas = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_BY_COMPRA_ID)) {
            stmt.setString(1, compraId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entradas.add(mapRowToEntrada(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Entradas by CompraID: " + e.getMessage());
        }
        return entradas;
    }

    @Override
    public List<Entrada> findAllByEventoId(String eventoId) {
        List<Entrada> entradas = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_BY_EVENTO_ID)) {
            stmt.setString(1, eventoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entradas.add(mapRowToEntrada(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Entradas by EventoID: " + e.getMessage());
        }
        return entradas;
    }

    @Override
    public List<Entrada> findAllByEventoIdAndTipoEntrada(String eventoId, String nombreTipoEntrada) {
        List<Entrada> entradas = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_BY_EVENTO_ID_AND_TIPO)) {
            stmt.setString(1, eventoId);
            stmt.setString(2, nombreTipoEntrada);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entradas.add(mapRowToEntrada(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Entradas by EventoID and Tipo: " + e.getMessage());
        }
        return entradas;
    }

    @Override
    public void deleteById(String idEntrada) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_BY_ID)) {
            stmt.setString(1, idEntrada);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting Entrada by ID: " + e.getMessage());
        }
    }

    @Override
    public void deleteAllByCompraId(String compraId) {
         try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_ALL_BY_COMPRA_ID)) {
            stmt.setString(1, compraId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting Entradas by CompraID: " + e.getMessage());
        }
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
            System.err.println("Error counting Entradas by EventoID: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public long countByEventoIdAndTipoEntrada(String eventoId, String nombreTipoEntrada) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_BY_EVENTO_ID_AND_TIPO)) {
            stmt.setString(1, eventoId);
            stmt.setString(2, nombreTipoEntrada);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting Entradas by EventoID and Tipo: " + e.getMessage());
        }
        return 0;
    }

    private Entrada mapRowToEntrada(ResultSet rs) throws SQLException {
        String id = rs.getString(COLUMN_ID);
        String eventoId = rs.getString(COLUMN_EVENTO_ID);
        // String compraId = rs.getString(COLUMN_COMPRA_ID); // Needed if EntradaImpl stores it
        String tipoEntradaNombre = rs.getString(COLUMN_TIPO_ENTRADA_NOMBRE);
        double precioFinal = rs.getDouble(COLUMN_PRECIO_FINAL);
        String descripcionFinal = rs.getString(COLUMN_DESCRIPCION_FINAL);

        if (this.eventoDAO == null) {
            throw new IllegalStateException("EventoDAO not initialized in EntradaDAOImplMySQL. Cannot fetch Evento for Entrada.");
        }

        Evento eventoAsociado = this.eventoDAO.findById(eventoId)
                .orElseThrow(() -> new SQLException("Evento not found for ID: " + eventoId + " while mapping Entrada ID: " + id));

        // We need a concrete class that implements Entrada.
        // Using EntradaImpl.
        String compraId = rs.getString(COLUMN_COMPRA_ID); // Retrieve compra_id from ResultSet

        // Use the constructor of EntradaImpl that accepts compraId
        EntradaImpl entrada = new EntradaImpl(id, eventoAsociado, tipoEntradaNombre, precioFinal, descripcionFinal, compraId);

        return entrada;
    }
}
