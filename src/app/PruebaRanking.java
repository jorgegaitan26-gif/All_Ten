package app;

import java.util.List;

import controller.RankingController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.EntradaRanking;
import servicios.DAOPartida;

/**
 * Ventana de prueba para ver el ranking con los datos reales de la base de
 * datos (Clever Cloud, según el archivo .env). Sirve para revisar el diseño y
 * la consulta sin pasar por el menú principal.
 *
 * Si la tabla sale vacía, es que aún no hay partidas guardadas: se pueden
 * cargar datos de ejemplo con datosEjemploRanking.sql.
 *
 * Se ejecuta desde VS Code con Run sobre esta clase. Esta clase no extiende
 * Application a propósito (ver Launcher): lanza una clase interna.
 */
public class PruebaRanking {

    /** Cantidad de partidas que se muestran en el ranking. */
    private static final int PUESTOS_RANKING = 10;

    /**
     * Inicia la ventana de prueba.
     *
     * @param args argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        Application.launch(Demo.class, args);
    }

    /** Aplicación JavaFX de la ventana de prueba. */
    public static class Demo extends Application {

        @Override
        public void start(Stage stage) {

            Label titulo = new Label("Prueba del ranking");
            titulo.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2e1b5b;");

            Button btnRanking = new Button("Ranking");
            btnRanking.setStyle("-fx-background-color: #97c459; -fx-text-fill: #2D1B69; "
                    + "-fx-font-size: 22px; -fx-font-weight: bold; -fx-background-radius: 20; "
                    + "-fx-padding: 8 24 8 24; -fx-cursor: hand;");
            btnRanking.setOnAction(e -> {
                try {
                    List<EntradaRanking> ranking = new DAOPartida().readRanking(PUESTOS_RANKING);
                    RankingController.abrir(ranking, stage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert alerta = new Alert(Alert.AlertType.ERROR,
                            "No se pudo cargar el ranking: " + ex.getMessage());
                    alerta.showAndWait();
                }
            });

            VBox raiz = new VBox(30, titulo, btnRanking);
            raiz.setAlignment(Pos.CENTER);
            raiz.setPadding(new Insets(40));
            raiz.setStyle("-fx-background-color: #f1efe8;");

            stage.setTitle("Prueba de ranking");
            stage.setScene(new Scene(raiz, 500, 320));
            stage.show();
        }
    }
}
