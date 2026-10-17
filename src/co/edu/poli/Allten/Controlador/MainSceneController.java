package co.edu.poli.Allten.Controlador;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import co.edu.poli.Allten.DAO.ConexionBD;
import co.edu.poli.Allten.DAO.IPartidaDAO;
import co.edu.poli.Allten.DAO.PartidaDAOImpl;
import co.edu.poli.Allten.Modelo.Jugador;

/**
 * Controlador de la pantalla de menu principal (MainMenuView.fxml).
 *
 * Se encarga unicamente de la navegacion entre pantallas: abrir las
 * instrucciones en una ventana aparte, y pasar de este menu a la pantalla
 * de juego (JuegoView.fxml) pidiendo antes el nickname del jugador.
 */
public class MainSceneController {

    private IPartidaDAO partidaDAO = new PartidaDAOImpl();

    /** Constructor por defecto; JavaFX lo invoca al cargar el FXML de esta pantalla. */
    public MainSceneController() {
    }

    @FXML
    private Button btnJugar;

    public void setPartidaDAO(IPartidaDAO partidaDAO) {
        this.partidaDAO = partidaDAO;
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
            Jugador jugador = Jugador.identificarJugador(nickname, partidaDAO);
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
     * Muestra un dialogo para que el jugador escriba su nickname.
     *
     * @return el nickname escrito (sin espacios sobrantes), o {@code null}
     *         si el jugador cancelo el dialogo o dejo el campo vacio
     */
    private String pedirNickname() {

        TextInputDialog dialogo = new TextInputDialog();
        dialogo.setTitle("All Ten");
        dialogo.setHeaderText("Antes de empezar...");
        dialogo.setContentText("Escribe tu nickname:");

        Optional<String> respuesta = dialogo.showAndWait();

        if (respuesta.isEmpty() || respuesta.get().isBlank()) {
            if (respuesta.isPresent()) {
                new Alert(Alert.AlertType.WARNING, "Debes escribir un nickname para jugar.").showAndWait();
            }
            return null;
        }

        return respuesta.get().trim();
    }

    @FXML
    private void abrirHistorial() {
        try {
            List<String> partidas = partidaDAO.obtenerHistorialPartidas();
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Historial de partidas");
            alerta.setHeaderText(partidas.isEmpty()
                    ? "No hay partidas registradas"
                    : "Partidas registradas: " + partidas.size());
            alerta.setContentText(partidas.isEmpty()
                    ? "Aún no hay partidas guardadas."
                    : String.join("\n\n", partidas));
            alerta.showAndWait();
        } catch (SQLException e) {
            mostrarErrorPersistencia("No se pudo consultar el historial de partidas.", e);
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
