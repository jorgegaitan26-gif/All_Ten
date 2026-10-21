package servicios;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import model.Partida;

/**
 * Gestiona la persistencia de partidas en la tabla Partida de MySQL.
 */
public class DAOPartida implements CRUD<Partida> {

    /** Longitud máxima de operacionesRealizadas según la base de datos, VARCHAR(45). */
    private static final int MAX_OPERACIONES = 45;

    private static final String COLUMNAS =
            "id_Partida, tiempoTotal, Jugador_id_Jugador, operacionesRealizadas, fechaFinalizacion";

    /**
     * Registra una partida nueva. Al terminar, el objeto recibido queda con el
     * identificador generado por la base de datos. Si la partida no trae fecha
     * de finalización, se usa la fecha y hora actuales.
     *
     * @param p partida a registrar
     * @return mensaje con el resultado de la operación
     * @throws IllegalArgumentException si la partida es nula, el tiempo es negativo,
     *         el jugador no existe o sus operaciones superan 45 caracteres
     * @throws Exception si ocurre un error durante el registro
     */
    @Override
    public String create(Partida p) throws Exception {

        validar(p);

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = "INSERT INTO Partida (tiempoTotal, Jugador_id_Jugador, operacionesRealizadas, "
                + "fechaFinalizacion) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, p.getTiempoTotal());
            ps.setInt(2, p.getJugadorIdJugador());
            ps.setString(3, p.getOperacionesRealizadas());
            ps.setTimestamp(4, fecha(p));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setIdPartida(rs.getInt(1));
                }
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            throw jugadorInexistente(p, e);
        }

        return "Partida guardada con id " + p.getIdPartida();
    }

    /**
     * Consulta una partida por su identificador.
     *
     * @param <K> tipo del identificador
     * @param id identificador de la partida
     * @return partida encontrada o null si no existe
     * @throws Exception si ocurre un error durante la consulta
     */
    @Override
    public <K> Partida readone(K id) throws Exception {

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = "SELECT " + COLUMNAS + " FROM Partida WHERE id_Partida = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, aEntero(id));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }

        return null;
    }

    /**
     * Consulta todas las partidas registradas.
     *
     * @return lista de partidas, de la más reciente a la más antigua
     * @throws Exception si ocurre un error durante la consulta
     */
    @Override
    public List<Partida> readall() throws Exception {

        List<Partida> lista = new ArrayList<>();

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = "SELECT " + COLUMNAS + " FROM Partida ORDER BY fechaFinalizacion DESC, id_Partida DESC";

        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }

        return lista;
    }

    /**
     * Consulta las partidas de un jugador.
     *
     * @param idJugador identificador del jugador
     * @return lista de partidas del jugador, de la más reciente a la más antigua
     * @throws Exception si ocurre un error durante la consulta
     */
    public List<Partida> readByJugador(int idJugador) throws Exception {

        List<Partida> lista = new ArrayList<>();

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = "SELECT " + COLUMNAS
                + " FROM Partida WHERE Jugador_id_Jugador = ? ORDER BY fechaFinalizacion DESC, id_Partida DESC";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idJugador);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }

        return lista;
    }

    /**
     * Consulta las mejores partidas, es decir, las de menor tiempo total.
     *
     * @param limite cantidad máxima de partidas a devolver (mayor que 0)
     * @return lista de partidas ordenada del menor al mayor tiempo
     * @throws IllegalArgumentException si el límite no es positivo
     * @throws Exception si ocurre un error durante la consulta
     */
    public List<Partida> readTop(int limite) throws Exception {

        if (limite <= 0) {
            throw new IllegalArgumentException("El límite debe ser mayor que 0.");
        }

        List<Partida> lista = new ArrayList<>();

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = "SELECT " + COLUMNAS + " FROM Partida ORDER BY tiempoTotal ASC, id_Partida ASC LIMIT ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, limite);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }

        return lista;
    }

    /**
     * Actualiza una partida existente. Si la partida no trae fecha de
     * finalización, se conserva la que ya tenía guardada.
     *
     * @param p partida con el identificador y los datos nuevos
     * @return mensaje con el resultado de la operación
     * @throws IllegalArgumentException si la partida es nula, el tiempo es negativo,
     *         el jugador no existe o sus operaciones superan 45 caracteres
     * @throws Exception si ocurre un error durante la actualización
     */
    @Override
    public String update(Partida p) throws Exception {

        validar(p);

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = "UPDATE Partida SET tiempoTotal = ?, Jugador_id_Jugador = ?, "
                + "operacionesRealizadas = ?, fechaFinalizacion = COALESCE(?, fechaFinalizacion) "
                + "WHERE id_Partida = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, p.getTiempoTotal());
            ps.setInt(2, p.getJugadorIdJugador());
            ps.setString(3, p.getOperacionesRealizadas());

            Date f = p.getFechaFinalizacion();
            ps.setTimestamp(4, f != null ? new Timestamp(f.getTime()) : null);

            ps.setInt(5, p.getIdPartida());

            int filas = ps.executeUpdate();

            if (filas == 0) {
                return "No existe una partida con id " + p.getIdPartida();
            }

        } catch (SQLIntegrityConstraintViolationException e) {
            throw jugadorInexistente(p, e);
        }

        return "Partida actualizada con id " + p.getIdPartida();
    }

    /**
     * Elimina una partida por su identificador.
     *
     * @param <K> tipo del identificador
     * @param id identificador de la partida
     * @return mensaje con el resultado de la operación
     * @throws Exception si ocurre un error durante la eliminación
     */
    @Override
    public <K> String delete(K id) throws Exception {

        int idPartida = aEntero(id);

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = "DELETE FROM Partida WHERE id_Partida = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPartida);

            int filas = ps.executeUpdate();

            if (filas == 0) {
                return "No existe una partida con id " + idPartida;
            }
        }

        return "Partida eliminada con id " + idPartida;
    }

    /**
     * Elimina todas las partidas de un jugador. Sirve para poder borrar después
     * al jugador, ya que la clave foránea no permite borrarlo con partidas.
     *
     * @param idJugador identificador del jugador
     * @return mensaje con la cantidad de partidas eliminadas
     * @throws Exception si ocurre un error durante la eliminación
     */
    public String deleteByJugador(int idJugador) throws Exception {

        Connection con = ConexionBD.getInstancia().getConexion();

        String sql = "DELETE FROM Partida WHERE Jugador_id_Jugador = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idJugador);

            int filas = ps.executeUpdate();

            return filas + " partida(s) eliminada(s) del jugador " + idJugador;
        }
    }

    private void validar(Partida p) {

        if (p == null) {
            throw new IllegalArgumentException("La partida no puede ser nula.");
        }

        if (p.getJugadorIdJugador() <= 0) {
            throw new IllegalArgumentException("La partida debe pertenecer a un jugador válido.");
        }

        if (p.getTiempoTotal() < 0) {
            throw new IllegalArgumentException("El tiempo total no puede ser negativo.");
        }

        if (p.getOperacionesRealizadas() != null && p.getOperacionesRealizadas().length() > MAX_OPERACIONES) {
            throw new IllegalArgumentException(
                    "operacionesRealizadas admite máximo " + MAX_OPERACIONES + " caracteres.");
        }
    }

    private IllegalArgumentException jugadorInexistente(Partida p, Exception causa) {
        return new IllegalArgumentException(
                "No existe un jugador con id " + p.getJugadorIdJugador() + ".", causa);
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

    private Timestamp fecha(Partida p) {

        Date f = p.getFechaFinalizacion();

        return new Timestamp(f != null ? f.getTime() : System.currentTimeMillis());
    }

    private Partida mapear(ResultSet rs) throws Exception {

        return new Partida(
                rs.getInt("id_Partida"),
                rs.getInt("tiempoTotal"),
                rs.getInt("Jugador_id_Jugador"),
                rs.getString("operacionesRealizadas"),
                rs.getTimestamp("fechaFinalizacion"));
    }
}
