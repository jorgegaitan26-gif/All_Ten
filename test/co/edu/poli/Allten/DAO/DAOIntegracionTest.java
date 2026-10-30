package co.edu.poli.Allten.DAO;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.Allten.Modelo.Jugador;
import co.edu.poli.Allten.Modelo.Partida;

/**
 * Pruebas de integracion de DAOJugador y DAOPartida contra MySQL
 * (SCRUM-204).
 *
 * Usan la base configurada en el archivo .env. Si no hay conexion (MySQL
 * apagado, sin .env o sin permisos), las pruebas se marcan como OMITIDAS en
 * lugar de fallar, para que {@code mvn test} funcione en cualquier equipo.
 *
 * Cada prueba crea jugadores con nicknames unicos y partidas en un reto de
 * una fecha lejana (para no mezclarse con datos reales), y al terminar borra
 * todo lo que inserto.
 */
class DAOIntegracionTest {

    private static final ZoneId ZONA_RETO = ZoneId.of("America/Bogota");

    private static boolean hayBaseDeDatos;

    private DAOJugador jugadorDAO;
    private DAOPartida partidaDAO;

    /** Jugadores creados en cada prueba, para borrarlos (con sus partidas) al final. */
    private final List<Jugador> creados = new ArrayList<>();

    /** Fecha de reto unica por prueba, para que el ranking solo tenga los datos de la prueba. */
    private Date fechaReto;

    @BeforeAll
    static void verificarConexion() {
        try {
            hayBaseDeDatos = ConexionBD.getInstancia().getConexion().isValid(3);
        } catch (Exception e) {
            hayBaseDeDatos = false;
            System.out.println("[DAOIntegracionTest] Sin base de datos, pruebas omitidas: " + e.getMessage());
        }
    }

    @BeforeEach
    void preparar() {
        assumeTrue(hayBaseDeDatos, "No hay conexion a MySQL (revisa el .env)");
        jugadorDAO = new DAOJugador();
        partidaDAO = new DAOPartida();

        // Un dia aleatorio del año 2099: nadie juega ese reto de verdad.
        LocalDate dia = LocalDate.of(2099, 1, 1).plusDays((long) (Math.random() * 364));
        fechaReto = Date.from(dia.atStartOfDay(ZONA_RETO).toInstant());
    }

    @AfterEach
    void limpiar() throws Exception {
        for (Jugador jugador : creados) {
            partidaDAO.deleteByJugador(jugador.getIdJugador());
            jugadorDAO.delete(jugador.getIdJugador());
        }
        creados.clear();
    }

    // ----- Utilidades -----

    private Jugador crearJugador(String prefijo) throws Exception {
        String nickname = prefijo + "_" + UUID.randomUUID().toString().substring(0, 8);
        Jugador jugador = new Jugador(nickname);
        jugadorDAO.create(jugador);
        creados.add(jugador);
        return jugador;
    }

    /** Guarda una partida terminada del jugador; fechaReto null = partida libre. */
    private Partida guardarPartida(Jugador jugador, int segundos, Date fechaReto, Date finalizada) throws Exception {
        Partida partida = new Partida(null, jugador, segundos, List.of("1", "2", "3"), finalizada);
        partida.setFechaReto(fechaReto);
        partidaDAO.create(partida);
        return partida;
    }

    private static Date haceMinutos(int minutos) {
        return new Date(System.currentTimeMillis() - minutos * 60_000L);
    }

    // ----- DAOJugador -----

    @Test
    @DisplayName("DAOJugador: crea el jugador, le asigna id y lo encuentra por nickname")
    void crearYBuscarJugador() throws Exception {
        Jugador jugador = crearJugador("prueba");

        assertNotNull(jugador.getIdJugador());
        Jugador encontrado = jugadorDAO.readByNickname(jugador.getNickname());
        assertEquals(jugador.getIdJugador(), encontrado.getIdJugador());
        assertEquals(jugador.getNickname(), encontrado.getNickname());
    }

    @Test
    @DisplayName("DAOJugador: no permite dos jugadores con el mismo nickname")
    void nicknameDuplicado() throws Exception {
        Jugador jugador = crearJugador("dup");
        assertThrows(IllegalArgumentException.class,
                () -> jugadorDAO.create(new Jugador(jugador.getNickname())));
    }

