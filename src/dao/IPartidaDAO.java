package dao;

import model.Jugador;
import model.Partida;
import java.util.List;

public interface IPartidaDAO {
    void registrarJugador(Jugador jugador);
    void registrarPartida(Partida partida);
    List<Partida> obtenerHistorialPartidas();
}