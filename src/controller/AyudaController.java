package controller;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Panel de ayuda: muestra una lista de posibles soluciones matemáticas para
 * los números actuales del reto, para que el jugador aprenda formas nuevas de
 * resolverlo.
 *
 * Este controlador solo se encarga de lo visual: no calcula nada. Quien lo
 * llama (la pantalla del juego) obtiene las soluciones de la clase de lógica
 * y se las pasa ya listas, como texto.
 *
 * Si el panel ya está abierto, no se crea otro: se actualiza con el contenido
 * nuevo.
 */
public class AyudaController {

    private static Stage ventanaAbierta;
    private static AyudaController controladorAbierto;

    @FXML
    private Label lblNumeros;

    @FXML
    private VBox contenedorSoluciones;

    @FXML
    private Button btnCerrar;

    // =====================================
    // APERTURA
    // =====================================

    /**
     * Abre el panel de ayuda con las soluciones dadas. Si ya estaba abierto,
     * lo actualiza y lo trae al frente.
     *
     * @param numerosActuales texto con los números del reto, por ejemplo
     *        "2 · 4 · 6 · 8" (puede ser null o vacío para no mostrarlo)
     * @param soluciones soluciones a mostrar, una por elemento, por ejemplo
     *        "(4+8-6)×2 = 3" (si es null o está vacía, se muestra un aviso)
     * @param dueno ventana del juego a la que pertenece el panel (puede ser null)
     * @throws IOException si no se puede cargar AyudaView.fxml
     */
    public static void abrir(String numerosActuales, List<String> soluciones, Window dueno)
            throws IOException {

        if (ventanaAbierta != null && ventanaAbierta.isShowing()) {
            controladorAbierto.mostrar(numerosActuales, soluciones);
            ventanaAbierta.toFront();
            return;
        }

        FXMLLoader loader = new FXMLLoader(AyudaController.class.getResource("/view/AyudaView.fxml"));
        Parent raiz = loader.load();

        AyudaController controlador = loader.getController();
        controlador.mostrar(numerosActuales, soluciones);

        Stage ventana = new Stage();
        ventana.setTitle("Ayuda");
        if (dueno != null) {
            ventana.initOwner(dueno);
        }
        ventana.setScene(new Scene(raiz, 480, 560));
        ventana.setOnHidden(e -> {
            ventanaAbierta = null;
            controladorAbierto = null;
        });

        ventanaAbierta = ventana;
        controladorAbierto = controlador;

        ventana.show();
    }

    /**
     * Si el panel está abierto, lo actualiza con el contenido nuevo. Si no, no
     * hace nada. Sirve para llamarlo cuando cambia el reto.
     *
     * @param numerosActuales texto con los números del reto
     * @param soluciones soluciones a mostrar
     */
    public static void actualizarSiEstaAbierto(String numerosActuales, List<String> soluciones) {
        if (ventanaAbierta != null && ventanaAbierta.isShowing()) {
            controladorAbierto.mostrar(numerosActuales, soluciones);
        }
    }

    // =====================================
    // CONTENIDO
    // =====================================

    /**
     * Pinta en el panel los números y la lista de soluciones.
     *
     * @param numerosActuales texto con los números del reto (puede ser null)
     * @param soluciones soluciones a mostrar (puede ser null)
     */
    public void mostrar(String numerosActuales, List<String> soluciones) {

        boolean hayNumeros = numerosActuales != null && !numerosActuales.isBlank();

        lblNumeros.setText(hayNumeros ? "Números actuales:  " + numerosActuales : "");
        lblNumeros.setVisible(hayNumeros);
        lblNumeros.setManaged(hayNumeros);

        contenedorSoluciones.getChildren().clear();

        if (soluciones == null || soluciones.isEmpty()) {
            Label vacio = new Label("No hay soluciones disponibles para estos números");
            vacio.getStyleClass().add("ayuda-sin-solucion");
            contenedorSoluciones.getChildren().add(vacio);
            return;
        }

        int posicion = 1;
        for (String solucion : soluciones) {
            contenedorSoluciones.getChildren().add(crearFila(posicion++, solucion));
        }
    }

    private HBox crearFila(int posicion, String solucion) {

        Label insignia = new Label(String.valueOf(posicion));
        insignia.getStyleClass().add("ayuda-insignia");

        Label texto = new Label(solucion);
        texto.getStyleClass().add("ayuda-solucion");
        texto.setWrapText(true);

        HBox fila = new HBox(15, insignia, texto);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("ayuda-fila");
        return fila;
    }

    // =====================================
    // CERRAR
    // =====================================

    @FXML
    private void cerrar() {
        ((Stage) btnCerrar.getScene().getWindow()).close();
    }
}
