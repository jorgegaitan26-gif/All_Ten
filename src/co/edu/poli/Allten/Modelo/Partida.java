package co.edu.poli.Allten.Modelo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * Representa una partida de All Ten. El Jugador hace parte de la partida
 * y la partida se compone de un Cronometro (1 - 1) y de sus Expresiones
 * (1 - 0..*).
 *
 * Metodos del diagrama: {@link #generarNumerosValidos()},
 * {@link #completarObjetivo(double)}, {@link #comprobarObjetivosCompletados()},
 * {@link #posiblesSoluciones()}, {@link #exportarResultados()} y
 * {@link #setDificultad(String)}.
 *
 * Niveles de dificultad: "FACIL" (+ - × ÷), "MEDIO" (+ potencia) y
 * "DIFICIL" (+ potencia y raiz cuadrada). La interfaz solo ofrece FACIL y DIFICIL.
 *
 * Modos de juego: si la partida tiene {@code fechaReto} es un reto diario
 * (todos reciben los mismos numeros ese dia); si {@code fechaReto} es
 * {@code null} es una partida libre con numeros aleatorios.
 */
public class Partida {

    /** Identificador de la partida en la base de datos; {@code null} mientras no se ha guardado. */
    private Integer idPartida;

    private List<Integer> numerosDisponibles;
    private List<String> operacionesRealizadas;
    private double tiempoTotal;
    private Date fechaFinalizacion;
    /** Fecha del reto diario; {@code null} en una partida libre. */
    private Date fechaReto;
    private String nivelDificultad;
    private Jugador jugador;
    private Cronometro cronometro;

    /**
     * Crea una partida nueva del jugador, en dificultad FACIL y sin numeros
     * todavia (se generan con {@link #generarNumerosValidos()}).
     *
     * @param jugador jugador que hace parte de la partida
     */
    public Partida(Jugador jugador) {
        this.jugador = jugador;
        this.numerosDisponibles = new ArrayList<>();
        this.operacionesRealizadas = new ArrayList<>();
        this.tiempoTotal = 0;
        this.fechaFinalizacion = null;
        this.nivelDificultad = "FACIL";
        this.cronometro = new Cronometro();
    }

    /**
     * Reconstruye una partida guardada en la base de datos.
     *
     * @param idPartida             identificador de la partida
     * @param jugador               jugador de la partida
     * @param tiempoTotal           tiempo total en segundos
     * @param operacionesRealizadas objetivos logrados
     * @param fechaFinalizacion     fecha y hora en que termino
     */
    public Partida(Integer idPartida, Jugador jugador, double tiempoTotal,
                   List<String> operacionesRealizadas, Date fechaFinalizacion) {
        this(jugador);
        this.idPartida = idPartida;
        this.tiempoTotal = tiempoTotal;
        this.fechaFinalizacion = fechaFinalizacion;
        if (operacionesRealizadas != null) {
            this.operacionesRealizadas = new ArrayList<>(operacionesRealizadas);
        }
    }

    /**
     * Genera 4 numeros (del 1 al 9) con los que se pueden lograr los 10
     * objetivos segun la dificultad: sortea numeros hasta que
     * {@link #posiblesSoluciones()} encuentra solucion para los 10.
     *
     * <ul>
     *   <li>Reto diario ({@code fechaReto} definida): la fecha es la semilla,
     *       asi que todos los jugadores reciben la misma combinacion ese dia.</li>
     *   <li>Partida libre ({@code fechaReto} nula): combinacion aleatoria,
     *       distinta de la que tenia la partida antes.</li>
     * </ul>
     * Se descartan combinaciones con menos de 3 valores distintos o con un
     * valor repetido mas de 2 veces.
     *
     * @return los 4 numeros generados (quedan en numerosDisponibles)
     */
    public List<Integer> generarNumerosValidos() {
        List<Integer> anteriores = new ArrayList<>(numerosDisponibles);
        anteriores.sort(null);

        // La semilla del reto es el dia (sin hora) de fechaReto.
        Random aleatorio = fechaReto != null
                ? new Random(fechaReto.toInstant().atZone(java.time.ZoneId.of("America/Bogota"))
                        .toLocalDate().toEpochDay())
                : new Random();

        boolean valida;
        do {
            List<Integer> candidatos = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                candidatos.add(1 + aleatorio.nextInt(9));
            }
            this.numerosDisponibles = candidatos;

            int distintos = 0;
            boolean repetidoDeMas = false;
            for (int i = 0; i < 4; i++) {
                if (candidatos.indexOf(candidatos.get(i)) == i) {
                    distintos++;
                }
                if (java.util.Collections.frequency(candidatos, candidatos.get(i)) > 2) {
                    repetidoDeMas = true;
                }
            }
            List<Integer> ordenados = new ArrayList<>(candidatos);
            ordenados.sort(null);
            boolean igualALaAnterior = fechaReto == null && ordenados.equals(anteriores);

            valida = distintos >= 3 && !repetidoDeMas && !igualALaAnterior
                    && contarObjetivosResolubles() == 10;
        } while (!valida);

        return numerosDisponibles;
    }

    /**
     * Fecha de hoy en la zona horaria del reto (America/Bogota), a las
     * 00:00. Se usa como {@code fechaReto} del reto diario.
     *
     * @return la fecha del reto de hoy
     */
    public static Date fechaActualDelReto() {
        java.time.ZoneId zona = java.time.ZoneId.of("America/Bogota");
        return Date.from(java.time.LocalDate.now(zona).atStartOfDay(zona).toInstant());
    }

    /**
     * Registra un objetivo si el resultado es un entero del 1 al 10 que
     * todavia no se habia logrado.
     *
     * @param resultado valor calculado por la expresion
     * @return true si el objetivo se registro en operacionesRealizadas
     */
    public boolean completarObjetivo(double resultado) {
        double redondeado = Math.rint(resultado);
        if (Double.isNaN(resultado) || Math.abs(resultado - redondeado) > 1e-9
                || redondeado < 1 || redondeado > 10) {
            return false;
        }

        String objetivo = String.valueOf((int) redondeado);
        if (operacionesRealizadas.contains(objetivo)) {
            return false;
        }

        operacionesRealizadas.add(objetivo);
        return true;
    }

    /**
     * Revisa si ya se lograron los 10 objetivos. La primera vez que se
     * cumple detiene el cronometro, guarda el tiempoTotal y la
     * fechaFinalizacion.
     *
     * @return true si ya se completaron los 10 objetivos
     */
    public boolean comprobarObjetivosCompletados() {
        boolean completados = operacionesRealizadas.size() >= 10;

        if (completados && fechaFinalizacion == null) {
            this.tiempoTotal = cronometro.detener();
            this.fechaFinalizacion = new Date();
        }
        return completados;
    }

    /**
     * Busca, con los 4 numeros y los operadores de la dificultad, una
     * expresion para cada objetivo del 1 al 10. Prueba todos los ordenes de
     * los numeros, todas las combinaciones de operadores y las 5 formas de
     * agruparlos con parentesis.
     *
     * @return lista "objetivo = expresion" del 1 al 10 (solo los que tienen solucion)
     */
    public List<String> posiblesSoluciones() {

        List<String> lista = new ArrayList<>();
        // En DIFICIL se recorre todo el espacio de busqueda para poder mostrar,
        // cuando existan, soluciones que usen potencia o raiz.
        String[] soluciones = buscarSoluciones("DIFICIL".equals(nivelDificultad));
        for (int objetivo = 1; objetivo <= 10; objetivo++) {
            if (soluciones[objetivo] != null) {
                lista.add(objetivo + " = " + soluciones[objetivo]);
            }
        }
        return lista;
    }

    /**
     * Cuenta cuantos objetivos del 1 al 10 se pueden lograr (busqueda rapida,
     * se detiene apenas encuentra los 10).
     */
    private int contarObjetivosResolubles() {
        int total = 0;
        for (String solucion : buscarSoluciones(false)) {
            if (solucion != null) {
                total++;
            }
        }
        return total;
    }

    /**
     * Motor de busqueda de soluciones.
     *
     * @param preferirNuevos si es true no se detiene al encontrar los 10
     *        objetivos y, para cada uno, prefiere una solucion que use potencia o
     *        raiz sobre una que solo use + - × ÷
     * @return arreglo de 11 posiciones (indice = objetivo); null si no hay solucion
     */
    private String[] buscarSoluciones(boolean preferirNuevos) {

        if (numerosDisponibles == null || numerosDisponibles.size() != 4) {
            return new String[11];
        }

        List<String> operadores = new ArrayList<>(List.of(
                Expresion.SUMA, Expresion.RESTA, Expresion.MULTIPLICACION, Expresion.DIVISION));
        if (!"FACIL".equals(nivelDificultad)) {
            operadores.add(Expresion.POTENCIA);
        }
        BusquedaSoluciones busqueda = new BusquedaSoluciones(
                operadores, "DIFICIL".equals(nivelDificultad) ? 16 : 1, preferirNuevos);
        buscarOrdenes(busqueda, new int[4], new boolean[4], 0);
        return busqueda.soluciones;
    }

    private boolean buscarOrdenes(BusquedaSoluciones busqueda, int[] orden,
                                  boolean[] indicesUsados, int posicion) {
        if (posicion == orden.length) {
            return buscarSolucionesParaOrden(busqueda, orden);
        }

        for (int indice = 0; indice < numerosDisponibles.size(); indice++) {
            if (indicesUsados[indice]) {
                continue;
            }
            orden[posicion] = indice;
            indicesUsados[indice] = true;
            boolean detener = buscarOrdenes(busqueda, orden, indicesUsados, posicion + 1);
            indicesUsados[indice] = false;
            if (detener) {
                return true;
            }
        }
        return false;
    }

    private boolean buscarSolucionesParaOrden(BusquedaSoluciones busqueda, int[] orden) {
        // Las 5 formas de agrupar 4 numeros, en notacion posfija (N = numero, O = operador).
        String[] formas = {"NNONONO", "NNONNOO", "NNNOONO", "NNNONOO", "NNNNOOO"};
        int cantidadOperadores = busqueda.operadores.size();

        for (int mascaraRaices = 0; mascaraRaices < busqueda.combinacionesRaiz; mascaraRaices++) {
            for (int combinacion = 0; combinacion < cantidadOperadores * cantidadOperadores
                    * cantidadOperadores; combinacion++) {
                String[] operadores = {
                    busqueda.operadores.get(combinacion % cantidadOperadores),
                    busqueda.operadores.get(combinacion / cantidadOperadores % cantidadOperadores),
                    busqueda.operadores.get(combinacion / (cantidadOperadores * cantidadOperadores))
                };
                for (String forma : formas) {
                    Evaluacion evaluacion = evaluarForma(forma, orden, mascaraRaices, operadores);
                    if (evaluacion != null) {
                        registrarSolucion(busqueda, evaluacion);
                        if (!busqueda.preferirNuevos && busqueda.encontradas == 10) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private Evaluacion evaluarForma(String forma, int[] orden, int mascaraRaices, String[] operadores) {
        double[] valores = new double[4];
        String[] textos = new String[4];
        int tope = 0;
        int siguienteNumero = 0;
        int siguienteOperador = 0;

        for (char simbolo : forma.toCharArray()) {
            if (simbolo == 'N') {
                int indice = orden[siguienteNumero++];
                int numero = numerosDisponibles.get(indice);
                if ((mascaraRaices & (1 << indice)) != 0) {
                    valores[tope] = Math.sqrt(numero);
                    textos[tope] = Expresion.RAIZ + " " + numero;
                } else {
                    valores[tope] = numero;
                    textos[tope] = String.valueOf(numero);
                }
                tope++;
                continue;
            }

            String operador = operadores[siguienteOperador++];
            double y = valores[--tope];
            String textoY = textos[tope];
            double x = valores[--tope];
            String textoX = textos[tope];
            double resultado = aplicarOperador(operador, x, y);
            if (!Double.isFinite(resultado)) {
                return null;
            }
            valores[tope] = resultado;
            textos[tope] = "( " + textoX + " " + operador + " " + textoY + " )";
            tope++;
        }
        return new Evaluacion(valores[0], textos[0]);
    }

    private double aplicarOperador(String operador, double x, double y) {
        if (Expresion.SUMA.equals(operador)) {
            return x + y;
        }
        if (Expresion.RESTA.equals(operador)) {
            return x - y;
        }
        if (Expresion.MULTIPLICACION.equals(operador)) {
            return x * y;
        }
        if (Expresion.DIVISION.equals(operador)) {
            return Math.abs(y) < 1e-9 ? Double.NaN : x / y;
        }

        double exponente = Math.rint(y);
        if (Math.abs(y - exponente) > 1e-9 || exponente < 0 || exponente > 5) {
            return Double.NaN;
        }
        return Math.pow(x, exponente);
    }

    private void registrarSolucion(BusquedaSoluciones busqueda, Evaluacion evaluacion) {
        double redondeado = Math.rint(evaluacion.valor);
        int objetivo = (int) redondeado;
        if (Math.abs(evaluacion.valor - redondeado) >= 1e-9 || objetivo < 1 || objetivo > 10) {
            return;
        }

        String texto = evaluacion.texto.substring(2, evaluacion.texto.length() - 2);
        boolean usaNuevos = texto.contains(Expresion.POTENCIA) || texto.contains(Expresion.RAIZ);
        if (busqueda.soluciones[objetivo] == null) {
            busqueda.soluciones[objetivo] = texto;
            busqueda.usaNuevos[objetivo] = usaNuevos;
            busqueda.encontradas++;
        } else if (busqueda.preferirNuevos && usaNuevos && !busqueda.usaNuevos[objetivo]) {
            busqueda.soluciones[objetivo] = texto;
            busqueda.usaNuevos[objetivo] = true;
        }
    }

    private static final class BusquedaSoluciones {
        private final String[] soluciones = new String[11];
        private final boolean[] usaNuevos = new boolean[11];
        private final List<String> operadores;
        private final int combinacionesRaiz;
        private final boolean preferirNuevos;
        private int encontradas;

        private BusquedaSoluciones(List<String> operadores, int combinacionesRaiz, boolean preferirNuevos) {
            this.operadores = operadores;
            this.combinacionesRaiz = combinacionesRaiz;
            this.preferirNuevos = preferirNuevos;
        }
    }

    private static final class Evaluacion {
        private final double valor;
        private final String texto;

        private Evaluacion(double valor, String texto) {
            this.valor = valor;
            this.texto = texto;
        }
    }

    /**
     * Genera una imagen PNG con el resumen de la partida (jugador, tiempo,
     * fecha y objetivos logrados) en la carpeta "resultados".
     *
     * @throws IOException si no se pudo escribir el archivo
     */
    public void exportarResultados() throws IOException {

        String nickname = jugador != null && jugador.getNickname() != null ? jugador.getNickname() : "Jugador";
        long minutos = (long) (tiempoTotal / 60);
        String tiempo = String.format("%02d:%05.2f (%.2f s)", minutos, tiempoTotal - minutos * 60, tiempoTotal);
        String fecha = fechaFinalizacion != null
                ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaFinalizacion)
                : "Partida en curso";

        int ancho = 720;
        int alto = 300 + Math.max(1, operacionesRealizadas.size()) * 34;

        // Colores de la paleta Okabe-Ito usada en la interfaz.
        Color azul = new Color(0x00, 0x72, 0xB2);
        Color verde = new Color(0x00, 0x9E, 0x73);

        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = imagen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, ancho, alto);
        g.setColor(azul);
        g.fillRect(0, 0, ancho, 80);
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        g.drawString("All Ten - Resultados", 32, 52);

        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        int y = 125;
        g.drawString("Jugador: " + nickname, 32, y);
        y += 32;
        g.drawString("Tiempo total: " + tiempo, 32, y);
        y += 32;
        g.drawString("Fecha de finalizacion: " + fecha, 32, y);
        y += 32;
        g.drawString("Dificultad: " + nivelDificultad + "    Numeros: " + numerosDisponibles, 32, y);

        y += 22;
        g.setColor(azul);
        g.setStroke(new BasicStroke(2));
        g.drawLine(32, y, ancho - 32, y);

        y += 36;
        g.setColor(Color.BLACK);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g.drawString("Objetivos logrados (" + operacionesRealizadas.size() + "/10)", 32, y);

        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
        if (operacionesRealizadas.isEmpty()) {
            y += 34;
            g.drawString("Sin objetivos logrados", 48, y);
        }
        for (String operacion : operacionesRealizadas) {
            y += 34;
            g.setColor(verde);
            g.fillOval(36, y - 13, 10, 10);
            g.setColor(Color.BLACK);
            g.drawString(operacion, 56, y);
        }
        g.dispose();

        Path carpeta = Paths.get("resultados");
        Files.createDirectories(carpeta);
        String sello = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(fechaFinalizacion != null ? fechaFinalizacion : new Date());
        String nombreSeguro = nickname.replaceAll("[^A-Za-z0-9_-]", "_");
        ImageIO.write(imagen, "png", carpeta.resolve("AllTen_" + nombreSeguro + "_" + sello + ".png").toFile());
    }

    /**
     * Cambia el nivel de dificultad: "FACIL", "MEDIO" o "DIFICIL" (sin
     * importar mayusculas ni tildes).
     *
     * @param nuevaDificultad nivel de dificultad
     * @throws IllegalArgumentException si el nivel no es uno de los tres
     */
    public void setDificultad(String nuevaDificultad) {
        String nivel = nuevaDificultad == null ? "" : nuevaDificultad.trim().toUpperCase()
                .replace("Á", "A").replace("Í", "I");

        if (!nivel.equals("FACIL") && !nivel.equals("MEDIO") && !nivel.equals("DIFICIL")) {
            throw new IllegalArgumentException("Dificultad no valida: " + nuevaDificultad
                    + ". Usa FACIL, MEDIO o DIFICIL.");
        }
        this.nivelDificultad = nivel;
    }

    // ----- Getters y setters -----

    /** @return identificador de la partida, o {@code null} si no se ha guardado */
    public Integer getIdPartida() {
        return idPartida;
    }

    /** @param idPartida identificador generado por la base de datos */
    public void setIdPartida(Integer idPartida) {
        this.idPartida = idPartida;
    }

    /** @return los 4 numeros de la partida */
    public List<Integer> getNumerosDisponibles() {
        return numerosDisponibles;
    }

    /** @param numerosDisponibles nuevos numeros de la partida */
    public void setNumerosDisponibles(List<Integer> numerosDisponibles) {
        this.numerosDisponibles = numerosDisponibles;
    }

    /** @return objetivos logrados, en el orden en que se lograron */
    public List<String> getOperacionesRealizadas() {
        return operacionesRealizadas;
    }

    /** @param operacionesRealizadas nuevos objetivos logrados */
    public void setOperacionesRealizadas(List<String> operacionesRealizadas) {
        this.operacionesRealizadas = operacionesRealizadas;
    }

    /** @return tiempo total en segundos (0 si la partida no ha terminado) */
    public double getTiempoTotal() {
        return tiempoTotal;
    }

    /** @param tiempoTotal nuevo tiempo total en segundos */
    public void setTiempoTotal(double tiempoTotal) {
        this.tiempoTotal = tiempoTotal;
    }

    /** @return fecha y hora de finalizacion, o {@code null} si sigue en curso */
    public Date getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    /** @param fechaFinalizacion nueva fecha de finalizacion */
    public void setFechaFinalizacion(Date fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }

    /** @return fecha del reto diario, o {@code null} si es una partida libre */
    public Date getFechaReto() {
        return fechaReto;
    }

    /** @param fechaReto fecha del reto diario, o {@code null} para una partida libre */
    public void setFechaReto(Date fechaReto) {
        this.fechaReto = fechaReto;
    }

    /** @return nivel de dificultad ("FACIL", "MEDIO" o "DIFICIL") */
    public String getNivelDificultad() {
        return nivelDificultad;
    }

    /** @return jugador de la partida */
    public Jugador getJugador() {
        return jugador;
    }

    /** @param jugador jugador de la partida */
    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
    }

    /** @return cronometro de la partida */
    public Cronometro getCronometro() {
        return cronometro;
    }

    /** @param cronometro cronometro de la partida */
    public void setCronometro(Cronometro cronometro) {
        this.cronometro = cronometro;
    }
}
