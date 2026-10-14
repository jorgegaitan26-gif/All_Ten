package model;

public class Jugador {
    private int idJugador;
    private String nickname;

    public Jugador(int idJugador, String nickname) {
        this.idJugador = idJugador;
        this.nickname = nickname;
    }

    public Jugador(String nickname) {
        this.nickname = nickname;
    }

    public int getIdJugador() {
        return idJugador;
    }

    public void setIdJugador(int idJugador) {
        this.idJugador = idJugador;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}