    @Test
    @DisplayName("DAOJugador: un nickname que no existe devuelve null")
    void nicknameInexistente() throws Exception {
        assertNull(jugadorDAO.readByNickname("no_existe_" + UUID.randomUUID()));
    }

    // ----- DAOPartida -----

    @Test
    @DisplayName("DAOPartida: guarda la partida con su fechaReto y la vuelve a leer")
    void guardarYLeerPartida() throws Exception {
        Jugador jugador = crearJugador("partida");
        Partida guardada = guardarPartida(jugador, 75, fechaReto, haceMinutos(1));

        assertNotNull(guardada.getIdPartida());
        Partida leida = partidaDAO.readone(guardada.getIdPartida());
        assertEquals(75, leida.getTiempoTotal());
        assertEquals(List.of("1", "2", "3"), leida.getOperacionesRealizadas());
        assertNotNull(leida.getFechaReto());
    }

    @Test
    @DisplayName("Ranking del reto: ordena por menor tiempo, un puesto por jugador y sin partidas libres")
    void rankingDelReto() throws Exception {
        Jugador ana = crearJugador("ana");
        Jugador beto = crearJugador("beto");
        Jugador caro = crearJugador("caro");

        guardarPartida(ana, 90, fechaReto, haceMinutos(30));
        guardarPartida(ana, 60, fechaReto, haceMinutos(20));    // mejor tiempo de ana
        guardarPartida(beto, 45, fechaReto, haceMinutos(10));
        guardarPartida(caro, 10, null, haceMinutos(5));         // partida libre: no cuenta

        List<EntradaRanking> ranking = partidaDAO.readRankingDelReto(fechaReto, 10);

        assertEquals(2, ranking.size(), "un puesto por jugador y sin la partida libre");
        assertEquals(beto.getNickname(), ranking.get(0).getNickname());
        assertEquals(45, ranking.get(0).getTiempoTotal());
        assertEquals(1, ranking.get(0).getPosicion());
        assertEquals(ana.getNickname(), ranking.get(1).getNickname());
        assertEquals(60, ranking.get(1).getTiempoTotal(), "se toma el mejor tiempo de ana");
        assertEquals(2, ranking.get(1).getPosicion());
    }

    @Test
    @DisplayName("Ranking del reto: con el mismo tiempo va primero quien lo logro antes")
    void rankingDesempatePorFecha() throws Exception {
        Jugador primero = crearJugador("primero");
        Jugador segundo = crearJugador("segundo");

        guardarPartida(segundo, 50, fechaReto, haceMinutos(5));
        guardarPartida(primero, 50, fechaReto, haceMinutos(15));

        List<EntradaRanking> ranking = partidaDAO.readRankingDelReto(fechaReto, 10);

        assertEquals(primero.getNickname(), ranking.get(0).getNickname());
        assertEquals(segundo.getNickname(), ranking.get(1).getNickname());
    }

    @Test
    @DisplayName("Ranking del reto: respeta el limite de puestos")
    void rankingRespetaLimite() throws Exception {
        for (int i = 0; i < 3; i++) {
            guardarPartida(crearJugador("lim" + i), 30 + i, fechaReto, haceMinutos(10 - i));
        }
        assertEquals(2, partidaDAO.readRankingDelReto(fechaReto, 2).size());
    }

    @Test
    @DisplayName("Ranking: limites invalidos o fecha nula se rechazan")
    void parametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> partidaDAO.readRankingDelReto(fechaReto, 0));
        assertThrows(IllegalArgumentException.class, () -> partidaDAO.readRankingDelReto(null, 10));
    }

    @Test
    @DisplayName("DAOPartida: no guarda una partida de un jugador que no existe")
    void partidaSinJugadorValido() {
        Partida partida = new Partida(null, new Jugador(999_999_999, "fantasma"), 30, List.of("1"), new Date());
        partida.setFechaReto(fechaReto);
        assertThrows(IllegalArgumentException.class, () -> partidaDAO.create(partida));
    }
}
