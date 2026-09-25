package co.edu.poli.Allten.Modelo;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Cronometro de una partida (relacion partida 1 - 1 cronometro en el
 * diagrama de clases).
 */
public class Cronometro {

    private LocalDateTime tiempoInicio;
    private Duration tiempoTotal;

    /** Crea un cronometro detenido, con tiempo total en cero. */
    public Cronometro() {
        this.tiempoTotal = Duration.ZERO;
    }

    /** Inicia (o reinicia) el conteo del tiempo. */
    public void iniciar() {
        this.tiempoInicio = LocalDateTime.now();
        this.tiempoTotal = Duration.ZERO;
    }

    /** Detiene el cronometro y calcula el tiempo total transcurrido. */
    public void detener() {
        if (tiempoInicio != null) {
            this.tiempoTotal = Duration.between(tiempoInicio, LocalDateTime.now());
        }
    }

    /** Calcula el tiempo transcurrido sin necesidad de llamar {@link #detener()}.
     * @return el tiempo transcurrido desde {@link #iniciar()} */
    public Duration tiempoTranscurrido() {
        if (tiempoInicio == null) {
            return Duration.ZERO;
        }
        return Duration.between(tiempoInicio, LocalDateTime.now());
    }

    /** Da acceso al instante de inicio.
     * @return el instante en que se llamo a {@link #iniciar()} por ultima vez, o {@code null} si nunca se ha iniciado */
    public LocalDateTime getTiempoInicio() {
        return tiempoInicio;
    }

    /** Da acceso al tiempo total ya calculado.
     * @return el tiempo total calculado por {@link #detener()} (Duration.ZERO si aun no se ha detenido) */
    public Duration getTiempoTotal() {
        return tiempoTotal;
    }
}
