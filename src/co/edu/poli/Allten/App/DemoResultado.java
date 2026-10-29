package co.edu.poli.Allten.App;

import java.util.Date;
import java.util.List;

import co.edu.poli.Allten.Controlador.ResultadoController;
import co.edu.poli.Allten.Modelo.Jugador;
import co.edu.poli.Allten.Modelo.Partida;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Vista previa de la tarjeta de resultados con datos de ejemplo.
 *
 * Sirve para revisar el diseño sin jugar una partida completa. Se ejecuta con:
 * <pre>
 *     mvn javafx:run -Djavafx.mainClass=co.edu.poli.Allten.App.DemoResultado
 * </pre>
 * No toca la base de datos.
 */
public class DemoResultado extends Application {

    /** Constructor por defecto, requerido por {@link Application}. */
    public DemoResultado() {
    }

    @Override
    public void start(Stage stage) throws Exception {

        Jugador jugador = new Jugador(1, "Angie");

        Partida partida = new Partida(1, jugador, 83,
                List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), new Date());

        ResultadoController.mostrar(null, jugador, partida, List.of(
                "1 = ( 8 - 1 ) ÷ 7", "7 = 4 + 3", "10 = 9 + 1"));
    }

    /**
     * Arranca la vista previa.
     *
     * @param args argumentos de linea de comandos (no se usan)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
