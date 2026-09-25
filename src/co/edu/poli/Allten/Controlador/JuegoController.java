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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

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
    @FXML private Label lblMensaje;
    @FXML private Label lblTiempo;
    @FXML private Label lblTiempoFinal;
    @FXML private StackPane panelVictoria;

    private Jugador jugador;
    private Partida partida;
    private Expresion intentoActual;

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
     * Llamado por {@link MainSceneController} justo despues de cargar el
     * FXML de esta pantalla. Crea al Jugador y la Partida, las inicia, y
     * construye los controles dinamicos (fila de objetivos, operadores y
     * numeros) y el reloj visible.
     *
     * @param nickname nickname que el jugador escribio en el menu principal
     */
    public void iniciarPartida(String nickname) {

        jugador = new Jugador(nickname);
        partida = new Partida();
        partida.iniciarPartida();
        intentoActual = partida.crearNuevoIntento();

        construirFilaObjetivos();
        construirFilaOperadores();
        reconstruirFilaNumeros();
        refrescarVista();
        iniciarReloj();
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
     * orden que {@code Partida.getNumerosDisponibles()}, para que el
     * indice de cada boton coincida con el indice usado por
     * {@link Expresion#isNumeroUsado(int)}.
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
     * ese digito a la expresion actual (ver {@link Expresion#concatenarDigitos(String)})
     * y refresca la vista.
     *
     * @param valor valor numerico mostrado en el tile presionado
     */
    private void onDigito(int valor) {

        boolean agregado = intentoActual.concatenarDigitos(String.valueOf(valor));

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
        intentoActual = partida.crearNuevoIntento();
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
                mostrarPanelVictoria();
                break;
        }

        // Solo se descarta el intento cuando llega a un estado terminal;
        // si la expresion todavia esta incompleta se deja que el jugador
        // la siga editando (o la borre manualmente).
        if (resultado != ResultadoIntento.EXPRESION_INCOMPLETA
                && resultado != ResultadoIntento.NUMEROS_INCOMPLETOS) {
            intentoActual = partida.crearNuevoIntento();
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

        if (!intentoActual.validarUsoDeNumeros()) {
            return ResultadoIntento.NUMEROS_INCOMPLETOS;
        }

        if (!intentoActual.validarDivisionCero()) {
            return ResultadoIntento.DIVISION_POR_CERO;
        }

        if (!intentoActual.validarReglasOperaciones() || !intentoActual.validarNumerosPermitidos()) {
            return ResultadoIntento.NO_COINCIDE_OBJETIVO;
        }

        intentoActual.calcularResultado();

        if (!intentoActual.comprobarCoincidenciaObjetivo()) {
            return ResultadoIntento.NO_COINCIDE_OBJETIVO;
        }

        int objetivo = (int) Math.rint(intentoActual.getResultado());

        if (partida.getObjetivosCompletados().contains(objetivo)) {
            return ResultadoIntento.OBJETIVO_YA_LOGRADO;
        }

        partida.completarObjetivo(objetivo, intentoActual.getTextoFormateado());

        if (partida.comprobarObjetivosCompletados()) {
            partida.finalizarTiempo();
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

        String texto = intentoActual.getTextoFormateado();
        lblExpresion.setText(texto.isEmpty() ? "Toca un número para empezar" : texto);

        for (int i = 0; i < botonesNumero.size(); i++) {
            botonesNumero.get(i).setDisable(intentoActual.isNumeroUsado(i));
        }
    }

    /** Colorea en verde la etiqueta del ultimo objetivo agregado a {@code Partida.getObjetivosCompletados()}. */
    private void marcarUltimoObjetivoLogrado() {

        List<Integer> logrados = partida.getObjetivosCompletados();

        if (logrados.isEmpty()) {
            return;
        }

        int ultimo = logrados.get(logrados.size() - 1);
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
        lblTiempo.setText(formatearDuracion(partida.getCronometro().tiempoTranscurrido()));
    }

    /**
     * Formatea una duracion como "mm:ss" para mostrarla en pantalla.
     *
     * @param duracion duracion a formatear
     * @return el texto formateado, por ejemplo "02:07"
     */
    private String formatearDuracion(java.time.Duration duracion) {
        long totalSegundos = Math.max(0, duracion.getSeconds());
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

    /** Oculta el panel de victoria (por ejemplo, al volver a jugar o al ir al menu). */
    private void ocultarPanelVictoria() {
        panelVictoria.setVisible(false);
        panelVictoria.setManaged(false);
    }

    /** Boton "Jugar otra vez": oculta la victoria y arranca una partida nueva sin salir de esta pantalla. */
    @FXML
    private void jugarOtraVez() {

        ocultarPanelVictoria();
        partida.reiniciarPartida();
        intentoActual = partida.crearNuevoIntento();

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
