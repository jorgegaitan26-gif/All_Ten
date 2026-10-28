package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Jugador;
import model.Partida;

import java.util.Date;

public class MainSceneController {

    @FXML
    private void abrirInstrucciones() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/InstruccionesView.fxml"));

        Stage ventanaInstrucciones = new Stage();
        ventanaInstrucciones.setTitle("Como jugar");
        ventanaInstrucciones.setScene(new Scene(root, 800, 450));
        ventanaInstrucciones.show();
    }

    /**
     * TEMPORAL: muestra la tarjeta de resultados con datos de ejemplo.
     * Cuando exista la pantalla de juego, esta llamada debe hacerse allí,
     * solo al ganar la partida.
     */
    @FXML
    private void jugar() throws Exception {
        Jugador jugador = new Jugador(1, "Angie");
        Partida partida = new Partida(83, 1, "(8-1)+3 | 4*(2+1) | 9+1", new Date());

        ResultadoController.mostrar(null, jugador, partida);
    }
}
