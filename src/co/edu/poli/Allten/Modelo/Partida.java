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
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import javax.imageio.ImageIO;

/**
 * Representa una partida de All Ten: los 4 numeros disponibles, los
 * objetivos (numeros del 1 al 10) que se han logrado, las expresiones que
 * los lograron, el tiempo total jugado y la fecha en que se termino.
 *
 * Implementa los diagramas de flujo de la clase Partida:
 * <ul>
 *   <li>Tarea 2: ciclo de la partida ({@link #setDificultad},
 *       {@link #generarNumerosValidos()}, {@link #completarObjetivo},
 *       {@link #comprobarObjetivosCompletados()}).</li>
 *   <li>Tarea 3: {@link #posiblesSoluciones()} y {@link #exportarResultados()}.</li>
 * </ul>
 */
public class Partida {

    /** Modos de juego disponibles. */
    public enum ModoPartida {
        RETO_DIARIO,
        LIBRE
    }

    /**
     * Niveles de dificultad de la partida y los operadores que permite cada uno
     * (las potencias y raices solo se permiten en los niveles avanzados).
     */
    public enum NivelDificultad {

        /** Suma, resta, multiplicacion y division. */
        FACIL(Set.of(Expresion.SUMA, Expresion.RESTA, Expresion.MULTIPLICACION, Expresion.DIVISION)),

        /** Lo de FACIL mas potencias. */
        MEDIO(Set.of(Expresion.SUMA, Expresion.RESTA, Expresion.MULTIPLICACION, Expresion.DIVISION,
                Expresion.POTENCIA)),

        /** Lo de MEDIO mas raices cuadradas. */
        DIFICIL(Set.of(Expresion.SUMA, Expresion.RESTA, Expresion.MULTIPLICACION, Expresion.DIVISION,
                Expresion.POTENCIA, Expresion.RAIZ));

        private final Set<String> operadoresPermitidos;

        NivelDificultad(Set<String> operadoresPermitidos) {
            this.operadoresPermitidos = operadoresPermitidos;
        }

        /**
         * Indica si el nivel permite un operador.
         *
         * @param operador simbolo del operador (ver constantes de {@link Expresion})
         * @return true si el operador se puede usar en este nivel
         */
        public boolean permite(String operador) {
            return operadoresPermitidos.contains(operador);
        }

        /** Da acceso a los operadores del nivel.
         * @return los operadores permitidos en este nivel */
        public Set<String> getOperadoresPermitidos() {
            return operadoresPermitidos;
        }
    }

    private static final int CANTIDAD_NUMEROS = 4;
    private static final int OBJETIVO_MINIMO = 1;
    private static final int OBJETIVO_MAXIMO = 10;
    private static final double TOLERANCIA = 1e-9;
    private static final ZoneId ZONA_RETO = ZoneId.of("America/Bogota");

    /** Carpeta por defecto donde {@link #exportarResultados()} guarda la imagen. */
    private static final String CARPETA_EXPORTACION = "resultados";

    // ----- Atributos del diagrama de clases -----
    private int idPartida;
    private List<Integer> numerosDisponibles;
    private List<Integer> objetivosCompletados;
    private List<String> operacionesRealizadas;
    private double tiempoTotal;
    private LocalDateTime fechaFinalizacion;
    private LocalDate fechaReto;
    private NivelDificultad nivelDificultad;
    private ModoPartida modoPartida;

    /** Jugador dueno de la partida (Partida.Jugador_id_Jugador; relacion jugador 1 - 0..* partida). */
    private Jugador jugador;

    // ----- Composicion (partida 1 - 1 cronometro / partida 1 - 0..* expresion) -----
    private final Cronometro cronometro;
    private final List<Expresion> historialExpresiones;

    private Random aleatorio;

    /** Crea una partida vacia (sin numeros ni objetivos), en dificultad FACIL; llamar a {@link #iniciarPartida()} para comenzarla. */
    public Partida() {
        this(null);
    }

    /**
     * Crea una partida vacia vinculada a un jugador.
     *
     * @param jugador jugador dueno de la partida
     */
    public Partida(Jugador jugador) {
        this.jugador = jugador;
        this.idPartida = 0;
        this.numerosDisponibles = new ArrayList<>();
        this.objetivosCompletados = new ArrayList<>();
        this.operacionesRealizadas = new ArrayList<>();
        this.tiempoTotal = 0;
        this.fechaFinalizacion = null;
        this.fechaReto = null;
        this.nivelDificultad = NivelDificultad.FACIL;
        this.modoPartida = ModoPartida.RETO_DIARIO;
        this.cronometro = new Cronometro();
        this.historialExpresiones = new ArrayList<>();
    }

