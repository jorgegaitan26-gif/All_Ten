package co.edu.poli.Allten.Modelo;

import java.time.Duration;

/**
 * Cronometro de una partida (relacion partida 1 - 1 cronometro en el
 * diagrama de clases).
 *
 * Implementa el diagrama de flujo "Clase: Cronometro":
 * <ul>
 *   <li>{@link #iniciar()}: toma la marca de referencia con
 *       {@code System.nanoTime()}, la guarda en {@code tiempo_Inicio} y pone
 *       {@code tiempo_resultado} en 0.</li>
 *   <li>{@link #detener()}: captura la marca actual, calcula la diferencia
 *       respecto a {@code tiempo_Inicio}, la convierte a segundos, la guarda
 *       en {@code tiempo_resultado} y la retorna hacia
 *       {@code Partida.tiempoTotal}.</li>
 * </ul>
 */
public class Cronometro {

    /** Nanosegundos que tiene un segundo, usados para la conversion de {@link #detener()}. */
    private static final double NANOS_POR_SEGUNDO = 1_000_000_000.0;

    // ----- Atributos del diagrama (Tarea 1) -----

    /** Referencia en nanosegundos obtenida con {@code System.nanoTime()} al arrancar el juego. */
    private Long tiempo_Inicio;

    /** Tiempo transcurrido convertido a segundos (ej. 47.35), listo para pasarse a la partida. */
    private double tiempo_resultado;

    /** Crea un cronometro detenido, con tiempo_resultado en cero. */
    public Cronometro() {
        this.tiempo_Inicio = null;
        this.tiempo_resultado = 0;
    }

    /**
     * Inicia (o reinicia) el conteo del tiempo. Se ejecuta una vez que los
     * numeros de la partida ya estan listos.
     */
    public void iniciar() {
        // [A] Inicio: solicitud de iniciar cronometro
        // [B] Obtener marca de referencia con System.nanoTime
        long marcaReferencia = System.nanoTime();

        // [C] Asignar valor a tiempo_Inicio y poner tiempo_resultado en 0
        this.tiempo_Inicio = marcaReferencia;
        this.tiempo_resultado = 0;

        // [D] Fin: cronometro activo midiendo tiempo en segundo plano
    }

    /**
     * Detiene el cronometro. Se activa cuando la partida detecta que los
     * diez objetivos fueron completados.
     *
     * @return el tiempo transcurrido en segundos (tiempo_resultado), que la
     *         partida guarda en su atributo tiempoTotal; 0 si nunca se inicio
     */
    public double detener() {
        // [E] Inicio: solicitud de detener cronometro
        if (tiempo_Inicio == null) {
            return tiempo_resultado;
        }

        // [F] Capturar marca de tiempo actual con System.nanoTime
        long marcaActual = System.nanoTime();

        // [G] Calcular diferencia respecto a tiempo_Inicio
        long diferenciaNanos = marcaActual - tiempo_Inicio;

        // [H] Convertir el valor calculado a segundos
        double segundos = diferenciaNanos / NANOS_POR_SEGUNDO;

        // [I] Almacenar el valor en tiempo_resultado
        this.tiempo_resultado = segundos;

        // [J] Retornar tiempo_resultado (double) hacia Partida.tiempoTotal
        // [K] Fin: cronometro detenido
        return tiempo_resultado;
    }

    /**
     * Calcula el tiempo transcurrido sin detener el cronometro (lo usa la
     * vista para refrescar el reloj en pantalla cada segundo).
     *
     * @return el tiempo transcurrido desde {@link #iniciar()}, o cero si no se ha iniciado
     */
    public Duration tiempoTranscurrido() {
        if (tiempo_Inicio == null) {
            return Duration.ZERO;
        }
        return Duration.ofNanos(System.nanoTime() - tiempo_Inicio);
    }

    /** Da acceso a la marca de inicio.
     * @return el valor de System.nanoTime() tomado en {@link #iniciar()}, o {@code null} si nunca se ha iniciado */
    public Long getTiempo_Inicio() {
        return tiempo_Inicio;
    }

    /** Da acceso al tiempo calculado al detener.
     * @return el tiempo en segundos calculado por {@link #detener()} (0 si aun no se ha detenido) */
    public double getTiempo_resultado() {
        return tiempo_resultado;
    }
}