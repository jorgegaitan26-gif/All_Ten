package controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.imageio.ImageIO;

import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Transform;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import model.Jugador;
import model.Partida;

/**
 * Controla la tarjeta de resultados que se muestra al terminar con éxito una
 * partida. La tarjeta está pensada para compartirse como imagen PNG: el
 * jugador puede guardarla o copiarla al portapapeles.
 *
 * Uso desde el juego, solo cuando la partida termina con éxito:
 * <pre>
 *     ResultadoController.mostrar(ventanaActual, jugador, partida);
 * </pre>
 */
public class ResultadoController {

    /** Factor de escala de la imagen exportada (2 = el doble de nítida). */
    private static final double ESCALA_EXPORTACION = 2.0;

    /** Cantidad de números que el jugador debe obtener (1 al 10). */
    private static final int TOTAL_NUMEROS = 10;

    @FXML
    private VBox tarjeta;

    @FXML
    private Label lblTiempo;

    @FXML
    private Label lblCategoria;

    @FXML
    private FlowPane panelNumeros;

    @FXML
    private Label lblJugador;

    @FXML
    private Label lblFecha;

    @FXML
    private VBox cajaOperaciones;

    @FXML
    private Label lblOperaciones;

    @FXML
    private Button btnGuardar;

    @FXML
    private Label lblEstado;

    private final PauseTransition limpiarEstado = new PauseTransition(Duration.seconds(3));


    // =====================================
    // APERTURA DE LA TARJETA
    // =====================================

    /**
     * Abre la ventana con la tarjeta de resultados de una partida ganada.
     *
     * @param propietario ventana desde la que se abre (puede ser null)
     * @param jugador jugador que terminó la partida
     * @param partida partida finalizada con éxito
     * @throws IOException si no se puede cargar la vista
     */
    public static void mostrar(Window propietario, Jugador jugador, Partida partida) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                ResultadoController.class.getResource("/view/ResultadoView.fxml"));

        Parent root = loader.load();

        ResultadoController controlador = loader.getController();
        controlador.setResultado(jugador, partida);

        Stage ventana = new Stage();
        ventana.setTitle("Resultado - All Ten");
        ventana.setScene(new Scene(root));
        ventana.setResizable(false);

        if (propietario != null) {
            ventana.initOwner(propietario);
            ventana.initModality(Modality.WINDOW_MODAL);
        }

        ventana.show();
    }


    // =====================================
    // INICIO
    // =====================================

    @FXML
    public void initialize() {

        // Los 10 números conseguidos, uno por círculo
        for (int i = 1; i <= TOTAL_NUMEROS; i++) {

            Label bola = new Label(String.valueOf(i));
            bola.getStyleClass().add("bola-numero");
            bola.setAlignment(Pos.CENTER);

            panelNumeros.getChildren().add(bola);
        }

        limpiarEstado.setOnFinished(e -> lblEstado.setText(""));
    }


    // =====================================
    // DATOS DE LA TARJETA
    // =====================================

    /**
     * Rellena la tarjeta con los datos de la partida.
     *
     * @param jugador jugador que terminó la partida
     * @param partida partida finalizada con éxito
     */
    public void setResultado(Jugador jugador, Partida partida) {

        int segundos = Math.max(0, partida.getTiempoTotal());

        lblTiempo.setText(formatearTiempo(segundos));
        lblCategoria.setText(categoria(segundos));

        String nickname = (jugador == null || jugador.getNickname() == null
                || jugador.getNickname().isBlank())
                        ? "Jugador anónimo"
                        : jugador.getNickname().trim();
        lblJugador.setText(nickname);

        Date fecha = partida.getFechaFinalizacion() != null
                ? partida.getFechaFinalizacion()
                : new Date();
        lblFecha.setText(new SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-CO"))
                .format(fecha));

        String operaciones = partida.getOperacionesRealizadas();
        boolean hayOperaciones = operaciones != null && !operaciones.isBlank();

        lblOperaciones.setText(hayOperaciones ? operaciones.trim() : "");
        cajaOperaciones.setVisible(hayOperaciones);
        cajaOperaciones.setManaged(hayOperaciones);
    }

    /**
     * Convierte segundos a formato mm:ss (por ejemplo, 83 pasa a 01:23).
     *
     * @param segundos tiempo total en segundos
     * @return tiempo con formato mm:ss
     */
    static String formatearTiempo(int segundos) {
        return String.format("%02d:%02d", segundos / 60, segundos % 60);
    }

    /**
     * Clasifica la velocidad de la partida para darle un título al jugador.
     *
     * @param segundos tiempo total en segundos
     * @return título de velocidad
     */
    static String categoria(int segundos) {

        if (segundos <= 60) {
            return "VELOCIDAD RAYO";
        }
        if (segundos <= 120) {
            return "MUY VELOZ";
        }
        if (segundos <= 300) {
            return "RITMO CONSTANTE";
        }
        return "RETO COMPLETADO";
    }


    // =====================================
    // COMPARTIR
    // =====================================

    @FXML
    private void guardarImagen() {

        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar tarjeta de resultados");
        selector.setInitialFileName("all-ten-resultado.png");
        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagen PNG", "*.png"));

        File destino = selector.showSaveDialog(btnGuardar.getScene().getWindow());

        if (destino == null) {
            return;
        }

        if (!destino.getName().toLowerCase().endsWith(".png")) {
            destino = new File(destino.getParentFile(), destino.getName() + ".png");
        }

        try {
            ImageIO.write(aBufferedImage(capturarTarjeta()), "png", destino);
            mostrarEstado("Imagen guardada. ¡Ya puedes compartirla!");

        } catch (IOException e) {
            mostrarEstado("No se pudo guardar la imagen.");
        }
    }

    @FXML
    private void copiarImagen() {

        ClipboardContent contenido = new ClipboardContent();
        contenido.putImage(capturarTarjeta());

        Clipboard.getSystemClipboard().setContent(contenido);

        mostrarEstado("Imagen copiada. Pégala donde quieras.");
    }

    @FXML
    private void cerrar() {

        Stage ventana = (Stage) btnGuardar.getScene().getWindow();

        ventana.close();
    }

    /**
     * Captura solo la tarjeta (sin botones ni mensajes) con el doble de
     * resolución y esquinas transparentes.
     */
    private WritableImage capturarTarjeta() {

        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.TRANSPARENT);
        parametros.setTransform(Transform.scale(ESCALA_EXPORTACION, ESCALA_EXPORTACION));

        return tarjeta.snapshot(parametros, null);
    }

    /**
     * Convierte la imagen de JavaFX a BufferedImage para escribirla como PNG
     * sin depender del módulo javafx-swing.
     */
    private static BufferedImage aBufferedImage(WritableImage imagen) {

        int ancho = (int) imagen.getWidth();
        int alto = (int) imagen.getHeight();

        BufferedImage salida = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
        PixelReader lector = imagen.getPixelReader();

        for (int y = 0; y < alto; y++) {
            for (int x = 0; x < ancho; x++) {
                salida.setRGB(x, y, lector.getArgb(x, y));
            }
        }

        return salida;
    }

    private void mostrarEstado(String mensaje) {

        lblEstado.setText(mensaje);
        limpiarEstado.playFromStart();
    }
}
