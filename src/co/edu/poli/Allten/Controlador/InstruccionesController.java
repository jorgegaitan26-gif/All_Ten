package co.edu.poli.Allten.Controlador;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Controlador de la pantalla "¿Como jugar?" (InstruccionesView.fxml).
 *
 * Muestra, en un menu lateral con categorias (Objetivo, Numeros, Operadores,
 * Como comprobar, Tiempo), el texto, la imagen y un ejemplo de cada una.
 * Es una pantalla puramente informativa: no depende del Modelo del juego.
 *
 * Se abre con {@link #mostrar(Window)} tanto desde el menu principal como
 * desde la pantalla de juego (reto diario y partida libre).
 */
public class InstruccionesController {

    /** Una categoria del menu lateral. */
    private record Categoria(String titulo, String texto, List<String> ejemplos, String imagen) {
    }

    /** Categorias en el mismo orden que los botones del menu lateral. */
    private static final List<Categoria> CATEGORIAS = List.of(
            new Categoria("Objetivo",
                    "Obtén todos los números del 1 al 10 utilizando cuatro dígitos iniciales "
                    + "que cambian según el reto.",
                    List.of("Ejemplo con 1 · 2 · 3 · 4",
                            "10 = 1 + 2 + 3 + 4",
                            "1 = (4 − 3) × (2 − 1)"),
                    "objetivo.png"),
            new Categoria("Números",
                    "Usa los 4 números dados en cada operación. Cada número se usa una sola vez: "
                    + "los disponibles aparecen en azul y los ya utilizados, en gris.",
                    null,
                    "numeros.png"),
            new Categoria("Operadores",
                    "En modo Fácil puedes usar suma (+), resta (−), multiplicación (×) y división (÷). "
                    + "En modo Difícil también tienes potencia (^) y raíz cuadrada (√), que se escribe "
                    + "antes del número. Usa paréntesis para alterar el orden de las operaciones.",
                    List.of("Ejemplos",
                            "Potencia:  2 ^ 3 = 8",
                            "Raíz:  √ 9 = 3",
                            "Paréntesis:  (2 + 3) × 4 = 20"),
                    "operadores.png"),
            new Categoria("Cómo comprobar",
                    "El resultado aparece luego de operar todos los números en pantalla. "
                    + "Pulsa Comprobar: si el resultado es un objetivo del 1 al 10 que aún no tienes, "
                    + "se marca en verde.",
                    null,
                    "comprobar.png"),
            new Categoria("Tiempo",
                    "Resuelve el reto en el menor tiempo posible. El cronómetro corre desde que empieza "
                    + "la partida hasta que completas el 10. En el reto diario se guarda tu mejor tiempo "
                    + "en el ranking. Mientras lees estas instrucciones el cronómetro se pausa.",
                    null,
                    "tiempo.png"));

    /** Constructor por defecto; JavaFX lo invoca al cargar el FXML de esta pantalla. */
    public InstruccionesController() {
    }

    @FXML private VBox menuLateral;
    @FXML private Button btnObjetivo;
    @FXML private Button btnNumeros;
    @FXML private Button btnOperadores;
    @FXML private Button btnComprobar;
    @FXML private Button btnTiempo;
    @FXML private Button btnVolver;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private Label lblCategoria;
    @FXML private Label lblTexto;
    @FXML private VBox cajaEjemplo;
    @FXML private ImageView imagenContenido;

    private int indiceActual;

    // =====================================================================
    // APERTURA (compartida por el menu y la pantalla de juego)
    // =====================================================================

    /**
     * Abre "¿Como jugar?" en una ventana modal sobre la ventana indicada.
     * Se puede llamar igual desde el menu, desde el reto diario y desde la
     * partida libre.
     *
     * @param propietario ventana desde la que se abre (puede ser {@code null})
     */
    public static void mostrar(Window propietario) {
        mostrar(propietario, null);
    }

    /**
     * Igual que {@link #mostrar(Window)}, pero avisa cuando la ventana se cierra.
     * No bloquea con showAndWait (evita bucles de eventos anidados): la pantalla
     * de juego usa el aviso para reanudar el cronometro.
     *
     * @param propietario ventana desde la que se abre (puede ser {@code null})
     * @param alCerrar    accion a ejecutar al cerrarse (puede ser {@code null})
     */
    public static void mostrar(Window propietario, Runnable alCerrar) {
        try {
            java.net.URL fxml = InstruccionesController.class.getResource("/vista/InstruccionesView.fxml");
            if (fxml == null) {
                throw new IOException("No se encontro /vista/InstruccionesView.fxml en el classpath");
            }
            Parent root = FXMLLoader.load(fxml);

            Stage ventana = new Stage();
            ventana.setTitle("All Ten - Cómo jugar");
            if (propietario != null) {
                ventana.initOwner(propietario);
                ventana.initModality(Modality.WINDOW_MODAL);
            }
            ventana.setScene(new Scene(root, 960, 620));
            ventana.setMinWidth(900);
            ventana.setMinHeight(580);
            if (alCerrar != null) {
                ventana.setOnHidden(evento -> alCerrar.run());
            }
            ventana.show();
        } catch (Exception e) {
            // Cualquier fallo (no solo IOException) se muestra y se deja en consola.
            e.printStackTrace();
            if (alCerrar != null) {
                alCerrar.run();   // que el cronometro no quede pausado
            }
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            co.edu.poli.Allten.App.Estilos.aplicarFuente(alerta);
            alerta.setTitle("Error al abrir las instrucciones");
            alerta.setHeaderText("No se pudo abrir la pantalla \"Cómo jugar\".");
            alerta.setContentText(String.valueOf(e));
            alerta.show();
        }
    }

    // =====================================================================
    // INICIO
    // =====================================================================

    /** Se ejecuta al cargar el FXML: muestra la categoria "Objetivo" por defecto. */
    @FXML
    public void initialize() {
        // Esquinas redondeadas para la imagen. Se usa layoutBounds (no boundsInLocal, que ya
        // incluye el recorte y lo dejaria en 0x0, haciendo invisible la imagen).
        Rectangle recorte = new Rectangle();
        recorte.setArcWidth(24);
        recorte.setArcHeight(24);
        imagenContenido.layoutBoundsProperty().addListener((obs, antes, ahora) -> {
            recorte.setX(ahora.getMinX());
            recorte.setY(ahora.getMinY());
            recorte.setWidth(ahora.getWidth());
            recorte.setHeight(ahora.getHeight());
        });
        imagenContenido.setClip(recorte);

        mostrar(0);
    }

    // =====================================================================
    // CATEGORIAS (manejadores de los botones laterales)
    // =====================================================================

    @FXML private void mostrarObjetivo()   { mostrar(0); }
    @FXML private void mostrarNumeros()    { mostrar(1); }
    @FXML private void mostrarOperadores() { mostrar(2); }
    @FXML private void mostrarComprobar()  { mostrar(3); }
    @FXML private void mostrarTiempo()     { mostrar(4); }

    /** Boton "Anterior". */
    @FXML
    private void anterior() {
        if (indiceActual > 0) {
            mostrar(indiceActual - 1);
        }
    }

    /** Boton "Siguiente"; en la ultima categoria se convierte en "¡A jugar!" y cierra la ventana. */
    @FXML
    private void siguiente() {
        if (indiceActual < CATEGORIAS.size() - 1) {
            mostrar(indiceActual + 1);
        } else {
            volver();
        }
    }

    /**
     * Pinta la categoria indicada: titulo, texto, imagen, ejemplo y boton activo.
     *
     * @param indice posicion de la categoria en {@link #CATEGORIAS}
     */
    private void mostrar(int indice) {
        indiceActual = indice;
        Categoria categoria = CATEGORIAS.get(indice);

        lblCategoria.setText(categoria.titulo());
        lblTexto.setText(categoria.texto());

        cargarImagen(categoria.imagen());
        cargarEjemplos(categoria.ejemplos());
        marcarBotonActivo(indice);

        btnAnterior.setDisable(indice == 0);
        boolean ultima = indice == CATEGORIAS.size() - 1;
        btnSiguiente.setText(ultima ? "¡A jugar!" : "Siguiente ›");
    }

    private void cargarImagen(String nombreImagen) {
        try {
            imagenContenido.setImage(new Image(
                    getClass().getResource("/vista/images/" + nombreImagen).toExternalForm()));
        } catch (Exception e) {
            System.out.println("ERROR: no se pudo cargar la imagen: " + nombreImagen);
            imagenContenido.setImage(null);
        }
    }

    /** La primera linea se muestra como encabezado pequeño si la lista tiene mas de una linea. */
    private void cargarEjemplos(List<String> ejemplos) {
        cajaEjemplo.getChildren().clear();

        if (ejemplos == null || ejemplos.isEmpty()) {
            cajaEjemplo.setVisible(false);
            cajaEjemplo.setManaged(false);
            return;
        }

        for (int i = 0; i < ejemplos.size(); i++) {
            Label linea = new Label(ejemplos.get(i));
            linea.getStyleClass().add(i == 0 && ejemplos.size() > 1 ? "linea-ejemplo-titulo" : "linea-ejemplo");
            cajaEjemplo.getChildren().add(linea);
        }
        cajaEjemplo.setVisible(true);
        cajaEjemplo.setManaged(true);
    }

    private void marcarBotonActivo(int indice) {
        List<Button> botones = List.of(btnObjetivo, btnNumeros, btnOperadores, btnComprobar, btnTiempo);
        for (int i = 0; i < botones.size(); i++) {
            botones.get(i).getStyleClass().remove("btn-categoria-activo");
            if (i == indice) {
                botones.get(i).getStyleClass().add("btn-categoria-activo");
            }
        }
    }

    // =====================================================================
    // VOLVER
    // =====================================================================

    /** Cierra la ventana de instrucciones y regresa a la pantalla desde la que se abrio. */
    @FXML
    private void volver() {
        ((Stage) btnVolver.getScene().getWindow()).close();
    }
}
