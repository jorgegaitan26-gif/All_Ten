package co.edu.poli.Allten.Controlador;

import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.edu.poli.Allten.DAO.DAOPartida;
import co.edu.poli.Allten.DAO.EntradaRanking;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador de la ventana de ranking (RankingView.fxml).
 *
 * Permite filtrar el ranking (SCRUM-196):
 * <ul>
 *   <li>por fecha del reto diario (selector de fecha);</li>
 *   <li>por cantidad de puestos a mostrar (Top 10, 20 o 50);</li>
 *   <li>por jugador (busqueda por nickname); al buscar se muestra la posicion
 *       real del jugador en el ranking, aunque este fuera del Top elegido.</li>
 * </ul>
 */
public class RankingController {

    private static final String FORMATO_FECHA = "dd/MM/yyyy";
    private static final ZoneId ZONA_RETO = ZoneId.of("America/Bogota");

    /** Cantidades de puestos que se pueden elegir. */
    private static final List<Integer> OPCIONES_PUESTOS = List.of(10, 20, 50);

    /** Maximo de filas que se traen de la base para poder buscar jugadores fuera del Top. */
    private static final int MAXIMO_CONSULTA = 1000;

    @FXML private Label lblRetoDiario;
    @FXML private Label lblSinResultados;
    @FXML private DatePicker selectorFecha;
    @FXML private ComboBox<Integer> selectorPuestos;
    @FXML private TextField campoBuscar;
    @FXML private TableView<EntradaRanking> tablaRanking;
    @FXML private TableColumn<EntradaRanking, Number> columnaPosicion;
    @FXML private TableColumn<EntradaRanking, String> columnaJugador;
    @FXML private TableColumn<EntradaRanking, String> columnaTiempo;
    @FXML private TableColumn<EntradaRanking, String> columnaFecha;

    private DAOPartida partidaDAO;

    /** Ranking completo del reto elegido, traido de la base. */
    private List<EntradaRanking> rankingCompleto = new ArrayList<>();

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

        selectorPuestos.getItems().setAll(OPCIONES_PUESTOS);
        selectorPuestos.setValue(OPCIONES_PUESTOS.get(0));
        selectorFecha.setValue(LocalDate.now(ZONA_RETO));

        // Cambiar la fecha vuelve a consultar la base;
        // cambiar los puestos o la busqueda solo filtra lo que ya se trajo.
        selectorFecha.valueProperty().addListener((obs, antes, ahora) -> recargar());
        selectorPuestos.valueProperty().addListener((obs, antes, ahora) -> aplicarFiltros());
        campoBuscar.textProperty().addListener((obs, antes, ahora) -> aplicarFiltros());
    }

    /**
     * Recibe el DAO y carga el ranking del reto de hoy. Lo llama
     * {@link MainSceneController} justo despues de cargar el FXML.
     *
     * @param partidaDAO DAO de partidas
     */
    public void setPartidaDAO(DAOPartida partidaDAO) {
        this.partidaDAO = partidaDAO;
        recargar();
    }

    /** Consulta la base segun la fecha elegida y aplica los filtros. */
    private void recargar() {
        if (partidaDAO == null) {
            return;
        }

        LocalDate fecha = selectorFecha.getValue() != null ? selectorFecha.getValue() : LocalDate.now(ZONA_RETO);

        try {
            rankingCompleto = partidaDAO.readRankingDelReto(aFechaReto(fecha), MAXIMO_CONSULTA);
            lblRetoDiario.setText("Reto del " + fecha.format(DateTimeFormatter.ofPattern(FORMATO_FECHA)));

        } catch (Exception e) {
            rankingCompleto = new ArrayList<>();
            lblRetoDiario.setText("No se pudo consultar el ranking: " + e.getMessage());
        }

        aplicarFiltros();
    }

    /** Aplica la busqueda por jugador y la cantidad de puestos sobre el ranking ya consultado. */
    private void aplicarFiltros() {
        int puestos = selectorPuestos.getValue() != null ? selectorPuestos.getValue() : OPCIONES_PUESTOS.get(0);
        String busqueda = campoBuscar.getText();

        tablaRanking.getItems().setAll(filtrar(rankingCompleto, busqueda, puestos));

        boolean buscando = busqueda != null && !busqueda.isBlank();
        lblSinResultados.setText(buscando
                ? "Ningún jugador coincide con \"" + busqueda.trim() + "\"."
                : "Aún no hay partidas completadas. ¡Sé el primero en entrar al ranking!");
    }

    /**
     * Filtra el ranking. Sin busqueda, devuelve los primeros {@code puestos}
     * puestos. Con busqueda, devuelve todos los jugadores cuyo nickname
     * contiene el texto (sin importar mayusculas ni tildes), con su posicion
     * real en el ranking.
     *
     * @param ranking  ranking completo, ordenado del mas rapido al mas lento
     * @param busqueda texto a buscar en el nickname (puede ser vacio o null)
     * @param puestos  cantidad maxima de puestos a mostrar sin busqueda
     * @return las filas a mostrar
     */
    static List<EntradaRanking> filtrar(List<EntradaRanking> ranking, String busqueda, int puestos) {
        List<EntradaRanking> resultado = new ArrayList<>();
        String texto = normalizar(busqueda);

        for (EntradaRanking entrada : ranking) {
            if (texto.isEmpty()) {
                if (resultado.size() >= puestos) {
                    break;
                }
                resultado.add(entrada);
            } else if (normalizar(entrada.getNickname()).contains(texto)) {
                resultado.add(entrada);
            }
        }
        return resultado;
    }

    /** Pasa a minusculas y quita tildes, para buscar sin importar como se escribio. */
    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    /** Convierte la fecha elegida al formato de fechaReto (00:00 en Bogota). */
    private static Date aFechaReto(LocalDate fecha) {
        return Date.from(fecha.atStartOfDay(ZONA_RETO).toInstant());
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
