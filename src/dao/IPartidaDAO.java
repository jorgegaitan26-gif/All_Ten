package dao;

import java.util.List;
import model.Partida;

public interface IPartidaDAO {

    boolean guardarPartida(Partida partida, int idJugador);

    List<Partida> obtenerTopPuntuaciones();

}