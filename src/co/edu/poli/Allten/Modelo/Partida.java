package co.edu.poli.Allten.Modelo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Representa una partida de All Ten: los 4 numeros disponibles, los
 * objetivos (numeros del 1 al 10) que se han logrado, las expresiones que
 * los lograron y el tiempo total jugado.
 */
public class Partida {

    private static final int CANTIDAD_NUMEROS = 4;
    private static final int OBJETIVO_MINIMO = 1;
    private static final int OBJETIVO_MAXIMO = 10;

    // ----- Atributos del diagrama de clases -----
    private List<Integer> numerosDisponibles;
    private List<Integer> objetivosCompletados;
    private List<String> operacionesRealizadas;
    private Duration tiempoTotal;

    // ----- Composicion (partida 1 - 1 cronometro / partida 1 - 0..* expresion) -----
    private final Cronometro cronometro;
    private final List<Expresion> historialExpresiones;

    private final Random aleatorio;

    /** Crea una partida vacia (sin numeros ni objetivos); llamar a {@link #iniciarPartida()} para comenzarla. */
    public Partida() {
        this.numerosDisponibles = new ArrayList<>();
        this.objetivosCompletados = new ArrayList<>();
        this.operacionesRealizadas = new ArrayList<>();
        this.tiempoTotal = Duration.ZERO;
        this.cronometro = new Cronometro();
        this.historialExpresiones = new ArrayList<>();
        this.aleatorio = new Random();
    }

    /** Comienza la partida: genera numeros validos y arranca el cronometro. */
    public void iniciarPartida() {
        objetivosCompletados = new ArrayList<>();
        operacionesRealizadas = new ArrayList<>();
        historialExpresiones.clear();
        generarNumerosValidos();
        cronometro.iniciar();
    }

    /**
     * Genera los 4 numeros (del 1 al 9) con los que se juega el reto.
     */
    public void generarNumerosValidos() {

        List<Integer> nuevosNumeros = new ArrayList<>();

        for (int i = 0; i < CANTIDAD_NUMEROS; i++) {
            nuevosNumeros.add(1 + aleatorio.nextInt(9));
        }

        this.numerosDisponibles = nuevosNumeros;
    }

    /**
     * Registra que se alcanzo el objetivo indicado con la expresion dada,
     * siempre que el resultado sea un objetivo valido (1 a 10) y no se haya
     * completado antes.
     *
     * @param resultado valor entero (1 a 10) obtenido al calcular la expresion
     * @param expresion texto de la expresion que produjo ese resultado
     */
    public void completarObjetivo(int resultado, String expresion) {

        if (resultado < OBJETIVO_MINIMO || resultado > OBJETIVO_MAXIMO) {
            return;
        }

        if (objetivosCompletados.contains(resultado)) {
            return;
        }

        objetivosCompletados.add(resultado);
        operacionesRealizadas.add(resultado + " = " + expresion);
    }

    /** Comprueba si la partida ya esta completamente ganada.
     * @return true si ya se completaron los 10 objetivos (1 al 10) */
    public boolean comprobarObjetivosCompletados() {
        return objetivosCompletados.size() >= (OBJETIVO_MAXIMO - OBJETIVO_MINIMO + 1);
    }

    /** Reinicia la partida: nuevos numeros, objetivos vacios, cronometro en cero. */
    public void reiniciarPartida() {
        iniciarPartida();
    }

    /** Detiene el cronometro y guarda el tiempo total jugado. */
    public void finalizarTiempo() {
        cronometro.detener();
        this.tiempoTotal = cronometro.getTiempoTotal();
    }

    /**
     * Crea una nueva Expresion (un nuevo intento) ligada a los numeros de
     * esta partida y la agrega al historial de intentos.
     *
     * @return la Expresion recien creada, vacia
     */
    public Expresion crearNuevoIntento() {
        Expresion expresion = new Expresion(numerosDisponibles);
        historialExpresiones.add(expresion);
        return expresion;
    }

    /** Da acceso a los numeros de la partida.
     * @return los 4 numeros disponibles para esta partida (vista de solo lectura) */
    public List<Integer> getNumerosDisponibles() {
        return Collections.unmodifiableList(numerosDisponibles);
    }

    /** Da acceso a los objetivos logrados.
     * @return los objetivos (1 a 10) logrados hasta el momento, en el orden en que se lograron */
    public List<Integer> getObjetivosCompletados() {
        return Collections.unmodifiableList(objetivosCompletados);
    }

    /** Da acceso a las expresiones ganadoras.
     * @return el texto "objetivo = expresion" de cada objetivo logrado, en el mismo orden que {@link #getObjetivosCompletados()} */
    public List<String> getOperacionesRealizadas() {
        return Collections.unmodifiableList(operacionesRealizadas);
    }

    /** Da acceso al tiempo total jugado.
     * @return el tiempo total, disponible una vez se llama a {@link #finalizarTiempo()} */
    public Duration getTiempoTotal() {
        return tiempoTotal;
    }

    /** Da acceso al cronometro de la partida.
     * @return el cronometro asociado a esta partida (relacion partida 1 - 1 cronometro) */
    public Cronometro getCronometro() {
        return cronometro;
    }

    /** Da acceso al historial de intentos.
     * @return todos los intentos (Expresion) que se han creado en esta partida, ganadores o no */
    public List<Expresion> getHistorialExpresiones() {
        return Collections.unmodifiableList(historialExpresiones);
    }
}
