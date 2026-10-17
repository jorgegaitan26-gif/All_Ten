package co.edu.poli.Allten.Modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas del diagrama de flujo de la clase Jugador (sin base de datos). */
class JugadorTest {

    /** Base de datos falsa en memoria para probar el flujo sin MySQL. */
    private static final class RegistroEnMemoria implements Jugador.RegistroJugadores {
        final Map<String, Integer> tabla = new HashMap<>();
        int inserciones = 0;

        @Override
        public Jugador buscarPorNickname(String nickname) {
            Integer id = tabla.get(nickname);
            return id == null ? null : new Jugador(id, nickname);
        }

        @Override
        public Integer registrarJugador(String nickname) {
            inserciones++;
            tabla.put(nickname, tabla.size() + 1);
            return tabla.get(nickname);
        }
    }

    private RegistroEnMemoria registro;

    @BeforeEach
    void crearRegistro() {
        registro = new RegistroEnMemoria();
    }

    @Test
    @DisplayName("UC-02: un nickname nuevo se registra y recibe un id")
    void registraNuevo() {
        Jugador jugador = Jugador.identificarJugador("Julian", registro);
        assertEquals(1, jugador.getIdJugador());
        assertEquals("Julian", jugador.getNickname());
        assertEquals(1, registro.inserciones);
    }

    @Test
    @DisplayName("UC-01: un nickname existente se carga sin duplicarlo")
    void cargaExistente() {
        Jugador.identificarJugador("Julian", registro);
        Jugador otraVez = Jugador.identificarJugador("  Julian  ", registro);
        assertEquals(1, otraVez.getIdJugador());
        assertEquals(1, registro.inserciones);
    }

    @Test
    @DisplayName("Nickname vacio o de mas de 45 caracteres se rechaza")
    void nicknameInvalido() {
        assertThrows(IllegalArgumentException.class, () -> Jugador.identificarJugador("   ", registro));
        assertThrows(IllegalArgumentException.class, () -> Jugador.identificarJugador("x".repeat(46), registro));
    }

    @Test
    @DisplayName("UC-03: vincularNuevaPartida crea la partida asociada al jugador")
    void vincularPartida() {
        Jugador jugador = Jugador.identificarJugador("Jorge", registro);
        Partida partida = jugador.vincularNuevaPartida();
        assertSame(jugador, partida.getJugador());
        assertNull(partida.getIdPartida());
    }
}
