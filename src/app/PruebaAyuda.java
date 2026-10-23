package app;

import java.util.List;

import controller.AyudaController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Ventana de prueba para ver el diseño del panel de ayuda sin necesidad de la
 * pantalla del juego. Usa soluciones de EJEMPLO escritas a mano (no vienen de
 * ninguna clase de lógica); sirve solo para revisar cómo se ve.
 *
 * Se ejecuta desde VS Code con Run sobre esta clase. Esta clase no extiende
 * Application a propósito (ver Launcher): lanza una clase interna.
 */
public class PruebaAyuda {

    // × se escribe como \u00D7, ÷ como \u00F7 y el punto medio como \u00B7
    // para que compile igual en cualquier editor
    private static final String NUMEROS = "2  \u00B7  4  \u00B7  6  \u00B7  8";

    private static final List<String> SOLUCIONES = List.of(
            "4 \u00D7 6 \u00F7 8 - 2 = 1",
            "4 + 8 \u00F7 2 - 6 = 2",
            "(4 + 8 - 6) \u00F7 2 = 3",
            "2 + 4 + 6 - 8 = 4",
            "2 + 4 \u00D7 6 \u00F7 8 = 5",
            "2 + 6 - 8 \u00F7 4 = 6",
            "6 + 2 \u00D7 4 \u00F7 8 = 7",
            "2 + 4 + 8 - 6 = 8",
            "4 + 8 - 6 \u00F7 2 = 9",
            "2 + 6 + 8 \u00F7 4 = 10");

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

            Label titulo = new Label("Prueba del panel de ayuda");
            titulo.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #2e1b5b;");

            Button btnAyuda = new Button("Ayuda");
            btnAyuda.setStyle("-fx-background-color: #F5A623; -fx-text-fill: white; "
                    + "-fx-font-size: 22px; -fx-font-weight: bold; -fx-background-radius: 20; "
                    + "-fx-padding: 8 24 8 24; -fx-cursor: hand;");
            btnAyuda.setOnAction(e -> {
                try {
                    AyudaController.abrir(NUMEROS, SOLUCIONES, stage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            VBox raiz = new VBox(30, titulo, btnAyuda);
            raiz.setAlignment(Pos.CENTER);
            raiz.setPadding(new Insets(40));
            raiz.setStyle("-fx-background-color: #f1efe8;");

            stage.setTitle("Prueba de ayuda");
            stage.setScene(new Scene(raiz, 500, 320));
            stage.show();
        }
    }
}