package co.edu.poli.Allten.Controlador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import co.edu.poli.Allten.DAO.DAOJugador;
import co.edu.poli.Allten.DAO.DAOPartida;
import co.edu.poli.Allten.Modelo.Jugador;

/**
 * Controlador de la pantalla de menu principal (MainMenuView.fxml).
 *
 * Se encarga unicamente de la navegacion entre pantallas: abrir las
 * instrucciones en una ventana aparte, y pasar de este menu a la pantalla
 * de juego (JuegoView.fxml) pidiendo antes el nickname del jugador.
 */
public class MainSceneController {

    /**
     * Datos que se piden antes de empezar a jugar.
     *
     * @param nickname   nickname del jugador
     * @param dificultad "FACIL" o "DIFICIL"
     */
    private record DatosInicio(String nickname, String dificultad) {
    }

    /** Longitud maxima del nickname (columna VARCHAR(45) de la tabla Jugador). */
    private static final int LONGITUD_MAXIMA_NICKNAME = 45;

    /** Ultima dificultad jugada; el ranking y el dialogo se abren en ella. */
    private static String ultimaDificultad = "FACIL";

    private DAOJugador jugadorDAO = new DAOJugador();
    private DAOPartida partidaDAO = new DAOPartida();

    /** Constructor por defecto; JavaFX lo invoca al cargar el FXML de esta pantalla. */
    public MainSceneController() {
    }

    @FXML private Button btnRetoDiario;
    @FXML private Pane capaDecoracion;
    @FXML private HBox filaDiez;

    /** Animaciones del fondo; se detienen al salir del menu para no dejarlas corriendo. */
    private final List<Animation> animacionesFondo = new ArrayList<>();

    /**
     * Ficha decorativa del fondo: simbolo, tipo (numero/operador), posicion
     * relativa al ancho/alto de la ventana (0..1) y giro en grados.
     */
    private record Ficha(String simbolo, boolean esNumero, double x, double y, double giro) {
    }

    /** Fichas repartidas por los bordes para no tapar la tarjeta central. */
    private static final Ficha[] FICHAS = {
        new Ficha("7", true, 0.07, 0.12, -12),
        new Ficha("+", false, 0.19, 0.27, 10),
        new Ficha("×", false, 0.06, 0.50, -8),
        new Ficha("3", true, 0.17, 0.70, 14),
        new Ficha("÷", false, 0.08, 0.88, -10),
        new Ficha("9", true, 0.24, 0.90, 8),
        new Ficha("√", false, 0.92, 0.12, 10),
        new Ficha("4", true, 0.81, 0.28, -10),
        new Ficha("−", false, 0.94, 0.50, 12),
        new Ficha("^", false, 0.82, 0.72, -14),
        new Ficha("6", true, 0.92, 0.88, 8),
        new Ficha("10", true, 0.76, 0.92, -6)
    };

    /** Arma la fila 1..10 y las fichas flotantes del fondo. */
    @FXML
    private void initialize() {
        crearFilaDiez();
        crearFichasDecorativas();
        // Al cambiar de pantalla (la escena queda sin este menu) se frenan las animaciones.
        capaDecoracion.sceneProperty().addListener((obs, antes, ahora) -> {
            if (ahora == null) {
                animacionesFondo.forEach(Animation::stop);
            }
        });
    }

    /** Fila de circulos 1..10 bajo el titulo; aparecen uno tras otro. */
    private void crearFilaDiez() {
        for (int i = 1; i <= 10; i++) {
            Label bola = new Label(String.valueOf(i));
            bola.getStyleClass().add("menu-bola");
            bola.setScaleX(0);
            bola.setScaleY(0);
            filaDiez.getChildren().add(bola);

            ScaleTransition aparecer = new ScaleTransition(Duration.millis(260), bola);
            aparecer.setToX(1);
            aparecer.setToY(1);
            aparecer.setInterpolator(Interpolator.EASE_OUT);
            aparecer.setDelay(Duration.millis(120 + i * 70L));
            aparecer.play();
        }
    }

    /** Fichas de numeros (azul) y operadores (naranja) que flotan suavemente al fondo. */
    private void crearFichasDecorativas() {
        for (int i = 0; i < FICHAS.length; i++) {
            Ficha ficha = FICHAS[i];
            double tam = ficha.simbolo().length() > 1 ? 76 : 64;

            Label etiqueta = new Label(ficha.simbolo());
            etiqueta.getStyleClass().addAll("deco-ficha", ficha.esNumero() ? "deco-numero" : "deco-operador");
            etiqueta.setMinSize(tam, tam);
            etiqueta.setPrefSize(tam, tam);
            etiqueta.setMaxSize(tam, tam);
            etiqueta.setRotate(ficha.giro());
            etiqueta.setMouseTransparent(true);
            etiqueta.setFocusTraversable(false);
            // Decorativas: los lectores de pantalla no deben leerlas.
            etiqueta.setAccessibleRole(javafx.scene.AccessibleRole.NODE);
            etiqueta.setAccessibleText("");

            etiqueta.layoutXProperty().bind(capaDecoracion.widthProperty().multiply(ficha.x()).subtract(tam / 2));
            etiqueta.layoutYProperty().bind(capaDecoracion.heightProperty().multiply(ficha.y()).subtract(tam / 2));
            capaDecoracion.getChildren().add(etiqueta);

            TranslateTransition flotar = new TranslateTransition(Duration.seconds(2.6 + (i % 4) * 0.5), etiqueta);
            flotar.setByY(i % 2 == 0 ? -12 : 12);
            flotar.setAutoReverse(true);
            flotar.setCycleCount(Animation.INDEFINITE);
            flotar.setInterpolator(Interpolator.EASE_BOTH);
            flotar.setDelay(Duration.millis(i * 150L));
            flotar.play();
            animacionesFondo.add(flotar);
        }
    }

