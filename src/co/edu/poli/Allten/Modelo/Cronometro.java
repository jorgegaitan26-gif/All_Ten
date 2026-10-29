package co.edu.poli.Allten.Modelo;

/**
 * Cronometro de una partida (relacion Partida 1 - 1 Cronometro).
 *
 * Metodos del diagrama: {@link #iniciar()} y {@link #detener()}.
 */
public class Cronometro {

    /** Marca de referencia tomada con {@code System.nanoTime()} al iniciar; {@code null} si no se ha iniciado. */
    private Long tiempo_Inicio;

    /** Tiempo transcurrido, en segundos, calculado al detener. */
    private double tiempo_resultado;

    /** Crea un cronometro detenido, con tiempo_resultado en cero. */
    public Cronometro() {
        this.tiempo_Inicio = null;
        this.tiempo_resultado = 0;
    }

    /** Inicia (o reinicia) el conteo del tiempo. */
    public void iniciar() {
        this.tiempo_Inicio = System.nanoTime();
        this.tiempo_resultado = 0;
    }

    /**
     * Detiene el cronometro y calcula el tiempo transcurrido.
     *
     * @return el tiempo transcurrido en segundos; 0 si nunca se inicio
     */
    public double detener() {
        if (tiempo_Inicio == null) {
            return tiempo_resultado;
        }
        this.tiempo_resultado = (System.nanoTime() - tiempo_Inicio) / 1_000_000_000.0;
        return tiempo_resultado;
    }

    /** @return marca de inicio (System.nanoTime), o {@code null} si no se ha iniciado */
    public Long getTiempo_Inicio() {
        return tiempo_Inicio;
    }

    /** @param tiempo_Inicio nueva marca de inicio */
    public void setTiempo_Inicio(Long tiempo_Inicio) {
        this.tiempo_Inicio = tiempo_Inicio;
    }

    /** @return tiempo en segundos calculado por {@link #detener()} */
    public double getTiempo_resultado() {
        return tiempo_resultado;
    }

    /** @param tiempo_resultado nuevo tiempo en segundos */
    public void setTiempo_resultado(double tiempo_resultado) {
        this.tiempo_resultado = tiempo_resultado;
    }
}
