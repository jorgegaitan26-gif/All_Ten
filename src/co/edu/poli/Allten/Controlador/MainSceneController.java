package co.edu.poli.Allten.Controlador;

import java.io.IOException;

import javafx.beans.binding.Bindings;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;
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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

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

    public void setPartidaDAO(DAOPartida partidaDAO) {
        this.partidaDAO = partidaDAO;
    }

    public void setJugadorDAO(DAOJugador jugadorDAO) {
        this.jugadorDAO = jugadorDAO;
    }

    /**
     * Maneja el boton "Como jugar": abre InstruccionesView.fxml en una
     * ventana (Stage) nueva e independiente de la ventana principal.
     *
     * @throws Exception si el archivo FXML de instrucciones no se puede cargar
     */
    @FXML
    private void abrirInstrucciones() throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/vista/InstruccionesView.fxml"));

        Stage ventanaInstrucciones = new Stage();
        ventanaInstrucciones.setTitle("Como jugar");
        ventanaInstrucciones.setScene(new Scene(root, 800, 450));
        ventanaInstrucciones.show();
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

        VBox contenido = new VBox(12, titulo, descripcion, campoNickname, tituloDificultad, filaDificultad);
        contenido.getStyleClass().add("nickname-content");
        dialogo.getDialogPane().setContent(contenido);

        ButtonType comenzar = new ButtonType("Comenzar", ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().setAll(comenzar, ButtonType.CANCEL);
        dialogo.getDialogPane().lookupButton(comenzar).disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> campoNickname.getText().isBlank(), campoNickname.textProperty()));

        dialogo.setResultConverter(boton -> boton == comenzar
                ? new DatosInicio(campoNickname.getText().trim(),
                        (String) grupoDificultad.getSelectedToggle().getUserData())
                : null);
        dialogo.setOnShown(evento -> campoNickname.requestFocus());
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
            controlador.configurar(partidaDAO, ultimaDificultad);

            Stage ventana = new Stage();
            ventana.setTitle("All Ten - Ranking");
            ventana.initOwner(btnRetoDiario.getScene().getWindow());
            ventana.initModality(Modality.WINDOW_MODAL);
            ventana.setScene(new Scene(root, 820, 560));
            ventana.showAndWait();
        } catch (IOException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error al abrir el ranking");
            alerta.setHeaderText("No se pudo cargar la pantalla del ranking.");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        } catch (Exception e) {
            mostrarErrorPersistencia("No se pudo consultar el ranking.", e);
        }
    }

    private void mostrarErrorPersistencia(String encabezado, Exception e) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error de base de datos");
        alerta.setHeaderText(encabezado);
        alerta.setContentText(e.getMessage());
        alerta.showAndWait();
    }
}