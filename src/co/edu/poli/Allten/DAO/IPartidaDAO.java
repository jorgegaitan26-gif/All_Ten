package co.edu.poli.Allten.DAO;

import co.edu.poli.Allten.Modelo.Jugador;
import co.edu.poli.Allten.Modelo.Partida;
import java.sql.SQLException;
import java.util.List;

public interface IPartidaDAO {
    void registrarJugador(Jugador jugador) throws SQLException;
    void registrarPartida(Partida partida) throws SQLException;
    void registrarPartida(Jugador jugador, Partida partida) throws SQLException;
    List<Partida> obtenerHistorialPartidas() throws SQLException;
}