    public void setPartidaDAO(DAOPartida partidaDAO) {
        this.partidaDAO = partidaDAO;
    }

    public void setJugadorDAO(DAOJugador jugadorDAO) {
        this.jugadorDAO = jugadorDAO;
    }

    /**
     * Maneja el boton "Como jugar": abre la ventana de instrucciones
     * (la misma que se abre desde la pantalla de juego).
     */
    @FXML
    private void abrirInstrucciones() {
        InstruccionesController.mostrar(btnRetoDiario.getScene().getWindow());
    }

    /**
     * Inicia el reto diario para el nickname indicado.
     */
    @FXML
    private void jugarRetoDiario() throws Exception {
        iniciarPartida(true);
    }

    /** Inicia una partida libre con números nuevos. */
    @FXML
    private void jugarPartidaLibre() throws Exception {
        iniciarPartida(false);
    }

    /**
     * Pide el nickname, identifica al jugador en la base de datos y abre la
     * pantalla de juego.
     *
     * @param retoDiario true para el reto diario, false para una partida libre
     */
    private void iniciarPartida(boolean retoDiario) throws Exception {
        DatosInicio datos = pedirDatosDeInicio();
        if (datos == null) {
            return; // el jugador cancelo el dialogo o no escribio nada
        }
        String nickname = datos.nickname();
        ultimaDificultad = datos.dificultad();

        Jugador jugador;
        try {
            jugador = identificarJugador(nickname);
        } catch (IllegalArgumentException e) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            co.edu.poli.Allten.App.Estilos.aplicarFuente(alerta);
            alerta.setTitle("Nickname no válido");
            alerta.setHeaderText("No se pudo iniciar la partida.");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
            return;
        } catch (Exception e) {
            mostrarErrorPersistencia("No se pudo registrar o cargar al jugador.", e);
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/JuegoView.fxml"));
        Parent root = loader.load();

        JuegoController controlador = loader.getController();
        controlador.setPartidaDAO(partidaDAO);
        controlador.iniciarPartida(jugador, retoDiario, datos.dificultad());

        Stage ventanaActual = (Stage) btnRetoDiario.getScene().getWindow();
        ventanaActual.setScene(new Scene(root, 1000, 700));
        ventanaActual.setTitle("All Ten - " + jugador.getNickname());
    }

    /**
     * Busca al jugador por su nickname; si no existe, lo registra. En ambos
     * casos devuelve el jugador con su id_Jugador.
     *
     * @param nickname nickname escrito por el jugador
     * @return el jugador identificado
     * @throws IllegalArgumentException si el nickname esta vacio o es muy largo
     * @throws Exception si falla la base de datos
     */
    private Jugador identificarJugador(String nickname) throws Exception {
        String limpio = nickname == null ? "" : nickname.trim();
        if (limpio.isEmpty() || limpio.length() > LONGITUD_MAXIMA_NICKNAME) {
            throw new IllegalArgumentException(
                    "El nickname debe tener entre 1 y " + LONGITUD_MAXIMA_NICKNAME + " caracteres");
        }

        Jugador existente = jugadorDAO.readByNickname(limpio);
        if (existente != null) {
            return existente;
        }

        Jugador nuevo = new Jugador(limpio);
        jugadorDAO.create(nuevo);   // le asigna el id generado
        return nuevo;
    }

