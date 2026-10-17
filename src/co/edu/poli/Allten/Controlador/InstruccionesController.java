package co.edu.poli.Allten.Controlador;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controlador de la pantalla "Como jugar" (InstruccionesView.fxml).
 *
 * Muestra, en un panel lateral con categorias (Objetivo, Operadores,
 * Tiempo, Numeros, Como comprobar), el texto y la imagen correspondientes
 * a cada categoria. Es una pantalla puramente informativa: no depende del
 * Modelo ni de los Servicios del juego.
 */
public class InstruccionesController {

    /** Constructor por defecto; JavaFX lo invoca al cargar el FXML de esta pantalla. */
    public InstruccionesController() {
    }

    @FXML
    private VBox menuLateral;

    @FXML
    private Button btnObjetivo;

    @FXML
    private Button btnOperadores;

    @FXML
    private Button btnTiempo;

    @FXML
    private Button btnNumeros;

    @FXML
    private Button btnComprobar;

    @FXML
    private Button btnVolver;

    @FXML
    private TextFlow contenidoTexto;

    @FXML
    private Text contenidoTextNode;

    @FXML
    private VBox cajaEjemplo;

    @FXML
    private ImageView imagenContenido;


    // =====================================
    // INICIO
    // =====================================

    /** Se ejecuta al cargar el FXML: muestra la categoria "Objetivo" por defecto. */
    @FXML
    public void initialize() {
        mostrarObjetivo();
    }


    // =====================================
    // OBJETIVO
    // =====================================

    /** Muestra la explicacion del objetivo del juego. */
    @FXML
    private void mostrarObjetivo() {

        actualizarContenido(
                "Obten todos los números del 1 al 10 utilizando cuatro "
                + "dígitos iniciales que cambian según el reto.",
                null,
                btnObjetivo,
                "objetivo.png"
        );
    }


    // =====================================
    // OPERADORES
    // =====================================

    /** Muestra que operadores estan permitidos y como usar parentesis. */
    @FXML
    private void mostrarOperadores() {

        actualizarContenido(
                "Solo puedes usar suma (+), resta (-), multiplicación (×) y "
                + "división (÷). Usa paréntesis para alterar el orden de las "
                + "operaciones si lo necesitas.",
                null,
                btnOperadores,
                "operadores.png"
        );
    }


    // =====================================
    // TIEMPO
    // =====================================

    /** Muestra la explicacion sobre el cronometro/tiempo del reto. */
    @FXML
    private void mostrarTiempo() {

        actualizarContenido(
                "Resuelve el reto en el menor tiempo posible.",
                null,
                btnTiempo,
                "tiempo.png"
        );
    }


    // =====================================
    // NÚMEROS
    // =====================================

    /** Muestra la explicacion sobre el uso de los 4 numeros disponibles. */
    @FXML
    private void mostrarNumeros() {

        actualizarContenido(
                "Usa los 4 números dados en cada operación.",
                null,
                btnNumeros,
                "numeros.png"
        );
    }


    // =====================================
    // COMPROBAR
    // =====================================

    /** Muestra la explicacion sobre como y cuando se comprueba el resultado. */
    @FXML
    private void mostrarComprobar() {

        actualizarContenido(
                "El resultado aparece luego de operar todos los números "
                + "en pantalla.",
                null,
                btnComprobar,
                "comprobar.png"
        );
    }


    // =====================================
    // ACTUALIZAR CONTENIDO
    // =====================================

    /**
     * Actualiza el panel de contenido con el texto, la imagen y (si aplica)
     * la lista de ejemplos de la categoria seleccionada, y resalta el boton
     * de esa categoria en el menu lateral.
     *
     * @param cuerpo       texto explicativo a mostrar
     * @param ejemplos     lineas de ejemplo a listar, o {@code null} si la categoria no tiene
     * @param activo       boton del menu lateral que debe quedar marcado como activo
     * @param nombreImagen nombre del archivo de imagen dentro de /vista/images
     */
    private void actualizarContenido(
            String cuerpo,
            List<String> ejemplos,
            Button activo,
            String nombreImagen) {

        // Cambiar texto
        contenidoTextNode.setText(cuerpo);


        // =====================================
        // CARGAR IMAGEN
        // =====================================

        try {

            Image imagen = new Image(
                    getClass()
                            .getResource(
                                    "/vista/images/" + nombreImagen
                            )
                            .toExternalForm()
            );

            imagenContenido.setImage(imagen);

        } catch (Exception e) {

            System.out.println(
                    "ERROR: no se pudo cargar la imagen: "
                    + nombreImagen
            );

            imagenContenido.setImage(null);
        }


        // =====================================
        // EJEMPLOS
        // =====================================

        cajaEjemplo.getChildren().clear();

        if (ejemplos != null) {

            for (String linea : ejemplos) {

                javafx.scene.control.Label lbl =
                        new javafx.scene.control.Label(linea);

                lbl.getStyleClass().add("linea-ejemplo");

                cajaEjemplo.getChildren().add(lbl);
            }

            cajaEjemplo.setVisible(true);
            cajaEjemplo.setManaged(true);

        } else {

            cajaEjemplo.setVisible(false);
            cajaEjemplo.setManaged(false);
        }


        // =====================================
        // BOTÓN ACTIVO
        // =====================================

        for (var nodo : menuLateral.getChildren()) {

            nodo.getStyleClass()
                    .remove("btn-categoria-activo");
        }

        activo.getStyleClass()
                .add("btn-categoria-activo");
    }


    // =====================================
    // VOLVER
    // =====================================

    /** Cierra la ventana de instrucciones y regresa al menu principal. */
    @FXML
    private void volver() {

        Stage ventanaActual =
                (Stage) btnVolver.getScene().getWindow();

        ventanaActual.close();
    }
}
