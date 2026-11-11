package co.edu.poli.Allten.DAO;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.Allten.Modelo.Jugador;
import co.edu.poli.Allten.Modelo.Partida;

class DAOValidacionesTest {

    @Test
    @DisplayName("El DAO rechaza jugadores sin nickname válido sin conectarse a la base de datos")
    void validaJugadorAntesDeAccederALaBase() {
        DAOJugador dao = new DAOJugador();

        assertThrows(IllegalArgumentException.class, () -> dao.create(new Jugador("  ")));
        assertThrows(IllegalArgumentException.class, () -> dao.create(new Jugador("x".repeat(46))));
    }

    @Test
    @DisplayName("El DAO rechaza partidas incompletas o con valores fuera de rango")
    void validaPartidaAntesDeAccederALaBase() {
        DAOPartida dao = new DAOPartida();
        Partida sinJugador = new Partida(new Jugador("jugador"));
        Partida sinIdDeJugador = new Partida(new Jugador("jugador"));
        Partida conTiempoNegativo = new Partida(new Jugador(1, "jugador"));
        conTiempoNegativo.setTiempoTotal(-1);
        Partida conDemasiadasOperaciones = new Partida(new Jugador(1, "jugador"));
        conDemasiadasOperaciones.setOperacionesRealizadas(
                List.of("12345", "23456", "34567", "45678", "56789", "67890", "78901"));

        assertThrows(IllegalArgumentException.class, () -> dao.create(null));
        assertThrows(IllegalArgumentException.class, () -> dao.create(sinJugador));
        sinIdDeJugador.setJugador(new Jugador(0, "jugador"));
        assertThrows(IllegalArgumentException.class, () -> dao.create(sinIdDeJugador));
        assertThrows(IllegalArgumentException.class, () -> dao.create(conTiempoNegativo));
        assertThrows(IllegalArgumentException.class, () -> dao.create(conDemasiadasOperaciones));
    }
}
