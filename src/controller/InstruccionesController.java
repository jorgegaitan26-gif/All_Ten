package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.List;

public class InstruccionesController {

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

    @FXML
    public void initialize() {
        mostrarObjetivo();
    }


    // =====================================
    // OBJETIVO
    // =====================================

    @FXML
    private void mostrarObjetivo() {

        actualizarContenido(
                "Obten todos los números del 1 al 10 utilizando cuatro "
                + "dígitos iniciales que cambian según el reto.",
                null,
                btnObjetivo,
                "objetivo.png.png"
        );
    }


    // =====================================
    // OPERADORES
    // =====================================

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
                                    "/view/images/" + nombreImagen
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

    @FXML
    private void volver() {

        Stage ventanaActual =
                (Stage) btnVolver.getScene().getWindow();

        ventanaActual.close();
    }
}