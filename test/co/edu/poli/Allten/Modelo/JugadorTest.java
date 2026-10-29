package co.edu.poli.Allten.Modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JugadorTest {

    @Test
    @DisplayName("Un jugador nuevo tiene nickname y aun no tiene id")
    void jugadorNuevo() {
        Jugador jugador = new Jugador("julian");
        assertEquals("julian", jugador.getNickname());
        assertNull(jugador.getIdJugador());
    }

    @Test
    @DisplayName("Un jugador de la base de datos tiene id y nickname; los setters los cambian")
    void jugadorExistente() {
        Jugador jugador = new Jugador(7, "angie");
        assertEquals(7, jugador.getIdJugador());

        jugador.setIdJugador(8);
        jugador.setNickname("jorge");
        assertEquals(8, jugador.getIdJugador());
        assertEquals("jorge", jugador.getNickname());
    }

    @Test
    @DisplayName("El jugador hace parte de la partida")
    void jugadorEnPartida() {
        Jugador jugador = new Jugador("julian");
        assertSame(jugador, new Partida(jugador).getJugador());
    }
}
