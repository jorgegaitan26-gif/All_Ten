package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainSceneController {

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
        Parent root = FXMLLoader.load(getClass().getResource("/view/GameView.fxml"));

        Stage ventanaJuego = new Stage();
        ventanaJuego.setTitle("All Ten - Tablero de Juego");
        ventanaJuego.setScene(new Scene(root, 520, 860));
        ventanaJuego.show();
    }
}