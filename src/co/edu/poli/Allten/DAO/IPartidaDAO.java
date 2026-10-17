package co.edu.poli.Allten.DAO;

import java.sql.SQLException;
import java.util.List;

import co.edu.poli.Allten.Modelo.Jugador;
import co.edu.poli.Allten.Modelo.Partida;

/**
 * Contrato de persistencia para las partidas jugadas (patron DAO).
 */
public interface IPartidaDAO extends Jugador.RegistroJugadores<SQLException> {

    @Override
    Jugador buscarPorNickname(String nickname) throws SQLException;

    @Override
    Integer registrarJugador(String nickname) throws SQLException;

    /**
     * Guarda una partida jugada por el jugador indicado.
     *
     * @param partida   partida a guardar
     * @param idJugador identificador del jugador dueno de la partida
     * @throws SQLException si la base de datos no pudo guardar la partida
     */
    void guardarPartida(Partida partida, int idJugador) throws SQLException;

    /** Consulta el historial completo en formato legible para la interfaz.
     * @return registros ordenados del mas reciente al mas antiguo */
    List<String> obtenerHistorialPartidas() throws SQLException;
}
