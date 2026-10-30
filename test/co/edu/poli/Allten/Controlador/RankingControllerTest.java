package co.edu.poli.Allten.Controlador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import co.edu.poli.Allten.DAO.EntradaRanking;

/** Pruebas de los filtros del ranking (SCRUM-196), sin base de datos ni ventana. */
class RankingControllerTest {

    /** Ranking de ejemplo con 15 jugadores, ya ordenado por tiempo. */
    private static List<EntradaRanking> ranking() {
        List<EntradaRanking> lista = new ArrayList<>();
        String[] nombres = {"Julián", "Angie", "Jorge", "ana", "Andrés", "Beto", "Caro", "Dani",
            "Eva", "Fede", "Gabi", "Hugo", "Iván", "Juana", "Kevin"};
        for (int i = 0; i < nombres.length; i++) {
            lista.add(new EntradaRanking(i + 1, nombres[i], 30 + i, new Date()));
        }
        return lista;
    }

    @Test
    @DisplayName("Sin busqueda muestra solo los primeros N puestos")
    void topN() {
        assertEquals(10, RankingController.filtrar(ranking(), "", 10).size());
        assertEquals(15, RankingController.filtrar(ranking(), null, 20).size());
        assertEquals(1, RankingController.filtrar(ranking(), "  ", 10).get(0).getPosicion());
    }

    @Test
    @DisplayName("La busqueda ignora mayusculas y tildes")
    void busquedaSinTildesNiMayusculas() {
        List<EntradaRanking> resultado = RankingController.filtrar(ranking(), "JULIAN", 10);
        assertEquals(1, resultado.size());
        assertEquals("Julián", resultado.get(0).getNickname());
    }

    @Test
    @DisplayName("La busqueda encuentra coincidencias parciales y conserva la posicion real")
    void busquedaParcial() {
        List<EntradaRanking> resultado = RankingController.filtrar(ranking(), "an", 3);
        // Julián, Angie, ana, Andrés, Dani, Iván, Juana (la busqueda no se corta en el Top 3)
        List<Integer> posiciones = new ArrayList<>();
        for (EntradaRanking entrada : resultado) {
            posiciones.add(entrada.getPosicion());
        }
        assertEquals(List.of(1, 2, 4, 5, 8, 13, 14), posiciones);
    }

    @Test
    @DisplayName("La busqueda encuentra jugadores fuera del Top elegido")
    void busquedaFueraDelTop() {
        List<EntradaRanking> resultado = RankingController.filtrar(ranking(), "kevin", 10);
        assertEquals(1, resultado.size());
        assertEquals(15, resultado.get(0).getPosicion());
    }

    @Test
    @DisplayName("Sin coincidencias devuelve una lista vacia")
    void sinCoincidencias() {
        assertTrue(RankingController.filtrar(ranking(), "zzz", 10).isEmpty());
    }
}
