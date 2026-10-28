package co.edu.poli.Allten.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import co.edu.poli.Allten.Modelo.Jugador;

/** Implementación MySQL de las operaciones CRUD de jugadores. */
public class DAOJugador implements CRUD<Jugador>, Jugador.RegistroJugadores<SQLException> {

    @Override
    public Jugador create(Jugador jugador) throws SQLException {
        validar(jugador);
        String nickname = jugador.getNickname().trim();
        String sql = "INSERT INTO Jugador (nickname) VALUES (?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement insercion = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            insercion.setString(1, nickname);
            insercion.executeUpdate();
            try (ResultSet claves = insercion.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("No se pudo obtener el ID del jugador registrado.");
                }
                jugador.setNickname(nickname);
                jugador.setIdJugador(claves.getInt(1));
                return jugador;
            }
        }
    }

    @Override
    public Jugador readone(int id) throws SQLException {
        validarId(id);
        String sql = "SELECT id_Jugador, nickname FROM Jugador WHERE id_Jugador = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    public Jugador readByNickname(String nickname) throws SQLException {
        if (nickname == null || nickname.isBlank()) {
            throw new IllegalArgumentException("El nickname no puede estar vacío.");
        }
        String sql = "SELECT id_Jugador, nickname FROM Jugador WHERE nickname = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, nickname.trim());
            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    @Override
    public Jugador buscarPorNickname(String nickname) throws SQLException {
        return readByNickname(nickname);
    }

    @Override
    public Integer registrarJugador(String nickname) throws SQLException {
        Jugador jugador = create(new Jugador(nickname));
        return jugador.getIdJugador();
    }

    @Override
    public List<Jugador> readall() throws SQLException {
        List<Jugador> jugadores = new ArrayList<>();
        String sql = "SELECT id_Jugador, nickname FROM Jugador ORDER BY id_Jugador";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) {
                jugadores.add(mapear(resultado));
            }
        }
        return jugadores;
    }

    @Override
    public boolean update(Jugador jugador) throws SQLException {
        validar(jugador);
        Integer id = jugador.getIdJugador();
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("El jugador debe tener un identificador válido.");
        }

        String sql = "UPDATE Jugador SET nickname = ? WHERE id_Jugador = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement actualizacion = conexion.prepareStatement(sql)) {
            actualizacion.setString(1, jugador.getNickname().trim());
            actualizacion.setInt(2, id);
            return actualizacion.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        validarId(id);
        String sql = "DELETE FROM Jugador WHERE id_Jugador = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement eliminacion = conexion.prepareStatement(sql)) {
            eliminacion.setInt(1, id);
            return eliminacion.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                throw new SQLException("No se puede eliminar el jugador porque tiene partidas registradas.", e);
            }
            throw e;
        }
    }

    private static void validar(Jugador jugador) {
        if (jugador == null || jugador.getNickname() == null || jugador.getNickname().isBlank()) {
            throw new IllegalArgumentException("El jugador debe tener un nickname.");
        }
        if (jugador.getNickname().trim().length() > Jugador.LONGITUD_MAXIMA_NICKNAME) {
            throw new IllegalArgumentException("El nickname admite máximo "
                    + Jugador.LONGITUD_MAXIMA_NICKNAME + " caracteres.");
        }
    }

    private static void validarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El identificador debe ser mayor que cero.");
        }
    }

    private static Jugador mapear(ResultSet resultado) throws SQLException {
        return new Jugador(resultado.getInt("id_Jugador"), resultado.getString("nickname"));
    }
}
