package co.edu.poli.Allten.Modelo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Pruebas de los diagramas de flujo de la clase Expresion. */
class ExpresionTest {

    private static final List<Integer> NUMEROS = List.of(1, 2, 3, 4);

    /** Arma una expresion tocando los elementos uno a uno, como lo haria el jugador. */
    static Expresion armar(List<Integer> numeros, Partida.NivelDificultad nivel, String... elementos) {
        Expresion expresion = new Expresion(numeros, nivel);
        for (String elemento : elementos) {
            boolean agregado;
            if (elemento.matches("\\d")) {
                agregado = expresion.concatenarDigitos(Integer.parseInt(elemento));
            } else if (elemento.equals("(") || elemento.equals(")")) {
                agregado = expresion.agregarParentesis(elemento);
            } else {
                agregado = expresion.agregarOperacion(elemento);
            }
            assertTrue(agregado, "No se pudo agregar '" + elemento + "' en: " + expresion.getTextoFormateado());
        }
        return expresion;
    }

    private static Expresion facil(String... elementos) {
        return armar(NUMEROS, Partida.NivelDificultad.FACIL, elementos);
    }

    @Nested
    @DisplayName("Tarea 1: construccion")
    class Construccion {

        @Test
        @DisplayName("elementosExpresion es un String con los elementos separados por espacios")
        void elementosComoString() {
            assertEquals("1 + 2 × 3 - 4", facil("1", "+", "2", "×", "3", "-", "4").getElementosExpresion());
        }

        @Test
        @DisplayName("concatenarDigitos forma numeros de varias cifras y consume los digitos")
        void concatenar() {
            Expresion e = facil("1", "2");
            assertEquals("12", e.getTextoFormateado());
            assertTrue(e.isNumeroUsado(0));
            assertTrue(e.isNumeroUsado(1));
        }

        @Test
        @DisplayName("No se puede usar un digito que no esta disponible")
        void digitoNoDisponible() {
            Expresion e = new Expresion(NUMEROS);
            assertFalse(e.concatenarDigitos(9));
            e.concatenarDigitos(1);
            e.agregarOperacion(Expresion.SUMA);
            assertFalse(e.concatenarDigitos(1), "el 1 ya se uso");
        }

        @Test
        @DisplayName("Posiciones invalidas de operadores y parentesis se rechazan")
        void posicionesInvalidas() {
            Expresion e = new Expresion(NUMEROS);
            assertFalse(e.agregarOperacion(Expresion.SUMA));
            assertFalse(e.agregarParentesis(")"));
            e.concatenarDigitos(1);
            assertFalse(e.agregarParentesis("("));
            e.agregarOperacion(Expresion.SUMA);
            assertFalse(e.agregarOperacion(Expresion.MULTIPLICACION));
        }

        @Test
        @DisplayName("borrarUltimoElemento deshace paso a paso y libera los numeros")
        void borrar() {
            Expresion e = facil("1", "2", "+", "(");
            e.borrarUltimoElemento();
            e.borrarUltimoElemento();
            assertEquals("12", e.getTextoFormateado());
            e.borrarUltimoElemento();
            assertEquals("1", e.getTextoFormateado());
            assertFalse(e.isNumeroUsado(1));
            assertTrue(e.concatenarDigitos(2), "se puede volver a concatenar");
        }
    }

    @Nested
    @DisplayName("Tarea 2: validarExpresion")
    class Validacion {

        @Test
        @DisplayName("Una expresion bien formada es valida")
        void valida() {
            assertTrue(facil("(", "1", "+", "2", ")", "×", "3").validarExpresion());
        }

        @Test
        @DisplayName("Division entre cero hace la expresion invalida")
        void divisionCero() {
            Expresion e = armar(List.of(1, 1, 2, 3), Partida.NivelDificultad.FACIL,
                    "2", "÷", "(", "1", "-", "1", ")");
            assertFalse(e.validarDivisionCero());
            assertFalse(e.validarExpresion());
        }

        @Test
        @DisplayName("validarUsoDeNumeros: cada numero maximo una vez (no exige usar los 4)")
        void usoDeNumeros() {
            assertTrue(facil("1", "+", "2").validarUsoDeNumeros());
        }

