package co.edu.poli.Allten.Controlador;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import co.edu.poli.Allten.DAO.DAOPartida;
import co.edu.poli.Allten.Modelo.Expresion;
import co.edu.poli.Allten.Modelo.Jugador;
import co.edu.poli.Allten.Modelo.Partida;

/**
 * Controlador de la pantalla de juego. Habla directamente con el Modelo
 * (Jugador, Partida, Expresion): crea la partida, construye los controles
 * dinamicos, y traduce cada accion del jugador en llamadas a Expresion y
 * Partida, y sus resultados en cambios sobre la vista.
 */
public class JuegoController {

    @FXML private HBox filaObjetivos;
    @FXML private HBox filaNumeros;
    @FXML private HBox filaOperadores;
    @FXML private StackPane zonaTrabajo;
    @FXML private Label lblExpresion;
    @FXML private Label lblResultadoParcial;
    @FXML private Label lblMensaje;
    @FXML private Label lblTiempo;
    @FXML private Label lblTiempoFinal;
    @FXML private Label lblModoPartida;
    @FXML private Label lblTituloVictoria;
    @FXML private Label lblInfoVictoria;
    @FXML private Button btnJugarOtraVez;
    @FXML private Button btnReiniciar;
    @FXML private StackPane panelVictoria;
    @FXML private VBox panelAyuda;
    @FXML private FlowPane listaAyuda;
    @FXML private Label lblNumerosAyuda;
    @FXML private Button btnAyuda;

    private Jugador jugador;
    private Partida partida;
    private Expresion intentoActual;
    private DAOPartida partidaDAO;
    private boolean partidaGuardada;
    private boolean retoDiario;
    private boolean ayudaCargada;

    /** Expresiones con las que se logro cada objetivo (ej. "7 = 3 + 4"), para la tarjeta de resultados. */
    private final List<String> expresionesLogradas = new ArrayList<>();

    private final Map<Integer, Label> etiquetasObjetivo = new HashMap<>();
    private final List<Button> botonesNumero = new ArrayList<>();

    private Timeline reloj;

    /** Constructor por defecto; JavaFX lo invoca al cargar el FXML de esta pantalla. */
    public JuegoController() {
    }

    /**
     * Resultado de comprobar la expresion actual contra los objetivos de la partida.
     */
    private enum ResultadoIntento {

        /** La expresion no termina en un numero o ")" valido, o quedo un parentesis sin cerrar. */
        EXPRESION_INCOMPLETA,

        /** La expresion es sintacticamente valida pero no usa los 4 numeros disponibles. */
        NUMEROS_INCOMPLETOS,

        /** La expresion esta completa pero contiene una division por cero. */
        DIVISION_POR_CERO,

        /** La expresion es valida y completa, pero su resultado no es un entero entre 1 y 10. */
        NO_COINCIDE_OBJETIVO,

        /** El resultado es un objetivo valido, pero ya habia sido logrado antes en esta partida. */
        OBJETIVO_YA_LOGRADO,

        /** Se logro un objetivo nuevo, pero la partida todavia no termina. */
        OBJETIVO_LOGRADO,

        /** Se logro el ultimo objetivo que faltaba: la partida quedo completamente ganada. */
        PARTIDA_GANADA
    }

    /**
     * Recibe el DAO con el que se guarda la partida al terminarla.
     *
     * @param partidaDAO DAO de partidas
     */
    public void setPartidaDAO(DAOPartida partidaDAO) {
        this.partidaDAO = partidaDAO;
    }

