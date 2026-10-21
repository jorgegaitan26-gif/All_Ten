package model;

import java.util.Date;

/**
 * Representa una partida jugada por un jugador.
 */
public class Partida {

    private int idPartida;
    private int tiempoTotal;
    private int jugadorIdJugador;
    private String operacionesRealizadas;
    private Date fechaFinalizacion;

    /**
     * Crea una partida nueva, aún sin identificador (lo asigna la base de datos).
     *
     * @param tiempoTotal tiempo total de la partida en segundos
     * @param jugadorIdJugador identificador del jugador que la jugó
     * @param operacionesRealizadas operaciones realizadas durante la partida
     * @param fechaFinalizacion fecha y hora en que terminó
     */
    public Partida(int tiempoTotal, int jugadorIdJugador, String operacionesRealizadas, Date fechaFinalizacion) {
        this.tiempoTotal = tiempoTotal;
        this.jugadorIdJugador = jugadorIdJugador;
        this.operacionesRealizadas = operacionesRealizadas;
        this.fechaFinalizacion = fechaFinalizacion;
    }

    /**
     * Crea una partida que ya existe en la base de datos.
     *
     * @param idPartida identificador de la partida
     * @param tiempoTotal tiempo total de la partida en segundos
     * @param jugadorIdJugador identificador del jugador que la jugó
     * @param operacionesRealizadas operaciones realizadas durante la partida
     * @param fechaFinalizacion fecha y hora en que terminó
     */
    public Partida(int idPartida, int tiempoTotal, int jugadorIdJugador, String operacionesRealizadas,
            Date fechaFinalizacion) {
        this.idPartida = idPartida;
        this.tiempoTotal = tiempoTotal;
        this.jugadorIdJugador = jugadorIdJugador;
        this.operacionesRealizadas = operacionesRealizadas;
        this.fechaFinalizacion = fechaFinalizacion;
    }

    /** @return identificador de la partida */
    public int getIdPartida() { return idPartida; }

    /** @param idPartida nuevo identificador de la partida */
    public void setIdPartida(int idPartida) { this.idPartida = idPartida; }

    /** @return tiempo total en segundos */
    public int getTiempoTotal() { return tiempoTotal; }

    /** @param tiempoTotal nuevo tiempo total en segundos */
    public void setTiempoTotal(int tiempoTotal) { this.tiempoTotal = tiempoTotal; }

    /** @return identificador del jugador de la partida */
    public int getJugadorIdJugador() { return jugadorIdJugador; }

    /** @param jugadorIdJugador nuevo identificador del jugador */
    public void setJugadorIdJugador(int jugadorIdJugador) { this.jugadorIdJugador = jugadorIdJugador; }

    /** @return operaciones realizadas en la partida */
    public String getOperacionesRealizadas() { return operacionesRealizadas; }

    /** @param operacionesRealizadas nuevas operaciones realizadas */
    public void setOperacionesRealizadas(String operacionesRealizadas) {
        this.operacionesRealizadas = operacionesRealizadas;
    }

    /** @return fecha y hora de finalización */
    public Date getFechaFinalizacion() { return fechaFinalizacion; }

    /** @param fechaFinalizacion nueva fecha y hora de finalización */
    public void setFechaFinalizacion(Date fechaFinalizacion) { this.fechaFinalizacion = fechaFinalizacion; }

    @Override
    public String toString() {
        return "Partida [idPartida=" + idPartida + ", tiempoTotal=" + tiempoTotal + ", jugadorIdJugador="
                + jugadorIdJugador + ", operacionesRealizadas=" + operacionesRealizadas
                + ", fechaFinalizacion=" + fechaFinalizacion + "]";
    }
}
