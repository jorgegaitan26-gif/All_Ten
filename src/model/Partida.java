package model;

import java.util.Date;

public class Partida {
    private int idPartida;
    private int tiempoTotal;
    private int jugadorIdJugador;
    private String operacionesRealizadas;
    private Date fechaFinalizacion;

    public Partida(int tiempoTotal, int jugadorIdJugador, String operacionesRealizadas, Date fechaFinalizacion) {
        this.tiempoTotal = tiempoTotal;
        this.jugadorIdJugador = jugadorIdJugador;
        this.operacionesRealizadas = operacionesRealizadas;
        this.fechaFinalizacion = fechaFinalizacion;
    }

    public Partida(int idPartida, int tiempoTotal, int jugadorIdJugador, String operacionesRealizadas, Date fechaFinalizacion) {
        this.idPartida = idPartida;
        this.tiempoTotal = tiempoTotal;
        this.jugadorIdJugador = jugadorIdJugador;
        this.operacionesRealizadas = operacionesRealizadas;
        this.fechaFinalizacion = fechaFinalizacion;
    }

    // Getters y Setters
    public int getIdPartida() { return idPartida; }
    public void setIdPartida(int idPartida) { this.idPartida = idPartida; }

    public int getTiempoTotal() { return tiempoTotal; }
    public void setTiempoTotal(int tiempoTotal) { this.tiempoTotal = tiempoTotal; }

    public int getJugadorIdJugador() { return jugadorIdJugador; }
    public void setJugadorIdJugador(int jugadorIdJugador) { this.jugadorIdJugador = jugadorIdJugador; }

    public String getOperacionesRealizadas() { return operacionesRealizadas; }
    public void setOperacionesRealizadas(String operacionesRealizadas) { this.operacionesRealizadas = operacionesRealizadas; }

    public Date getFechaFinalizacion() { return fechaFinalizacion; }
    public void setFechaFinalizacion(Date fechaFinalizacion) { this.fechaFinalizacion = fechaFinalizacion; }
}