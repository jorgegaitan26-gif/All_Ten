package dao;

import model.Jugador;
import model.Partida;
import java.sql.SQLException;
import java.util.List;

public interface IPartidaDAO {
    void registrarJugador(Jugador jugador) throws SQLException;
    void registrarPartida(Partida partida) throws SQLException;
    void registrarPartida(Jugador jugador, Partida partida) throws SQLException;
    List<Partida> obtenerHistorialPartidas() throws SQLException;
}