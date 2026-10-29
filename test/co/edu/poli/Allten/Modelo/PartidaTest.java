package co.edu.poli.Allten.Modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PartidaTest {

    @Test
    @DisplayName("Una partida nueva empieza en FACIL, sin numeros ni objetivos")
    void partidaNueva() {
        Partida partida = new Partida(new Jugador("julian"));
        assertEquals("FACIL", partida.getNivelDificultad());
        assertTrue(partida.getNumerosDisponibles().isEmpty());
        assertTrue(partida.getOperacionesRealizadas().isEmpty());
        assertNotNull(partida.getCronometro());
        assertNull(partida.getFechaFinalizacion());
    }

    @Test
    @DisplayName("setDificultad acepta FACIL, MEDIO y DIFICIL (sin importar tildes o mayusculas)")
    void setDificultad() {
        Partida partida = new Partida(new Jugador("julian"));
        partida.setDificultad("medio");
        assertEquals("MEDIO", partida.getNivelDificultad());
        partida.setDificultad("Difícil");
        assertEquals("DIFICIL", partida.getNivelDificultad());
        assertThrows(IllegalArgumentException.class, () -> partida.setDificultad("imposible"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"FACIL", "MEDIO", "DIFICIL"})
    @DisplayName("generarNumerosValidos da 4 numeros del 1 al 9 que resuelven los 10 objetivos")
    void generarNumerosValidos(String nivel) {
        Partida partida = new Partida(new Jugador("julian"));
        partida.setDificultad(nivel);

        List<Integer> numeros = partida.generarNumerosValidos();

        assertEquals(4, numeros.size());
        for (int numero : numeros) {
            assertTrue(numero >= 1 && numero <= 9);
        }
        assertEquals(10, partida.posiblesSoluciones().size());
    }

    @Test
    @DisplayName("posiblesSoluciones devuelve expresiones que Expresion calcula al objetivo")
    void posiblesSolucionesSonCorrectas() {
        Partida partida = new Partida(new Jugador("julian"));
        partida.setNumerosDisponibles(new ArrayList<>(List.of(1, 2, 3, 4)));

        List<String> soluciones = partida.posiblesSoluciones();
        assertEquals(10, soluciones.size());

        for (int i = 0; i < soluciones.size(); i++) {
            String[] partes = soluciones.get(i).split(" = ", 2);
            assertEquals(String.valueOf(i + 1), partes[0]);

            Expresion expresion = new Expresion(partida);
            expresion.setElementosExpresion(partes[1]);
            assertEquals(i + 1, expresion.calcularResultado(), 1e-9, partes[1]);
        }
    }

    @Test
    @DisplayName("completarObjetivo solo registra enteros pendientes del 1 al 10")
    void completarObjetivo() {
        Partida partida = new Partida(new Jugador("julian"));

        assertTrue(partida.completarObjetivo(3.0));
        assertFalse(partida.completarObjetivo(3.0));   // ya logrado
        assertFalse(partida.completarObjetivo(11));    // fuera de rango
        assertFalse(partida.completarObjetivo(2.5));   // no es entero
        assertFalse(partida.completarObjetivo(Double.NaN));

        assertEquals(List.of("3"), partida.getOperacionesRealizadas());
    }

    @Test
    @DisplayName("Al completar los 10 objetivos se detiene el cronometro y se registra la fecha")
    void partidaCompleta() throws InterruptedException {
        Partida partida = new Partida(new Jugador("julian"));
        partida.getCronometro().iniciar();
        Thread.sleep(50);

        for (int objetivo = 1; objetivo <= 9; objetivo++) {
            partida.completarObjetivo(objetivo);
        }
        assertFalse(partida.comprobarObjetivosCompletados());
        assertNull(partida.getFechaFinalizacion());

        partida.completarObjetivo(10);
        assertTrue(partida.comprobarObjetivosCompletados());
        assertNotNull(partida.getFechaFinalizacion());
        assertTrue(partida.getTiempoTotal() > 0);
    }

    @Test
    @DisplayName("exportarResultados genera un PNG en la carpeta resultados")
    void exportarResultados() throws Exception {
        Partida partida = new Partida(new Jugador("prueba_export"));
        partida.setNumerosDisponibles(new ArrayList<>(List.of(1, 2, 3, 4)));
        partida.completarObjetivo(5);
        partida.setFechaFinalizacion(new Date());

        File carpeta = new File("resultados");
        int antes = carpeta.exists() ? carpeta.listFiles().length : 0;

        partida.exportarResultados();

        File[] archivos = carpeta.listFiles((dir, nombre) -> nombre.startsWith("AllTen_prueba_export_"));
        assertNotNull(archivos);
        assertTrue(archivos.length >= 1);
        assertTrue(carpeta.listFiles().length >= antes);
        for (File archivo : archivos) {
            archivo.delete();
        }
    }

    @Test
    @DisplayName("El constructor de la base de datos reconstruye una partida terminada")
    void constructorDesdeBaseDeDatos() {
        Jugador jugador = new Jugador(1, "julian");
        Date fecha = new Date();
        Partida partida = new Partida(9, jugador, 42, List.of("1", "2"), fecha);

        assertEquals(9, partida.getIdPartida());
        assertEquals(jugador, partida.getJugador());
        assertEquals(42, partida.getTiempoTotal());
        assertEquals(List.of("1", "2"), partida.getOperacionesRealizadas());
        assertEquals(fecha, partida.getFechaFinalizacion());
    }
}
