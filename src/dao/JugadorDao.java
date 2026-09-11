package dao;

import model.Jugador;

public interface IJugadorDAO {

    boolean guardarJugador(Jugador jugador);

    Jugador obtenerJugador(int idJugador);
}
