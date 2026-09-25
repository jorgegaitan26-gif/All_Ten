package co.edu.poli.Allten.Modelo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 * Representa la expresion matematica que el jugador va construyendo con los
 * 4 numeros disponibles de la partida, los operadores (+, -, x, /) y los
 * parentesis, tal como aparece en el diagrama de clases "All Ten".
 *
 * Cada instancia de Expresion corresponde a UN intento sobre una Partida
 * (relacion partida 1 - 0..* expresion).
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

    /** Simbolo de parentesis de apertura. */
    public static final String PARENTESIS_ABRE = "(";

    /** Simbolo de parentesis de cierre. */
    public static final String PARENTESIS_CIERRA = ")";

    private static final Set<String> OPERADORES = Set.of(SUMA, RESTA, MULTIPLICACION, DIVISION);

    // ----- Atributos del diagrama de clases -----
    private List<String> elementosExpresion;
    private double resultado;

    // ----- Estado interno necesario para validar el uso correcto de los 4 numeros -----
    private final List<Integer> numerosDisponibles;
    private final boolean[] numerosUsados;

    /** Indices consumidos de numerosDisponibles para cada elemento numerico de elementosExpresion. */
    private final List<List<Integer>> indicesPorElemento;

    private boolean numeroAbierto;
    private int parentesisAbiertos;

    /** Historial de acciones, usado unicamente por borrarUltimoElemento() para deshacer paso a paso. */
    private final Deque<Accion> historialAcciones;

    /** Buffer de trabajo utilizado por resolverParentesis()/respetarOrdenOperaciones(). */
    private List<String> trabajo;

    /**
     * Crea una expresion vacia asociada a los numeros disponibles de una
     * partida (normalmente invocado desde {@code Partida.crearNuevoIntento()}).
     *
     * @param numerosDisponibles los numeros de la partida con los que se debe construir la expresion
     */
    public Expresion(List<Integer> numerosDisponibles) {
        this.numerosDisponibles = new ArrayList<>(numerosDisponibles);
        this.numerosUsados = new boolean[this.numerosDisponibles.size()];
        this.elementosExpresion = new ArrayList<>();
        this.indicesPorElemento = new ArrayList<>();
        this.historialAcciones = new ArrayDeque<>();
        this.resultado = Double.NaN;
        this.numeroAbierto = false;
        this.parentesisAbiertos = 0;
    }

    // =====================================================================
    // CONSTRUCCION DE LA EXPRESION
    // =====================================================================

    /**
     * Agrega un digito a la expresion. Si el ultimo elemento es un numero
     * que todavia se puede extender (por ejemplo, se acaba de tocar "1" y
     * ahora se toca "2"), el digito se concatena para formar un numero de
     * varias cifras ("12"). En caso contrario, se crea un nuevo elemento
     * numerico. Solo se permite usar digitos que sigan disponibles entre
     * los numeros de la partida.
     *
     * @param digito digito a agregar, como String de un solo caracter ("0"-"9")
     * @return true si el digito se pudo agregar, false si la posicion no es
     *         valida o no queda ningun numero disponible con ese valor
     */
    public boolean concatenarDigitos(String digito) {

        if (digito == null || !digito.matches("\\d")) {
            return false;
        }

        int indiceDisponible = buscarIndiceDisponible(digito);
        if (indiceDisponible == -1) {
            return false;
        }

        boolean puedeConcatenar = numeroAbierto && !elementosExpresion.isEmpty();
        boolean puedeIniciarNumero = elementosExpresion.isEmpty()
                || esOperador(ultimoElemento())
                || PARENTESIS_ABRE.equals(ultimoElemento());

        if (puedeConcatenar) {

            int ultimo = elementosExpresion.size() - 1;
            elementosExpresion.set(ultimo, elementosExpresion.get(ultimo) + digito);
            indicesPorElemento.get(ultimo).add(indiceDisponible);
            numerosUsados[indiceDisponible] = true;

            historialAcciones.push(Accion.digitoConcatenado(indiceDisponible));
            return true;

        } else if (puedeIniciarNumero) {

            elementosExpresion.add(digito);
            List<Integer> indices = new ArrayList<>();
            indices.add(indiceDisponible);
            indicesPorElemento.add(indices);
            numerosUsados[indiceDisponible] = true;
            numeroAbierto = true;

            historialAcciones.push(Accion.digitoNuevoElemento(indiceDisponible));
            return true;
        }

        return false;
    }

    /**
     * Agrega un operador (+, -, x, /) a la expresion, si la posicion es
     * valida (debe ir despues de un numero o de un parentesis que cierra).
     *
     * @param operador uno de {@link #SUMA}, {@link #RESTA}, {@link #MULTIPLICACION} o {@link #DIVISION}
     * @return true si se pudo agregar; false si la posicion no es valida o el simbolo no es un operador
     */
    public boolean agregarOperacion(String operador) {

        if (operador == null || !OPERADORES.contains(operador)) {
            return false;
        }

        boolean posicionValida = !elementosExpresion.isEmpty()
                && (esNumero(ultimoElemento()) || PARENTESIS_CIERRA.equals(ultimoElemento()));

        if (!posicionValida) {
            return false;
        }

        elementosExpresion.add(operador);
        indicesPorElemento.add(new ArrayList<>());
        numeroAbierto = false;

        historialAcciones.push(Accion.simbolo(TipoAccion.OPERADOR, false));
        return true;
    }

    /**
     * Agrega un parentesis de apertura o cierre, si la posicion es valida.
     *
     * @param parentesis {@link #PARENTESIS_ABRE} o {@link #PARENTESIS_CIERRA}
     * @return true si se pudo agregar; false si la posicion no es valida
     */
    public boolean agregarParentesis(String parentesis) {

        if (PARENTESIS_ABRE.equals(parentesis)) {

            boolean posicionValida = elementosExpresion.isEmpty()
                    || esOperador(ultimoElemento())
                    || PARENTESIS_ABRE.equals(ultimoElemento());

            if (!posicionValida) {
                return false;
            }

            elementosExpresion.add(PARENTESIS_ABRE);
            indicesPorElemento.add(new ArrayList<>());
            parentesisAbiertos++;
            numeroAbierto = false;

            historialAcciones.push(Accion.simbolo(TipoAccion.PARENTESIS_ABRE, false));
            return true;

        } else if (PARENTESIS_CIERRA.equals(parentesis)) {

            boolean posicionValida = parentesisAbiertos > 0
                    && !elementosExpresion.isEmpty()
                    && (esNumero(ultimoElemento()) || PARENTESIS_CIERRA.equals(ultimoElemento()));

            if (!posicionValida) {
                return false;
            }

            elementosExpresion.add(PARENTESIS_CIERRA);
            indicesPorElemento.add(new ArrayList<>());
            parentesisAbiertos--;
            numeroAbierto = false;

            historialAcciones.push(Accion.simbolo(TipoAccion.PARENTESIS_CIERRA, false));
            return true;
        }

        return false;
    }

    /**
     * Deshace el ultimo paso realizado (un digito, un operador o un
     * parentesis). Si el ultimo numero tenia varias cifras concatenadas,
     * solo se retira la ultima cifra.
     */
    public void borrarUltimoElemento() {

        if (historialAcciones.isEmpty() || elementosExpresion.isEmpty()) {
            return;
        }

        Accion accion = historialAcciones.pop();
        int ultimo = elementosExpresion.size() - 1;

        if (accion.tipo == TipoAccion.PARENTESIS_ABRE) {
            parentesisAbiertos--;
        } else if (accion.tipo == TipoAccion.PARENTESIS_CIERRA) {
            parentesisAbiertos++;
        }

        if (accion.creoElementoNuevo) {

            // Se elimina el elemento completo (operador, parentesis o
            // primer digito de un numero).
            List<Integer> indices = indicesPorElemento.remove(ultimo);
            elementosExpresion.remove(ultimo);
            liberarIndices(indices);

        } else if (accion.tipo == TipoAccion.DIGITO) {

            // Se retira solo la ultima cifra concatenada.
            String valorActual = elementosExpresion.get(ultimo);
            String nuevoValor = valorActual.substring(0, valorActual.length() - 1);

            List<Integer> indices = indicesPorElemento.get(ultimo);
            int indiceLiberado = indices.remove(indices.size() - 1);
            numerosUsados[indiceLiberado] = false;

            if (nuevoValor.isEmpty()) {
                elementosExpresion.remove(ultimo);
                indicesPorElemento.remove(ultimo);
            } else {
                elementosExpresion.set(ultimo, nuevoValor);
            }

        } else {
            // Operador o parentesis simple.
            elementosExpresion.remove(ultimo);
            indicesPorElemento.remove(ultimo);
        }

        numeroAbierto = !elementosExpresion.isEmpty() && esNumero(ultimoElemento());
    }

    /**
     * Indica si todavia se puede seguir construyendo la expresion.
     *
     * @return true si quedan numeros disponibles por usar o hay parentesis abiertos pendientes de cerrar
     */
    public boolean continuarOperacion() {
        return quedanNumerosDisponibles() || parentesisAbiertos > 0;
    }

    // =====================================================================
    // VALIDACIONES
    // =====================================================================

    /**
     * Validacion completa: agrupa todas las reglas del juego.
     *
     * @return true si la expresion cumple todas las reglas (numeros permitidos, uso completo, sintaxis y sin division por cero)
     */
    public boolean validarExpresion() {
        return validarNumerosPermitidos()
                && validarUsoDeNumeros()
                && validarReglasOperaciones()
                && validarExpresionFinalizada()
                && validarDivisionCero();
    }

    /**
     * Verifica la posicion de los operadores: no puede haber dos seguidos,
     * ni operadores al inicio/final ni justo despues de "(".
     *
     * @return true si todos los operadores estan en una posicion valida
     */
    public boolean validarPosicionOperador() {

        for (int i = 0; i < elementosExpresion.size(); i++) {

            String actual = elementosExpresion.get(i);
            if (!esOperador(actual)) {
                continue;
            }

            if (i == 0 || i == elementosExpresion.size() - 1) {
                return false;
            }

            String anterior = elementosExpresion.get(i - 1);
            if (esOperador(anterior) || PARENTESIS_ABRE.equals(anterior)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica que los parentesis queden balanceados y nunca vacios "()".
     *
     * @return true si los parentesis de la expresion son validos
     */
    public boolean validarPosicionParentesis() {

        int balance = 0;
        for (int i = 0; i < elementosExpresion.size(); i++) {

            String actual = elementosExpresion.get(i);

            if (PARENTESIS_ABRE.equals(actual)) {
                balance++;
            } else if (PARENTESIS_CIERRA.equals(actual)) {
                balance--;
                if (balance < 0) {
                    return false;
                }
                if (i > 0 && PARENTESIS_ABRE.equals(elementosExpresion.get(i - 1))) {
                    return false; // "()" vacio
                }
            }
        }
        return balance == 0;
    }

    /**
     * Verifica que la expresion este correctamente terminada.
     *
     * @return true si la expresion termina en un numero o en un parentesis que cierra, y no quedan parentesis abiertos
     */
    public boolean validarExpresionFinalizada() {

        if (elementosExpresion.isEmpty()) {
            return false;
        }

        String ultimo = ultimoElemento();
        boolean terminaBien = esNumero(ultimo) || PARENTESIS_CIERRA.equals(ultimo);

        return terminaBien && parentesisAbiertos == 0;
    }

    /**
     * Verifica que cada digito usado provenga de los numeros disponibles de la partida.
     *
     * @return true si todos los digitos usados son consistentes con los numeros consumidos
     */
    public boolean validarNumerosPermitidos() {

        for (int i = 0; i < elementosExpresion.size(); i++) {
            String elemento = elementosExpresion.get(i);
            if (esNumero(elemento)) {
                if (indicesPorElemento.get(i).size() != elemento.length()) {
                    return false;
                }
                for (int indice : indicesPorElemento.get(i)) {
                    if (indice < 0 || indice >= numerosDisponibles.size()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Verifica que los 4 numeros de la partida se hayan usado exactamente una vez cada uno.
     *
     * @return true si todos los numeros disponibles fueron usados
     */
    public boolean validarUsoDeNumeros() {
        for (boolean usado : numerosUsados) {
            if (!usado) {
                return false;
            }
        }
        return true;
    }

    /**
     * Agrupa las reglas de posicion de operadores y parentesis.
     *
     * @return true si tanto los operadores como los parentesis estan bien colocados
     */
    public boolean validarReglasOperaciones() {
        return validarPosicionOperador() && validarPosicionParentesis();
    }

    /**
     * Comprueba que la expresion no contenga ninguna division por cero.
     *
     * @return true si la expresion NO tiene ninguna division por cero
     */
    public boolean validarDivisionCero() {
        try {
            evaluarCompleto(new ArrayList<>(elementosExpresion));
            return true;
        } catch (ArithmeticException divisionPorCero) {
            return false;
        } catch (RuntimeException otraFallaDeCalculo) {
            // Una expresion incompleta o mal formada no es un problema de
            // division por cero: se considera valida para este chequeo
            // puntual (otras validaciones se encargan del resto).
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
     * Comprueba si el resultado obtenido corresponde a uno de los objetivos del juego.
     *
     * @return true si el resultado es un numero entero entre 1 y 10
     */
    public boolean comprobarCoincidenciaObjetivo() {
        if (!validarResultado()) {
            return false;
        }
        double redondeado = Math.rint(resultado);
        boolean esEntero = Math.abs(resultado - redondeado) < 1e-9;
        return esEntero && redondeado >= 1 && redondeado <= 10;
    }

    // =====================================================================
    // CALCULO DEL RESULTADO (respetando parentesis y precedencia)
    // =====================================================================

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

            double valorSubExpresion = evaluarSinParentesis(subExpresion);

            List<String> nuevoTrabajo = new ArrayList<>(trabajo.subList(0, indiceApertura));
            nuevoTrabajo.add(formatearNumero(valorSubExpresion));
            nuevoTrabajo.addAll(trabajo.subList(indiceCierre + 1, trabajo.size()));

            trabajo = nuevoTrabajo;
        }
    }

    /**
     * Aplica la precedencia de operadores (multiplicacion y division antes
     * que suma y resta) sobre el buffor de trabajo, que ya no debe tener
     * parentesis.
     */
    public void respetarOrdenOperaciones() {
        trabajo = aplicarPaso(trabajo, Set.of(MULTIPLICACION, DIVISION));
        trabajo = aplicarPaso(trabajo, Set.of(SUMA, RESTA));
    }

    /**
     * Calcula el resultado final de la expresion combinando
     * {@link #resolverParentesis()} y {@link #respetarOrdenOperaciones()}.
     *
     * @return el resultado calculado (tambien queda guardado en {@link #getResultado()}); NaN si hubo una division por cero
     */
    public double calcularResultado() {
        try {
            resultado = evaluarCompleto(new ArrayList<>(elementosExpresion));
        } catch (RuntimeException error) {
            resultado = Double.NaN;
        }
        return resultado;
    }

    private double evaluarCompleto(List<String> elementos) {
        trabajo = elementos;
        resolverParentesis();
        respetarOrdenOperaciones();
        return Double.parseDouble(trabajo.get(0));
    }

    private double evaluarSinParentesis(List<String> elementos) {
        List<String> resultadoParcial = aplicarPaso(elementos, Set.of(MULTIPLICACION, DIVISION));
        resultadoParcial = aplicarPaso(resultadoParcial, Set.of(SUMA, RESTA));
        return Double.parseDouble(resultadoParcial.get(0));
    }

    private List<String> aplicarPaso(List<String> elementos, Set<String> operadoresDeEstePaso) {

        List<String> resultado = new ArrayList<>();
        resultado.add(elementos.get(0));

        for (int i = 1; i < elementos.size(); i += 2) {

            String operador = elementos.get(i);
            double siguiente = Double.parseDouble(elementos.get(i + 1));

            if (operadoresDeEstePaso.contains(operador)) {

                double anterior = Double.parseDouble(resultado.get(resultado.size() - 1));
                double calculado = operar(anterior, operador, siguiente);
                resultado.set(resultado.size() - 1, formatearNumero(calculado));

            } else {
                resultado.add(operador);
                resultado.add(formatearNumero(siguiente));
            }
        }
        return resultado;
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
                    throw new ArithmeticException("Division por cero");
                }
                return a / b;
            default:
                throw new IllegalArgumentException("Operador desconocido: " + operador);
        }
    }

    private String formatearNumero(double valor) {
        if (valor == Math.rint(valor) && !Double.isInfinite(valor)) {
            return String.valueOf((long) valor);
        }
        return String.valueOf(valor);
    }

    // =====================================================================
    // UTILIDADES
    // =====================================================================

    private String ultimoElemento() {
        return elementosExpresion.get(elementosExpresion.size() - 1);
    }

    private boolean esOperador(String elemento) {
        return OPERADORES.contains(elemento);
    }

    private boolean esNumero(String elemento) {
        return elemento != null && elemento.matches("\\d+");
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
        return String.join(" ", elementosExpresion);
    }

    /** Da acceso a los elementos de la expresion.
     * @return la lista de elementos (digitos concatenados, operadores y parentesis), de solo lectura */
    public List<String> getElementosExpresion() {
        return Collections.unmodifiableList(elementosExpresion);
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
        DIGITO, OPERADOR, PARENTESIS_ABRE, PARENTESIS_CIERRA
    }

    private static final class Accion {
        final TipoAccion tipo;
        final boolean creoElementoNuevo;

        private Accion(TipoAccion tipo, boolean creoElementoNuevo) {
            this.tipo = tipo;
            this.creoElementoNuevo = creoElementoNuevo;
        }

        static Accion digitoNuevoElemento(int indice) {
            return new Accion(TipoAccion.DIGITO, true);
        }

        static Accion digitoConcatenado(int indice) {
            return new Accion(TipoAccion.DIGITO, false);
        }

        static Accion simbolo(TipoAccion tipo, boolean creoElementoNuevo) {
            return new Accion(tipo, true);
        }
    }
}