    /**
     * Reconstruye una partida guardada en la base de datos.
     *
     * @param idPartida identificador persistido
     * @param jugador jugador propietario
     * @param tiempoTotal tiempo jugado en segundos
     * @param operaciones operaciones guardadas
     * @param fechaFinalizacion fecha de finalización
     */
    public Partida(int idPartida, Jugador jugador, double tiempoTotal,
            List<String> operaciones, LocalDateTime fechaFinalizacion) {
        this(idPartida, jugador, tiempoTotal, operaciones, fechaFinalizacion, null);
    }

    /**
     * Reconstruye una partida guardada junto con la fecha de su reto diario.
     *
     * @param idPartida identificador persistido
     * @param jugador jugador propietario
     * @param tiempoTotal tiempo jugado en segundos
     * @param operaciones operaciones guardadas
     * @param fechaFinalizacion fecha de finalización
     * @param fechaReto fecha común del reto diario, o {@code null} para una partida libre
     */
    public Partida(int idPartida, Jugador jugador, double tiempoTotal,
            List<String> operaciones, LocalDateTime fechaFinalizacion, LocalDate fechaReto) {
        this(jugador);
        this.idPartida = idPartida;
        this.tiempoTotal = tiempoTotal;
        this.operacionesRealizadas.addAll(operaciones);
        this.fechaFinalizacion = fechaFinalizacion;
        this.fechaReto = fechaReto;
        this.modoPartida = fechaReto == null ? ModoPartida.LIBRE : ModoPartida.RETO_DIARIO;
    }

    // =====================================================================
    // TAREA 2: CICLO DE LA PARTIDA
    // =====================================================================

    /**
     * [B] Establece el nivel de dificultad (debe hacerse antes de
     * {@link #iniciarPartida()}, porque define los numeros que se generan).
     *
     * @param nivelDificultad nuevo nivel de la partida
     */
    public void setDificultad(NivelDificultad nivelDificultad) {
        this.nivelDificultad = nivelDificultad == null ? NivelDificultad.FACIL : nivelDificultad;
    }

    /**
     * Comienza la partida: genera los numeros validos y, una vez estan
     * listos, arranca el cronometro.
     */
    public void iniciarPartida() {
        iniciarPartida(fechaActualDelReto());
    }

    /** Inicia una partida libre con una combinación aleatoria no ligada a la fecha. */
    public void iniciarPartidaLibre() {
        prepararPartida(null, ModoPartida.LIBRE);
    }

    /**
     * Inicia la partida con el reto determinista correspondiente a una fecha.
     *
     * @param fechaReto fecha del reto en la zona horaria oficial
     */
    public void iniciarPartida(LocalDate fechaReto) {
        if (fechaReto == null) {
            throw new IllegalArgumentException("La fecha del reto no puede ser nula.");
        }
        prepararPartida(fechaReto, ModoPartida.RETO_DIARIO);
    }

    private void prepararPartida(LocalDate fechaReto, ModoPartida modoPartida) {
        // [A] Inicio: configurar partida (el nivel ya se fijo con setDificultad [B])
        idPartida = 0;
        objetivosCompletados = new ArrayList<>();
        operacionesRealizadas = new ArrayList<>();
        historialExpresiones.clear();
        tiempoTotal = 0;
        fechaFinalizacion = null;
        this.fechaReto = fechaReto;
        this.modoPartida = modoPartida;

        // [C] Generar 4 numeros validos
        generarNumerosValidos();

        // [D] Iniciar cronometro
        cronometro.iniciar();

        // [E] A partir de aqui el jugador ingresa expresiones (crearNuevoIntento)
    }

