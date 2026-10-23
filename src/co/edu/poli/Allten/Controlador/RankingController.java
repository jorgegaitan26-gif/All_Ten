package co.edu.poli.Allten.Controlador;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import co.edu.poli.Allten.Modelo.Partida;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class RankingController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private Label lblRetoDiario;
    @FXML private TableView<Partida> tablaRanking;
    @FXML private TableColumn<Partida, Number> columnaPosicion;
    @FXML private TableColumn<Partida, String> columnaJugador;
    @FXML private TableColumn<Partida, String> columnaTiempo;
    @FXML private TableColumn<Partida, String> columnaFecha;

    @FXML
    private void initialize() {
        columnaPosicion.setCellValueFactory(datos ->
                new ReadOnlyIntegerWrapper(tablaRanking.getItems().indexOf(datos.getValue()) + 1));
        columnaJugador.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue().getJugador().getNickname()));
        columnaTiempo.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(formatearTiempo(datos.getValue().getTiempoTotalSegundos())));
        columnaFecha.setCellValueFactory(datos ->
                new ReadOnlyStringWrapper(datos.getValue().getFechaFinalizacion().format(FORMATO_FECHA)));

        columnaPosicion.setSortable(false);
        columnaJugador.setSortable(false);
        columnaTiempo.setSortable(false);
        columnaFecha.setSortable(false);
    }

    public void setPartidas(List<Partida> partidas, LocalDate fechaReto) {
        tablaRanking.getItems().setAll(partidas);
        lblRetoDiario.setText("Reto del " + fechaReto.format(FORMATO_FECHA));
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
