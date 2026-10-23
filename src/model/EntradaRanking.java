package model;

import java.util.Date;

/**
 * Representa una fila del ranking: una partida con el nombre de quien la jugó,
 * su tiempo y la fecha y hora exactas en que terminó.
 */
public class EntradaRanking {

    private final int posicion;
    private final String nickname;
    private final int tiempoTotal;
    private final Date fechaFinalizacion;

    /**
     * Crea una fila del ranking.
     *
     * @param posicion puesto en el ranking (1 es el más rápido)
     * @param nickname nombre con el que juega el jugador
     * @param tiempoTotal tiempo total de la partida en segundos
     * @param fechaFinalizacion fecha y hora en que terminó la partida
     */
    public EntradaRanking(int posicion, String nickname, int tiempoTotal, Date fechaFinalizacion) {
        this.posicion = posicion;
        this.nickname = nickname;
        this.tiempoTotal = tiempoTotal;
        this.fechaFinalizacion = fechaFinalizacion;
    }

    /** @return puesto en el ranking (1 es el más rápido) */
    public int getPosicion() { return posicion; }

    /** @return nickname del jugador */
    public String getNickname() { return nickname; }

    /** @return tiempo total de la partida en segundos */
    public int getTiempoTotal() { return tiempoTotal; }

    /** @return fecha y hora en que terminó la partida */
    public Date getFechaFinalizacion() { return fechaFinalizacion; }

    @Override
    public String toString() {
        return "EntradaRanking [posicion=" + posicion + ", nickname=" + nickname + ", tiempoTotal=" + tiempoTotal
                + ", fechaFinalizacion=" + fechaFinalizacion + "]";
    }
}
