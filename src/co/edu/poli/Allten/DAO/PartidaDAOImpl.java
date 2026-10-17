package co.edu.poli.Allten.DAO;

import co.edu.poli.Allten.Modelo.Partida;
import co.edu.poli.Allten.Modelo.Jugador;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementacion MySQL de {@link IPartidaDAO}. Tambien implementa
 * {@link Jugador.RegistroJugadores} para el flujo
 * {@code Jugador.identificarJugador(nickname, dao)} (UC-01 y UC-02).
 */
public class PartidaDAOImpl implements IPartidaDAO, Jugador.RegistroJugadores {

    /** Separador con el que se guardan las operaciones realizadas en una sola columna de texto. */
    static final String SEPARADOR_OPERACIONES = " | ";

    @Override
    public void registrarJugador(Jugador jugador) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion()) {
            jugador.setIdJugador(insertarJugador(conn, jugador));
        }
    }

    private int insertarJugador(Connection conn, Jugador jugador) throws SQLException {
        String sql = "INSERT INTO Jugador (nickname) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, jugador.getNickname());
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("No se pudo obtener el ID del jugador registrado.");
                }
                return rs.getInt(1);
            }
        }
    }

    @Override
    public void registrarPartida(Partida partida) throws SQLException {
        Jugador jugador = partida.getJugador();
        if (jugador == null || jugador.getIdJugador() == null) {
            throw new SQLException("La partida debe tener un jugador ya registrado (con id_Jugador).");
        }
        try (Connection conn = ConexionBD.obtenerConexion()) {
            insertarPartida(conn, partida, jugador.getIdJugador());
        }
    }

    private void insertarPartida(Connection conn, Partida partida, int jugadorId) throws SQLException {
        String sql = "INSERT INTO Partida (tiempoTotal, Jugador_id_Jugador, operacionesRealizadas, fechaFinalizacion) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            // tiempoTotal se guarda en segundos enteros (columna INT)
            pstmt.setInt(1, (int) Math.round(partida.getTiempoTotalSegundos()));
            pstmt.setInt(2, jugadorId);
            pstmt.setString(3, String.join(SEPARADOR_OPERACIONES, partida.getOperacionesRealizadas()));
            if (partida.getFechaFinalizacion() == null) {
                throw new SQLException("La fecha de finalización de la partida es obligatoria.");
            }
            pstmt.setTimestamp(4, Timestamp.valueOf(partida.getFechaFinalizacion()));

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    partida.setIdPartida(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void registrarPartida(Jugador jugador, Partida partida) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);
            try {
                int jugadorId = jugador.getIdJugador() != null && jugador.getIdJugador() > 0
                    ? jugador.getIdJugador()
                    : insertarJugador(conn, jugador);
                insertarPartida(conn, partida, jugadorId);
                conn.commit();
                jugador.setIdJugador(jugadorId);
                partida.setJugador(jugador);
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }

    @Override
    public List<Partida> obtenerHistorialPartidas() throws SQLException {
        List<Partida> lista = new ArrayList<>();
        String sql = "SELECT p.id_Partida, p.tiempoTotal, p.Jugador_id_Jugador, "
            + "p.operacionesRealizadas, p.fechaFinalizacion, j.nickname FROM Partida p "
            + "LEFT JOIN Jugador j ON j.id_Jugador = p.Jugador_id_Jugador "
            + "ORDER BY p.fechaFinalizacion DESC, p.id_Partida DESC";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Jugador jugador = new Jugador(rs.getInt("Jugador_id_Jugador"), rs.getString("nickname"));
                Timestamp fecha = rs.getTimestamp("fechaFinalizacion");
                Partida p = new Partida(
                    rs.getInt("id_Partida"),
                    jugador,
                    rs.getInt("tiempoTotal"),
                    separarOperaciones(rs.getString("operacionesRealizadas")),
                    fecha == null ? null : fecha.toLocalDateTime()
                );
                lista.add(p);
            }
        }
        return lista;
    }

    private List<String> separarOperaciones(String texto) {
        if (texto == null || texto.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(texto.split(Pattern.quote(SEPARADOR_OPERACIONES))));
    }

    // =====================================================================
    // Jugador.RegistroJugadores (flujo Jugador.identificarJugador)
    // =====================================================================

    /** UC-01: {@code SELECT * FROM Jugador WHERE nickname = ?} */
    @Override
    public Jugador buscarPorNickname(String nickname) {
        String sql = "SELECT * FROM Jugador WHERE nickname = ?";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nickname);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new Jugador(rs.getInt("id_Jugador"), rs.getString("nickname"));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar el jugador en la base de datos.", e);
        }
    }

    /** UC-02: {@code INSERT INTO Jugador (nickname) VALUES (?)} */
    @Override
    public Integer registrarJugador(String nickname) {
        try (Connection conn = ConexionBD.obtenerConexion()) {
            return insertarJugador(conn, new Jugador(nickname));
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo registrar el jugador en la base de datos.", e);
        }
    }
}