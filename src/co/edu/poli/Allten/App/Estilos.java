package co.edu.poli.Allten.App;

import javafx.scene.control.Dialog;

/**
 * Utilidades de estilo compartidas. Garantiza que alertas y dialogos usen
 * la misma fuente (Poppins) que el resto del juego.
 */
public final class Estilos {

    private Estilos() {
    }

    /** Aplica la hoja de estilos comun (fuente Poppins) a una alerta o dialogo. */
    public static void aplicarFuente(Dialog<?> dialogo) {
        dialogo.getDialogPane().getStylesheets().add(
                Estilos.class.getResource("/vista/interfaz.css").toExternalForm());
    }
}
