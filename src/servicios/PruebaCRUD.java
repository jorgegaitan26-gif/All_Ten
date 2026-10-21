package servicios;

import java.util.Date;
import java.util.List;

import model.Jugador;
import model.Partida;

/**
 * Prueba de humo de la conexión y de los CRUD de Jugador y Partida.
 *
 * Crea datos temporales (nickname "prueba_..."), ejecuta cada operación,
 * verifica el resultado y al final borra todo lo que creó, así que no deja
 * basura en la base de datos. Ejecutar con el archivo .env en la raíz del proyecto.
 */
public class PruebaCRUD {

    private static int fallos = 0;

    public static void main(String[] args) {

        DAOJugador daoJ = new DAOJugador();
        DAOPartida daoP = new DAOPartida();

        Jugador j = new Jugador("prueba_" + System.currentTimeMillis());
        boolean jugadorCreado = false;

        try {
            // ---------- Conexión ----------
            verificar("Conexión a la base de datos",
                    ConexionBD.getInstancia().getConexion().isValid(5));

            // ---------- CRUD Jugador ----------
            System.out.println(daoJ.create(j));
            jugadorCreado = true;
            verificar("Jugador.create asigna id", j.getIdJugador() > 0);

            Jugador leido = daoJ.readone(j.getIdJugador());
            verificar("Jugador.readone", leido != null && leido.getNickname().equals(j.getNickname()));

            verificar("Jugador.readByNickname", daoJ.readByNickname(j.getNickname()) != null);

            verificar("Jugador.readall incluye al nuevo", contieneJugador(daoJ.readall(), j.getIdJugador()));

            try {
                daoJ.create(new Jugador(j.getNickname()));
                verificar("Jugador.create rechaza nickname repetido", false);
            } catch (IllegalArgumentException e) {
                verificar("Jugador.create rechaza nickname repetido", true);
            }

            j.setNickname(j.getNickname() + "_v2");
            System.out.println(daoJ.update(j));
            verificar("Jugador.update", daoJ.readone(j.getIdJugador()).getNickname().equals(j.getNickname()));

            // ---------- CRUD Partida ----------
            Partida p1 = new Partida(95, j.getIdJugador(), "(1+2)*3+1", new Date());
            Partida p2 = new Partida(40, j.getIdJugador(), "8/2+6", null); // sin fecha: usa la actual
            System.out.println(daoP.create(p1));
            System.out.println(daoP.create(p2));
            verificar("Partida.create asigna id", p1.getIdPartida() > 0 && p2.getIdPartida() > 0);

            Partida l1 = daoP.readone(p1.getIdPartida());
            verificar("Partida.readone", l1 != null && l1.getTiempoTotal() == 95
                    && l1.getJugadorIdJugador() == j.getIdJugador());

            verificar("Partida.create sin fecha guarda la actual",
                    daoP.readone(p2.getIdPartida()).getFechaFinalizacion() != null);

            verificar("Partida.readByJugador", daoP.readByJugador(j.getIdJugador()).size() == 2);

            verificar("Partida.readall incluye las nuevas", daoP.readall().size() >= 2);

            List<Partida> top = daoP.readTop(1000);
            verificar("Partida.readTop ordena por menor tiempo", top.size() >= 2 && ordenadoPorTiempo(top));

            p1.setTiempoTotal(80);
            p1.setOperacionesRealizadas("2*3+4");
            p1.setFechaFinalizacion(null); // update conserva la fecha guardada
            System.out.println(daoP.update(p1));
            Partida act = daoP.readone(p1.getIdPartida());
            verificar("Partida.update", act.getTiempoTotal() == 80 && act.getOperacionesRealizadas().equals("2*3+4")
                    && act.getFechaFinalizacion() != null);

            try {
                daoP.create(new Partida(10, 2147483647, "x", null));
                verificar("Partida.create rechaza jugador inexistente", false);
            } catch (IllegalArgumentException e) {
                verificar("Partida.create rechaza jugador inexistente", true);
            }

            // ---------- Integridad: no se borra un jugador con partidas ----------
            String msg = daoJ.delete(j.getIdJugador());
            System.out.println(msg);
            verificar("Jugador.delete con partidas es rechazado", daoJ.readone(j.getIdJugador()) != null);

            // ---------- Delete ----------
            System.out.println(daoP.delete(p1.getIdPartida()));
            verificar("Partida.delete", daoP.readone(p1.getIdPartida()) == null);
            System.out.println(daoP.deleteByJugador(j.getIdJugador()));
            verificar("Partida.deleteByJugador", daoP.readByJugador(j.getIdJugador()).isEmpty());

            System.out.println(daoJ.delete(j.getIdJugador()));
            jugadorCreado = false;
            verificar("Jugador.delete", daoJ.readone(j.getIdJugador()) == null);

        } catch (Exception e) {
            fallos++;
            System.out.println("[FALLO] Error inesperado: " + e);
            e.printStackTrace();
        } finally {
            // Limpieza: si algo falló a medias, no dejar datos de prueba
            if (jugadorCreado) {
                try {
                    daoP.deleteByJugador(j.getIdJugador());
                    daoJ.delete(j.getIdJugador());
                } catch (Exception e) {
                    System.out.println("No se pudo limpiar el jugador de prueba " + j.getIdJugador()
                            + ": " + e.getMessage());
                }
            }
            ConexionBD.cerrarSiExiste();
        }

        System.out.println(fallos == 0 ? "\nTODO OK" : "\nHubo " + fallos + " fallo(s)");
        System.exit(fallos == 0 ? 0 : 1);
    }

    private static void verificar(String nombre, boolean ok) {
        System.out.println((ok ? "[OK]    " : "[FALLO] ") + nombre);
        if (!ok) {
            fallos++;
        }
    }

    private static boolean contieneJugador(List<Jugador> lista, int id) {
        return lista.stream().anyMatch(x -> x.getIdJugador() == id);
    }

    private static boolean ordenadoPorTiempo(List<Partida> lista) {
        for (int i = 1; i < lista.size(); i++) {
            if (lista.get(i - 1).getTiempoTotal() > lista.get(i).getTiempoTotal()) {
                return false;
            }
        }
        return true;
    }
}