        @Test
        @DisplayName("Potencia no permitida en FACIL, si en MEDIO; raiz solo en DIFICIL")
        void reglasPorDificultad() {
            assertFalse(facil("2").agregarOperacion(Expresion.POTENCIA));
            assertTrue(armar(NUMEROS, Partida.NivelDificultad.MEDIO, "2").agregarOperacion(Expresion.POTENCIA));
            assertFalse(new Expresion(NUMEROS, Partida.NivelDificultad.MEDIO).agregarOperacion(Expresion.RAIZ));
            assertTrue(new Expresion(NUMEROS, Partida.NivelDificultad.DIFICIL).agregarOperacion(Expresion.RAIZ));
        }

        @Test
        @DisplayName("Una expresion incompleta no esta finalizada")
        void incompleta() {
            Expresion e = facil("1", "+");
            assertFalse(e.validarExpresionFinalizada());
        }
    }

    @Nested
    @DisplayName("Tarea 3: calcularResultado y continuarOperacion")
    class Calculo {

        @Test
        @DisplayName("Respeta la jerarquia: 1 + 2 × 3 - 4 = 3")
        void jerarquia() {
            Expresion e = facil("1", "+", "2", "×", "3", "-", "4");
            assertEquals(3, e.calcularResultado());
            assertEquals(3, e.getResultado());
            assertTrue(e.comprobarCoincidenciaObjetivo());
        }

        @Test
        @DisplayName("Resuelve parentesis primero: (1 + 2) × (3 + 4) = 21")
        void parentesis() {
            Expresion e = facil("(", "1", "+", "2", ")", "×", "(", "3", "+", "4", ")");
            assertEquals(21, e.calcularResultado());
            assertFalse(e.comprobarCoincidenciaObjetivo(), "21 no es objetivo");
        }

        @Test
        @DisplayName("Potencias y raices: 2 ^ 3 + 1 - 4 = 5 ; √4 + √(1 + 3) × 2 = 6")
        void potenciasYRaices() {
            assertEquals(5, armar(NUMEROS, Partida.NivelDificultad.MEDIO,
                    "2", "^", "3", "+", "1", "-", "4").calcularResultado());
            assertEquals(6, armar(NUMEROS, Partida.NivelDificultad.DIFICIL,
                    "√", "4", "+", "√", "(", "1", "+", "3", ")", "×", "2").calcularResultado());
        }

        @Test
        @DisplayName("Expresion incompleta: retorna NaN y mantiene el texto")
        void incompletaRetornaNaN() {
            Expresion e = facil("1", "+");
            assertTrue(Double.isNaN(e.calcularResultado()));
            assertEquals("1 +", e.getTextoFormateado());
        }

        @Test
        @DisplayName("Un resultado parcial no cuenta como objetivo si faltan numeros")
        void parcialNoEsObjetivo() {
            Expresion e = facil("1", "+", "2");
            assertEquals(3, e.calcularResultado());
            assertFalse(e.comprobarCoincidenciaObjetivo());
        }

        @Test
        @DisplayName("continuarOperacion fija el resultado parcial y libera los numeros no usados")
        void continuar() {
            Expresion e = facil("4", "÷", "2", "+", "1");      // = 3, queda libre el 3
            assertTrue(e.continuarOperacion());
            assertEquals("3", e.getTextoFormateado());
            assertFalse(e.isNumeroUsado(2));
            assertFalse(e.concatenarDigitos(3), "no se concatena sobre el resultado parcial");
            assertTrue(e.agregarOperacion(Expresion.MULTIPLICACION));
            assertTrue(e.concatenarDigitos(3));
            assertEquals(9, e.calcularResultado());
            assertTrue(e.comprobarCoincidenciaObjetivo());
            assertFalse(e.continuarOperacion(), "ya no quedan numeros");
        }

        @Test
        @DisplayName("Resultados parciales negativos y decimales")
        void parcialesNegativosYDecimales() {
            Expresion negativo = facil("1", "-", "4");
            assertTrue(negativo.continuarOperacion());
            assertEquals("-3", negativo.getTextoFormateado());

            Expresion decimal = facil("3", "÷", "2");
            assertTrue(decimal.continuarOperacion());
            assertEquals("1.5", decimal.getTextoFormateado());
        }
    }
}