    /**
     * Llamado por {@link MainSceneController} justo despues de cargar el
     * FXML de esta pantalla. Crea la Partida del jugador, genera los numeros,
     * arranca el cronometro y construye los controles dinamicos (fila de
     * objetivos, operadores y numeros) y el reloj visible.
     *
     * @param jugador    jugador identificado en el menu principal
     * @param retoDiario true para el reto diario, false para una partida libre
     */
    public void iniciarPartida(Jugador jugador, boolean retoDiario) {

        this.jugador = jugador;
        this.retoDiario = retoDiario;
        crearPartidaNueva();

        lblModoPartida.setText(retoDiario ? "Reto diario" : "Partida libre");
        btnReiniciar.setText(retoDiario ? "Reiniciar reto" : "Nueva partida");
        btnJugarOtraVez.setText(retoDiario ? "Repetir reto diario" : "Nueva partida");
        lblTituloVictoria.setText(retoDiario ? "Completaste el reto diario!" : "Completaste la partida!");
        lblInfoVictoria.setText(retoDiario
                ? "Puedes repetir este reto; el ranking diario conserva tu mejor tiempo."
                : "Cada nueva partida genera una combinación diferente.");

        construirFilaObjetivos();
        construirFilaOperadores();
        reconstruirFilaNumeros();
        refrescarVista();
        iniciarReloj();
    }

    /**
     * Crea una partida nueva del jugador en el modo actual: en el reto
     * diario los numeros salen de la fecha de hoy; en la libre son aleatorios.
     */
    private void crearPartidaNueva() {
        partida = new Partida(jugador);
        if (retoDiario) {
            partida.setFechaReto(Partida.fechaActualDelReto());
        }
        partida.generarNumerosValidos();
        partida.getCronometro().iniciar();
        partidaGuardada = false;
        expresionesLogradas.clear();
        intentoActual = new Expresion(partida);
    }

    // =====================================================================
    // CONSTRUCCION DE LA INTERFAZ DINAMICA
    // =====================================================================

    /** Crea las 10 etiquetas de objetivos (1 al 10) en estado "pendiente" y las guarda en {@link #etiquetasObjetivo}. */
    private void construirFilaObjetivos() {

        filaObjetivos.getChildren().clear();
        etiquetasObjetivo.clear();

        for (int objetivo = 1; objetivo <= 10; objetivo++) {

            Label etiqueta = new Label(String.valueOf(objetivo));
            etiqueta.getStyleClass().add("objetivo-pendiente");

            etiquetasObjetivo.put(objetivo, etiqueta);
            filaObjetivos.getChildren().add(etiqueta);
        }
    }

    /** Crea los botones "(", +, -, x, /, ")" y les asigna su accion sobre {@link #intentoActual}. */
    private void construirFilaOperadores() {

        filaOperadores.getChildren().clear();

        Button parAbre = crearBotonSimbolo("(");
        parAbre.setOnAction(e -> onParentesis("("));
        filaOperadores.getChildren().add(parAbre);

        for (String operador : List.of(
                Expresion.SUMA, Expresion.RESTA, Expresion.MULTIPLICACION, Expresion.DIVISION)) {

            Button boton = crearBotonSimbolo(operador);
            boton.setOnAction(e -> onOperador(operador));
            filaOperadores.getChildren().add(boton);
        }

        Button parCierra = crearBotonSimbolo(")");
        parCierra.setOnAction(e -> onParentesis(")"));
        filaOperadores.getChildren().add(parCierra);
    }

