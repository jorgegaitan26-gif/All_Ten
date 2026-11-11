package co.edu.poli.Allten.App;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.TimeZone;

/**
 * Punto de entrada de la aplicacion All Ten.
 *
 * Arranca JavaFX y carga la primera pantalla (el menu principal). Desde
 * ahi, la navegacion entre pantallas la manejan los controladores
 * (ver {@code co.edu.poli.Allten.Controlador}).
 */
public class App extends Application {
        /** Todas las fechas y horas del juego se manejan en hora de Colombia, sin depender del computador. */
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
    }

    /** Constructor por defecto, requerido por {@link Application}; JavaFX lo invoca internamente. */
    public App() {
    }

    /**
     * Metodo de arranque de JavaFX: carga MainMenuView.fxml y lo muestra
     * en la ventana (Stage) principal.
     *
     * @param stage ventana principal, provista por el framework de JavaFX
     * @throws Exception si el archivo FXML del menu principal no se puede cargar
     */
    @Override
    public void start(Stage stage) throws Exception {

        // Fuente Poppins para todas las pantallas (ver src/vista/fonts)
        Font.loadFont(App.class.getResourceAsStream("/vista/fonts/Poppins-Regular.ttf"), 12);
        Font.loadFont(App.class.getResourceAsStream("/vista/fonts/Poppins-Bold.ttf"), 12);

        // La aplicacion inicia en el menu principal
        Parent root = FXMLLoader.load(
                getClass().getResource("/vista/MainMenuView.fxml")
        );
        Scene scene = new Scene(root, 1000, 700);

        stage.setScene(scene);
        stage.setTitle("All Ten");
        stage.show();
    }

    /**
     * Metodo main de la aplicacion.
     *
     * @param args argumentos de linea de comandos (no se usan)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
