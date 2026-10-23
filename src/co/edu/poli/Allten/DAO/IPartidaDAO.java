package co.edu.poli.Allten.DAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import co.edu.poli.Allten.Modelo.Partida;

/**
 * Contrato de persistencia para las partidas jugadas.
 */
public interface IPartidaDAO extends CRUD<Partida> {

    /** Consulta las partidas de un jugador, de la más reciente a la más antigua. */
    List<Partida> readByJugador(int idJugador) throws SQLException;

    /** Consulta la mejor partida de cada jugador, ordenada por menor tiempo. */
    List<Partida> readTop(int limite) throws SQLException;

    /** Consulta la mejor partida de cada jugador para un reto diario. */
    List<Partida> readTopForDate(LocalDate fechaReto, int limite) throws SQLException;

    /** Elimina todas las partidas de un jugador. */
    int deleteByJugador(int idJugador) throws SQLException;
}
