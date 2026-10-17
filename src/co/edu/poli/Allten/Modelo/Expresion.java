package co.edu.poli.Allten.Modelo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 * Representa la expresion matematica que el jugador va construyendo con los
 * 4 numeros disponibles de la partida, los operadores y los parentesis.
 *
 * Cada instancia de Expresion corresponde a UN intento sobre una Partida
 * (relacion partida 1 - 0..* expresion).
 *
 * Implementa los diagramas de flujo de la clase Expresion:
 * <ul>
 *   <li>Tarea 1: atributos {@code elementosExpresion} (String) y
 *       {@code resultado} (double) y los metodos de construccion.</li>
 *   <li>Tarea 2: metodo coordinador {@link #validarExpresion()}.</li>
 *   <li>Tarea 3: {@link #calcularResultado()} y {@link #continuarOperacion()}.</li>
 * </ul>
 */
public class Expresion {

    /** Simbolo del operador de suma. */
    public static final String SUMA = "+";

    /** Simbolo del operador de resta. */
    public static final String RESTA = "-";

    /** Simbolo del operador de multiplicacion. */
    public static final String MULTIPLICACION = "×"; // ×

    /** Simbolo del operador de division. */
    public static final String DIVISION = "÷";        // ÷

    /** Simbolo del operador de potencia (a ^ b). Solo en dificultades avanzadas. */
    public static final String POTENCIA = "^";

    /** Simbolo del operador de raiz cuadrada (√ a), unario y prefijo. Solo en la dificultad mas alta. */
    public static final String RAIZ = "√";            // √

    /** Simbolo de parentesis de apertura. */
    public static final String PARENTESIS_ABRE = "(";

    /** Simbolo de parentesis de cierre. */
    public static final String PARENTESIS_CIERRA = ")";

    /** Separador entre los elementos dentro de {@code elementosExpresion}. */
    private static final String SEPARADOR = " ";

    private static final Set<String> OPERADORES_BINARIOS = Set.of(SUMA, RESTA, MULTIPLICACION, DIVISION, POTENCIA);
    private static final Set<String> OPERADORES = Set.of(SUMA, RESTA, MULTIPLICACION, DIVISION, POTENCIA, RAIZ);

    /** Tolerancia para comparar resultados double con enteros. */
    private static final double TOLERANCIA = 1e-9;

    // ----- Atributos del diagrama (Tarea 1) -----

    /** Cadena matematica en construccion (ej. "3 + 4 × 2"); cada elemento separado por un espacio. */
    private String elementosExpresion;

    /** Valor numerico obtenido tras la evaluacion de la expresion. */
    private double resultado;

    // ----- Estado interno necesario para validar el uso correcto de los 4 numeros -----
    private final List<Integer> numerosDisponibles;
    private final boolean[] numerosUsados;
    private final Partida.NivelDificultad nivelDificultad;

    /** Indices consumidos de numerosDisponibles para cada elemento de elementosExpresion (vacio si no es numero). */
    private final List<List<Integer>> indicesPorElemento;

    private boolean numeroAbierto;
    private int parentesisAbiertos;

    /** true cuando el primer elemento es un resultado parcial fijado con {@link #continuarOperacion()}. */
    private boolean resultadoParcialFijado;

    /** Historial de acciones, usado unicamente por borrarUltimoElemento() para deshacer paso a paso. */
    private final Deque<Accion> historialAcciones;

    /** Buffer de trabajo utilizado por resolverParentesis()/respetarOrdenOperaciones(). */
    private List<String> trabajo;

    /**
     * Crea una expresion vacia, en dificultad {@link Partida.NivelDificultad#FACIL}.
     *
     * @param numerosDisponibles los numeros de la partida con los que se debe construir la expresion
     */
    public Expresion(List<Integer> numerosDisponibles) {
        this(numerosDisponibles, Partida.NivelDificultad.FACIL);
    }

    /**
     * Crea una expresion vacia asociada a los numeros disponibles y a la
     * dificultad de una partida (normalmente invocado desde
     * {@code Partida.crearNuevoIntento()}).
     *
     * @param numerosDisponibles los numeros de la partida con los que se debe construir la expresion
     * @param nivelDificultad    dificultad de la partida; define que operadores se permiten
     */
    public Expresion(List<Integer> numerosDisponibles, Partida.NivelDificultad nivelDificultad) {
        this.numerosDisponibles = new ArrayList<>(numerosDisponibles);
        this.numerosUsados = new boolean[this.numerosDisponibles.size()];
        this.nivelDificultad = nivelDificultad == null ? Partida.NivelDificultad.FACIL : nivelDificultad;
        this.elementosExpresion = "";
        this.indicesPorElemento = new ArrayList<>();
        this.historialAcciones = new ArrayDeque<>();
        this.resultado = Double.NaN;
        this.numeroAbierto = false;
        this.parentesisAbiertos = 0;
        this.resultadoParcialFijado = false;
    }

    // =====================================================================
    // TAREA 1: METODOS DE CONSTRUCCION / MODIFICACION
    // =====================================================================

    /**
     * Agrega un digito a la expresion. Si el ultimo elemento es un numero
     * que todavia se puede extender (por ejemplo, se acaba de tocar "1" y
     * ahora se toca "2"), el digito se concatena para formar un numero de
     * varias cifras ("12"). En caso contrario se crea un nuevo elemento
     * numerico. Cada digito consume uno de los numeros disponibles.
     *
     * @param digito digito a agregar (0 a 9)
     * @return true si el digito se pudo agregar, false si la posicion no es
     *         valida o no queda ningun numero disponible con ese valor
     */
    public boolean concatenarDigitos(int digito) {

        if (digito < 0 || digito > 9) {
            return false;
        }

        String textoDigito = String.valueOf(digito);
        int indiceDisponible = buscarIndiceDisponible(textoDigito);
        if (indiceDisponible == -1) {
            return false;
        }

        List<String> elementos = elementos();
        boolean puedeConcatenar = numeroAbierto && !elementos.isEmpty();
        boolean puedeIniciarNumero = elementos.isEmpty()
                || esOperador(ultimo(elementos))
                || PARENTESIS_ABRE.equals(ultimo(elementos));

        if (puedeConcatenar) {

            int ultimo = elementos.size() - 1;
            elementos.set(ultimo, elementos.get(ultimo) + textoDigito);
            guardar(elementos);
            indicesPorElemento.get(ultimo).add(indiceDisponible);
            numerosUsados[indiceDisponible] = true;

            historialAcciones.push(new Accion(TipoAccion.DIGITO, false));
            return true;

        } else if (puedeIniciarNumero) {

            elementos.add(textoDigito);
            guardar(elementos);
            List<Integer> indices = new ArrayList<>();
            indices.add(indiceDisponible);
            indicesPorElemento.add(indices);
            numerosUsados[indiceDisponible] = true;
            numeroAbierto = true;

            historialAcciones.push(new Accion(TipoAccion.DIGITO, true));
            return true;
        }

        return false;
    }

    /**
     * Version que recibe el digito como texto (la usa la vista al tocar un tile).
     *
     * @param digito digito a agregar, como String de un solo caracter ("0"-"9")
     * @return el mismo resultado que {@link #concatenarDigitos(int)}
     */
    public boolean concatenarDigitos(String digito) {
        if (digito == null || !digito.matches("\\d")) {
            return false;
        }
        return concatenarDigitos(Integer.parseInt(digito));
    }

    /**
     * Inserta un operador en la expresion: +, -, ×, ÷ y, segun la
     * dificultad, potencia (^) o raiz (√).
     * <ul>
     *   <li>Los operadores binarios deben ir despues de un numero o de ")".</li>
     *   <li>La raiz es prefija: va donde podria empezar un numero (al inicio,
     *       despues de otro operador o despues de "(").</li>
     * </ul>
     *
     * @param operador uno de {@link #SUMA}, {@link #RESTA}, {@link #MULTIPLICACION},
     *                 {@link #DIVISION}, {@link #POTENCIA} o {@link #RAIZ}
     * @return true si se pudo agregar; false si la posicion no es valida, el
     *         simbolo no es un operador o la dificultad no lo permite
     */
    public boolean agregarOperacion(String operador) {

        if (operador == null || !OPERADORES.contains(operador)) {
            return false;
        }

        if (!nivelDificultad.permite(operador)) {
            return false;
        }

        List<String> elementos = elementos();
        boolean posicionValida;

        if (RAIZ.equals(operador)) {
            posicionValida = elementos.isEmpty()
                    || esOperadorBinario(ultimo(elementos))
                    || PARENTESIS_ABRE.equals(ultimo(elementos));
        } else {
            posicionValida = !elementos.isEmpty()
                    && (esNumero(ultimo(elementos)) || PARENTESIS_CIERRA.equals(ultimo(elementos)));
        }

        if (!posicionValida) {
            return false;
        }

        elementos.add(operador);
        guardar(elementos);
        indicesPorElemento.add(new ArrayList<>());
        numeroAbierto = false;

        historialAcciones.push(new Accion(TipoAccion.OPERADOR, true));
        return true;
    }

    /**
     * Controla la apertura y el cierre correcto de parentesis.
     *
     * @param parentesis {@link #PARENTESIS_ABRE} o {@link #PARENTESIS_CIERRA}
     * @return true si se pudo agregar; false si la posicion no es valida
     */
    public boolean agregarParentesis(String parentesis) {

        List<String> elementos = elementos();

        if (PARENTESIS_ABRE.equals(parentesis)) {

            boolean posicionValida = elementos.isEmpty()
                    || esOperador(ultimo(elementos))
                    || PARENTESIS_ABRE.equals(ultimo(elementos));

            if (!posicionValida) {
                return false;
            }

            elementos.add(PARENTESIS_ABRE);
            guardar(elementos);
            indicesPorElemento.add(new ArrayList<>());
            parentesisAbiertos++;
            numeroAbierto = false;

            historialAcciones.push(new Accion(TipoAccion.PARENTESIS_ABRE, true));
            return true;

        } else if (PARENTESIS_CIERRA.equals(parentesis)) {

            boolean posicionValida = parentesisAbiertos > 0
                    && !elementos.isEmpty()
                    && (esNumero(ultimo(elementos)) || PARENTESIS_CIERRA.equals(ultimo(elementos)));

            if (!posicionValida) {
                return false;
            }

            elementos.add(PARENTESIS_CIERRA);
            guardar(elementos);
            indicesPorElemento.add(new ArrayList<>());
            parentesisAbiertos--;
            numeroAbierto = false;

            historialAcciones.push(new Accion(TipoAccion.PARENTESIS_CIERRA, true));
            return true;
        }

        return false;
    }

    /**
     * Elimina el ultimo elemento anadido (un digito, un operador, un
     * parentesis o el resultado parcial). Si el ultimo numero tenia varias
     * cifras concatenadas, solo se retira la ultima cifra.
     */
    public void borrarUltimoElemento() {

        List<String> elementos = elementos();
        if (historialAcciones.isEmpty() || elementos.isEmpty()) {
            return;
        }

        Accion accion = historialAcciones.pop();
        int ultimo = elementos.size() - 1;

        if (accion.tipo == TipoAccion.PARENTESIS_ABRE) {
            parentesisAbiertos--;
        } else if (accion.tipo == TipoAccion.PARENTESIS_CIERRA) {
            parentesisAbiertos++;
        }

        if (accion.creoElementoNuevo) {

            // Se elimina el elemento completo (operador, parentesis, primer
            // digito de un numero o resultado parcial) y se liberan sus numeros.
            liberarIndices(indicesPorElemento.remove(ultimo));
            elementos.remove(ultimo);

            if (accion.tipo == TipoAccion.RESULTADO_PARCIAL) {
                resultadoParcialFijado = false;
            }

        } else {

            // Se retira solo la ultima cifra concatenada.
            String valorActual = elementos.get(ultimo);
            elementos.set(ultimo, valorActual.substring(0, valorActual.length() - 1));

            List<Integer> indices = indicesPorElemento.get(ultimo);
            int indiceLiberado = indices.remove(indices.size() - 1);
            numerosUsados[indiceLiberado] = false;
        }

        guardar(elementos);
        numeroAbierto = !elementos.isEmpty()
                && esNumeroDeDigitos(ultimo(elementos))
                && !esResultadoParcial(elementos.size() - 1);
    }

    /**
     * Permite reutilizar un resultado parcial combinandolo con los numeros
     * restantes: si la expresion actual es valida, se calcula, se fija su
     * resultado como el primer elemento de una nueva secuencia y los
     * numeros no usados quedan libres para seguir operando
     * (nodo [K] del diagrama de calculo y continuidad).
     *
     * @return true si se fijo el resultado parcial; false si no quedan
     *         numeros por usar o la expresion actual no se puede calcular
     */
    public boolean continuarOperacion() {

        if (!quedanNumerosDisponibles()) {
            return false;
        }

        double parcial = calcularResultado();
        if (!validarResultado()) {
            return false;
        }

        // Fijar resultado parcial: todos los numeros consumidos hasta ahora
        // quedan asociados a ese unico elemento.
        List<Integer> indicesConsumidos = new ArrayList<>();
        for (List<Integer> indices : indicesPorElemento) {
            indicesConsumidos.addAll(indices);
        }

        List<String> elementos = new ArrayList<>();
        elementos.add(formatearNumero(parcial));
        guardar(elementos);

        indicesPorElemento.clear();
        indicesPorElemento.add(indicesConsumidos);

        // Liberar numeros no usados: los que no estan en indicesConsumidos
        // siguen marcados como disponibles en numerosUsados.
        historialAcciones.clear();
        historialAcciones.push(new Accion(TipoAccion.RESULTADO_PARCIAL, true));

        resultadoParcialFijado = true;
        numeroAbierto = false;
        parentesisAbiertos = 0;
        return true;
    }

    // =====================================================================
    // TAREA 2: VALIDACION DE REGLAS (metodo coordinador)
    // =====================================================================

    /**
     * Metodo coordinador: ejecuta en cadena todas las reglas sintacticas,
     * de operadores, de parentesis y de control de numeros antes de
     * permitir cualquier calculo.
     *
     * @return true si la expresion es totalmente valida
     */
    public boolean validarExpresion() {

        // [A] Inicio: solicitar validacion de la expresion actual
        // [B] Ejecutar validarPosicionOperador
        // [C] ¿Operadores en posicion correcta?
        if (!validarPosicionOperador()) {
            return false;                       // [D] Error sintactico de operadores
        }

        // [E] Ejecutar validarPosicionParentesis
        // [F] ¿Parentesis balanceados y correctos?
        if (!validarPosicionParentesis()) {
            return false;                       // [G] Error de parentesis
        }

        // [H] Ejecutar validarNumerosPermitidos y validarUsoDeNumeros
        // [I] ¿Usa solo numeros disponibles y maximo una vez?
        if (!validarNumerosPermitidos() || !validarUsoDeNumeros()) {
            return false;                       // [G_NUM] Error de uso de numeros
        }

        // [J] Ejecutar validarReglasOperaciones segun la dificultad
        // [K] ¿Operadores permitidos por el nivel?
        if (!validarReglasOperaciones()) {
            return false;                       // [L] Operador no permitido por dificultad
        }

        // [M] Ejecutar validarDivisionCero
        // [N] ¿Existe division entre cero?
        if (!validarDivisionCero()) {
            return false;                       // [O] Division entre cero invalida
        }

        return true;                            // [P] Expresion totalmente valida
    }

    /**
     * Validacion sintactica de operadores: evita acumulaciones de
     * operadores y posiciones invalidas al inicio/fin.
     * <ul>
     *   <li>Un operador binario no puede ir al inicio ni al final, ni justo
     *       despues de otro operador o de "(".</li>
     *   <li>La raiz no puede ir al final ni despues de un numero o ")", y
     *       debe ir seguida de un numero o de "(".</li>
     * </ul>
     *
     * @return true si todos los operadores estan en una posicion valida
     */
    public boolean validarPosicionOperador() {

        List<String> elementos = elementos();

        for (int i = 0; i < elementos.size(); i++) {

            String actual = elementos.get(i);
            if (!esOperador(actual)) {
                continue;
            }

            if (i == elementos.size() - 1) {
                return false;
            }

            String siguiente = elementos.get(i + 1);
            String anterior = i == 0 ? null : elementos.get(i - 1);

            if (RAIZ.equals(actual)) {
                if (anterior != null && (esNumero(anterior) || PARENTESIS_CIERRA.equals(anterior))) {
                    return false;
                }
                if (!esNumero(siguiente) && !PARENTESIS_ABRE.equals(siguiente)) {
                    return false;
                }
            } else {
                if (anterior == null || esOperador(anterior) || PARENTESIS_ABRE.equals(anterior)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Validacion de parentesis: revisa que esten balanceados, que no se
     * cierre un parentesis que no haya sido abierto previamente y que no
     * queden parentesis vacios "()".
     *
     * @return true si los parentesis de la expresion son validos
     */
    public boolean validarPosicionParentesis() {

        List<String> elementos = elementos();
        int balance = 0;

        for (int i = 0; i < elementos.size(); i++) {

            String actual = elementos.get(i);

            if (PARENTESIS_ABRE.equals(actual)) {
                balance++;
            } else if (PARENTESIS_CIERRA.equals(actual)) {
                balance--;
                if (balance < 0) {
                    return false;               // se cerro uno que no estaba abierto
                }
                if (i > 0 && PARENTESIS_ABRE.equals(elementos.get(i - 1))) {
                    return false;               // "()" vacio
                }
            }
        }
        return balance == 0;
    }

    /**
     * Comprueba que la cadena posea una estructura matematica completa
     * antes de evaluarla. No exige usar los 4 numeros: se permiten cortes
     * parciales para trabajar con resultados intermedios.
     *
     * @return true si la expresion termina en un numero o en ")" y no quedan parentesis abiertos
     */
    public boolean validarExpresionFinalizada() {

        List<String> elementos = elementos();
        if (elementos.isEmpty()) {
            return false;
        }

        String ultimo = ultimo(elementos);
        boolean terminaBien = esNumero(ultimo) || PARENTESIS_CIERRA.equals(ultimo);

        return terminaBien && parentesisAbiertos == 0;
    }

    /**
     * Control de numeros (1/2): comprueba que los digitos pertenezcan
     * estrictamente a los cuatro numeros generados para la partida.
     *
     * @return true si cada digito usado corresponde a un numero disponible
     */
    public boolean validarNumerosPermitidos() {

        List<String> elementos = elementos();

        for (int i = 0; i < elementos.size(); i++) {

            String elemento = elementos.get(i);
            if (!esNumero(elemento)) {
                continue;
            }

            List<Integer> indices = indicesPorElemento.get(i);
            for (int indice : indices) {
                if (indice < 0 || indice >= numerosDisponibles.size()) {
                    return false;
                }
            }

            if (esResultadoParcial(i)) {
                continue; // el resultado parcial no se compara digito a digito
            }

            if (indices.size() != elemento.length()) {
                return false;
            }
            for (int d = 0; d < elemento.length(); d++) {
                String digito = String.valueOf(elemento.charAt(d));
                if (!String.valueOf(numerosDisponibles.get(indices.get(d))).equals(digito)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Control de numeros (2/2): comprueba que ningun numero disponible se
     * reutilice de manera indebida (cada uno se usa maximo una vez,
     * incluyendo el consumo al concatenar cifras).
     *
     * @return true si cada numero disponible se uso como maximo una vez
     */
    public boolean validarUsoDeNumeros() {

        int[] vecesUsado = new int[numerosDisponibles.size()];

        for (List<Integer> indices : indicesPorElemento) {
            for (int indice : indices) {
                if (indice < 0 || indice >= vecesUsado.length) {
                    return false;
                }
                vecesUsado[indice]++;
                if (vecesUsado[indice] > 1) {
                    return false;
                }
            }
        }

        for (int i = 0; i < vecesUsado.length; i++) {
            if ((vecesUsado[i] == 1) != numerosUsados[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Reglas de dificultad: verifica que los operadores empleados
     * correspondan al nivel de dificultad (potencias o raices solo en los
     * niveles avanzados).
     *
     * @return true si todos los operadores de la expresion estan permitidos por el nivel
     */
    public boolean validarReglasOperaciones() {

        for (String elemento : elementos()) {
            if (esOperador(elemento) && !nivelDificultad.permite(elemento)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Control de errores matematicos: asegura que la operacion no intente
     * dividir ningun valor entre cero.
     *
     * @return true si la expresion NO tiene ninguna division por cero
     */
    public boolean validarDivisionCero() {
        try {
            evaluarCompleto(elementos());
            return true;
        } catch (DivisionPorCeroException divisionPorCero) {
            return false;
        } catch (RuntimeException otraFallaDeCalculo) {
            // Una expresion incompleta o mal formada no es un problema de
            // division por cero: otras validaciones se encargan del resto.
            return true;
        }
    }

    /**
     * Verifica que el resultado calculado sea un numero valido.
     *
     * @return true si el resultado no es NaN ni infinito
     */
    public boolean validarResultado() {
        return !Double.isNaN(resultado) && !Double.isInfinite(resultado);
    }

    /**
     * Comprueba si el resultado obtenido corresponde a uno de los objetivos
     * del juego. Para contar como objetivo, la expresion (o la cadena de
     * resultados parciales) debe haber usado los 4 numeros de la partida.
     *
     * @return true si se usaron todos los numeros y el resultado es un entero entre 1 y 10
     */
    public boolean comprobarCoincidenciaObjetivo() {
        if (!validarResultado() || quedanNumerosDisponibles()) {
            return false;
        }
        double redondeado = Math.rint(resultado);
        boolean esEntero = Math.abs(resultado - redondeado) < TOLERANCIA;
        return esEntero && redondeado >= 1 && redondeado <= 10;
    }

    // =====================================================================
    // TAREA 3: CALCULO (respetando parentesis y jerarquia de operadores)
    // =====================================================================

    /**
     * Metodo central de procesamiento: evalua la cadena matematica
     * respetando parentesis y la jerarquia de operadores.
     *
     * @return el resultado calculado (tambien queda guardado en el atributo
     *         resultado), listo para enviarse a {@code Partida.completarObjetivo};
     *         NaN si la expresion no es valida o no esta completa
     */
    public double calcularResultado() {

        // [A] Inicio: solicitar calculo de elementosExpresion
        // [B] Verificar si la expresion esta finalizada (validarExpresionFinalizada)
        // [C] ¿La expresion es valida y completa para evaluar?
        if (!validarExpresionFinalizada() || !validarExpresion()) {
            // [D] Retornar error; elementosExpresion se mantiene en su estado intermedio
            resultado = Double.NaN;
            return resultado;
        }

        try {
            trabajo = elementos();

            // [E] Procesar subexpresiones dentro de parentesis
            resolverParentesis();

            // [F] Aplicar jerarquia: Potencias/Raices, Multiplicacion/Division, Suma/Resta
            respetarOrdenOperaciones();

            // [G] Calcular el valor numerico final
            double valorFinal = Double.parseDouble(trabajo.get(0));

            // [H] Almacenar el resultado en el atributo resultado
            resultado = valorFinal;

        } catch (RuntimeException errorDeCalculo) {
            resultado = Double.NaN;
        }

        // [I] Retornar el valor double hacia Partida.completarObjetivo
        // [J] ¿Desea el jugador continuar? -> lo decide quien llama a continuarOperacion()
        return resultado;
    }

    /**
     * Resuelve, de adentro hacia afuera, cada grupo entre parentesis del
     * buffer de trabajo, reemplazandolo por su valor numerico.
     */
    public void resolverParentesis() {

        int indiceApertura;
        while ((indiceApertura = trabajo.lastIndexOf(PARENTESIS_ABRE)) != -1) {

            int indiceCierre = indiceApertura + 1;
            while (!PARENTESIS_CIERRA.equals(trabajo.get(indiceCierre))) {
                indiceCierre++;
            }

            List<String> subExpresion = new ArrayList<>(
                    trabajo.subList(indiceApertura + 1, indiceCierre));

            double valorSubExpresion = aplicarJerarquia(subExpresion);

            List<String> nuevoTrabajo = new ArrayList<>(trabajo.subList(0, indiceApertura));
            nuevoTrabajo.add(formatearNumero(valorSubExpresion));
            nuevoTrabajo.addAll(trabajo.subList(indiceCierre + 1, trabajo.size()));

            trabajo = nuevoTrabajo;
        }
    }

    /**
     * Aplica de manera estricta la precedencia matematica sobre el buffer
     * de trabajo (que ya no debe tener parentesis): primero potencias y
     * raices, luego multiplicacion y division, y por ultimo suma y resta.
     */
    public void respetarOrdenOperaciones() {
        double valor = aplicarJerarquia(trabajo);
        trabajo = new ArrayList<>(List.of(formatearNumero(valor)));
    }

    private double evaluarCompleto(List<String> elementos) {
        trabajo = new ArrayList<>(elementos);
        resolverParentesis();
        respetarOrdenOperaciones();
        return Double.parseDouble(trabajo.get(0));
    }

    private double aplicarJerarquia(List<String> elementos) {
        List<String> parcial = aplicarRaices(elementos);
        parcial = aplicarPotencias(parcial);
        parcial = aplicarPaso(parcial, Set.of(MULTIPLICACION, DIVISION));
        parcial = aplicarPaso(parcial, Set.of(SUMA, RESTA));
        return Double.parseDouble(parcial.get(0));
    }

    /** Resuelve cada "√ numero" (de derecha a izquierda) por su valor. */
    private List<String> aplicarRaices(List<String> elementos) {

        List<String> resultadoParcial = new ArrayList<>(elementos);

        for (int i = resultadoParcial.size() - 2; i >= 0; i--) {
            if (RAIZ.equals(resultadoParcial.get(i))) {
                double radicando = Double.parseDouble(resultadoParcial.get(i + 1));
                if (radicando < 0) {
                    throw new IllegalArgumentException("Raiz de un numero negativo");
                }
                resultadoParcial.set(i, formatearNumero(Math.sqrt(radicando)));
                resultadoParcial.remove(i + 1);
            }
        }
        return resultadoParcial;
    }

    /** Resuelve las potencias, asociando de derecha a izquierda (2 ^ 3 ^ 2 = 2 ^ 9). */
    private List<String> aplicarPotencias(List<String> elementos) {

        List<String> resultadoParcial = new ArrayList<>(elementos);

        for (int i = resultadoParcial.size() - 2; i >= 1; i--) {
            if (POTENCIA.equals(resultadoParcial.get(i))) {
                double base = Double.parseDouble(resultadoParcial.get(i - 1));
                double exponente = Double.parseDouble(resultadoParcial.get(i + 1));
                resultadoParcial.set(i - 1, formatearNumero(operar(base, POTENCIA, exponente)));
                resultadoParcial.remove(i + 1);
                resultadoParcial.remove(i);
                i--;
            }
        }
        return resultadoParcial;
    }

    private List<String> aplicarPaso(List<String> elementos, Set<String> operadoresDeEstePaso) {

        List<String> resultadoParcial = new ArrayList<>();
        resultadoParcial.add(elementos.get(0));

        for (int i = 1; i < elementos.size(); i += 2) {

            String operador = elementos.get(i);
            double siguiente = Double.parseDouble(elementos.get(i + 1));

            if (operadoresDeEstePaso.contains(operador)) {

                double anterior = Double.parseDouble(resultadoParcial.get(resultadoParcial.size() - 1));
                double calculado = operar(anterior, operador, siguiente);
                resultadoParcial.set(resultadoParcial.size() - 1, formatearNumero(calculado));

            } else {
                resultadoParcial.add(operador);
                resultadoParcial.add(formatearNumero(siguiente));
            }
        }
        return resultadoParcial;
    }

    private double operar(double a, String operador, double b) {
        switch (operador) {
            case SUMA:
                return a + b;
            case RESTA:
                return a - b;
            case MULTIPLICACION:
                return a * b;
            case DIVISION:
                if (b == 0) {
                    throw new DivisionPorCeroException();
                }
                return a / b;
            case POTENCIA:
                double potencia = Math.pow(a, b);
                if (Double.isNaN(potencia) || Double.isInfinite(potencia)) {
                    throw new IllegalArgumentException("Potencia sin resultado real");
                }
                return potencia;
            default:
                throw new IllegalArgumentException("Operador desconocido: " + operador);
        }
    }

    private String formatearNumero(double valor) {
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            throw new IllegalArgumentException("Valor no numerico");
        }
        double redondeado = Math.rint(valor);
        if (Math.abs(valor - redondeado) < TOLERANCIA && Math.abs(redondeado) < Long.MAX_VALUE) {
            return String.valueOf((long) redondeado);
        }
        return String.valueOf(valor);
    }

    // =====================================================================
    // UTILIDADES
    // =====================================================================

    /** Separa elementosExpresion en sus elementos (copia modificable). */
    private List<String> elementos() {
        if (elementosExpresion.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(elementosExpresion.split(SEPARADOR)));
    }

    /** Vuelve a armar elementosExpresion a partir de sus elementos. */
    private void guardar(List<String> elementos) {
        elementosExpresion = String.join(SEPARADOR, elementos);
    }

    private String ultimo(List<String> elementos) {
        return elementos.get(elementos.size() - 1);
    }

    private boolean esOperador(String elemento) {
        return OPERADORES.contains(elemento);
    }

    private boolean esOperadorBinario(String elemento) {
        return OPERADORES_BINARIOS.contains(elemento);
    }

    /** Numero formado solo por digitos de la partida (se puede seguir concatenando). */
    private boolean esNumeroDeDigitos(String elemento) {
        return elemento != null && elemento.matches("\\d+");
    }

    /** Cualquier valor numerico: digitos o un resultado parcial (puede ser negativo o decimal). */
    private boolean esNumero(String elemento) {
        return elemento != null && elemento.matches("-?\\d+(\\.\\d+)?([eE]-?\\d+)?");
    }

    private boolean esResultadoParcial(int posicion) {
        return resultadoParcialFijado && posicion == 0;
    }

    private boolean quedanNumerosDisponibles() {
        for (boolean usado : numerosUsados) {
            if (!usado) {
                return true;
            }
        }
        return false;
    }

    private int buscarIndiceDisponible(String digito) {
        for (int i = 0; i < numerosDisponibles.size(); i++) {
            if (!numerosUsados[i] && String.valueOf(numerosDisponibles.get(i)).equals(digito)) {
                return i;
            }
        }
        return -1;
    }

    private void liberarIndices(List<Integer> indices) {
        for (int indice : indices) {
            numerosUsados[indice] = false;
        }
    }

    /** Arma el texto legible de la expresion, para mostrar en la zona de trabajo.
     * @return los elementos de la expresion separados por espacios */
    public String getTextoFormateado() {
        return elementosExpresion;
    }

    /** Da acceso a la cadena matematica en construccion.
     * @return elementosExpresion (ej. "3 + 4 × 2") */
    public String getElementosExpresion() {
        return elementosExpresion;
    }

    /** Da acceso al ultimo resultado calculado.
     * @return el resultado de {@link #calcularResultado()} (NaN si todavia no se ha calculado) */
    public double getResultado() {
        return resultado;
    }

    /** Da acceso a los numeros con los que se construye esta expresion.
     * @return los numeros de la partida asociados a esta expresion, de solo lectura */
    public List<Integer> getNumerosDisponibles() {
        return Collections.unmodifiableList(numerosDisponibles);
    }

    /** Da acceso a la dificultad con la que se valida esta expresion.
     * @return el nivel de dificultad de la partida */
    public Partida.NivelDificultad getNivelDificultad() {
        return nivelDificultad;
    }

    /**
     * Indica si el numero en la posicion dada de {@link #getNumerosDisponibles()}
     * ya fue consumido por algun elemento de la expresion.
     *
     * @param indice posicion dentro de los numeros disponibles de la partida
     * @return true si ese numero ya esta en uso en esta expresion
     */
    public boolean isNumeroUsado(int indice) {
        return numerosUsados[indice];
    }

    // =====================================================================
    // Registro interno de acciones (para poder deshacer paso a paso)
    // =====================================================================

    private enum TipoAccion {
        DIGITO, OPERADOR, PARENTESIS_ABRE, PARENTESIS_CIERRA, RESULTADO_PARCIAL
    }

    private static final class Accion {
        final TipoAccion tipo;
        final boolean creoElementoNuevo;

        Accion(TipoAccion tipo, boolean creoElementoNuevo) {
            this.tipo = tipo;
            this.creoElementoNuevo = creoElementoNuevo;
        }
    }

    /** Senal interna de que el calculo intento dividir entre cero. */
    private static final class DivisionPorCeroException extends ArithmeticException {
        private static final long serialVersionUID = 1L;

        DivisionPorCeroException() {
            super("Division por cero");
        }
    }
}