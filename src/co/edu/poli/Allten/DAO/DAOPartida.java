package co.edu.poli.Allten.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import co.edu.poli.Allten.Modelo.Jugador;
import co.edu.poli.Allten.Modelo.Partida;

/** Implementación MySQL de las operaciones CRUD de partidas. */
public class DAOPartida implements IPartidaDAO {

    private static final String COLUMNAS = "p.id_Partida, p.tiempoTotal, p.Jugador_id_Jugador, "
            + "j.nickname, p.operacionesRealizadas, p.fechaFinalizacion, p.fechaReto";

    @Override
    public Partida create(Partida partida) throws SQLException {
        validar(partida);
        String sql = "INSERT INTO Partida "
                + "(tiempoTotal, Jugador_id_Jugador, operacionesRealizadas, fechaFinalizacion, fechaReto) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement insercion = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            insertarDatos(insercion, partida);
            insercion.executeUpdate();
            try (ResultSet claves = insercion.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException("No se pudo obtener el ID de la partida registrada.");
                }
                partida.setIdPartida(claves.getInt(1));
                return partida;
            }
        }
    }

    @Override
    public Partida readone(int id) throws SQLException {
        validarId(id);
        String sql = "SELECT " + COLUMNAS + " FROM Partida p "
                + "JOIN Jugador j ON j.id_Jugador = p.Jugador_id_Jugador WHERE p.id_Partida = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setInt(1, id);
            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next() ? mapear(resultado) : null;
            }
        }
    }

    @Override
    public List<Partida> readall() throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM Partida p "
                + "JOIN Jugador j ON j.id_Jugador = p.Jugador_id_Jugador "
                + "ORDER BY p.fechaFinalizacion DESC, p.id_Partida DESC";
        return consultar(sql, statement -> { });
    }

    @Override
    public List<Partida> readByJugador(int idJugador) throws SQLException {
        validarId(idJugador);
        String sql = "SELECT " + COLUMNAS + " FROM Partida p "
                + "JOIN Jugador j ON j.id_Jugador = p.Jugador_id_Jugador "
                + "WHERE p.Jugador_id_Jugador = ? "
                + "ORDER BY p.fechaFinalizacion DESC, p.id_Partida DESC";
        return consultar(sql, statement -> statement.setInt(1, idJugador));
    }

    @Override
    public List<Partida> readTop(int limite) throws SQLException {
        if (limite <= 0) {
            throw new IllegalArgumentException("El límite debe ser mayor que cero.");
        }
        String sql = "SELECT " + COLUMNAS + " FROM Partida p "
                + "JOIN Jugador j ON j.id_Jugador = p.Jugador_id_Jugador "
                + "WHERE p.id_Partida = ("
                + "SELECT p2.id_Partida FROM Partida p2 "
                + "WHERE p2.Jugador_id_Jugador = p.Jugador_id_Jugador "
                + "ORDER BY p2.tiempoTotal ASC, p2.fechaFinalizacion ASC, p2.id_Partida ASC LIMIT 1) "
                + "ORDER BY p.tiempoTotal ASC, p.id_Partida ASC LIMIT ?";
        return consultar(sql, statement -> statement.setInt(1, limite));
    }

    @Override
    public List<Partida> readTopForDate(LocalDate fechaReto, int limite) throws SQLException {
        if (fechaReto == null) {
            throw new IllegalArgumentException("La fecha del reto no puede ser nula.");
        }
        if (limite <= 0) {
            throw new IllegalArgumentException("El límite debe ser mayor que cero.");
        }
        String sql = "SELECT " + COLUMNAS + " FROM Partida p "
                + "JOIN Jugador j ON j.id_Jugador = p.Jugador_id_Jugador "
                + "WHERE p.fechaReto = ? AND p.id_Partida = ("
                + "SELECT p2.id_Partida FROM Partida p2 "
                + "WHERE p2.Jugador_id_Jugador = p.Jugador_id_Jugador "
                + "AND p2.fechaReto = p.fechaReto "
                + "ORDER BY p2.tiempoTotal ASC, p2.id_Partida ASC LIMIT 1) "
                + "ORDER BY p.tiempoTotal ASC, p.id_Partida ASC LIMIT ?";
        return consultar(sql, statement -> {
            statement.setDate(1, java.sql.Date.valueOf(fechaReto));
            statement.setInt(2, limite);
        });
    }

    @Override
    public boolean update(Partida partida) throws SQLException {
        validar(partida);
        if (partida.getIdPartida() <= 0) {
            throw new IllegalArgumentException("La partida debe tener un identificador válido.");
        }
        String sql = "UPDATE Partida SET tiempoTotal = ?, Jugador_id_Jugador = ?, "
                + "operacionesRealizadas = ?, fechaFinalizacion = ?, fechaReto = ? WHERE id_Partida = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement actualizacion = conexion.prepareStatement(sql)) {
            insertarDatos(actualizacion, partida);
            actualizacion.setInt(6, partida.getIdPartida());
            return actualizacion.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        validarId(id);
        String sql = "DELETE FROM Partida WHERE id_Partida = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement eliminacion = conexion.prepareStatement(sql)) {
            eliminacion.setInt(1, id);
            return eliminacion.executeUpdate() > 0;
        }
    }

    @Override
    public int deleteByJugador(int idJugador) throws SQLException {
        validarId(idJugador);
        String sql = "DELETE FROM Partida WHERE Jugador_id_Jugador = ?";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement eliminacion = conexion.prepareStatement(sql)) {
            eliminacion.setInt(1, idJugador);
            return eliminacion.executeUpdate();
        }
    }

    private List<Partida> consultar(String sql, PreparadorConsulta preparador) throws SQLException {
        List<Partida> partidas = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            preparador.preparar(consulta);
            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    partidas.add(mapear(resultado));
                }
            }
        }
        return partidas;
    }

    private static void validar(Partida partida) {
        if (partida == null) {
            throw new IllegalArgumentException("La partida no puede ser nula.");
        }
        if (partida.getJugador() == null || partida.getJugador().getIdJugador() == null
                || partida.getJugador().getIdJugador() <= 0) {
            throw new IllegalArgumentException("La partida debe pertenecer a un jugador registrado.");
        }
        if (partida.getTiempoTotalSegundos() < 0) {
            throw new IllegalArgumentException("El tiempo total no puede ser negativo.");
        }
        if (partida.getFechaFinalizacion() == null) {
            throw new IllegalArgumentException("No se puede guardar una partida que no ha finalizado.");
        }
        if (partida.getFechaReto() == null) {
            throw new IllegalArgumentException("La partida debe tener una fecha de reto.");
        }
    }

    private static void validarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El identificador debe ser mayor que cero.");
        }
    }

    private static void insertarDatos(PreparedStatement statement, Partida partida) throws SQLException {
        statement.setLong(1, Math.round(partida.getTiempoTotalSegundos()));
        statement.setInt(2, partida.getJugador().getIdJugador());
        statement.setString(3, String.join("\n", partida.getOperacionesRealizadas()));
        statement.setTimestamp(4, Timestamp.valueOf(partida.getFechaFinalizacion()));
        statement.setDate(5, java.sql.Date.valueOf(partida.getFechaReto()));
    }

    private static Partida mapear(ResultSet resultado) throws SQLException {
        Jugador jugador = new Jugador(
                resultado.getInt("Jugador_id_Jugador"),
                resultado.getString("nickname"));
        String operacionesGuardadas = resultado.getString("operacionesRealizadas");
        List<String> operaciones = operacionesGuardadas == null || operacionesGuardadas.isEmpty()
                ? List.of()
                : List.of(operacionesGuardadas.split("\\R", -1));
        Timestamp fecha = resultado.getTimestamp("fechaFinalizacion");
        java.sql.Date fechaReto = resultado.getDate("fechaReto");
        return new Partida(
                resultado.getInt("id_Partida"),
                jugador,
                resultado.getLong("tiempoTotal"),
                operaciones,
                fecha.toLocalDateTime(),
                fechaReto == null ? null : fechaReto.toLocalDate());
    }

    @FunctionalInterface
    private interface PreparadorConsulta {
        void preparar(PreparedStatement statement) throws SQLException;
    }
}
