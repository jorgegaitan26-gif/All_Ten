package co.edu.poli.Allten.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import co.edu.poli.Allten.Modelo.Jugador;

/**
 * Gestiona la persistencia de jugadores en la tabla Jugador de MySQL.
 */
public class DAOJugador implements CRUD<Jugador> {

    /** Longitud máxima de nickname según la base de datos, VARCHAR(45). */
    private static final int MAX_NICKNAME = 45;

    /**
     * Registra un jugador nuevo. Al terminar, el objeto recibido queda con el
     * identificador generado por la base de datos.
     *
     * @param j jugador a registrar
     * @return mensaje con el resultado de la operación
     * @throws IllegalArgumentException si el jugador es nulo, no tiene nickname,
     *         el nickname supera 45 caracteres o ya está en uso
     * @throws Exception si ocurre un error durante el registro
     */
    @Override
    public String create(Jugador j) throws Exception {

        validar(j);

        String nickname = j.getNickname().trim();

        if (readByNickname(nickname) != null) {
            throw new IllegalArgumentException("Ya existe un jugador con el nickname \"" + nickname + "\".");
        }

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = "INSERT INTO Jugador (nickname) VALUES (?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nickname);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    j.setIdJugador(rs.getInt(1));
                }
            }
        }

        return "Jugador guardado con id " + j.getIdJugador();
    }

    /**
     * Consulta un jugador por su identificador.
     *
     * @param <K> tipo del identificador
     * @param id identificador del jugador
     * @return jugador encontrado o null si no existe
     * @throws Exception si ocurre un error durante la consulta
     */
    @Override
    public <K> Jugador readone(K id) throws Exception {

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = "SELECT id_Jugador, nickname FROM Jugador WHERE id_Jugador = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, aEntero(id));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Jugador(rs.getInt("id_Jugador"), rs.getString("nickname"));
                }
            }
        }

        return null;
    }

    /**
     * Consulta un jugador por su nickname.
     *
     * @param nickname nickname del jugador
     * @return jugador encontrado o null si no existe
     * @throws Exception si ocurre un error durante la consulta
     */
    public Jugador readByNickname(String nickname) throws Exception {

        if (nickname == null) {
            return null;
        }

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = "SELECT id_Jugador, nickname FROM Jugador WHERE nickname = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nickname.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Jugador(rs.getInt("id_Jugador"), rs.getString("nickname"));
                }
            }
        }

        return null;
    }

    /**
     * Consulta todos los jugadores registrados.
     *
     * @return lista de jugadores ordenada por identificador
     * @throws Exception si ocurre un error durante la consulta
     */
    @Override
    public List<Jugador> readall() throws Exception {

        List<Jugador> lista = new ArrayList<>();

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = "SELECT id_Jugador, nickname FROM Jugador ORDER BY id_Jugador";

        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Jugador(rs.getInt("id_Jugador"), rs.getString("nickname")));
            }
        }

        return lista;
    }

    /**
     * Actualiza el nickname de un jugador existente.
     *
     * @param j jugador con el identificador y el nickname nuevo
     * @return mensaje con el resultado de la operación
     * @throws IllegalArgumentException si el jugador es nulo, no tiene nickname,
     *         el nickname supera 45 caracteres o ya lo usa otro jugador
     * @throws Exception si ocurre un error durante la actualización
     */
    @Override
    public String update(Jugador j) throws Exception {

        validar(j);

        String nickname = j.getNickname().trim();

        Jugador otro = readByNickname(nickname);
        if (otro != null && !otro.getIdJugador().equals(j.getIdJugador())) {
            throw new IllegalArgumentException("Ya existe otro jugador con el nickname \"" + nickname + "\".");
        }

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = "UPDATE Jugador SET nickname = ? WHERE id_Jugador = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nickname);
            ps.setInt(2, j.getIdJugador());

            int filas = ps.executeUpdate();

            if (filas == 0) {
                return "No existe un jugador con id " + j.getIdJugador();
            }
        }

        return "Jugador actualizado con id " + j.getIdJugador();
    }

    /**
     * Elimina un jugador por su identificador. Si el jugador tiene partidas,
     * no se elimina (la clave foránea lo impide) y se informa en el mensaje
     * devuelto; hay que borrar primero sus partidas.
     *
     * @param <K> tipo del identificador
     * @param id identificador del jugador
     * @return mensaje con el resultado de la operación
     * @throws Exception si ocurre un error durante la eliminación
     */
    @Override
    public <K> String delete(K id) throws Exception {

        int idJugador = aEntero(id);

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = "DELETE FROM Jugador WHERE id_Jugador = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idJugador);

            int filas = ps.executeUpdate();

            if (filas == 0) {
                return "No existe un jugador con id " + idJugador;
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            return "No se puede eliminar el jugador " + idJugador
                    + " porque tiene partidas registradas. Elimina primero sus partidas.";
        }

        return "Jugador eliminado con id " + idJugador;
    }

    private void validar(Jugador j) {

        if (j == null || j.getNickname() == null || j.getNickname().isBlank()) {
            throw new IllegalArgumentException("El jugador debe tener un nickname.");
        }

        if (j.getNickname().trim().length() > MAX_NICKNAME) {
            throw new IllegalArgumentException("El nickname admite máximo " + MAX_NICKNAME + " caracteres.");
        }
    }

    private int aEntero(Object id) {

        if (id == null) {
            throw new IllegalArgumentException("El identificador no puede ser nulo.");
        }

        try {
            return Integer.parseInt(id.toString().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El identificador debe ser un número entero: " + id);
        }
    }
}
