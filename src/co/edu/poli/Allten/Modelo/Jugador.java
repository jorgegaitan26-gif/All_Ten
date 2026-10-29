package co.edu.poli.Allten.Modelo;

/**
 * Representa a la persona que juega All Ten. Hace parte de la Partida
 * (relacion Jugador 1 - 0..* Partida del diagrama de clases).
 *
 * Segun el diagrama, Jugador solo tiene atributos (nickname e id_Jugador)
 * y no tiene metodos propios; aqui solo se incluyen constructores,
 * getters y setters.
 */
public class Jugador {

    /** Nombre con el que el jugador se identifica en el juego (VARCHAR(45)). */
    private String nickname;

    /** Identificador del jugador en la base de datos; {@code null} hasta que se registra. */
    private Integer id_Jugador;

    /**
     * Crea un jugador nuevo, aun sin identificador (lo asigna la base de datos).
     *
     * @param nickname nombre con el que juega
     */
    public Jugador(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Crea un jugador que ya existe en la base de datos.
     *
     * @param id_Jugador identificador del jugador
     * @param nickname   nombre con el que juega
     */
    public Jugador(Integer id_Jugador, String nickname) {
        this.id_Jugador = id_Jugador;
        this.nickname = nickname;
    }

    /** @return nickname del jugador */
    public String getNickname() {
        return nickname;
    }

    /** @param nickname nuevo nickname del jugador */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /** @return identificador del jugador, o {@code null} si no se ha registrado */
    public Integer getIdJugador() {
        return id_Jugador;
    }

    /** @param id_Jugador nuevo identificador del jugador */
    public void setIdJugador(Integer id_Jugador) {
        this.id_Jugador = id_Jugador;
    }
}
