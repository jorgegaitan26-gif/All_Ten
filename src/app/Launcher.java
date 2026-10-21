package app;

/**
 * Punto de entrada para ejecutar la aplicación desde VS Code.
 *
 * Con JavaFX cargado como dependencia de Maven, la clase principal no debe
 * extender Application; por eso esta clase solo llama a App.main.
 */
public class Launcher {

    /**
     * Inicia la aplicación All Ten.
     *
     * @param args argumentos de la línea de comandos
     */
    public static void main(String[] args) {
        App.main(args);
    }
}