    /**
     * Muestra un dialogo personalizado para que el jugador escriba su nickname
     * y escoja la dificultad de toda la partida (Facil o Dificil).
     *
     * @return nickname (sin espacios sobrantes) y dificultad, o {@code null}
     *         si el jugador cancelo el dialogo o dejo el campo vacio
     */
    private DatosInicio pedirDatosDeInicio() {
        Dialog<DatosInicio> dialogo = new Dialog<>();
        dialogo.setTitle("All Ten");
        dialogo.initOwner(btnRetoDiario.getScene().getWindow());
        dialogo.getDialogPane().getStyleClass().add("nickname-dialog");
        dialogo.getDialogPane().getStylesheets().add(
                getClass().getResource("/vista/interfaz.css").toExternalForm());

        Label titulo = new Label("¿Listo para jugar?");
        titulo.getStyleClass().add("nickname-title");
        Label descripcion = new Label("Elige un nickname para guardar tus partidas.");
        descripcion.getStyleClass().add("nickname-description");

        TextField campoNickname = new TextField();
        campoNickname.setPromptText("Tu nickname");
        campoNickname.setMaxWidth(Double.MAX_VALUE);
        campoNickname.getStyleClass().add("nickname-field");
        campoNickname.setTextFormatter(new TextFormatter<>(
                change -> change.getControlNewText().length() <= LONGITUD_MAXIMA_NICKNAME
                        ? change : null));

        Label tituloDificultad = new Label("Dificultad");
        tituloDificultad.getStyleClass().add("dificultad-title");
        ToggleGroup grupoDificultad = new ToggleGroup();
        ToggleButton btnFacil = crearBotonDificultad("Fácil", "+  −  ×  ÷", "FACIL", grupoDificultad);
        ToggleButton btnDificil = crearBotonDificultad("Difícil", "+  −  ×  ÷  ^  √", "DIFICIL", grupoDificultad);
        ("DIFICIL".equals(ultimaDificultad) ? btnDificil : btnFacil).setSelected(true);
        // Siempre debe haber una dificultad escogida: no se deja deseleccionar.
        grupoDificultad.selectedToggleProperty().addListener((obs, anterior, actual) -> {
            if (actual == null && anterior != null) {
                anterior.setSelected(true);
            }
        });
        HBox filaDificultad = new HBox(10, btnFacil, btnDificil);
        HBox.setHgrow(btnFacil, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(btnDificil, javafx.scene.layout.Priority.ALWAYS);

        Label insignia = new Label("10");
        insignia.getStyleClass().add("nickname-badge");
        VBox textosCabecera = new VBox(2, titulo, descripcion);
        HBox cabecera = new HBox(14, insignia, textosCabecera);
        cabecera.getStyleClass().add("nickname-header");

        // Botones dentro de la misma tarjeta crema
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("nickname-boton");
        btnCancelar.setCancelButton(true);
        btnCancelar.setOnAction(e -> dialogo.close());

        Button btnComenzar = new Button("Comenzar");
        btnComenzar.getStyleClass().addAll("nickname-boton", "nickname-boton-primario");
        btnComenzar.setDefaultButton(true);
        btnComenzar.disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> campoNickname.getText().isBlank(), campoNickname.textProperty()));
        btnComenzar.setOnAction(e -> dialogo.setResult(new DatosInicio(
                campoNickname.getText().trim(),
                (String) grupoDificultad.getSelectedToggle().getUserData())));
        campoNickname.setOnAction(e -> btnComenzar.fire());

        HBox filaBotones = new HBox(12, btnCancelar, btnComenzar);
        filaBotones.getStyleClass().add("nickname-botones");

        VBox contenido = new VBox(12, cabecera, campoNickname, tituloDificultad, filaDificultad, filaBotones);
        contenido.getStyleClass().add("nickname-content");
        dialogo.getDialogPane().setContent(contenido);

        // El dialogo necesita un boton de cancelar propio para poder cerrarse con la X y con Esc;
        // su barra queda oculta por CSS (.nickname-dialog .button-bar).
        dialogo.getDialogPane().getButtonTypes().setAll(ButtonType.CANCEL);
        dialogo.setResultConverter(boton -> null);
        dialogo.setOnShown(evento -> {
            javafx.scene.Node barra = dialogo.getDialogPane().lookup(".button-bar");
            if (barra != null) {
                barra.setVisible(false);
                barra.setManaged(false);
            }
            campoNickname.requestFocus();
        });
        return dialogo.showAndWait().orElse(null);
    }

    /** Crea un boton de dificultad (tipo interruptor) con su nombre y los operadores que incluye. */
    private ToggleButton crearBotonDificultad(String nombre, String operadores, String valor, ToggleGroup grupo) {
        ToggleButton boton = new ToggleButton(nombre + "\n" + operadores);
        boton.setUserData(valor);
        boton.setToggleGroup(grupo);
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.getStyleClass().add("dificultad-boton");
        return boton;
    }

    @FXML
    private void abrirRanking() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/RankingView.fxml"));
            Parent root = loader.load();
            RankingController controlador = loader.getController();
            controlador.configurar(partidaDAO, ultimaDificultad);   // ranking general; la ventana tiene filtros

            Stage ventana = new Stage();
            ventana.setTitle("All Ten - Ranking");
            ventana.initOwner(btnRetoDiario.getScene().getWindow());
            ventana.initModality(Modality.WINDOW_MODAL);
            ventana.setScene(new Scene(root, 900, 680));
            ventana.showAndWait();
        } catch (IOException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            co.edu.poli.Allten.App.Estilos.aplicarFuente(alerta);
            alerta.setTitle("Error al abrir el ranking");
            alerta.setHeaderText("No se pudo cargar la pantalla del ranking.");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }

    private void mostrarErrorPersistencia(String encabezado, Exception e) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        co.edu.poli.Allten.App.Estilos.aplicarFuente(alerta);
        alerta.setTitle("Error de base de datos");
        alerta.setHeaderText(encabezado);
        alerta.setContentText(e.getMessage());
        alerta.showAndWait();
    }
}