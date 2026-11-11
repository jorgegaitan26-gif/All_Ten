package co.edu.poli.Allten.App;

import java.net.URL;

import javafx.scene.media.AudioClip;

/**
 * Efectos de sonido del juego (SCRUM-41).
 *
 * Los sonidos se cargan una sola vez, la primera vez que se usan. Si el
 * equipo no puede reproducir audio (sin tarjeta de sonido, sin las librerias
 * multimedia de JavaFX, etc.), el juego sigue funcionando en silencio: un
 * sonido nunca debe interrumpir una partida.
 */
public final class Sonidos {

    private static final String RUTA_ACIERTO = "/vista/sonidos/acierto.wav";
    private static final String RUTA_VICTORIA = "/vista/sonidos/victoria.wav";

    private static AudioClip acierto;
    private static AudioClip victoria;
    private static boolean disponible = true;

    private Sonidos() {
    }

    /** Sonido corto al lograr un objetivo nuevo. */
    public static void reproducirAcierto() {
        if (acierto == null) {
            acierto = cargar(RUTA_ACIERTO);
        }
        reproducir(acierto);
    }

    /** Sonido al completar los 10 objetivos. */
    public static void reproducirVictoria() {
        if (victoria == null) {
            victoria = cargar(RUTA_VICTORIA);
        }
        reproducir(victoria);
    }

    private static AudioClip cargar(String ruta) {
        if (!disponible) {
            return null;
        }
        try {
            URL url = Sonidos.class.getResource(ruta);
            if (url == null) {
                System.err.println("[Sonidos] No se encontro " + ruta);
                return null;
            }
            return new AudioClip(url.toExternalForm());
        } catch (Throwable e) {
            // Sin soporte de audio: se desactivan los sonidos y se sigue jugando.
            disponible = false;
            System.err.println("[Sonidos] Audio no disponible: " + e.getMessage());
            return null;
        }
    }

    private static void reproducir(AudioClip clip) {
        if (clip == null) {
            return;
        }
        try {
            clip.play();
        } catch (Throwable e) {
            System.err.println("[Sonidos] No se pudo reproducir: " + e.getMessage());
        }
    }
}
