package co.edu.poli.Allten.DAO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.Allten.Modelo.Jugador;
import co.edu.poli.Allten.Modelo.Partida;

/**
 * Pruebas de integracion del DAO contra MySQL (base all_ten_db, ver
 * sql/all_ten_db.sql). Si no hay conexion (MySQL apagado o sin .env), las
 * pruebas se marcan como OMITIDAS en lugar de fallar.
 *
 * Cada prueba usa nicknames unicos y borra al final lo que inserto.
 */
class PartidaDAOImplTest {

    private static boolean hayBaseDeDatos;

    private PartidaDAOImpl dao;
    private final List<String> nicknamesCreados = new ArrayList<>();

    @BeforeAll
    static void verificarConexion() {
        try {
            ConexionBD.probarConexion();
            hayBaseDeDatos = true;
        } catch (SQLException e) {
            hayBaseDeDatos = false;
            System.out.println("[PartidaDAOImplTest] Sin base de datos, se omiten las pruebas: " + e.getMessage());
        }
    }

    @BeforeEach
    void preparar() {
        assumeTrue(hayBaseDeDatos, "MySQL no disponible");
        dao = new PartidaDAOImpl();
    }

    @AfterEach
    void limpiar() throws SQLException {
        if (!hayBaseDeDatos) {
            return;
        }
        try (Connection conn = ConexionBD.obtenerConexion()) {
            for (String nickname : nicknamesCreados) {
                try (PreparedStatement borrarPartidas = conn.prepareStatement(
                        "DELETE FROM Partida WHERE Jugador_id_Jugador IN "
                                + "(SELECT id_Jugador FROM (SELECT id_Jugador FROM Jugador WHERE nickname = ?) j)");
                     PreparedStatement borrarJugador = conn.prepareStatement(
                        "DELETE FROM Jugador WHERE nickname = ?")) {
                    borrarPartidas.setString(1, nickname);
                    borrarPartidas.executeUpdate();
                    borrarJugador.setString(1, nickname);
                    borrarJugador.executeUpdate();
                }
            }
        }
    }

    private String nicknameUnico() {
        String nickname = "test_" + System.nanoTime();
        nicknamesCreados.add(nickname);
        return nickname;
    }

    /** Partida terminada con datos fijos, lista para guardarse. */
    private Partida partidaTerminada(Jugador jugador) {
        return new Partida(null, jugador, 47.6, List.of("1 = 4 - 3", "2 = 4 - 2"),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
    }

    @Test
    @DisplayName("probarConexion responde SELECT 1")
    void conexion() throws SQLException {
        ConexionBD.probarConexion();
    }

    @Test
    @DisplayName("registrarJugador inserta el jugador y le asigna id_Jugador")
    void registrarJugador() throws SQLException {
        Jugador jugador = new Jugador(nicknameUnico());
        dao.registrarJugador(jugador);
        assertNotNull(jugador.getIdJugador());
        assertTrue(jugador.getIdJugador() > 0);
    }

    @Test
    @DisplayName("UC-01/UC-02: identificarJugador registra una vez y luego carga el mismo id")
    void identificarJugadorContraLaBase() {
        String nickname = nicknameUnico();
        assertNull(dao.buscarPorNickname(nickname));

        Jugador nuevo = Jugador.identificarJugador(nickname, dao);
        Jugador existente = Jugador.identificarJugador(nickname, dao);

        assertNotNull(nuevo.getIdJugador());
        assertEquals(nuevo.getIdJugador(), existente.getIdJugador());
    }

    @Test
    @DisplayName("registrarPartida(jugador, partida) guarda jugador y partida en una transaccion")
    void registrarPartidaConJugadorNuevo() throws SQLException {
        Jugador jugador = new Jugador(nicknameUnico());
        Partida partida = partidaTerminada(jugador);

        dao.registrarPartida(jugador, partida);

        assertNotNull(jugador.getIdJugador());
        assertNotNull(partida.getIdPartida());
        assertEquals(jugador, partida.getJugador());
    }

    @Test
    @DisplayName("registrarPartida(partida) exige un jugador ya registrado")
    void registrarPartidaSinJugador() {
        Partida partida = partidaTerminada(new Jugador("sin_id"));
        assertThrows(SQLException.class, () -> dao.registrarPartida(partida));
    }

    @Test
    @DisplayName("No se guarda una partida sin fecha de finalizacion")
    void partidaSinFecha() throws SQLException {
        Jugador jugador = new Jugador(nicknameUnico());
        dao.registrarJugador(jugador);
        Partida enCurso = new Partida(jugador);
        assertThrows(SQLException.class, () -> dao.registrarPartida(enCurso));
    }

    @Test
    @DisplayName("obtenerHistorialPartidas devuelve la partida guardada con sus datos")
    void historial() throws SQLException {
        Jugador jugador = new Jugador(nicknameUnico());
        dao.registrarJugador(jugador);
        Partida partida = partidaTerminada(jugador);
        dao.registrarPartida(partida);

        Partida leida = dao.obtenerHistorialPartidas().stream()
                .filter(p -> partida.getIdPartida().equals(p.getIdPartida()))
                .findFirst()
                .orElseThrow();

        assertEquals(jugador.getNickname(), leida.getJugador().getNickname());
        assertEquals(48, leida.getTiempoTotalSegundos(), "se guarda redondeado a segundos");
        assertEquals(partida.getOperacionesRealizadas(), leida.getOperacionesRealizadas());
        assertEquals(partida.getFechaFinalizacion(), leida.getFechaFinalizacion());
    }
}
