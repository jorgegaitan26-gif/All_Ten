package controller;

import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import model.EntradaRanking;
import servicios.DAOPartida;

public class MainSceneController {

    /** Cantidad de partidas que se muestran en el ranking. */
    private static final int PUESTOS_RANKING = 10;

    @FXML
    private Button btnRanking;

    @FXML
    private void abrirInstrucciones() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/view/InstruccionesView.fxml"));

        Stage ventanaInstrucciones = new Stage();
        ventanaInstrucciones.setTitle("Como jugar");
        ventanaInstrucciones.setScene(new Scene(root, 800, 450));
        ventanaInstrucciones.show();
    }

    @FXML
    private void abrirRanking() {
        try {
            List<EntradaRanking> ranking = new DAOPartida().readRanking(PUESTOS_RANKING);
            RankingController.abrir(ranking, btnRanking.getScene().getWindow());
        } catch (Exception e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR, "No se pudo cargar el ranking: " + e.getMessage());
            alerta.showAndWait();
        }
    }
}
