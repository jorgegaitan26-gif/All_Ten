package co.edu.poli.Allten.Modelo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Expresion matematica que el jugador construye con los 4 numeros de la
 * partida, los operadores y los parentesis (relacion Partida 1 - 0..*
 * Expresion). Cada elemento de {@code elementosExpresion} se separa con
 * un espacio, por ejemplo "( 3 + 4 ) × 2".
 *
 * Metodos del diagrama: construccion ({@link #concatenarDigitos(int)},
 * {@link #agregarOperacion(String)}, {@link #agregarParentesis(String)},
 * {@link #borrarUltimoElemento()}, {@link #continuarOperacion()}),
 * validacion ({@link #validarExpresion()} y las validaciones que coordina)
 * y {@link #calcularResultado()}.
 */
public class Expresion {

    /** Simbolo de suma. */
    public static final String SUMA = "+";
    /** Simbolo de resta. */
    public static final String RESTA = "-";
    /** Simbolo de multiplicacion. */
    public static final String MULTIPLICACION = "×";
    /** Simbolo de division. */
    public static final String DIVISION = "÷";
    /** Simbolo de potencia (dificultad MEDIO y DIFICIL). */
    public static final String POTENCIA = "^";
    /** Simbolo de raiz cuadrada, prefijo (solo dificultad DIFICIL). */
    public static final String RAIZ = "√";
    /** Parentesis de apertura. */
    public static final String PARENTESIS_ABRE = "(";
    /** Parentesis de cierre. */
    public static final String PARENTESIS_CIERRA = ")";

    private String elementosExpresion;
    private double resultado;
    private Partida partida;

    /**
     * Crea una expresion vacia (un intento) de la partida.
     *
     * @param partida partida a la que pertenece; de ella se toman los numeros y la dificultad
     */
    public Expresion(Partida partida) {
        this.partida = partida;
        this.elementosExpresion = "";
        this.resultado = Double.NaN;
    }

    /**
     * Agrega un digito. Si el ultimo elemento es un numero, el digito se
     * concatena (ej. "1" y luego "2" forman "12"); si no, empieza un numero
     * nuevo. Cada digito consume uno de los numeros disponibles.
     *
     * @param digito digito a agregar
     * @return true si se agrego; false si no esta disponible o la posicion no es valida
     */
    public boolean concatenarDigitos(int digito) {

        if (digito < 0 || digito > 9) {
            return false;
        }

        int disponibles = 0;
        for (int numero : partida.getNumerosDisponibles()) {
            if (numero == digito) {
                disponibles++;
            }
        }
        int usados = 0;
        for (char caracter : elementosExpresion.toCharArray()) {
            if (caracter == (char) ('0' + digito)) {
                usados++;
            }
        }
        if (usados >= disponibles) {
            return false;
        }

        List<String> elementos = elementosExpresion.isEmpty()
                ? new ArrayList<>() : new ArrayList<>(Arrays.asList(elementosExpresion.split(" ")));
        String ultimo = elementos.isEmpty() ? null : elementos.get(elementos.size() - 1);

        if (ultimo != null && ultimo.matches("\\d+")) {
            elementos.set(elementos.size() - 1, ultimo + digito);
        } else if (ultimo == null || PARENTESIS_ABRE.equals(ultimo)
                || List.of(SUMA, RESTA, MULTIPLICACION, DIVISION, POTENCIA, RAIZ).contains(ultimo)) {
            elementos.add(String.valueOf(digito));
        } else {
            return false;
        }

        elementosExpresion = String.join(" ", elementos);
        return true;
    }

    /**
     * Agrega un operador. Los binarios (+ - × ÷ ^) van despues de un numero
     * o de ")"; la raiz va al inicio, despues de un operador binario o de "(".
     * La potencia solo se permite en MEDIO y DIFICIL, y la raiz solo en DIFICIL.
     *
     * @param operador simbolo del operador
     * @return true si se agrego
     */
    public boolean agregarOperacion(String operador) {

        if (operador == null || !List.of(SUMA, RESTA, MULTIPLICACION, DIVISION, POTENCIA, RAIZ).contains(operador)) {
            return false;
        }
        String nivel = partida.getNivelDificultad();
        if ((POTENCIA.equals(operador) && "FACIL".equals(nivel)) || (RAIZ.equals(operador) && !"DIFICIL".equals(nivel))) {
            return false;
        }

        List<String> elementos = elementosExpresion.isEmpty()
                ? new ArrayList<>() : new ArrayList<>(Arrays.asList(elementosExpresion.split(" ")));
        String ultimo = elementos.isEmpty() ? null : elementos.get(elementos.size() - 1);

        boolean posicionValida;
        if (RAIZ.equals(operador)) {
            posicionValida = ultimo == null || PARENTESIS_ABRE.equals(ultimo)
                    || List.of(SUMA, RESTA, MULTIPLICACION, DIVISION, POTENCIA).contains(ultimo);
        } else {
            posicionValida = ultimo != null && (ultimo.matches("\\d+") || PARENTESIS_CIERRA.equals(ultimo));
        }
        if (!posicionValida) {
            return false;
        }

        elementos.add(operador);
        elementosExpresion = String.join(" ", elementos);
        return true;
    }

    /**
     * Agrega un parentesis controlando su apertura y cierre: "(" va al
     * inicio, despues de un operador o de otro "("; ")" solo si hay uno
     * abierto y va despues de un numero o de otro ")".
     *
     * @param parentesis "(" o ")"
     * @return true si se agrego
     */
    public boolean agregarParentesis(String parentesis) {

        List<String> elementos = elementosExpresion.isEmpty()
                ? new ArrayList<>() : new ArrayList<>(Arrays.asList(elementosExpresion.split(" ")));
        String ultimo = elementos.isEmpty() ? null : elementos.get(elementos.size() - 1);

        int abiertos = 0;
        for (String elemento : elementos) {
            if (PARENTESIS_ABRE.equals(elemento)) {
                abiertos++;
            } else if (PARENTESIS_CIERRA.equals(elemento)) {
                abiertos--;
            }
        }

        boolean posicionValida;
        if (PARENTESIS_ABRE.equals(parentesis)) {
            posicionValida = ultimo == null || PARENTESIS_ABRE.equals(ultimo)
                    || List.of(SUMA, RESTA, MULTIPLICACION, DIVISION, POTENCIA, RAIZ).contains(ultimo);
        } else if (PARENTESIS_CIERRA.equals(parentesis)) {
            posicionValida = abiertos > 0 && ultimo != null
                    && (ultimo.matches("\\d+") || PARENTESIS_CIERRA.equals(ultimo));
        } else {
            return false;
        }
        if (!posicionValida) {
            return false;
        }

        elementos.add(parentesis);
        elementosExpresion = String.join(" ", elementos);
        return true;
    }

    /**
     * Borra el ultimo elemento. Si es un numero de varias cifras, solo
     * borra la ultima cifra.
     */
    public void borrarUltimoElemento() {

        if (elementosExpresion.isEmpty()) {
            return;
        }
        List<String> elementos = new ArrayList<>(Arrays.asList(elementosExpresion.split(" ")));
        int posicion = elementos.size() - 1;
        String ultimo = elementos.get(posicion);

        if (ultimo.matches("\\d{2,}")) {
            elementos.set(posicion, ultimo.substring(0, ultimo.length() - 1));
        } else {
            elementos.remove(posicion);
        }
        elementosExpresion = String.join(" ", elementos);
    }

    /**
     * Permite seguir operando con el resultado parcial y los numeros que
     * faltan: si la expresion actual se puede calcular, guarda su valor en
     * resultado y la encierra entre parentesis para que funcione como un
     * solo valor (ej. "3 + 4" pasa a "( 3 + 4 )"). Un solo numero o una
     * expresion que ya esta toda entre parentesis se deja igual.
     *
     * @return true si se pudo continuar; false si ya se usaron los 4 numeros
     *         o la expresion actual no se puede calcular
     */
    public boolean continuarOperacion() {

        int digitosUsados = elementosExpresion.replaceAll("[^0-9]", "").length();
        if (digitosUsados >= partida.getNumerosDisponibles().size()) {
            return false;
        }

        double parcial = calcularResultado();
        if (Double.isNaN(parcial) || Double.isInfinite(parcial)) {
            return false;
        }

        // Un solo numero, o una expresion que ya esta toda entre parentesis,
        // ya funciona como un solo valor: no hace falta volver a envolverla.
        List<String> elementos = Arrays.asList(elementosExpresion.split(" "));
        boolean yaEsUnValor = elementos.size() == 1;
        if (!yaEsUnValor && PARENTESIS_ABRE.equals(elementos.get(0))) {
            int balance = 0;
            yaEsUnValor = true;
            for (int i = 0; i < elementos.size() - 1; i++) {
                if (PARENTESIS_ABRE.equals(elementos.get(i))) {
                    balance++;
                } else if (PARENTESIS_CIERRA.equals(elementos.get(i))) {
                    balance--;
                }
                if (balance == 0) {
                    yaEsUnValor = false;   // el primer "(" se cierra antes del final
                    break;
                }
            }
        }

        if (!yaEsUnValor) {
            elementosExpresion = PARENTESIS_ABRE + " " + elementosExpresion + " " + PARENTESIS_CIERRA;
        }
        return true;
    }

    /**
     * Metodo coordinador: aplica en cadena las validaciones de operadores,
     * parentesis y numeros, y revisa que los operadores esten permitidos
     * por la dificultad de la partida.
     *
     * @return true si la expresion es valida
     */
    public boolean validarExpresion() {

        if (!validarPosicionOperador() || !validarPosicionParentesis()
                || !validarNumerosPermitidos() || !validarUsoDeNumeros()) {
            return false;
        }

        String nivel = partida.getNivelDificultad();
        boolean usaPotencia = elementosExpresion.contains(POTENCIA);
        boolean usaRaiz = elementosExpresion.contains(RAIZ);
        return !(usaPotencia && "FACIL".equals(nivel)) && !(usaRaiz && !"DIFICIL".equals(nivel));
    }

    /**
     * Revisa que no haya operadores al inicio o al final, ni dos operadores
     * binarios seguidos, ni un operador binario despues de "(". La raiz no
     * puede ir despues de un numero o de ")" y debe ir seguida de un numero o "(".
     *
     * @return true si todos los operadores estan en una posicion valida
     */
    public boolean validarPosicionOperador() {

        if (elementosExpresion.isEmpty()) {
            return true;
        }
        List<String> elementos = Arrays.asList(elementosExpresion.split(" "));
        List<String> operadores = List.of(SUMA, RESTA, MULTIPLICACION, DIVISION, POTENCIA, RAIZ);

        for (int i = 0; i < elementos.size(); i++) {
            String actual = elementos.get(i);
            if (!operadores.contains(actual)) {
                continue;
            }
            if (i == elementos.size() - 1) {
                return false;
            }
            String anterior = i == 0 ? null : elementos.get(i - 1);
            String siguiente = elementos.get(i + 1);

            if (RAIZ.equals(actual)) {
                if (anterior != null && (anterior.matches("\\d+") || PARENTESIS_CIERRA.equals(anterior))) {
                    return false;
                }
                if (!siguiente.matches("\\d+") && !PARENTESIS_ABRE.equals(siguiente)) {
                    return false;
                }
            } else if (anterior == null || operadores.contains(anterior) || PARENTESIS_ABRE.equals(anterior)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Revisa que los parentesis esten balanceados, que no se cierre uno que
     * no se abrio y que no haya parentesis vacios "( )".
     *
     * @return true si los parentesis son validos
     */
    public boolean validarPosicionParentesis() {

        if (elementosExpresion.isEmpty()) {
            return true;
        }
        List<String> elementos = Arrays.asList(elementosExpresion.split(" "));
        int balance = 0;

        for (int i = 0; i < elementos.size(); i++) {
            if (PARENTESIS_ABRE.equals(elementos.get(i))) {
                balance++;
            } else if (PARENTESIS_CIERRA.equals(elementos.get(i))) {
                balance--;
                if (balance < 0 || PARENTESIS_ABRE.equals(elementos.get(i - 1))) {
                    return false;
                }
            }
        }
        return balance == 0;
    }

    /**
     * Revisa que la expresion este completa para calcularse: no esta vacia,
     * termina en un numero o en ")" y no quedan parentesis abiertos. No
     * exige usar los 4 numeros.
     *
     * @return true si la expresion esta completa
     */
    public boolean validarExpresionFinalizada() {

        if (elementosExpresion.isEmpty()) {
            return false;
        }
        List<String> elementos = Arrays.asList(elementosExpresion.split(" "));
        String ultimo = elementos.get(elementos.size() - 1);

        int abiertos = 0;
        for (String elemento : elementos) {
            if (PARENTESIS_ABRE.equals(elemento)) {
                abiertos++;
            } else if (PARENTESIS_CIERRA.equals(elemento)) {
                abiertos--;
            }
        }
        return (ultimo.matches("\\d+") || PARENTESIS_CIERRA.equals(ultimo)) && abiertos == 0;
    }

    /**
     * Revisa que cada digito de la expresion sea uno de los 4 numeros de la partida.
     *
     * @return true si todos los digitos pertenecen a los numeros disponibles
     */
    public boolean validarNumerosPermitidos() {

        for (char caracter : elementosExpresion.toCharArray()) {
            if (Character.isDigit(caracter)
                    && !partida.getNumerosDisponibles().contains(caracter - '0')) {
                return false;
            }
        }
        return true;
    }

    /**
     * Revisa que ningun numero disponible se use mas veces de las que
     * aparece en la partida (cada numero se usa maximo una vez).
     *
     * @return true si ningun numero se reutiliza
     */
    public boolean validarUsoDeNumeros() {

        for (int digito = 0; digito <= 9; digito++) {
            int disponibles = 0;
            for (int numero : partida.getNumerosDisponibles()) {
                if (numero == digito) {
                    disponibles++;
                }
            }
            int usados = 0;
            for (char caracter : elementosExpresion.toCharArray()) {
                if (caracter == (char) ('0' + digito)) {
                    usados++;
                }
            }
            if (usados > disponibles) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calcula la expresion respetando parentesis y la jerarquia de
     * operadores: raiz, potencia, multiplicacion/division y suma/resta.
     *
     * @return el resultado (tambien queda en el atributo resultado); NaN si
     *         la expresion no es valida, no esta completa o divide entre cero
     */
    public double calcularResultado() {

        resultado = Double.NaN;
        if (!validarExpresionFinalizada() || !validarExpresion()) {
            return resultado;
        }

        // 1) Pasar a notacion posfija (algoritmo shunting-yard).
        String simbolos = SUMA + RESTA + MULTIPLICACION + DIVISION + POTENCIA + RAIZ;
        int[] precedencia = {1, 1, 2, 2, 3, 4};
        List<String> posfija = new ArrayList<>();
        Deque<String> pila = new ArrayDeque<>();

        for (String elemento : elementosExpresion.split(" ")) {
            if (elemento.matches("\\d+")) {
                posfija.add(elemento);
            } else if (PARENTESIS_ABRE.equals(elemento) || RAIZ.equals(elemento)) {
                pila.push(elemento);
            } else if (PARENTESIS_CIERRA.equals(elemento)) {
                while (!PARENTESIS_ABRE.equals(pila.peek())) {
                    posfija.add(pila.pop());
                }
                pila.pop();
            } else {
                int actual = precedencia[simbolos.indexOf(elemento)];
                boolean asociaDerecha = POTENCIA.equals(elemento);
                while (!pila.isEmpty() && !PARENTESIS_ABRE.equals(pila.peek())) {
                    int tope = precedencia[simbolos.indexOf(pila.peek())];
                    if (tope > actual || (tope == actual && !asociaDerecha)) {
                        posfija.add(pila.pop());
                    } else {
                        break;
                    }
                }
                pila.push(elemento);
            }
        }
        while (!pila.isEmpty()) {
            posfija.add(pila.pop());
        }

        // 2) Evaluar la notacion posfija.
        Deque<Double> valores = new ArrayDeque<>();
        for (String elemento : posfija) {
            if (elemento.matches("\\d+")) {
                valores.push(Double.parseDouble(elemento));
            } else if (RAIZ.equals(elemento)) {
                double a = valores.pop();
                if (a < 0) {
                    return resultado;
                }
                valores.push(Math.sqrt(a));
            } else {
                double b = valores.pop();
                double a = valores.pop();
                double r;
                if (SUMA.equals(elemento)) {
                    r = a + b;
                } else if (RESTA.equals(elemento)) {
                    r = a - b;
                } else if (MULTIPLICACION.equals(elemento)) {
                    r = a * b;
                } else if (DIVISION.equals(elemento)) {
                    if (b == 0) {
                        return resultado;
                    }
                    r = a / b;
                } else {
                    r = Math.pow(a, b);
                }
                if (Double.isNaN(r) || Double.isInfinite(r)) {
                    return resultado;
                }
                valores.push(r);
            }
        }

        resultado = valores.pop();
        return resultado;
    }

    // ----- Getters y setters -----

    /** @return la expresion en construccion (ej. "3 + 4 × 2") */
    public String getElementosExpresion() {
        return elementosExpresion;
    }

    /** @param elementosExpresion nueva expresion (elementos separados por espacio) */
    public void setElementosExpresion(String elementosExpresion) {
        this.elementosExpresion = elementosExpresion == null ? "" : elementosExpresion;
    }

    /** @return el ultimo resultado calculado (NaN si no se ha calculado) */
    public double getResultado() {
        return resultado;
    }

    /** @param resultado nuevo resultado */
    public void setResultado(double resultado) {
        this.resultado = resultado;
    }

    /** @return partida a la que pertenece la expresion */
    public Partida getPartida() {
        return partida;
    }

    /** @param partida partida a la que pertenece la expresion */
    public void setPartida(Partida partida) {
        this.partida = partida;
    }
}
