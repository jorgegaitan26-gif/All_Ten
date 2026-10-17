package controller;

import dao.IPartidaDAO;
import dao.PartidaDAOImpl;
import javafx.fxml.FXML;
import model.Jugador;
import model.Partida;

import java.sql.SQLException;

public class GameController {
    private IPartidaDAO partidaDAO = new PartidaDAOImpl();

    @FXML
    public void initialize() {
        // Lógica que se ejecuta al cargar la vista del juego GameView.fxml
        System.out.println("GameController inicializado correctamente.");
    }

    public void setPartidaDAO(IPartidaDAO partidaDAO) {
        this.partidaDAO = partidaDAO;
    }

    public void guardarPartida(Jugador jugador, Partida partida) throws SQLException {
        partidaDAO.registrarPartida(jugador, partida);
    }
}