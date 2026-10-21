package model;

/**
 * Representa a un jugador de All Ten.
 */
public class Jugador {

    private int idJugador;
    private String nickname;

    /**
     * Crea un jugador que ya existe en la base de datos.
     *
     * @param idJugador identificador del jugador
     * @param nickname nombre con el que juega
     */
    public Jugador(int idJugador, String nickname) {
        this.idJugador = idJugador;
        this.nickname = nickname;
    }

    /**
     * Crea un jugador nuevo, aún sin identificador (lo asigna la base de datos).
     *
     * @param nickname nombre con el que juega
     */
    public Jugador(String nickname) {
        this.nickname = nickname;
    }

    /**
     * @return identificador del jugador
     */
    public int getIdJugador() {
        return idJugador;
    }

    /**
     * @param idJugador nuevo identificador del jugador
     */
    public void setIdJugador(int idJugador) {
        this.idJugador = idJugador;
    }

    /**
     * @return nickname del jugador
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * @param nickname nuevo nickname del jugador
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return "Jugador [idJugador=" + idJugador + ", nickname=" + nickname + "]";
    }
}