    /**
     * Genera de forma reproducible los 4 números del reto diario (del 1 al 9)
     * que permiten resolver los objetivos del 1 al 10 según la dificultad.
     * En modo diario, la fecha es la semilla para compartir la combinación.
     * En modo libre, cada partida usa una combinación aleatoria nueva.
     */
    public void generarNumerosValidos() {
        if (modoPartida == ModoPartida.RETO_DIARIO && fechaReto == null) {
            fechaReto = fechaActualDelReto();
        }
        List<Integer> numerosAnteriores = numerosDisponibles;
        aleatorio = modoPartida == ModoPartida.RETO_DIARIO
                ? new Random(fechaReto.toEpochDay())
                : new Random();
        List<Integer> candidatos;
        do {
            candidatos = new ArrayList<>();
            for (int i = 0; i < CANTIDAD_NUMEROS; i++) {
                candidatos.add(1 + aleatorio.nextInt(9));
            }
        } while (!esCombinacionEquilibrada(candidatos)
                || calcularSoluciones(candidatos, nivelDificultad).size()
                        < OBJETIVO_MAXIMO - OBJETIVO_MINIMO + 1
                || (modoPartida == ModoPartida.LIBRE
                        && mismaCombinacion(candidatos, numerosAnteriores)));

        this.numerosDisponibles = candidatos;
    }

    private static boolean mismaCombinacion(List<Integer> primero, List<Integer> segundo) {
        if (primero.size() != segundo.size()) {
            return false;
        }
        List<Integer> primeroOrdenado = new ArrayList<>(primero);
        List<Integer> segundoOrdenado = new ArrayList<>(segundo);
        Collections.sort(primeroOrdenado);
        Collections.sort(segundoOrdenado);
        return primeroOrdenado.equals(segundoOrdenado);
    }

    /** Evita retos con menos de tres valores distintos o un valor repetido más de dos veces. */
    private static boolean esCombinacionEquilibrada(List<Integer> numeros) {
        int cantidadDistintos = 0;
        for (int i = 0; i < numeros.size(); i++) {
            int numero = numeros.get(i);
            if (Collections.frequency(numeros, numero) > 2) {
                return false;
            }
            if (numeros.indexOf(numero) == i) {
                cantidadDistintos++;
            }
        }
        return cantidadDistintos >= 3;
    }

    /** @return fecha actual del reto según la zona horaria oficial de Colombia */
    public static LocalDate fechaActualDelReto() {
        return LocalDate.now(ZONA_RETO);
    }

    /**
     * Registra el resultado y la expresion cuando se alcanza un objetivo.
     *
     * @param resultado valor entero (1 a 10) obtenido al calcular la expresion
     * @param expresion texto de la expresion que produjo ese resultado
     * @return true si el resultado coincidia con un objetivo pendiente y se registro
     */
    public boolean completarObjetivo(int resultado, String expresion) {

        // [F] ¿El resultado coincide con un objetivo pendiente?
        if (resultado < OBJETIVO_MINIMO || resultado > OBJETIVO_MAXIMO) {
            return false;                                   // No -> vuelve a [E]
        }
        if (objetivosCompletados.contains(resultado)) {
            return false;                                   // No -> vuelve a [E]
        }

        // [G] Registrar en objetivosCompletados y operacionesRealizadas
        objetivosCompletados.add(resultado);
        operacionesRealizadas.add(resultado + " = " + expresion);

        // [H] ¿comprobarObjetivosCompletados = true? (si lo es, cierra la partida [I][J])
        comprobarObjetivosCompletados();
        return true;
    }

    /**
     * Version que recibe directamente el double que retorna
     * {@code Expresion.calcularResultado()}.
     *
     * @param resultado valor calculado por la expresion
     * @param expresion texto de la expresion que produjo ese resultado
     * @return true si el resultado es un entero que coincidia con un objetivo pendiente
     */
    public boolean completarObjetivo(double resultado, String expresion) {
        double redondeado = Math.rint(resultado);
        if (Double.isNaN(resultado) || Math.abs(resultado - redondeado) > TOLERANCIA) {
            return false;
        }
        return completarObjetivo((int) redondeado, expresion);
    }

    /**
     * Evalua si ya se consiguieron los 10 objetivos. Si es asi, detiene el
     * cronometro, guarda el tiempoTotal y registra la fechaFinalizacion.
     *
     * @return true si ya se completaron los 10 objetivos (1 al 10)
     */
    public boolean comprobarObjetivosCompletados() {

        boolean completados = objetivosCompletados.size() >= (OBJETIVO_MAXIMO - OBJETIVO_MINIMO + 1);

        if (completados) {
            finalizarTiempo();                              // [I] y [J]
        }
        return completados;                                 // [K] Partida finalizada con exito
    }

    /**
     * [I] Detiene el cronometro y guarda el tiempoTotal; [J] registra la
     * fechaFinalizacion. Si la partida ya estaba finalizada no hace nada,
     * para no alterar el tiempo ya registrado.
     */
    public void finalizarTiempo() {
        if (fechaFinalizacion != null) {
            return;
        }
        this.tiempoTotal = cronometro.detener();
        this.fechaFinalizacion = LocalDateTime.now();
    }

