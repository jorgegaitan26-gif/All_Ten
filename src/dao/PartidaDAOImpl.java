package dao;

import model.Partida;
import model.Jugador;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartidaDAOImpl implements IPartidaDAO {

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
        try (Connection conn = ConexionBD.obtenerConexion()) {
            insertarPartida(conn, partida, partida.getJugadorIdJugador());
        }
    }

    private void insertarPartida(Connection conn, Partida partida, int jugadorId) throws SQLException {
        String sql = "INSERT INTO Partida (tiempoTotal, Jugador_id_Jugador, operacionesRealizadas, fechaFinalizacion) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, partida.getTiempoTotal());
            pstmt.setInt(2, jugadorId);
            pstmt.setString(3, partida.getOperacionesRealizadas());
            if (partida.getFechaFinalizacion() == null) {
                throw new SQLException("La fecha de finalización de la partida es obligatoria.");
            }
            pstmt.setTimestamp(4, new Timestamp(partida.getFechaFinalizacion().getTime()));

            pstmt.executeUpdate();
        }
    }

    @Override
    public void registrarPartida(Jugador jugador, Partida partida) throws SQLException {
        try (Connection conn = ConexionBD.obtenerConexion()) {
            conn.setAutoCommit(false);
            try {
                int jugadorId = jugador.getIdJugador() > 0
                    ? jugador.getIdJugador()
                    : insertarJugador(conn, jugador);
                insertarPartida(conn, partida, jugadorId);
                conn.commit();
                jugador.setIdJugador(jugadorId);
                partida.setJugadorIdJugador(jugadorId);
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
        String sql = "SELECT id_Partida, tiempoTotal, Jugador_id_Jugador, "
            + "operacionesRealizadas, fechaFinalizacion FROM Partida "
            + "ORDER BY fechaFinalizacion DESC";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Partida p = new Partida(
                    rs.getInt("id_Partida"),
                    rs.getInt("tiempoTotal"),
                    rs.getInt("Jugador_id_Jugador"),
                    rs.getString("operacionesRealizadas"),
                    rs.getTimestamp("fechaFinalizacion")
                );
                lista.add(p);
            }
        }
        return lista;
    }
}