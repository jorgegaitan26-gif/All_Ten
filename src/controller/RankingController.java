package controller;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.EntradaRanking;

/**
 * Pantalla del ranking: tabla con las partidas más rápidas, mostrando
 * posición, jugador, tiempo y la fecha y hora exactas en que se lograron.
 *
 * Este controlador solo se encarga de lo visual: quien lo llama obtiene la
 * lista ya ordenada (por ejemplo con DAOPartida.readRanking) y se la pasa.
 * La tabla respeta el orden recibido y no se puede reordenar con clics.
 *
 * Si la ventana ya está abierta, no se crea otra: se actualiza.
 */
public class RankingController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm:ss");

    private static Stage ventanaAbierta;
    private static RankingController controladorAbierto;

    @FXML
    private TableView<EntradaRanking> tablaRanking;

    @FXML
    private TableColumn<EntradaRanking, String> colPosicion;

    @FXML
    private TableColumn<EntradaRanking, String> colJugador;

    @FXML
    private TableColumn<EntradaRanking, String> colTiempo;

    @FXML
    private TableColumn<EntradaRanking, String> colFecha;

    @FXML
    private Button btnCerrar;

    // =====================================
    // APERTURA
    // =====================================

    /**
     * Abre el ranking con la lista dada. Si ya estaba abierto, lo actualiza y
     * lo trae al frente.
     *
     * @param ranking filas del ranking, ya ordenadas del más rápido al más lento
     *        (si es null o está vacía, la tabla muestra un aviso)
     * @param dueno ventana a la que pertenece el ranking (puede ser null)
     * @throws IOException si no se puede cargar RankingView.fxml
     */
    public static void abrir(List<EntradaRanking> ranking, Window dueno) throws IOException {

        if (ventanaAbierta != null && ventanaAbierta.isShowing()) {
            controladorAbierto.mostrar(ranking);
            ventanaAbierta.toFront();
            return;
        }

        FXMLLoader loader = new FXMLLoader(RankingController.class.getResource("/view/RankingView.fxml"));
        Parent raiz = loader.load();

        RankingController controlador = loader.getController();
        controlador.mostrar(ranking);

        Stage ventana = new Stage();
        ventana.setTitle("Ranking");
        if (dueno != null) {
            ventana.initOwner(dueno);
        }
        ventana.setScene(new Scene(raiz, 720, 600));
        ventana.setOnHidden(e -> {
            ventanaAbierta = null;
            controladorAbierto = null;
        });

        ventanaAbierta = ventana;
        controladorAbierto = controlador;

        ventana.show();
    }

    /**
     * Si el ranking está abierto, lo actualiza con la lista nueva. Si no, no
     * hace nada. Sirve para llamarlo cuando termina una partida.
     *
     * @param ranking filas del ranking, ya ordenadas
     */
    public static void actualizarSiEstaAbierto(List<EntradaRanking> ranking) {
        if (ventanaAbierta != null && ventanaAbierta.isShowing()) {
            controladorAbierto.mostrar(ranking);
        }
    }

    // =====================================
    // TABLA
    // =====================================

    @FXML
    private void initialize() {

        colPosicion.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getPosicion())));
        colJugador.setCellValueFactory(c -> new SimpleStringProperty(nombre(c.getValue().getNickname())));
        colTiempo.setCellValueFactory(c -> new SimpleStringProperty(formatearTiempo(c.getValue().getTiempoTotal())));
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(formatearFecha(c.getValue().getFechaFinalizacion())));

        // Los tres primeros puestos se destacan con color (oro, plata y bronce)
        colPosicion.setCellFactory(columna -> new TableCell<EntradaRanking, String>() {
            @Override
            protected void updateItem(String posicion, boolean vacia) {
                super.updateItem(posicion, vacia);

                getStyleClass().removeAll(List.of("ranking-oro", "ranking-plata", "ranking-bronce"));

                if (vacia || posicion == null) {
                    setText(null);
                    return;
                }

                setText(posicion);

                if ("1".equals(posicion)) {
                    getStyleClass().add("ranking-oro");
                } else if ("2".equals(posicion)) {
                    getStyleClass().add("ranking-plata");
                } else if ("3".equals(posicion)) {
                    getStyleClass().add("ranking-bronce");
                }
            }
        });

        tablaRanking.setPlaceholder(new Label("Aún no hay partidas registradas"));
    }

    /**
     * Pinta en la tabla las filas dadas, en el mismo orden en que llegan.
     *
     * @param ranking filas del ranking (puede ser null)
     */
    public void mostrar(List<EntradaRanking> ranking) {
        List<EntradaRanking> datos = ranking != null ? ranking : List.of();
        tablaRanking.setItems(FXCollections.observableArrayList(datos));
    }

    // =====================================
    // FORMATO
    // =====================================

    private static String nombre(String nickname) {
        return (nickname == null || nickname.isBlank()) ? "(sin nombre)" : nickname;
    }

    /** Convierte segundos a mm:ss (o h:mm:ss si pasa de una hora). */
    private static String formatearTiempo(int segundos) {

        int horas = segundos / 3600;
        int minutos = (segundos % 3600) / 60;
        int seg = segundos % 60;

        if (horas > 0) {
            return String.format("%d:%02d:%02d", horas, minutos, seg);
        }
        return String.format("%02d:%02d", minutos, seg);
    }

    /** Fecha y hora exactas, en la zona horaria del computador. */
    private static String formatearFecha(Date fecha) {

        if (fecha == null) {
            return "Sin fecha";
        }
        return FORMATO_FECHA.format(Instant.ofEpochMilli(fecha.getTime()).atZone(ZoneId.systemDefault()));
    }

    // =====================================
    // CERRAR
    // =====================================

    @FXML
    private void cerrar() {
        ((Stage) btnCerrar.getScene().getWindow()).close();
    }
}