    /** Reinicia la partida: nuevos numeros, objetivos vacios, cronometro en cero. */
    public void reiniciarPartida() {
        if (modoPartida == ModoPartida.LIBRE) {
            iniciarPartidaLibre();
        } else {
            iniciarPartida();
        }
    }

    /** @return modo de juego de esta partida */
    public ModoPartida getModoPartida() {
        return modoPartida;
    }

    /**
     * Crea una nueva Expresion (un nuevo intento) ligada a los numeros y a
     * la dificultad de esta partida y la agrega al historial de intentos.
     *
     * @return la Expresion recien creada, vacia
     */
    public Expresion crearNuevoIntento() {
        Expresion expresion = new Expresion(numerosDisponibles, nivelDificultad);
        historialExpresiones.add(expresion);
        return expresion;
    }

    // =====================================================================
    // TAREA 3: posiblesSoluciones
    // =====================================================================

    /**
     * Calcula una expresion con los 4 numeros de la partida y los operadores
     * permitidos por la dificultad para cada objetivo del 1 al 10.
     *
     * @return una lista de soluciones "objetivo = expresion", ordenada por objetivo
     */
    public List<String> posiblesSoluciones() {
        Map<Integer, String> soluciones = calcularSoluciones(numerosDisponibles, nivelDificultad);

        List<String> lista = new ArrayList<>();
        for (Map.Entry<Integer, String> solucion : soluciones.entrySet()) {
            lista.add(solucion.getKey() + " = " + solucion.getValue());
        }
        return lista;
    }

    /**
     * Busca una expresion por objetivo usando los 4 numeros y los
     * operadores del nivel.
     */
    private Map<Integer, String> calcularSoluciones(List<Integer> numeros, NivelDificultad nivel) {

        Map<Integer, String> soluciones = new TreeMap<>();
        if (numeros.size() != CANTIDAD_NUMEROS) {
            return soluciones;
        }

        // Hojas: cada numero (y, si el nivel permite raices, tambien su raiz).
        int combinacionesDeHojas = nivel.permite(Expresion.RAIZ) ? (1 << numeros.size()) : 1;

        for (int mascara = 0; mascara < combinacionesDeHojas; mascara++) {

            List<Nodo> hojas = new ArrayList<>();
            for (int i = 0; i < numeros.size(); i++) {
                int numero = numeros.get(i);
                if ((mascara & (1 << i)) != 0) {
                    hojas.add(new Nodo(Math.sqrt(numero), Expresion.RAIZ + " " + numero));
                } else {
                    hojas.add(new Nodo(numero, String.valueOf(numero)));
                }
            }

            if (buscarCombinaciones(hojas, numeros, nivel, soluciones)) {
                break;
            }
        }
        return soluciones;
    }

    /**
     * Combina recursivamente pares de nodos con cada operador permitido,
     * cubriendo todas las formas de agrupar con parentesis.
     */
    private boolean buscarCombinaciones(List<Nodo> nodos, List<Integer> numeros,
                                        NivelDificultad nivel, Map<Integer, String> soluciones) {

        if (nodos.size() == 1) {
            registrarSiEsObjetivo(nodos.get(0), numeros, nivel, soluciones);
            return soluciones.size() == OBJETIVO_MAXIMO - OBJETIVO_MINIMO + 1;
        }

        for (int i = 0; i < nodos.size(); i++) {
            for (int j = 0; j < nodos.size(); j++) {

                if (i == j) {
                    continue;
                }

                List<Nodo> resto = new ArrayList<>();
                for (int k = 0; k < nodos.size(); k++) {
                    if (k != i && k != j) {
                        resto.add(nodos.get(k));
                    }
                }

                for (String operador : List.of(Expresion.SUMA, Expresion.RESTA, Expresion.MULTIPLICACION,
                        Expresion.DIVISION, Expresion.POTENCIA)) {

                    if (!nivel.permite(operador)) {
                        continue;
                    }
                    // Suma y multiplicacion son conmutativas: basta un orden.
                    boolean conmutativo = Expresion.SUMA.equals(operador) || Expresion.MULTIPLICACION.equals(operador);
                    if (conmutativo && i > j) {
                        continue;
                    }

                    Nodo combinado = combinar(nodos.get(i), operador, nodos.get(j));
                    if (combinado == null) {
                        continue;
                    }

                    resto.add(combinado);
                    if (buscarCombinaciones(resto, numeros, nivel, soluciones)) {
                        return true;
                    }
                    resto.remove(resto.size() - 1);
                }
            }
        }
        return false;
    }

