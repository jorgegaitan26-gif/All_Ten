package controller;

import dao.IPartidaDAO;
import dao.PartidaDAOImpl;
import dao.ConexionBD;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import model.Partida;

import java.sql.SQLException;
import java.text.DateFormat;
import java.util.List;

public class MainSceneController {
    private final IPartidaDAO partidaDAO;

    public MainSceneController() {
        this(new PartidaDAOImpl());
    }

    public MainSceneController(IPartidaDAO partidaDAO) {
        this.partidaDAO = partidaDAO;
    }

    @FXML
    private void abrirInstrucciones() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/InstruccionesView.fxml"));

        Stage ventanaInstrucciones = new Stage();
        ventanaInstrucciones.setTitle("Como jugar");
        ventanaInstrucciones.setScene(new Scene(root, 800, 450));
        ventanaInstrucciones.show();
    }

    @FXML
    private void abrirJuego() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GameView.fxml"));
        Parent root = loader.load();
        GameController gameController = loader.getController();
        gameController.setPartidaDAO(partidaDAO);

        Stage ventanaJuego = new Stage();
        ventanaJuego.setTitle("All Ten - Tablero de Juego");
        ventanaJuego.setScene(new Scene(root, 520, 860));
        ventanaJuego.show();
    }

    @FXML
    private void abrirHistorial() {
        try {
            List<Partida> partidas = partidaDAO.obtenerHistorialPartidas();
            StringBuilder historial = new StringBuilder();
            DateFormat formatoFecha = DateFormat.getDateTimeInstance();

            for (Partida partida : partidas) {
                historial.append("Partida #").append(partida.getIdPartida())
                    .append(" | Jugador #").append(partida.getJugadorIdJugador())
                    .append(" | Tiempo: ").append(partida.getTiempoTotal()).append(" s")
                    .append(" | Fecha: ")
                    .append(partida.getFechaFinalizacion() == null
                        ? "Sin fecha"
                        : formatoFecha.format(partida.getFechaFinalizacion()))
                    .append("\nOperaciones: ")
                    .append(partida.getOperacionesRealizadas() == null
                        ? "Sin operaciones registradas"
                        : partida.getOperacionesRealizadas())
                    .append("\n\n");
            }

            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Historial de partidas");
            alerta.setHeaderText(partidas.isEmpty()
                ? "No hay partidas registradas"
                : "Partidas registradas: " + partidas.size());
            alerta.setContentText(partidas.isEmpty() ? "Aún no hay partidas guardadas."
                : historial.toString().trim());
            alerta.showAndWait();
        } catch (SQLException e) {
            mostrarErrorPersistencia(e);
        }
    }

    @FXML
    private void probarConexionBD() {
        try {
            ConexionBD.probarConexion();
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Conexión a la base de datos");
            alerta.setHeaderText("Conexión exitosa");
            alerta.setContentText("La aplicación se conectó a MySQL y ejecutó SELECT 1.");
            alerta.showAndWait();
        } catch (SQLException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Conexión a la base de datos");
            alerta.setHeaderText("No se pudo conectar con MySQL.");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }

    private void mostrarErrorPersistencia(SQLException e) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error de base de datos");
        alerta.setHeaderText("No se pudo acceder al historial de partidas.");
        alerta.setContentText(e.getMessage());
        alerta.showAndWait();
    }
}