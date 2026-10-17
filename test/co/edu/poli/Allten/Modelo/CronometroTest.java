package co.edu.poli.Allten.Modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Pruebas del diagrama de flujo de la clase Cronometro. */
class CronometroTest {

    @Test
    @DisplayName("Un cronometro nuevo no tiene inicio y su resultado es 0")
    void cronometroNuevo() {
        Cronometro cronometro = new Cronometro();
        assertNull(cronometro.getTiempo_Inicio());
        assertEquals(0, cronometro.getTiempo_resultado());
    }

    @Test
    @DisplayName("detener() sin iniciar() retorna 0")
    void detenerSinIniciar() {
        assertEquals(0, new Cronometro().detener());
    }

    @Test
    @DisplayName("iniciar() guarda tiempo_Inicio y pone tiempo_resultado en 0")
    void iniciar() {
        Cronometro cronometro = new Cronometro();
        cronometro.iniciar();
        assertNotNull(cronometro.getTiempo_Inicio());
        assertEquals(0, cronometro.getTiempo_resultado());
    }

    @Test
    @DisplayName("detener() calcula los segundos transcurridos y los guarda en tiempo_resultado")
    void detenerCalculaSegundos() throws InterruptedException {
        Cronometro cronometro = new Cronometro();
        cronometro.iniciar();
        Thread.sleep(150);
        double segundos = cronometro.detener();

        assertTrue(segundos >= 0.14 && segundos < 2, "segundos = " + segundos);
        assertEquals(segundos, cronometro.getTiempo_resultado());
    }

    @Test
    @DisplayName("tiempoTranscurrido() avanza mientras el cronometro corre")
    void tiempoTranscurrido() throws InterruptedException {
        Cronometro cronometro = new Cronometro();
        cronometro.iniciar();
        Thread.sleep(50);
        assertTrue(cronometro.tiempoTranscurrido().toMillis() >= 40);
    }
}