    private Nodo combinar(Nodo a, String operador, Nodo b) {

        double valor;
        switch (operador) {
            case Expresion.SUMA:
                valor = a.valor + b.valor;
                break;
            case Expresion.RESTA:
                valor = a.valor - b.valor;
                break;
            case Expresion.MULTIPLICACION:
                valor = a.valor * b.valor;
                break;
            case Expresion.DIVISION:
                if (Math.abs(b.valor) < TOLERANCIA) {
                    return null;
                }
                valor = a.valor / b.valor;
                break;
            case Expresion.POTENCIA:
                // Exponentes enteros pequenos para mantener los valores manejables.
                double exponente = Math.rint(b.valor);
                if (Math.abs(b.valor - exponente) > TOLERANCIA || exponente < 0 || exponente > 5) {
                    return null;
                }
                valor = Math.pow(a.valor, exponente);
                break;
            default:
                return null;
        }

        if (Double.isNaN(valor) || Double.isInfinite(valor) || Math.abs(valor) > 1_000_000) {
            return null;
        }
        return new Nodo(valor, "( " + a.texto + " " + operador + " " + b.texto + " )");
    }

    /**
     * [PS4] Si el valor es un objetivo pendiente, comprueba con una
     * {@link Expresion} real que la expresion es valida segun la
     * dificultad y, si lo es, la agrega si todavía no hay una para ese objetivo.
     */
    private void registrarSiEsObjetivo(Nodo nodo, List<Integer> numeros,
                                       NivelDificultad nivel, Map<Integer, String> soluciones) {

        double redondeado = Math.rint(nodo.valor);
        if (Math.abs(nodo.valor - redondeado) > TOLERANCIA) {
            return;
        }
        int objetivo = (int) redondeado;
        if (objetivo < OBJETIVO_MINIMO || objetivo > OBJETIVO_MAXIMO || soluciones.containsKey(objetivo)) {
            return;
        }

        // Se quitan los parentesis exteriores que agrega la ultima combinacion.
        String texto = nodo.texto;
        if (texto.startsWith("( ") && texto.endsWith(" )")) {
            texto = texto.substring(2, texto.length() - 2);
        }

        Expresion prueba = new Expresion(numeros, nivel);
        for (String elemento : texto.split(" ")) {

            boolean agregado;
            if (elemento.matches("\\d")) {
                agregado = prueba.concatenarDigitos(Integer.parseInt(elemento));
            } else if (Expresion.PARENTESIS_ABRE.equals(elemento) || Expresion.PARENTESIS_CIERRA.equals(elemento)) {
                agregado = prueba.agregarParentesis(elemento);
            } else {
                agregado = prueba.agregarOperacion(elemento);
            }
            if (!agregado) {
                return;                                     // [PS4] No -> sigue buscando
            }
        }

        if (prueba.validarExpresion()
                && Math.abs(prueba.calcularResultado() - objetivo) < TOLERANCIA
                && prueba.comprobarCoincidenciaObjetivo()) {
            soluciones.put(objetivo, prueba.getTextoFormateado());
        }
    }

    /** Valor parcial de la busqueda junto con el texto de la expresion que lo produce. */
    private static final class Nodo {
        final double valor;
        final String texto;

        Nodo(double valor, String texto) {
            this.valor = valor;
            this.texto = texto;
        }
    }

    // =====================================================================
    // TAREA 3: exportarResultados
    // =====================================================================

    /**
     * Genera una imagen PNG con el resumen de la partida y la guarda en la
     * carpeta "resultados" del directorio de trabajo.
     *
     * @return la ruta del archivo generado
     * @throws IOException si no se pudo escribir el archivo
     */
    public Path exportarResultados() throws IOException {
        return exportarResultados(Paths.get(CARPETA_EXPORTACION));
    }

