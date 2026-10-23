package co.edu.poli.Allten.Controlador;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import co.edu.poli.Allten.DAO.ConexionBD;
import co.edu.poli.Allten.DAO.DAOJugador;
import co.edu.poli.Allten.DAO.DAOPartida;
import co.edu.poli.Allten.DAO.IJugadorDAO;
import co.edu.poli.Allten.DAO.IPartidaDAO;
import co.edu.poli.Allten.Modelo.Jugador;
import co.edu.poli.Allten.Modelo.Partida;

/**
 * Controlador de la pantalla de menu principal (MainMenuView.fxml).
 *
 * Se encarga unicamente de la navegacion entre pantallas: abrir las
 * instrucciones en una ventana aparte, y pasar de este menu a la pantalla
 * de juego (JuegoView.fxml) pidiendo antes el nickname del jugador.
 */
public class MainSceneController {

    private IJugadorDAO jugadorDAO = new DAOJugador();
    private IPartidaDAO partidaDAO = new DAOPartida();

    /** Constructor por defecto; JavaFX lo invoca al cargar el FXML de esta pantalla. */
    public MainSceneController() {
    }

    @FXML
    private Button btnJugar;

    public void setPartidaDAO(IPartidaDAO partidaDAO) {
        this.partidaDAO = partidaDAO;
    }

    public void setJugadorDAO(IJugadorDAO jugadorDAO) {
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
     * Maneja el boton "Jugar": pide el nickname del jugador y, si lo
     * escribe, carga JuegoView.fxml, le entrega el control a
     * {@link JuegoController#iniciarPartida(Jugador)} y reemplaza la escena
     * de la ventana actual por la del juego.
     *
     * @throws Exception si el archivo FXML del juego no se puede cargar
     */
    @FXML
    private void jugar() throws Exception {

        String nickname = pedirNickname();
        if (nickname == null) {
            return; // el jugador cancelo el dialogo o no escribio nada
        }

        try {
            Jugador jugador = Jugador.identificarJugador(nickname, jugadorDAO);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/JuegoView.fxml"));
            Parent root = loader.load();

            JuegoController controlador = loader.getController();
            controlador.setPartidaDAO(partidaDAO);
            controlador.iniciarPartida(jugador);

            Stage ventanaActual = (Stage) btnJugar.getScene().getWindow();
            ventanaActual.setScene(new Scene(root, 1000, 700));
            ventanaActual.setTitle("All Ten - " + jugador.getNickname());
        } catch (SQLException e) {
            mostrarErrorPersistencia("No se pudo registrar o cargar al jugador.", e);
        } catch (IllegalArgumentException e) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Nickname no válido");
            alerta.setHeaderText("No se pudo iniciar la partida.");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }

    /**
     * Muestra un dialogo personalizado para que el jugador escriba su nickname.
     *
     * @return el nickname escrito (sin espacios sobrantes), o {@code null}
     *         si el jugador cancelo el dialogo o dejo el campo vacio
     */
    private String pedirNickname() {
        Dialog<String> dialogo = new Dialog<>();
        dialogo.setTitle("All Ten");
        dialogo.initOwner(btnJugar.getScene().getWindow());
        dialogo.getDialogPane().getStyleClass().add("nickname-dialog");
        dialogo.getDialogPane().getStylesheets().add(
                getClass().getResource("/vista/interfaz.css").toExternalForm());

        Label titulo = new Label("¿Listo para jugar?");
        titulo.getStyleClass().add("nickname-title");
        Label descripcion = new Label("Elige un nickname para guardar tu tiempo en el ranking.");
        descripcion.getStyleClass().add("nickname-description");

        TextField campoNickname = new TextField();
        campoNickname.setPromptText("Tu nickname");
        campoNickname.setMaxWidth(Double.MAX_VALUE);
        campoNickname.getStyleClass().add("nickname-field");
        campoNickname.setTextFormatter(new TextFormatter<>(
                change -> change.getControlNewText().length() <= Jugador.LONGITUD_MAXIMA_NICKNAME
                        ? change : null));

        VBox contenido = new VBox(12, titulo, descripcion, campoNickname);
        contenido.getStyleClass().add("nickname-content");
        dialogo.getDialogPane().setContent(contenido);

        ButtonType comenzar = new ButtonType("Comenzar", ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().setAll(comenzar, ButtonType.CANCEL);
        dialogo.getDialogPane().lookupButton(comenzar).disableProperty().bind(
                Bindings.createBooleanBinding(
                        () -> campoNickname.getText().isBlank(), campoNickname.textProperty()));

        dialogo.setResultConverter(boton -> boton == comenzar ? campoNickname.getText().trim() : null);
        dialogo.setOnShown(evento -> campoNickname.requestFocus());
        return dialogo.showAndWait().orElse(null);
    }

    @FXML
    private void abrirRanking() {
        try {
            LocalDate fechaReto = Partida.fechaActualDelReto();
            List<Partida> partidas = partidaDAO.readTopForDate(fechaReto, 10);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/vista/RankingView.fxml"));
            Parent root = loader.load();
            RankingController controlador = loader.getController();
            controlador.setPartidas(partidas, fechaReto);

            Stage ventana = new Stage();
            ventana.setTitle("All Ten - Ranking");
            ventana.initOwner(btnJugar.getScene().getWindow());
            ventana.initModality(Modality.WINDOW_MODAL);
            ventana.setScene(new Scene(root, 820, 560));
            ventana.showAndWait();
        } catch (SQLException e) {
            mostrarErrorPersistencia("No se pudo consultar el ranking.", e);
        } catch (IOException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Error al abrir el ranking");
            alerta.setHeaderText("No se pudo cargar la pantalla del ranking.");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }

    @FXML
    private void probarConexionBD() {
        try {
            ConexionBD.probarConexion();
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Conexión a la base de datos");
            alerta.setHeaderText("Conexión exitosa");
            alerta.setContentText("La aplicación se conectó a MySQL y ejecutó SELECT 1.");
            alerta.showAndWait();
        } catch (SQLException e) {
            mostrarErrorPersistencia("No se pudo conectar con MySQL.", e);
        }
    }

    private void mostrarErrorPersistencia(String encabezado, SQLException e) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error de base de datos");
        alerta.setHeaderText(encabezado);
        alerta.setContentText(e.getMessage());
        alerta.showAndWait();
    }
}
