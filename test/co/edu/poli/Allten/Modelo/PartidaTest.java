package co.edu.poli.Allten.Modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Pruebas de los diagramas de flujo de la clase Partida. */
class PartidaTest {

    /** Juega la partida completa usando las soluciones que calcula el propio modelo. */
    private static void completarTodosLosObjetivos(Partida partida) {
        for (String solucion : partida.posiblesSoluciones()) {
            String[] partes = solucion.split(" = ", 2);
            Expresion e = ExpresionTest.armar(partida.getNumerosDisponibles(), partida.getNivelDificultad(),
                    partes[1].split(" "));
            assertTrue(partida.completarObjetivo(e.calcularResultado(), e.getTextoFormateado()));
        }
    }

    @ParameterizedTest(name = "Dificultad {0}: los numeros generados resuelven los 10 objetivos")
    @EnumSource(Partida.NivelDificultad.class)
    void numerosValidosPorDificultad(Partida.NivelDificultad nivel) {
        Partida partida = new Partida();
        partida.setDificultad(nivel);
        partida.iniciarPartida();

        assertEquals(4, partida.getNumerosDisponibles().size());
        List<String> soluciones = partida.posiblesSoluciones();
        assertEquals(10, soluciones.size(), "soluciones: " + soluciones);
        for (int objetivo = 1; objetivo <= 10; objetivo++) {
            assertTrue(soluciones.get(objetivo - 1).startsWith(objetivo + " = "));
        }
    }

    @Test
    @DisplayName("iniciarPartida arranca el cronometro y deja la partida sin finalizar")
    void iniciarPartida() {
        Partida partida = new Partida();
        partida.iniciarPartida();
        assertNotNull(partida.getCronometro().getTiempo_Inicio());
        assertNull(partida.getFechaFinalizacion());
        assertTrue(partida.getObjetivosCompletados().isEmpty());
    }

    @Test
    @DisplayName("completarObjetivo solo registra objetivos pendientes del 1 al 10")
    void completarObjetivo() {
        Partida partida = new Partida();
        partida.iniciarPartida();

        assertTrue(partida.completarObjetivo(3, "1 + 2"));
        assertFalse(partida.completarObjetivo(3, "2 + 1"), "repetido");
        assertFalse(partida.completarObjetivo(11, "x"), "fuera de rango");
        assertFalse(partida.completarObjetivo(2.5, "x"), "no entero");
        assertEquals(List.of(3), partida.getObjetivosCompletados());
        assertEquals(List.of("3 = 1 + 2"), partida.getOperacionesRealizadas());
        assertFalse(partida.comprobarObjetivosCompletados());
    }

    @Test
    @DisplayName("Al completar los 10 objetivos se detiene el cronometro y se registra la fecha")
    void partidaCompleta() {
        Partida partida = new Partida();
        partida.iniciarPartida();
        completarTodosLosObjetivos(partida);

        assertTrue(partida.comprobarObjetivosCompletados());
        assertNotNull(partida.getFechaFinalizacion());
        assertTrue(partida.getTiempoTotalSegundos() > 0);

        double tiempo = partida.getTiempoTotalSegundos();
        partida.finalizarTiempo();
        assertEquals(tiempo, partida.getTiempoTotalSegundos(), "finalizarTiempo no cambia un tiempo ya registrado");
    }

    @Test
    @DisplayName("crearNuevoIntento usa los numeros y la dificultad de la partida")
    void crearNuevoIntento() {
        Partida partida = new Partida();
        partida.setDificultad(Partida.NivelDificultad.MEDIO);
        partida.iniciarPartida();
        Expresion intento = partida.crearNuevoIntento();

        assertEquals(partida.getNumerosDisponibles(), intento.getNumerosDisponibles());
        assertEquals(Partida.NivelDificultad.MEDIO, intento.getNivelDificultad());
        assertEquals(1, partida.getHistorialExpresiones().size());
    }

    @Test
    @DisplayName("exportarResultados genera un PNG con el resumen")
    void exportarResultados(@TempDir Path carpeta) throws Exception {
        Partida partida = new Jugador(1, "Julian").vincularNuevaPartida();
        partida.iniciarPartida();
        completarTodosLosObjetivos(partida);

        Path imagen = partida.exportarResultados(carpeta);
        assertTrue(Files.exists(imagen));
        assertTrue(imagen.getFileName().toString().startsWith("AllTen_Julian_"));
        assertTrue(Files.size(imagen) > 1000);
    }

    @Test
    @DisplayName("El constructor de la base de datos reconstruye una partida terminada")
    void constructorDesdeBaseDeDatos() {
        Jugador jugador = new Jugador(7, "Jorge");
        LocalDateTime fecha = LocalDateTime.of(2026, 10, 16, 20, 30);
        Partida partida = new Partida(5, jugador, 42, List.of("1 = 4 - 3"), fecha);

        assertEquals(5, partida.getIdPartida());
        assertEquals(jugador, partida.getJugador());
        assertEquals(42, partida.getTiempoTotalSegundos());
        assertEquals(List.of("1 = 4 - 3"), partida.getOperacionesRealizadas());
        assertEquals(fecha, partida.getFechaFinalizacion());
    }
}
