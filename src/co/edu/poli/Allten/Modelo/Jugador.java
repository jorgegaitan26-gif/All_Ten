package co.edu.poli.Allten.Modelo;

/**
 * Representa al jugador de una partida de All Ten.
 *
 * Segun el diagrama de clases del juego, Jugador se relaciona con Partida
 * en una multiplicidad 1 - 0..* (un jugador puede tener muchas partidas).
 */
public class Jugador {

    private int idJugador;
    private String nickname;

    /** Crea un jugador vacio (sin id ni nickname todavia). */
    public Jugador() {
    }

    /**
     * Crea un jugador a partir de su nickname; el id se asigna despues (por ejemplo, al guardarlo).
     *
     * @param nickname nombre con el que el jugador se identifica en el juego
     */
    public Jugador(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Crea un jugador con id y nickname ya conocidos.
     *
     * @param idJugador identificador numerico del jugador
     * @param nickname  nombre con el que el jugador se identifica en el juego
     */
    public Jugador(int idJugador, String nickname) {
        this.idJugador = idJugador;
        this.nickname = nickname;
    }

    /** Da acceso al identificador del jugador.
     * @return el identificador numerico del jugador */
    public int getIdJugador() {
        return idJugador;
    }

    /**
     * Asigna el identificador numerico del jugador.
     *
     * @param idJugador nuevo identificador del jugador
     */
    public void setIdJugador(int idJugador) {
        this.idJugador = idJugador;
    }

    /** Da acceso al nickname del jugador.
     * @return el nickname con el que el jugador se identifica en el juego */
    public String getNickname() {
        return nickname;
    }

    /**
     * Cambia el nickname del jugador.
     *
     * @param nickname nuevo nickname del jugador
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /** Representa al jugador por su nickname, para mostrarlo en la interfaz.
     * @return el nickname del jugador */
    @Override
    public String toString() {
        return nickname;
    }
}