    /**
     * Genera una imagen PNG con el resumen de la partida (nickname,
     * tiempoTotal, fechaFinalizacion y operacionesRealizadas) y la guarda
     * en la carpeta indicada.
     *
     * @param carpeta carpeta donde se guarda la imagen (se crea si no existe)
     * @return la ruta del archivo generado
     * @throws IOException si no se pudo escribir el archivo
     */
    public Path exportarResultados(Path carpeta) throws IOException {

        // [ER1] Inicio: solicitud de exportar resultados
        // [ER2] Recopilar datos finales de la partida
        String nickname = jugador != null && jugador.getNickname() != null ? jugador.getNickname() : "Jugador";
        String tiempo = formatearTiempo(tiempoTotal);
        String fecha = fechaFinalizacion != null
                ? fechaFinalizacion.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : "Partida en curso";
        List<String> operaciones = new ArrayList<>(operacionesRealizadas);

        // [ER3] Generar representacion grafica de los resultados
        BufferedImage imagen = dibujarResumen(nickname, tiempo, fecha, operaciones);

        // [ER4] Guardar archivo exportado en el sistema de ficheros
        Files.createDirectories(carpeta);
        String sello = (fechaFinalizacion != null ? fechaFinalizacion : LocalDateTime.now())
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nombreSeguro = nickname.replaceAll("[^A-Za-z0-9_-]", "_");
        Path archivo = carpeta.resolve("AllTen_" + nombreSeguro + "_" + sello + ".png");
        ImageIO.write(imagen, "png", archivo.toFile());

        // [ER5] Fin: resultado exportado exitosamente
        return archivo;
    }

    private BufferedImage dibujarResumen(String nickname, String tiempo, String fecha, List<String> operaciones) {

        int ancho = 720;
        int alto = 300 + Math.max(1, operaciones.size()) * 34;

        // Colores de la paleta Okabe-Ito usada en la interfaz.
        Color azul = new Color(0x00, 0x72, 0xB2);
        Color verde = new Color(0x00, 0x9E, 0x73);
        Color texto = Color.BLACK;

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

        g.setColor(texto);
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
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        g.setColor(texto);
        g.drawString("Operaciones realizadas (" + operaciones.size() + "/10)", 32, y);

        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
        if (operaciones.isEmpty()) {
            y += 34;
            g.drawString("Sin objetivos logrados", 48, y);
        }
        for (String operacion : operaciones) {
            y += 34;
            g.setColor(verde);
            g.fillOval(36, y - 13, 10, 10);
            g.setColor(texto);
            g.drawString(operacion, 56, y);
        }

        g.dispose();
        return imagen;
    }

    private String formatearTiempo(double segundos) {
        long minutos = (long) (segundos / 60);
        double resto = segundos - minutos * 60;
        return String.format("%02d:%05.2f (%.2f s)", minutos, resto, segundos);
    }

    // =====================================================================
    // ACCESO A LOS DATOS
    // =====================================================================

    /** @return identificador persistido de la partida, o cero si aún no se ha guardado */
    public int getIdPartida() {
        return idPartida;
    }

    /** @param idPartida identificador asignado por la base de datos */
    public void setIdPartida(int idPartida) {
        this.idPartida = idPartida;
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

    /** Da acceso al tiempo total jugado, como Duration (lo usa la vista para mostrar "mm:ss").
     * @return el tiempo total, disponible una vez finalizada la partida */
    public Duration getTiempoTotal() {
        return Duration.ofNanos(Math.round(tiempoTotal * 1_000_000_000.0));
    }

    /** Da acceso al tiempo total jugado en segundos (valor que entrega {@code Cronometro.detener()}).
     * @return el tiempo total en segundos, 0 si la partida no ha terminado */
    public double getTiempoTotalSegundos() {
        return tiempoTotal;
    }

    /** Da acceso a la fecha de finalizacion.
     * @return la fecha y hora en que se completaron los 10 objetivos, o {@code null} si la partida sigue en curso */
    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    /** @return fecha del reto diario, o {@code null} si la partida es libre o no tiene fecha registrada */
    public LocalDate getFechaReto() {
        return fechaReto;
    }

    /** Da acceso a la dificultad.
     * @return el nivel de dificultad de la partida */
    public NivelDificultad getNivelDificultad() {
        return nivelDificultad;
    }

    /** Da acceso al jugador de la partida.
     * @return el jugador dueno de la partida, o {@code null} si no se ha vinculado */
    public Jugador getJugador() {
        return jugador;
    }

    /**
     * Vincula la partida con su jugador (llave foranea Jugador_id_Jugador).
     *
     * @param jugador jugador dueno de la partida
     */
    public void setJugador(Jugador jugador) {
        this.jugador = jugador;
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