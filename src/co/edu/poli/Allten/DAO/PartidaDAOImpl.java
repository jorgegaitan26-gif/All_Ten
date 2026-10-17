package co.edu.poli.Allten.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import co.edu.poli.Allten.Modelo.Jugador;
import co.edu.poli.Allten.Modelo.Partida;

/** Implementacion MySQL del registro de jugadores y partidas. */
public class PartidaDAOImpl implements IPartidaDAO {

    @Override
    public Jugador buscarPorNickname(String nickname) throws SQLException {
        String sql = "SELECT id_Jugador, nickname FROM Jugador WHERE nickname = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, nickname);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (!resultado.next()) {
                    return null;
                }
                return new Jugador(resultado.getInt("id_Jugador"), resultado.getString("nickname"));
            }
        }
    }

    @Override
    public Integer registrarJugador(String nickname) throws SQLException {
        String sql = "INSERT INTO Jugador (nickname) VALUES (?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement insercion = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            insercion.setString(1, nickname);
            insercion.executeUpdate();
            try (ResultSet claves = insercion.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("No se pudo obtener el ID del jugador registrado.");
                }
                return claves.getInt(1);
            }
        }
    }

    @Override
    public void guardarPartida(Partida partida, int idJugador) throws SQLException {
        if (partida.getFechaFinalizacion() == null) {
            throw new SQLException("No se puede guardar una partida que todavía no ha finalizado.");
        }

        String sql = "INSERT INTO Partida "
                + "(tiempoTotal, Jugador_id_Jugador, operacionesRealizadas, fechaFinalizacion) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement insercion = conexion.prepareStatement(sql)) {
            insercion.setLong(1, Math.round(partida.getTiempoTotalSegundos()));
            insercion.setInt(2, idJugador);
            insercion.setString(3, String.join("\n", partida.getOperacionesRealizadas()));
            insercion.setTimestamp(4, Timestamp.valueOf(partida.getFechaFinalizacion()));
            insercion.executeUpdate();
        }
    }

    @Override
    public List<String> obtenerHistorialPartidas() throws SQLException {
        List<String> historial = new ArrayList<>();
        String sql = "SELECT p.id_Partida, p.tiempoTotal, p.Jugador_id_Jugador, "
                + "j.nickname, p.operacionesRealizadas, p.fechaFinalizacion "
                + "FROM Partida p JOIN Jugador j ON j.id_Jugador = p.Jugador_id_Jugador "
                + "ORDER BY p.fechaFinalizacion DESC";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) {
                historial.add("Partida #" + resultado.getInt("id_Partida")
                        + " | Jugador: " + resultado.getString("nickname")
                        + " | Tiempo: " + resultado.getLong("tiempoTotal") + " s"
                        + " | Fecha: " + resultado.getTimestamp("fechaFinalizacion")
                        + "\nOperaciones: " + resultado.getString("operacionesRealizadas"));
            }
        }
        return historial;
    }
}
