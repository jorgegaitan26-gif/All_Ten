package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.util.List;

public class InstruccionesController {

    /** Un ejemplo mostrado en la caja inferior: texto + estilo visual (neutral / correcto / incorrecto). */
    private static class EjemploItem {
        final String texto;
        final String estiloCss; // nombre de la clase CSS: "ejemplo-neutral", "ejemplo-correcto" o "ejemplo-incorrecto"

        EjemploItem(String texto, String estiloCss) {
            this.texto = texto;
            this.estiloCss = estiloCss;
        }
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
                + "dígitos iniciales que cambian segun los operes.",
                null,
                btnObjetivo,
                "objetivo.png"
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
                List.of(
                        new EjemploItem("5 + 3 × 2 = 11", "ejemplo-neutral"),
                        new EjemploItem("(5 + 3) × 2 = 16", "ejemplo-neutral")
                ),
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
                List.of(
                        new EjemploItem("6 × (4 ÷ 8) − 2 = 1  \u2713", "ejemplo-correcto")
                ),
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
                List.of(
                        new EjemploItem("8 \u2212 2 = 6  \u2717", "ejemplo-incorrecto"),
                        new EjemploItem("6 + 4 = 10  \u2713", "ejemplo-correcto")
                ),
                btnComprobar,
                "comprobar.png"
        );
    }


    // =====================================
    // ACTUALIZAR CONTENIDO
    // =====================================

    private void actualizarContenido(
            String cuerpo,
            List<EjemploItem> ejemplos,
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

            for (EjemploItem item : ejemplos) {

                Label lbl = new Label(item.texto);

                lbl.getStyleClass().addAll("linea-ejemplo", item.estiloCss);

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