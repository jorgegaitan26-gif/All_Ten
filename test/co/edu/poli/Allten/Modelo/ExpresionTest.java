package co.edu.poli.Allten.Modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ExpresionTest {

    private Partida partida;
    private Expresion expresion;

    @BeforeEach
    void preparar() {
        partida = new Partida(new Jugador("julian"));
        partida.setNumerosDisponibles(new ArrayList<>(List.of(1, 2, 3, 4)));
        expresion = new Expresion(partida);
    }

    /** Escribe una expresion elemento por elemento, como lo haria el jugador. */
    private void escribir(String texto) {
        for (String elemento : texto.split(" ")) {
            boolean agregado;
            if (elemento.matches("\\d")) {
                agregado = expresion.concatenarDigitos(Integer.parseInt(elemento));
            } else if (elemento.equals("(") || elemento.equals(")")) {
                agregado = expresion.agregarParentesis(elemento);
            } else {
                agregado = expresion.agregarOperacion(elemento);
            }
            assertTrue(agregado, "No se pudo agregar '" + elemento + "' en: " + expresion.getElementosExpresion());
        }
    }

    // ----- Construccion -----

    @Test
    @DisplayName("concatenarDigitos forma numeros de varias cifras y consume los digitos")
    void concatenar() {
        assertTrue(expresion.concatenarDigitos(1));
        assertTrue(expresion.concatenarDigitos(2));
        assertEquals("12", expresion.getElementosExpresion());
        assertFalse(expresion.concatenarDigitos(1));   // el 1 ya se uso
    }

    @Test
    @DisplayName("No se puede usar un digito que no esta disponible")
    void digitoNoDisponible() {
        assertFalse(expresion.concatenarDigitos(9));
    }

    @Test
    @DisplayName("Posiciones invalidas de operadores y parentesis se rechazan")
    void posicionesInvalidas() {
        assertFalse(expresion.agregarOperacion(Expresion.SUMA));          // operador al inicio
        assertFalse(expresion.agregarParentesis(")"));                    // cerrar sin abrir
        escribir("1 +");
        assertFalse(expresion.agregarOperacion(Expresion.MULTIPLICACION)); // dos operadores seguidos
        escribir("( 2");
        assertTrue(expresion.agregarParentesis(")"));
        assertFalse(expresion.concatenarDigitos(3));                       // numero despues de ")"
    }

    @Test
    @DisplayName("borrarUltimoElemento quita la ultima cifra o el ultimo elemento")
    void borrar() {
        escribir("( 1");
        expresion.concatenarDigitos(2);
        expresion.borrarUltimoElemento();
        assertEquals("( 1", expresion.getElementosExpresion());
        expresion.borrarUltimoElemento();
        assertEquals("(", expresion.getElementosExpresion());
        expresion.borrarUltimoElemento();
        assertEquals("", expresion.getElementosExpresion());
        expresion.borrarUltimoElemento();   // vacia: no falla
        assertTrue(expresion.concatenarDigitos(2));   // el 2 quedo libre
    }

    // ----- Validacion -----

    @Test
    @DisplayName("Una expresion bien formada es valida y esta finalizada")
    void valida() {
        escribir("( 1 + 2 ) × 3 - 4");
        assertTrue(expresion.validarExpresion());
        assertTrue(expresion.validarExpresionFinalizada());
    }

    @Test
    @DisplayName("Una expresion incompleta no esta finalizada")
    void incompleta() {
        escribir("( 1 + 2");
        assertFalse(expresion.validarExpresionFinalizada());
    }

    @Test
    @DisplayName("validarNumerosPermitidos y validarUsoDeNumeros detectan numeros ajenos o repetidos")
    void controlDeNumeros() {
        expresion.setElementosExpresion("1 + 9");
        assertFalse(expresion.validarNumerosPermitidos());

        expresion.setElementosExpresion("1 + 1");
        assertTrue(expresion.validarNumerosPermitidos());
        assertFalse(expresion.validarUsoDeNumeros());
    }

    @Test
    @DisplayName("validarPosicionOperador y validarPosicionParentesis detectan errores")
    void posiciones() {
        expresion.setElementosExpresion("1 + × 2");
        assertFalse(expresion.validarPosicionOperador());

        expresion.setElementosExpresion("( 1 + 2 ) )");
        assertFalse(expresion.validarPosicionParentesis());
    }

    @Test
    @DisplayName("Potencia no permitida en FACIL, si en MEDIO; raiz solo en DIFICIL")
    void reglasPorDificultad() {
        escribir("2");
        assertFalse(expresion.agregarOperacion(Expresion.POTENCIA));

        partida.setDificultad("MEDIO");
        assertTrue(expresion.agregarOperacion(Expresion.POTENCIA));
        assertFalse(expresion.agregarOperacion(Expresion.RAIZ));

        partida.setDificultad("DIFICIL");
        assertTrue(expresion.agregarOperacion(Expresion.RAIZ));
    }

    // ----- Calculo -----

    @Test
    @DisplayName("Respeta la jerarquia: 1 + 2 × 3 - 4 = 3")
    void jerarquia() {
        escribir("1 + 2 × 3 - 4");
        assertEquals(3, expresion.calcularResultado(), 1e-9);
        assertEquals(3, expresion.getResultado(), 1e-9);
    }

    @Test
    @DisplayName("Resuelve parentesis primero: (1 + 2) × (3 + 4) = 21")
    void parentesis() {
        escribir("( 1 + 2 ) × ( 3 + 4 )");
        assertEquals(21, expresion.calcularResultado(), 1e-9);
    }

    @Test
    @DisplayName("Potencias y raices: 2 ^ 3 + 1 - 4 = 5 ; √ 4 + √ ( 1 + 3 ) = 4")
    void potenciasYRaices() {
        partida.setDificultad("DIFICIL");
        escribir("2 ^ 3 + 1 - 4");
        assertEquals(5, expresion.calcularResultado(), 1e-9);

        expresion = new Expresion(partida);
        escribir("√ 4 + √ ( 1 + 3 )");
        assertEquals(4, expresion.calcularResultado(), 1e-9);
    }

    @Test
    @DisplayName("Division entre cero o expresion incompleta retornan NaN")
    void calculosInvalidos() {
        expresion.setElementosExpresion("4 ÷ ( 1 - 1 )");
        assertTrue(Double.isNaN(expresion.calcularResultado()));

        expresion = new Expresion(partida);
        escribir("1 +");
        assertTrue(Double.isNaN(expresion.calcularResultado()));
        assertEquals("1 +", expresion.getElementosExpresion());
    }

    @Test
    @DisplayName("continuarOperacion encierra el resultado parcial y deja seguir con los numeros que faltan")
    void continuar() {
        escribir("1 + 2");
        assertTrue(expresion.continuarOperacion());
        assertEquals(3, expresion.getResultado(), 1e-9);
        assertEquals("( 1 + 2 )", expresion.getElementosExpresion());

        escribir("× 3 + 4");
        assertEquals(13, expresion.calcularResultado(), 1e-9);
        assertFalse(expresion.continuarOperacion());   // ya se usaron los 4 numeros
    }

    @Test
    @DisplayName("continuarOperacion no envuelve un solo numero ni algo que ya esta entre parentesis")
    void continuarSinEnvolverDeMas() {
        escribir("3");
        assertTrue(expresion.continuarOperacion());
        assertEquals("3", expresion.getElementosExpresion());

        escribir("+ 4");
        assertTrue(expresion.continuarOperacion());
        assertEquals("( 3 + 4 )", expresion.getElementosExpresion());
        assertTrue(expresion.continuarOperacion());
        assertEquals("( 3 + 4 )", expresion.getElementosExpresion());

        // Empieza con "(" pero el grupo se cierra antes del final: si se envuelve.
        expresion = new Expresion(partida);
        escribir("( 1 + 2 ) × 3");
        assertTrue(expresion.continuarOperacion());
        assertEquals("( ( 1 + 2 ) × 3 )", expresion.getElementosExpresion());
        assertEquals(9, expresion.getResultado(), 1e-9);
    }
}
