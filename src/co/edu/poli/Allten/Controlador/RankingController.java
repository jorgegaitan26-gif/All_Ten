package co.edu.poli.Allten.Controlador;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;

import co.edu.poli.Allten.DAO.DAOPartida;

import co.edu.poli.Allten.DAO.EntradaRanking;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class RankingController {

    private static final String FORMATO_FECHA = "dd/MM/yyyy";

    @FXML private Label lblRetoDiario;
    @FXML private ToggleButton btnRankingFacil;
    @FXML private ToggleButton btnRankingDificil;
    @FXML private TableView<EntradaRanking> tablaRanking;
    @FXML private TableColumn<EntradaRanking, Number> columnaPosicion;
    @FXML private TableColumn<EntradaRanking, String> columnaJugador;
    @FXML private TableColumn<EntradaRanking, String> columnaTiempo;
    @FXML private TableColumn<EntradaRanking, String> columnaFecha;

    private final ToggleGroup grupoNivel = new ToggleGroup();
    private DAOPartida partidaDAO;

    @FXML
    private void initialize() {
        btnRankingFacil.setToggleGroup(grupoNivel);
        btnRankingDificil.setToggleGroup(grupoNivel);
        btnRankingFacil.setSelected(true);
        grupoNivel.selectedToggleProperty().addListener((obs, anterior, actual) -> {
            if (actual == null && anterior != null) {
                anterior.setSelected(true);   // siempre hay un nivel escogido
            } else if (actual != null) {
                cargarRanking();
            }
        });

        columnaPosicion.setCellValueFactory(datos ->
                new ReadOnlyIntegerWrapper(datos.getValue().getPosicion()));
        columnaJugador.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue().getNickname()));
        columnaTiempo.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(formatearTiempo(datos.getValue().getTiempoTotal())));
        columnaFecha.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(new SimpleDateFormat(FORMATO_FECHA)
                        .format(datos.getValue().getFechaFinalizacion())));

        columnaPosicion.setSortable(false);
        columnaJugador.setSortable(false);
        columnaTiempo.setSortable(false);
        columnaFecha.setSortable(false);
    }

    /**
     * Recibe el DAO y carga el ranking permanente (top 10 de todos los tiempos).
     * Se abre en la dificultad indicada y se cambia con los botones Facil / Dificil.
     *
     * @param partidaDAO   DAO de partidas
     * @param nivelInicial "FACIL" o "DIFICIL"
     */
    public void configurar(DAOPartida partidaDAO, String nivelInicial) {
        this.partidaDAO = partidaDAO;
        lblRetoDiario.setText("Los 10 mejores tiempos del reto diario");
        if ("DIFICIL".equals(nivelInicial)) {
            btnRankingDificil.setSelected(true);   // dispara cargarRanking()
        } else {
            btnRankingFacil.setSelected(true);
        }
        cargarRanking();
    }

    private void cargarRanking() {
        if (partidaDAO == null) {
            return;
        }
        String nivel = btnRankingDificil.isSelected() ? "DIFICIL" : "FACIL";
        try {
            List<EntradaRanking> partidas = partidaDAO.readRankingGeneral(nivel, 10);
            tablaRanking.getItems().setAll(partidas);
        } catch (Exception e) {
            tablaRanking.getItems().clear();
            javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alerta.setTitle("Error al consultar el ranking");
            alerta.setHeaderText("No se pudo consultar el ranking.");
            alerta.setContentText(e.getMessage());
            alerta.showAndWait();
        }
    }

    @FXML
    private void cerrar() {
        ((Stage) tablaRanking.getScene().getWindow()).close();
    }

    private static String formatearTiempo(double segundosTotales) {
        Duration duracion = Duration.ofSeconds(Math.round(segundosTotales));
        long minutos = duracion.toMinutes();
        long segundos = duracion.minusMinutes(minutos).toSeconds();
        return "%02d:%02d".formatted(minutos, segundos);
    }
}