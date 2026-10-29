package co.edu.poli.Allten.Controlador;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;

import co.edu.poli.Allten.DAO.EntradaRanking;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class RankingController {

    private static final String FORMATO_FECHA = "dd/MM/yyyy";

    @FXML private Label lblRetoDiario;
    @FXML private TableView<EntradaRanking> tablaRanking;
    @FXML private TableColumn<EntradaRanking, Number> columnaPosicion;
    @FXML private TableColumn<EntradaRanking, String> columnaJugador;
    @FXML private TableColumn<EntradaRanking, String> columnaTiempo;
    @FXML private TableColumn<EntradaRanking, String> columnaFecha;

    @FXML
    private void initialize() {
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

    public void setPartidas(List<EntradaRanking> partidas, Date fechaReto) {
        tablaRanking.getItems().setAll(partidas);
        lblRetoDiario.setText("Reto del " + new SimpleDateFormat(FORMATO_FECHA).format(fechaReto));
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
