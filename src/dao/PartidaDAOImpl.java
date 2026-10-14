package dao;

import model.Partida;
import model.Jugador;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartidaDAOImpl implements IPartidaDAO {

    @Override
    public void registrarJugador(Jugador jugador) {
        String sql = "INSERT INTO Jugador (nickname) VALUES (?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, jugador.getNickname());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                jugador.setIdJugador(rs.getInt(1));
            }
            System.out.println("Jugador registrado exitosamente en la BD.");
        } catch (SQLException e) {
            System.err.println("Error al registrar jugador: " + e.getMessage());
        }
    }

    @Override
    public void registrarPartida(Partida partida) {
        String sql = "INSERT INTO Partida (tiempoTotal, Jugador_id_Jugador, operacionesRealizadas, fechaFinalizacion) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionBD.obtenerConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, partida.getTiempoTotal());
            pstmt.setInt(2, partida.getJugadorIdJugador());
            pstmt.setString(3, partida.getOperacionesRealizadas());
            pstmt.setTimestamp(4, new Timestamp(partida.getFechaFinalizacion().getTime()));

            pstmt.executeUpdate();
            System.out.println("Partida registrada exitosamente en la BD.");
        } catch (SQLException e) {
            System.err.println("Error al registrar partida: " + e.getMessage());
        }
    }

    @Override
    public List<Partida> obtenerHistorialPartidas() {
        List<Partida> lista = new ArrayList<>();
        String sql = "SELECT * FROM Partida";
        try (Connection conn = ConexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

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
        } catch (SQLException e) {
            System.err.println("Error al obtener el historial: " + e.getMessage());
        }
        return lista;
    }
}