    /**
     * Crea un boton con estilo de "tile" de operador/parentesis.
     *
     * @param texto simbolo a mostrar en el boton (ej. "+", "(")
     * @return el boton ya estilizado, sin accion asignada todavia
     */
    private Button crearBotonSimbolo(String texto) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("tile-operador");
        return boton;
    }

    /**
     * Crea los 4 botones de numeros de la partida actual, en el mismo
     * orden que {@code Partida.getNumerosDisponibles()}.
     */
    private void reconstruirFilaNumeros() {

        filaNumeros.getChildren().clear();
        botonesNumero.clear();

        List<Integer> numeros = partida.getNumerosDisponibles();

        for (int i = 0; i < numeros.size(); i++) {

            int valor = numeros.get(i);
            Button boton = new Button(String.valueOf(valor));
            boton.getStyleClass().add("tile-numero");
            boton.setOnAction(e -> onDigito(valor));

            botonesNumero.add(boton);
            filaNumeros.getChildren().add(boton);
        }
    }

    // =====================================================================
    // ACCIONES DEL JUGADOR
    // =====================================================================

    /**
     * Se llama cuando el jugador toca un tile de numero. Intenta agregar
     * ese digito a la expresion actual (ver {@link Expresion#concatenarDigitos(int)})
     * y refresca la vista.
     *
     * @param valor valor numerico mostrado en el tile presionado
     */
    private void onDigito(int valor) {

        boolean agregado = intentoActual.concatenarDigitos(valor);

        if (!agregado) {
            mostrarMensaje("Ese número ya no está disponible en esta posición.");
        } else {
            mostrarMensaje("");
        }

        refrescarVista();
    }

    /**
     * Se llama cuando el jugador toca un tile de operador (+, -, x, /).
     *
     * @param operador simbolo del operador presionado
     */
    private void onOperador(String operador) {

        intentoActual.continuarOperacion();
        boolean agregado = intentoActual.agregarOperacion(operador);

        if (!agregado) {
            mostrarMensaje("No puedes poner un operador ahí.");
        } else {
            mostrarMensaje("");
        }

        refrescarVista();
    }

    /**
     * Se llama cuando el jugador toca el boton "(" o ")".
     *
     * @param parentesis "(" o ")" segun el boton presionado
     */
    private void onParentesis(String parentesis) {

        boolean agregado = intentoActual.agregarParentesis(parentesis);

        if (!agregado) {
            mostrarMensaje("Ese paréntesis no es válido en esta posición.");
        } else {
            mostrarMensaje("");
        }

        refrescarVista();
    }

    /** Boton "Borrar": deshace el ultimo elemento agregado a la expresion actual. */
    @FXML
    private void borrarUltimo() {
        intentoActual.borrarUltimoElemento();
        mostrarMensaje("");
        refrescarVista();
    }

    /** Boton "Limpiar intento": descarta la expresion actual y comienza una vacia con los mismos 4 numeros. */
    @FXML
    private void limpiarIntento() {
        intentoActual = new Expresion(partida);
        mostrarMensaje("");
        refrescarVista();
    }

    /**
     * Boton "Comprobar": valida y calcula la expresion actual, y traduce
     * el {@link ResultadoIntento} obtenido en un mensaje para el jugador
     * y, si corresponde, en marcar un objetivo como logrado o mostrar la
     * pantalla de victoria.
     */
    @FXML
    private void comprobarIntento() {

        ResultadoIntento resultado = evaluarIntentoActual();

        switch (resultado) {

            case EXPRESION_INCOMPLETA:
                mostrarMensaje("Termina la expresión antes de comprobar (te falta cerrar algo).");
                break;

            case NUMEROS_INCOMPLETOS:
                mostrarMensaje("Debes usar los 4 números disponibles.");
                break;

            case DIVISION_POR_CERO:
                mostrarMensaje("Esa expresión hace una división por cero.");
                break;

            case NO_COINCIDE_OBJETIVO:
                mostrarMensaje("El resultado no es un número entero entre 1 y 10. ¡Intenta otra combinación!");
                break;

            case OBJETIVO_YA_LOGRADO:
                mostrarMensaje("Ese objetivo ya lo habías logrado. ¡Prueba llegar a otro número!");
                break;

            case OBJETIVO_LOGRADO:
                marcarUltimoObjetivoLogrado();
                mostrarMensaje("¡Bien! Objetivo logrado.");
                break;

            case PARTIDA_GANADA:
                marcarUltimoObjetivoLogrado();
                guardarPartidaFinalizada();
                mostrarPanelVictoria();
                compartirResultado();
                break;
        }

        // Solo se descarta el intento cuando llega a un estado terminal;
        // si la expresion todavia esta incompleta se deja que el jugador
        // la siga editando (o la borre manualmente).
        if (resultado != ResultadoIntento.EXPRESION_INCOMPLETA
                && resultado != ResultadoIntento.NUMEROS_INCOMPLETOS) {
            intentoActual = new Expresion(partida);
        }

        refrescarVista();
    }

    /**
     * Aplica, en orden, todas las validaciones de Expresion y decide el
     * {@link ResultadoIntento} correspondiente; si el resultado coincide
     * con un objetivo nuevo, lo registra en la partida.
     *
     * @return el resultado de la evaluacion
     */
    private ResultadoIntento evaluarIntentoActual() {

        if (!intentoActual.validarExpresionFinalizada()) {
            return ResultadoIntento.EXPRESION_INCOMPLETA;
        }

        int digitosUsados = intentoActual.getElementosExpresion().replaceAll("[^0-9]", "").length();
        if (digitosUsados < partida.getNumerosDisponibles().size()) {
            return ResultadoIntento.NUMEROS_INCOMPLETOS;
        }

        if (!intentoActual.validarExpresion()) {
            return ResultadoIntento.NO_COINCIDE_OBJETIVO;
        }

        double valor = intentoActual.calcularResultado();
        if (Double.isNaN(valor)) {
            return ResultadoIntento.DIVISION_POR_CERO;
        }

        double redondeado = Math.rint(valor);
        if (Math.abs(valor - redondeado) > 1e-9 || redondeado < 1 || redondeado > 10) {
            return ResultadoIntento.NO_COINCIDE_OBJETIVO;
        }

        if (partida.getOperacionesRealizadas().contains(String.valueOf((int) redondeado))) {
            return ResultadoIntento.OBJETIVO_YA_LOGRADO;
        }

        partida.completarObjetivo(valor);
        expresionesLogradas.add((int) redondeado + " = " + intentoActual.getElementosExpresion());

        if (partida.comprobarObjetivosCompletados()) {
            return ResultadoIntento.PARTIDA_GANADA;
        }

        return ResultadoIntento.OBJETIVO_LOGRADO;
    }

    // =====================================================================
    // ACTUALIZACION DE LA VISTA
    // =====================================================================

    /**
     * Sincroniza la vista con el estado del intento actual: actualiza el
     * texto de la zona de trabajo y deshabilita los tiles de numeros que
     * ya fueron usados en la expresion en construccion.
     */
    private void refrescarVista() {

        String texto = intentoActual.getElementosExpresion();
        lblExpresion.setText(texto.isEmpty() ? "Toca un número para empezar" : texto);
        refrescarResultadoParcial();

        // Cuantas veces se ha usado cada digito en la expresion actual.
        int[] usados = new int[10];
        for (char caracter : texto.toCharArray()) {
            if (Character.isDigit(caracter)) {
                usados[caracter - '0']++;
            }
        }

        List<Integer> numeros = partida.getNumerosDisponibles();
        for (int i = 0; i < botonesNumero.size(); i++) {
            int valor = numeros.get(i);
            boolean usado = usados[valor] > 0;
            if (usado) {
                usados[valor]--;
            }
            botonesNumero.get(i).setDisable(usado);
            botonesNumero.get(i).pseudoClassStateChanged(javafx.css.PseudoClass.getPseudoClass("usado"), usado);
        }
    }

    private void refrescarResultadoParcial() {
        if (!intentoActual.validarExpresionFinalizada() || !intentoActual.validarExpresion()) {
            lblResultadoParcial.setText("");
            return;
        }

        double resultado = intentoActual.calcularResultado();
        if (Double.isNaN(resultado) || Double.isInfinite(resultado)) {
            lblResultadoParcial.setText("");
            return;
        }

        lblResultadoParcial.setText("Resultado parcial: " + formatearResultado(resultado));
    }

    private String formatearResultado(double resultado) {
        double entero = Math.rint(resultado);
        if (Math.abs(resultado - entero) < 1e-9) {
            return String.valueOf((long) entero);
        }
        return String.format(java.util.Locale.ROOT, "%.4f", resultado)
                .replaceAll("0+$", "")
                .replaceAll("\\.$", "");
    }

    /** Colorea en verde la etiqueta del ultimo objetivo agregado a {@code Partida.getOperacionesRealizadas()}. */
    private void marcarUltimoObjetivoLogrado() {

        List<String> logrados = partida.getOperacionesRealizadas();

        if (logrados.isEmpty()) {
            return;
        }

        int ultimo = Integer.parseInt(logrados.get(logrados.size() - 1));
        Label etiqueta = etiquetasObjetivo.get(ultimo);

        if (etiqueta != null) {
            etiqueta.getStyleClass().remove("objetivo-pendiente");
            etiqueta.getStyleClass().add("objetivo-logrado");
        }
    }

    /**
     * Muestra un mensaje corto de retroalimentacion debajo de la zona de
     * trabajo (o lo limpia, si se llama con cadena vacia).
     *
     * @param mensaje texto a mostrar al jugador
     */
    private void mostrarMensaje(String mensaje) {
        lblMensaje.setText(mensaje);
    }

    @FXML
    private void mostrarAyuda() {
        boolean mostrar = !panelAyuda.isVisible();
        if (mostrar && !ayudaCargada) {
            cargarSolucionesAyuda();
            ayudaCargada = true;
        }

        panelAyuda.setVisible(mostrar);
        panelAyuda.setManaged(mostrar);
        btnAyuda.setText(mostrar ? "×" : "?");
        btnAyuda.setAccessibleText(mostrar ? "Cerrar ayuda" : "Mostrar ayuda");
    }

    private void cargarSolucionesAyuda() {
        lblNumerosAyuda.setText("Números: " + partida.getNumerosDisponibles());
        List<String> soluciones = partida.posiblesSoluciones();
        if (soluciones.isEmpty()) {
            Label sinSoluciones = new Label("No se encontraron soluciones para este reto.");
            sinSoluciones.getStyleClass().add("sin-soluciones-ayuda");
            listaAyuda.getChildren().add(sinSoluciones);
            return;
        }

        for (String solucion : soluciones) {
            listaAyuda.getChildren().add(crearTarjetaAyuda(solucion));
        }
    }

    private HBox crearTarjetaAyuda(String solucion) {
        int separador = solucion.indexOf(" = ");
        if (separador < 0) {
            throw new IllegalArgumentException("Formato de solución inesperado: " + solucion);
        }

        Label objetivo = new Label(solucion.substring(0, separador));
        objetivo.getStyleClass().add("objetivo-tarjeta-ayuda");
        Label expresion = new Label(solucion.substring(separador + 3));
        expresion.setWrapText(true);
        HBox.setHgrow(expresion, Priority.ALWAYS);
        expresion.getStyleClass().add("expresion-tarjeta-ayuda");

        HBox tarjeta = new HBox(12, objetivo, expresion);
        tarjeta.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        tarjeta.setPrefWidth(260);
        tarjeta.getStyleClass().add("tarjeta-ayuda");
        return tarjeta;
    }

    private void guardarPartidaFinalizada() {
        if (partidaGuardada) {
            return;
        }
        if (partidaDAO == null || jugador.getIdJugador() == null) {
            mostrarErrorPersistencia("No se pudo guardar la partida: falta la conexión o el ID del jugador.");
            return;
        }
        try {
            partidaDAO.create(partida);
            partidaGuardada = true;
        } catch (Exception e) {
            mostrarErrorPersistencia("No se pudo guardar la partida: " + e.getMessage());
        }
    }

    private void mostrarErrorPersistencia(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error al guardar la partida");
        alerta.setHeaderText("La partida terminó, pero no quedó registrada.");
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // =====================================================================
    // CRONOMETRO EN PANTALLA
    // =====================================================================

    /** Arranca (o reinicia) el Timeline que actualiza {@link #lblTiempo} cada segundo. */
    private void iniciarReloj() {

        if (reloj != null) {
            reloj.stop();
        }

        reloj = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> actualizarTiempoEnPantalla()));
        reloj.setCycleCount(Timeline.INDEFINITE);
        reloj.play();
    }

    /** Refresca el texto de {@link #lblTiempo} con el tiempo transcurrido del {@link co.edu.poli.Allten.Modelo.Cronometro}. */
    private void actualizarTiempoEnPantalla() {
        Long inicio = partida.getCronometro().getTiempo_Inicio();
        double segundos = inicio == null ? 0 : (System.nanoTime() - inicio) / 1_000_000_000.0;
        lblTiempo.setText(formatearDuracion(segundos));
    }

    /**
     * Formatea un tiempo en segundos como "mm:ss" para mostrarlo en pantalla.
     *
     * @param segundosTotales tiempo en segundos
     * @return el texto formateado, por ejemplo "02:07"
     */
    private String formatearDuracion(double segundosTotales) {
        long totalSegundos = Math.max(0, (long) segundosTotales);
        long minutos = totalSegundos / 60;
        long segundos = totalSegundos % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    // =====================================================================
    // VICTORIA
    // =====================================================================

    /** Detiene el reloj y muestra el panel de victoria con el tiempo final. */
    private void mostrarPanelVictoria() {

        if (reloj != null) {
            reloj.stop();
        }

        lblTiempoFinal.setText("Tiempo total: " + formatearDuracion(partida.getTiempoTotal()));

        panelVictoria.setVisible(true);
        panelVictoria.setManaged(true);
    }

    /**
     * Boton "Compartir resultado" del panel de victoria (tambien se abre solo
     * al ganar): muestra la tarjeta de resultados, que se puede guardar o
     * copiar como imagen.
     */
    @FXML
    private void compartirResultado() {
        try {
            ResultadoController.mostrar(filaNumeros.getScene().getWindow(), jugador, partida,
                    new ArrayList<>(expresionesLogradas));
        } catch (java.io.IOException e) {
            mostrarMensaje("No se pudo abrir la tarjeta de resultados.");
        }
    }

    /** Oculta el panel de victoria (por ejemplo, al volver a jugar o al ir al menu). */
    private void ocultarPanelVictoria() {
        panelVictoria.setVisible(false);
        panelVictoria.setManaged(false);
    }

    /** Boton "Jugar otra vez": oculta la victoria y arranca una partida nueva sin salir de esta pantalla. */
    @FXML
    private void jugarOtraVez() {

        ocultarPanelVictoria();
        crearPartidaNueva();

        reconstruirFilaNumeros();
        construirFilaObjetivos();
        mostrarMensaje("");
        refrescarVista();
        iniciarReloj();
    }

    // =====================================================================
    // NAVEGACION
    // =====================================================================

    /** Boton "Reiniciar" del encabezado: equivalente a "Jugar otra vez" pero disponible durante la partida. */
    @FXML
    private void reiniciarPartida() {
        jugarOtraVez();
    }

    /**
     * Boton "Menu": detiene el reloj y vuelve a MainMenuView.fxml.
     *
     * @throws Exception si el archivo FXML del menu principal no se puede cargar
     */
    @FXML
    private void volverAlMenu() throws Exception {

        if (reloj != null) {
            reloj.stop();
        }

        Parent root = FXMLLoader.load(getClass().getResource("/vista/MainMenuView.fxml"));

        Stage ventanaActual = (Stage) filaNumeros.getScene().getWindow();
        ventanaActual.setScene(new Scene(root, 1000, 700));
        ventanaActual.setTitle("All Ten");
    }
}
