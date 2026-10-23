package co.edu.poli.Allten.DAO;

import java.sql.SQLException;

import co.edu.poli.Allten.Modelo.Jugador;

/** Contrato de persistencia para jugadores. */
public interface IJugadorDAO extends CRUD<Jugador>, Jugador.RegistroJugadores<SQLException> {

    /** Busca un jugador por nickname. */
    Jugador readByNickname(String nickname) throws SQLException;

    @Override
    default Jugador buscarPorNickname(String nickname) throws SQLException {
        return readByNickname(nickname);
    }

    @Override
    default Integer registrarJugador(String nickname) throws SQLException {
        Jugador jugador = create(new Jugador(nickname));
        return jugador.getIdJugador();
    }
}
