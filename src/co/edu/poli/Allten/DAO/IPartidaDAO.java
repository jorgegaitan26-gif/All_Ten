package co.edu.poli.Allten.DAO;

import java.util.List;

import co.edu.poli.Allten.Modelo.Partida;

/**
 * Contrato de persistencia para las partidas jugadas (patron DAO).
 */
public interface IPartidaDAO {

    /**
     * Guarda una partida jugada por el jugador indicado.
     *
     * @param partida   partida a guardar
     * @param idJugador identificador del jugador dueno de la partida
     * @return true si se guardo correctamente
     */
    boolean guardarPartida(Partida partida, int idJugador);

    /** Consulta las mejores partidas guardadas.
     * @return las partidas ordenadas de menor a mayor tiempo total */
    List<Partida> obtenerTopPuntuaciones();
}
