package co.edu.poli.Allten.Modelo;

/**
 * Representa a la persona que utiliza el sistema. Su responsabilidad se
 * limita a identificar al usuario y relacionarlo con las partidas que
 * realiza (relacion Jugador 1 - 0..* Partida).
 *
 * Implementa el diagrama de flujo de la clase Jugador:
 * {@link #identificarJugador(String, RegistroJugadores)} (captura de
 * identidad, validacion de existencia y rama de seleccion) y
 * {@link #vincularNuevaPartida()} (asociacion de partida).
 */
public class Jugador {

    /** Longitud maxima del nickname (columna VARCHAR(45) de la tabla Jugador). */
    public static final int LONGITUD_MAXIMA_NICKNAME = 45;

    /**
     * Contrato con la base de datos que necesita el flujo del jugador. Lo
     * implementa el DAO correspondiente (consultas UC-01 y UC-02).
     */
    public interface RegistroJugadores<E extends Exception> {

        /**
         * UC-01: {@code SELECT * FROM Jugador WHERE nickname = ?}
         *
         * @param nickname nickname a buscar
         * @return el jugador existente con su id_Jugador, o {@code null} si no existe
         */
        Jugador buscarPorNickname(String nickname) throws E;

        /**
         * UC-02: {@code INSERT INTO Jugador (nickname) VALUES (?)}
         *
         * @param nickname nickname del nuevo jugador
         * @return el id_Jugador autoincremental generado por la base de datos
         */
        Integer registrarJugador(String nickname) throws E;
    }

    // ----- Atributos del diagrama (Tarea 1) -----

    /** Identificador unico del jugador (llave primaria); {@code null} hasta que se registra. */
    private Integer id_Jugador;

    /** Nombre visible que el usuario utiliza dentro del juego (VARCHAR(45)). */
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
     * @param id_Jugador identificador numerico del jugador
     * @param nickname   nombre con el que el jugador se identifica en el juego
     */
    public Jugador(Integer id_Jugador, String nickname) {
        this.id_Jugador = id_Jugador;
        this.nickname = nickname;
    }

    // =====================================================================
    // TAREA 2: FLUJO OPERATIVO DEL JUGADOR
    // =====================================================================

    /**
     * Crea o identifica al jugador por su nickname al iniciar una sesion.
     *
     * @param nickname  nickname ingresado por el usuario en el menu principal
     * @param registro  acceso a la tabla Jugador (DAO)
     * @return la instancia activa del jugador, con su id_Jugador asignado
     * @throws IllegalArgumentException si el nickname esta vacio o supera 45 caracteres
     */
    public static <E extends Exception> Jugador identificarJugador(
            String nickname, RegistroJugadores<E> registro) throws E {

        // [A] Inicio: sesion de usuario
        // [B] Ingresar nickname del jugador
        String nicknameLimpio = nickname == null ? "" : nickname.trim();
        if (nicknameLimpio.isEmpty() || nicknameLimpio.length() > LONGITUD_MAXIMA_NICKNAME) {
            throw new IllegalArgumentException(
                    "El nickname debe tener entre 1 y " + LONGITUD_MAXIMA_NICKNAME + " caracteres");
        }

        Jugador jugadorActivo;

        // [C] ¿El jugador ya existe en la base de datos?
        Jugador existente = registro.buscarPorNickname(nicknameLimpio);

        if (existente != null) {
            // [D] Si: cargar datos existentes del jugador (id_Jugador y nickname)
            jugadorActivo = new Jugador(existente.getIdJugador(), existente.getNickname());
        } else {
            // [E] No: registrar nuevo jugador en la base de datos
            // [F] Generar y asignar nuevo id_Jugador automatico
            Integer nuevoId = registro.registrarJugador(nicknameLimpio);
            jugadorActivo = new Jugador(nuevoId, nicknameLimpio);
        }

        // [G] Establecer la instancia activa del jugador
        return jugadorActivo;
    }

    /**
     * [H] Vincula este jugador con la nueva partida a crear (UC-03: la
     * partida queda con la llave foranea Jugador_id_Jugador).
     *
     * @return la nueva partida, vinculada a este jugador
     */
    public Partida vincularNuevaPartida() {
        Partida partida = new Partida(this);
        // [I] Fin: jugador listo para iniciar partida
        return partida;
    }

    // =====================================================================
    // ACCESO A LOS DATOS
    // =====================================================================

    /** Da acceso al identificador del jugador.
     * @return el id_Jugador, o {@code null} si todavia no se ha registrado */
    public Integer getIdJugador() {
        return id_Jugador;
    }

    /**
     * Asigna el identificador numerico del jugador.
     *
     * @param id_Jugador nuevo identificador del jugador
     */
    public void setIdJugador(Integer id_Jugador) {
        this.id_Jugador = id_Jugador;
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