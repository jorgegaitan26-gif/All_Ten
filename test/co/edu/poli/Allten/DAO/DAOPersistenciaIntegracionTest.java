package co.edu.poli.Allten.DAO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.Allten.Modelo.Jugador;
import co.edu.poli.Allten.Modelo.Partida;

class DAOPersistenciaIntegracionTest {

    private Connection conexion;
    private DAOJugador jugadorDAO;
    private DAOPartida partidaDAO;

    @BeforeEach
    void prepararBaseDeDatos() throws Exception {
        String base = "allten_" + UUID.randomUUID().toString().replace("-", "");
        conexion = DriverManager.getConnection("jdbc:h2:mem:" + base + ";MODE=MySQL", "sa", "");
        try (Statement sentencia = conexion.createStatement()) {
            sentencia.execute("""
                    CREATE TABLE Jugador (
                        id_Jugador INT AUTO_INCREMENT PRIMARY KEY,
                        nickname VARCHAR(45) NOT NULL UNIQUE
                    )
                    """);
            sentencia.execute("""
                    CREATE TABLE Partida (
                        id_Partida INT AUTO_INCREMENT PRIMARY KEY,
                        tiempoTotal BIGINT NOT NULL,
                        Jugador_id_Jugador INT NOT NULL,
                        operacionesRealizadas TEXT NOT NULL,
                        fechaFinalizacion DATETIME NOT NULL,
                        fechaReto DATE,
                        nivelDificultad VARCHAR(10) NOT NULL DEFAULT 'FACIL',
                        FOREIGN KEY (Jugador_id_Jugador) REFERENCES Jugador(id_Jugador)
                    )
                    """);
        }

        ConnectionProvider proveedor = () -> conexion;
        jugadorDAO = new DAOJugador(proveedor);
        partidaDAO = new DAOPartida(proveedor);
    }

    @AfterEach
    void cerrarConexion() throws Exception {
        if (conexion != null) {
            conexion.close();
        }
    }

    @Test
    @DisplayName("El DAO de jugadores persiste, consulta, actualiza y elimina")
    void cicloDeVidaDeJugador() throws Exception {
        assertTrue(conexion.isValid(1));

        Jugador jugador = new Jugador("  jugador_integracion  ");
        jugadorDAO.create(jugador);

        assertNotNull(jugador.getIdJugador());
        assertEquals("jugador_integracion", jugadorDAO.readone(jugador.getIdJugador()).getNickname());
        assertEquals(jugador.getIdJugador(), jugadorDAO.readByNickname("jugador_integracion").getIdJugador());
        assertEquals(1, jugadorDAO.readall().size());

        jugador.setNickname("jugador_actualizado");
        jugadorDAO.update(jugador);
        assertEquals("jugador_actualizado", jugadorDAO.readone(jugador.getIdJugador()).getNickname());

        jugadorDAO.delete(jugador.getIdJugador());
        assertNull(jugadorDAO.readone(jugador.getIdJugador()));
    }

    @Test
    @DisplayName("El DAO de partidas persiste datos y genera el ranking esperado")
    void cicloDeVidaDePartidasYRanking() throws Exception {
        Jugador primero = crearJugador("jugador_rapido");
        Jugador segundo = crearJugador("jugador_lento");
        Date fecha = Timestamp.valueOf("2026-11-10 12:00:00");

        Partida partidaRapida = crearPartida(primero, 12.4, fecha, List.of("3", "7"));
        Partida partidaLenta = crearPartida(segundo, 28, fecha, List.of("2", "8"));
        partidaDAO.create(partidaRapida);
        partidaDAO.create(partidaLenta);

        assertNotNull(partidaRapida.getIdPartida());
        Partida recuperada = partidaDAO.readone(partidaRapida.getIdPartida());
        assertEquals(12, recuperada.getTiempoTotal());
        assertEquals(primero.getIdJugador(), recuperada.getJugador().getIdJugador());
        assertEquals(List.of("3", "7"), recuperada.getOperacionesRealizadas());
        assertEquals(2, partidaDAO.readall().size());
        assertEquals(1, partidaDAO.readByJugador(primero.getIdJugador()).size());
        assertEquals(partidaRapida.getIdPartida(), partidaDAO.readTop(1).get(0).getIdPartida());

        List<EntradaRanking> ranking = partidaDAO.readRanking(10);
        assertEquals(2, ranking.size());
        assertEquals("jugador_rapido", ranking.get(0).getNickname());
        assertEquals(12, ranking.get(0).getTiempoTotal());

        partidaRapida.setTiempoTotal(35);
        partidaDAO.update(partidaRapida);
        assertEquals(partidaLenta.getIdPartida(), partidaDAO.readTop(1).get(0).getIdPartida());

        assertTrue(jugadorDAO.delete(segundo.getIdJugador()).contains("No se puede eliminar"));
        partidaDAO.delete(partidaRapida.getIdPartida());
        assertNull(partidaDAO.readone(partidaRapida.getIdPartida()));
        assertTrue(partidaDAO.deleteByJugador(segundo.getIdJugador()).contains("1 partida(s)"));
        jugadorDAO.delete(segundo.getIdJugador());
    }

    @Test
    @DisplayName("Los rankings diario y general conservan solo el mejor tiempo por jugador")
    void rankingDelRetoYGeneral() throws Exception {
        Jugador primero = crearJugador("jugador_reto_1");
        Jugador segundo = crearJugador("jugador_reto_2");
        Date fechaReto = java.sql.Date.valueOf("2026-11-10");

        partidaDAO.create(crearPartidaReto(primero, 40, fechaReto));
        partidaDAO.create(crearPartidaReto(primero, 20, fechaReto));
        partidaDAO.create(crearPartidaReto(segundo, 30, fechaReto));

        Partida libre = crearPartida(segundo, 5, fechaReto, List.of("1"));
        partidaDAO.create(libre);

        List<EntradaRanking> rankingDiario = partidaDAO.readRankingDelReto(fechaReto, "FACIL", 10);
        assertEquals(2, rankingDiario.size());
        assertEquals("jugador_reto_1", rankingDiario.get(0).getNickname());
        assertEquals(20, rankingDiario.get(0).getTiempoTotal());
        assertEquals("jugador_reto_2", rankingDiario.get(1).getNickname());

        List<EntradaRanking> rankingGeneral = partidaDAO.readRankingGeneral("FACIL", 10);
        assertEquals(2, rankingGeneral.size());
        assertEquals("jugador_reto_1", rankingGeneral.get(0).getNickname());
        assertEquals(20, rankingGeneral.get(0).getTiempoTotal());
    }

    private Jugador crearJugador(String nickname) throws Exception {
        Jugador jugador = new Jugador(nickname);
        jugadorDAO.create(jugador);
        return jugador;
    }

    private Partida crearPartida(Jugador jugador, double tiempo, Date fecha, List<String> operaciones) {
        Partida partida = new Partida(jugador);
        partida.setTiempoTotal(tiempo);
        partida.setFechaFinalizacion(fecha);
        partida.setOperacionesRealizadas(operaciones);
        return partida;
    }

    private Partida crearPartidaReto(Jugador jugador, double tiempo, Date fechaReto) {
        Partida partida = crearPartida(jugador, tiempo, Timestamp.valueOf("2026-11-10 12:00:00"), List.of("1"));
        partida.setFechaReto(fechaReto);
        return partida;
    }
}
