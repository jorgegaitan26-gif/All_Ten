package app;

import java.util.Date;

import controller.ResultadoController;
import javafx.application.Application;
import javafx.stage.Stage;
import model.Jugador;
import model.Partida;

/**
 * Vista previa de la tarjeta de resultados con datos de ejemplo.
 *
 * Sirve para revisar el diseño sin jugar una partida completa. Se ejecuta con:
 * <pre>
 *     mvn javafx:run -Djavafx.mainClass=app.DemoResultado
 * </pre>
 * No toca la base de datos.
 */
public class DemoResultado extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Jugador jugador = new Jugador(1, "Angie");

        Partida partida = new Partida(
                83,
                jugador.getIdJugador(),
                "(8-1)+3 | 4*(2+1) | 9+1",
                new Date());

        ResultadoController.mostrar(null, jugador, partida);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
