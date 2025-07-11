package com.eventmaster.dao.impl.mysql;

import com.eventmaster.dao.EventoDAO;
import com.eventmaster.dao.LugarDAO; // For fetching Lugar
import com.eventmaster.dao.UsuarioDAO; // For fetching Organizador
import com.eventmaster.model.entity.Evento;
import com.eventmaster.model.entity.Lugar;
import com.eventmaster.model.entity.Organizador;
import com.eventmaster.model.entity.Usuario; // Required for casting
import com.eventmaster.model.pattern.state.*; // For estadoActual
import com.eventmaster.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EventoDAOImplMySQL implements EventoDAO {

    // Table and column names
    private static final String TABLE_EVENTO = "evento";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NOMBRE = "nombre";
    private static final String COLUMN_DESCRIPCION = "descripcion";
    private static final String COLUMN_CATEGORIA = "categoria";
    private static final String COLUMN_FECHA_HORA = "fecha_hora";
    private static final String COLUMN_LUGAR_ID = "lugar_id";
    private static final String COLUMN_ORGANIZADOR_ID = "organizador_id";
    private static final String COLUMN_CAPACIDAD_TOTAL = "capacidad_total";
    private static final String COLUMN_ENTRADAS_VENDIDAS = "entradas_vendidas";
    private static final String COLUMN_ESTADO_ACTUAL = "estado_actual"; // e.g., "BORRADOR", "PUBLICADO", "CANCELADO"

    // DAOs for related entities - these should be injected or available
    // For simplicity, we'll assume they can be instantiated if needed, or better, injected via constructor
    private LugarDAO lugarDAO;
    private UsuarioDAO usuarioDAO; // To fetch Organizador details

    // Constructor for dependency injection (preferred)
    public EventoDAOImplMySQL(LugarDAO lugarDAO, UsuarioDAO usuarioDAO) {
        this.lugarDAO = lugarDAO;
        this.usuarioDAO = usuarioDAO;
    }

    // Default constructor (less ideal, requires DAOs to be new-ed up or available statically)
    public EventoDAOImplMySQL() {
        // This is not ideal for testability and flexibility.
        // Consider how these dependencies will be provided in AppContextListener
        // For now, let's assume they might be new-ed up if necessary, but this needs refinement.
        // this.lugarDAO = new LugarDAOImplMySQL(); // Example, if such class exists
        // this.usuarioDAO = new UsuarioDAOImplMySQL(); // Example
        // This creates a circular dependency if EventoDAO is needed by LugarDAO/UsuarioDAO constructor.
        // Best to inject them. For now, I'll leave them potentially null and handle it in methods,
        // or assume they will be set by AppContextListener after construction.
        // For the purpose of this example, I will assume they are set via AppContextListener.
    }

    // Setter methods for DAOs if not using constructor injection
    public void setLugarDAO(LugarDAO lugarDAO) {
        this.lugarDAO = lugarDAO;
    }

    public void setUsuarioDAO(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }


    private static final String INSERT_EVENTO = "INSERT INTO " + TABLE_EVENTO +
            "(" + COLUMN_ID + ", " + COLUMN_NOMBRE + ", " + COLUMN_DESCRIPCION + ", " + COLUMN_CATEGORIA + ", " +
            COLUMN_FECHA_HORA + ", " + COLUMN_LUGAR_ID + ", " + COLUMN_ORGANIZADOR_ID + ", " + COLUMN_CAPACIDAD_TOTAL + ", " +
            COLUMN_ENTRADAS_VENDIDAS + ", " + COLUMN_ESTADO_ACTUAL + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_ID = "SELECT * FROM " + TABLE_EVENTO + " WHERE " + COLUMN_ID + " = ?";
    private static final String SELECT_ALL = "SELECT * FROM " + TABLE_EVENTO;
    private static final String UPDATE_EVENTO = "UPDATE " + TABLE_EVENTO + " SET " +
            COLUMN_NOMBRE + " = ?, " + COLUMN_DESCRIPCION + " = ?, " + COLUMN_CATEGORIA + " = ?, " +
            COLUMN_FECHA_HORA + " = ?, " + COLUMN_LUGAR_ID + " = ?, " + COLUMN_ORGANIZADOR_ID + " = ?, " +
            COLUMN_CAPACIDAD_TOTAL + " = ?, " + COLUMN_ENTRADAS_VENDIDAS + " = ?, " + COLUMN_ESTADO_ACTUAL + " = ? " +
            "WHERE " + COLUMN_ID + " = ?";
    private static final String DELETE_BY_ID = "DELETE FROM " + TABLE_EVENTO + " WHERE " + COLUMN_ID + " = ?";
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM " + TABLE_EVENTO;
    private static final String SELECT_BY_NOMBRE_CONTAINING = "SELECT * FROM " + TABLE_EVENTO + " WHERE " + COLUMN_NOMBRE + " LIKE ?";
    private static final String SELECT_BY_CATEGORIA = "SELECT * FROM " + TABLE_EVENTO + " WHERE " + COLUMN_CATEGORIA + " = ?";
    private static final String SELECT_BY_FECHA_HORA_BETWEEN = "SELECT * FROM " + TABLE_EVENTO + " WHERE " + COLUMN_FECHA_HORA + " BETWEEN ? AND ?";
    private static final String SELECT_BY_LUGAR_ID = "SELECT * FROM " + TABLE_EVENTO + " WHERE " + COLUMN_LUGAR_ID + " = ?";
    private static final String SELECT_BY_ORGANIZADOR_ID = "SELECT * FROM " + TABLE_EVENTO + " WHERE " + COLUMN_ORGANIZADOR_ID + " = ?";
    private static final String SELECT_PUBLICADOS = "SELECT * FROM " + TABLE_EVENTO + " WHERE " + COLUMN_ESTADO_ACTUAL + " = 'PUBLICADO'"; // Assuming 'PUBLICADO' is the string representation
    private static final String SELECT_FUTUROS = "SELECT * FROM " + TABLE_EVENTO + " WHERE " + COLUMN_FECHA_HORA + " > ?";


    @Override
    public Optional<Evento> findById(String id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToEvento(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Evento by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Evento> findAll() {
        List<Evento> eventos = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                eventos.add(mapRowToEvento(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all Eventos: " + e.getMessage());
        }
        return eventos;
    }

    @Override
    public Evento save(Evento evento) {
        if (evento.getLugar() == null || evento.getLugar().getId() == null) {
            System.err.println("Error saving Evento: Lugar or Lugar ID is null.");
            // Or throw IllegalArgumentException
            return null;
        }
        if (evento.getOrganizador() == null || evento.getOrganizador().getId() == null) {
            System.err.println("Error saving Evento: Organizador or Organizador ID is null.");
            // Or throw IllegalArgumentException
            return null;
        }

        boolean isNew = evento.getId() == null || findById(evento.getId()).isEmpty();

        if (isNew && evento.getId() == null) {
            evento.setId(UUID.randomUUID().toString());
        }

        String sql = isNew ? INSERT_EVENTO : UPDATE_EVENTO;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, evento.getNombre());
            stmt.setString(2, evento.getDescripcion());
            stmt.setString(3, evento.getCategoria());
            stmt.setTimestamp(4, Timestamp.valueOf(evento.getFechaHora()));
            stmt.setString(5, evento.getLugar().getId());
            stmt.setString(6, evento.getOrganizador().getId());
            stmt.setInt(7, evento.getCapacidadTotal());
            stmt.setInt(8, evento.getEntradasVendidas());
            stmt.setString(9, evento.getEstadoActual().getClass().getSimpleName().toUpperCase()); // e.g. "ESTADOBORRADOR" -> "BORRADOR"

            if (isNew) {
                stmt.setString(1, evento.getId()); // For INSERT, ID is the first param after column list
                stmt.setString(2, evento.getNombre());
                stmt.setString(3, evento.getDescripcion());
                stmt.setString(4, evento.getCategoria());
                stmt.setTimestamp(5, Timestamp.valueOf(evento.getFechaHora()));
                stmt.setString(6, evento.getLugar().getId());
                stmt.setString(7, evento.getOrganizador().getId());
                stmt.setInt(8, evento.getCapacidadTotal());
                stmt.setInt(9, evento.getEntradasVendidas());
                stmt.setString(10, getEstadoString(evento.getEstadoActual()));
            } else { // UPDATE
                stmt.setString(1, evento.getNombre());
                stmt.setString(2, evento.getDescripcion());
                stmt.setString(3, evento.getCategoria());
                stmt.setTimestamp(4, Timestamp.valueOf(evento.getFechaHora()));
                stmt.setString(5, evento.getLugar().getId());
                stmt.setString(6, evento.getOrganizador().getId());
                stmt.setInt(7, evento.getCapacidadTotal());
                stmt.setInt(8, evento.getEntradasVendidas());
                stmt.setString(9, getEstadoString(evento.getEstadoActual()));
                stmt.setString(10, evento.getId()); // WHERE id = ?
            }

            stmt.executeUpdate();
            return evento;
        } catch (SQLException e) {
            System.err.println("Error saving Evento: " + e.getMessage());
            e.printStackTrace(); // For more details during development
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
            System.err.println("Error deleting Evento by ID: " + e.getMessage());
        }
    }

    @Override
    public void delete(Evento evento) {
        if (evento != null && evento.getId() != null) {
            deleteById(evento.getId());
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
            System.err.println("Error counting Eventos: " + e.getMessage());
        }
        return 0;
    }

    @Override
    public List<Evento> findByNombreContaining(String nombre) {
        List<Evento> eventos = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_NOMBRE_CONTAINING)) {
            stmt.setString(1, "%" + nombre + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    eventos.add(mapRowToEvento(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Eventos by nombre: " + e.getMessage());
        }
        return eventos;
    }

    @Override
    public List<Evento> findByCategoria(String categoria) {
         List<Evento> eventos = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_CATEGORIA)) {
            stmt.setString(1, categoria);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    eventos.add(mapRowToEvento(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Eventos by categoria: " + e.getMessage());
        }
        return eventos;
    }

    @Override
    public List<Evento> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin) {
        List<Evento> eventos = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_FECHA_HORA_BETWEEN)) {
            stmt.setTimestamp(1, Timestamp.valueOf(inicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fin));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    eventos.add(mapRowToEvento(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Eventos by fecha/hora: " + e.getMessage());
        }
        return eventos;
    }

    @Override
    public List<Evento> findByLugar(Lugar lugar) {
        if (lugar == null || lugar.getId() == null) return new ArrayList<>();
        List<Evento> eventos = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_LUGAR_ID)) {
            stmt.setString(1, lugar.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    eventos.add(mapRowToEvento(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Eventos by Lugar ID: " + e.getMessage());
        }
        return eventos;
    }

    @Override
    public List<Evento> findByOrganizador(Organizador organizador) {
        if (organizador == null || organizador.getId() == null) return new ArrayList<>();
        List<Evento> eventos = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ORGANIZADOR_ID)) {
            stmt.setString(1, organizador.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    eventos.add(mapRowToEvento(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding Eventos by Organizador ID: " + e.getMessage());
        }
        return eventos;
    }

    @Override
    public List<Evento> findEventosPublicados() {
        List<Evento> eventos = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_PUBLICADOS);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                eventos.add(mapRowToEvento(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding publicados Eventos: " + e.getMessage());
        }
        return eventos;
    }

    @Override
    public List<Evento> findEventosFuturos() {
        List<Evento> eventos = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_FUTUROS)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    eventos.add(mapRowToEvento(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding futuros Eventos: " + e.getMessage());
        }
        return eventos;
    }

    private Evento mapRowToEvento(ResultSet rs) throws SQLException {
        String id = rs.getString(COLUMN_ID);
        String nombre = rs.getString(COLUMN_NOMBRE);
        String descripcion = rs.getString(COLUMN_DESCRIPCION);
        String categoria = rs.getString(COLUMN_CATEGORIA);
        LocalDateTime fechaHora = rs.getTimestamp(COLUMN_FECHA_HORA).toLocalDateTime();
        String lugarId = rs.getString(COLUMN_LUGAR_ID);
        String organizadorId = rs.getString(COLUMN_ORGANIZADOR_ID);
        int capacidadTotal = rs.getInt(COLUMN_CAPACIDAD_TOTAL);
        int entradasVendidas = rs.getInt(COLUMN_ENTRADAS_VENDIDAS);
        String estadoActualStr = rs.getString(COLUMN_ESTADO_ACTUAL);

        // Fetch Lugar and Organizador
        // This is a critical point: LugarDAO and UsuarioDAO must be available.
        if (this.lugarDAO == null || this.usuarioDAO == null) {
            // This indicates a configuration problem. For now, we might throw or return partial object.
            // Throwing an exception is cleaner to signal the issue.
            throw new IllegalStateException("LugarDAO or UsuarioDAO not initialized in EventoDAOImplMySQL");
        }

        Lugar lugar = lugarDAO.findById(lugarId)
                .orElseThrow(() -> new SQLException("Lugar not found for ID: " + lugarId + " for Evento ID: " + id));
        Organizador organizador = (Organizador) usuarioDAO.findById(organizadorId)
                .filter(user -> user instanceof Organizador) // Ensure it's an Organizador
                .orElseThrow(() -> new SQLException("Organizador not found for ID: " + organizadorId + " for Evento ID: " + id));


        // Use the EventoBuilder to construct the Evento object
        // This assumes EventoBuilder can be used this way or Evento has a suitable constructor/setters.
        // For simplicity, using setters on a new Evento object if builder is complex to use post-hoc.
        // The Evento constructor is private, expecting a builder.
        // We need to reconstruct the state of the builder or use setters if available.

        // Let's use the builder pattern as intended for construction.
        // However, the builder is for *new* Eventos. For mapping, we might need to adapt.
        // The Evento class uses a private constructor Evento(EventoBuilder builder).
        // We will set fields directly after construction via builder, or use a constructor if available.

        Evento.EventoBuilder builder = new Evento.EventoBuilder(nombre, organizador, lugar, fechaHora)
            .setId(id)
            .setDescripcion(descripcion)
            .setCategoria(categoria)
            .setCapacidadTotal(capacidadTotal);
            // Note: urlsImagenes, urlsVideos, tiposEntradaDisponibles are not in the 'evento' table directly.
            // They would typically be in related tables.

        Evento evento = builder.build(); // Build the basic event
        evento.setEntradasVendidas(entradasVendidas); // Set fields not covered by builder constructor or simple setters

        // Set state
        EstadoEvento estado;
        // The Evento constructor sets a default state if builder.getEstadoActual() is null.
        // We need to set the state loaded from DB.
        switch (estadoActualStr.toUpperCase()) {
            case "BORRADOR":
                estado = new EstadoBorrador(evento);
                break;
            case "PUBLICADO":
                estado = new EstadoPublicado(evento);
                break;
            case "CANCELADO":
                estado = new EstadoCancelado(evento);
                break;
            case "EN_CURSO": // Assuming "EN_CURSO" is a valid string for EstadoEnCurso
                estado = new EstadoEnCurso(evento);
                break;
            case "FINALIZADO":
                estado = new EstadoFinalizado(evento);
                break;
            default:
                throw new SQLException("Unknown evento estado in database: " + estadoActualStr);
        }
        evento.setEstadoActual(estado); // Set the state after construction

        // TODO: Load urlsImagenes, urlsVideos, tiposEntradaDisponibles from their respective tables
        // This would involve more DAOs and service logic. For now, they will be empty lists/maps.

        return evento;
    }
     private String getEstadoString(EstadoEvento estado) {
        if (estado instanceof EstadoBorrador) return "BORRADOR";
        if (estado instanceof EstadoPublicado) return "PUBLICADO";
        if (estado instanceof EstadoCancelado) return "CANCELADO";
        if (estado instanceof EstadoEnCurso) return "EN_CURSO";
        if (estado instanceof EstadoFinalizado) return "FINALIZADO";
        return "DESCONOCIDO"; // Should not happen
    }
}
