import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Sección de "números + operadores + zona de trabajo" del juego matemático.
 *
 * Aplica lo definido en los documentos de metodología:
 *  - Colorimetría: azul (#85B7EB) números, naranja (#EF9F27) operadores,
 *    verde (#97C459) resultado correcto, rojo-coral (#F09595) resultado inválido.
 *  - Gestalt / región común: la zona de trabajo tiene su propio contenedor
 *    (fondo blanco) separado de los grupos de tiles.
 *  - Gestalt / proximidad y similitud: números y operadores son dos bloques
 *    separados espacialmente, cada uno con estilo interno homogéneo.
 *  - Ley de Fitts: tiles de 96x96px (por encima de los mínimos de Apple/Google/WCAG),
 *    con 12px de separación entre ellos.
 *  - Carga cognitiva (Sweller): los números usados no desaparecen ni reordenan
 *    el layout, solo cambian de opacidad, para no obligar al usuario a
 *    reconstruir mentalmente el tablero.
 *
 * Flujo de interacción:
 *  1. El usuario toca un número (queda resaltado).
 *  2. Toca un operador (se resalta y se agrega a la zona de trabajo).
 *  3. Toca un segundo número distinto -> se calcula el resultado.
 *  4. La zona de trabajo muestra la ecuación completa con su estado
 *     (verde + check si es una operación válida, rojo-coral + X si no lo es,
 *     por ejemplo una división no exacta).
 *  5. El botón "Borrar" reinicia el intento actual sin afectar el resto del juego.
 */
public class Pruebajavafx extends Application {

    // ----- Paleta -----
    private static final String COLOR_BG           = "#F1EFE8";
    private static final String COLOR_WORKZONE_BG  = "#FFFFFF";
    private static final String COLOR_WORKZONE_OK  = "#97C459";
    private static final String COLOR_WORKZONE_ERR = "#F09595";
    private static final String COLOR_NUM_ACTIVE    = "#85B7EB";
    private static final String COLOR_NUM_SELECTED  = "#4A8FD6";
    private static final String COLOR_NUM_USED      = "#B5D4F4";
    private static final String COLOR_NUM_TEXT      = "#042C53";
    private static final String COLOR_OP_ACTIVE     = "#EF9F27";
    private static final String COLOR_OP_SELECTED   = "#C97F0F";
    private static final String COLOR_OP_TEXT       = "#412402";
    private static final String COLOR_LABEL         = "#888780";
    private static final String COLOR_TEXT_OK       = "#173404";
    private static final String COLOR_TEXT_ERR      = "#4A0E0E";

    // ----- Tamaños (Ley de Fitts) -----
    private static final double TILE_SIZE = 96;
    private static final double TILE_GAP  = 12;

    // ----- Estado del intento actual -----
    private Integer firstNumber = null;
    private Integer secondNumber = null;
    private String operator = null;
    private ToggleButton firstNumberButton = null;

    private final List<ToggleButton> numberButtons = new ArrayList<>();
    private final List<ToggleButton> operatorButtons = new ArrayList<>();
    private final boolean[] used = new boolean[4];

    private Label workzoneLabel;
    private StackPane workzoneBox;

    @Override
    public void start(Stage stage) {
        int[] numbers = {6, 4, 2, 8};
        String[] operators = {"+", "\u2212", "\u00D7", "\u00F7"}; // + − × ÷

        VBox root = new VBox(18);
        root.setPadding(new Insets(28));
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 20;");
        root.setMaxWidth(460);

        // 2. Zona de trabajo (región común, Gestalt)
        root.getChildren().add(buildSectionLabel("zona de trabajo"));
        root.getChildren().add(buildWorkzone());

        // 3. Grupo: números
        root.getChildren().add(buildSectionLabel("números"));
        root.getChildren().add(buildNumbersRow(numbers));

        // 4. Grupo: operadores
        root.getChildren().add(buildSectionLabel("operaciones"));
        root.getChildren().add(buildOperatorsRow(operators));

        // Acción para reiniciar el intento actual
        Button clearButton = new Button("Borrar intento");
        clearButton.setOnAction(e -> resetAttempt());
        root.getChildren().add(clearButton);

        Scene scene = new Scene(root, 500, 520);
        stage.setTitle("Números, operadores y zona de trabajo");
        stage.setScene(scene);
        stage.show();
    }

    // ---------------------------------------------------------------
    // Construcción de secciones
    // ---------------------------------------------------------------

    private Label buildSectionLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", 13));
        label.setTextFill(javafx.scene.paint.Color.web(COLOR_LABEL));
        return label;
    }

    private StackPane buildWorkzone() {
        workzoneLabel = new Label("Toca un número para empezar");
        workzoneLabel.setFont(Font.font("System", FontWeight.BOLD, 20));
        workzoneLabel.setTextFill(javafx.scene.paint.Color.web(COLOR_LABEL));

        workzoneBox = new StackPane(workzoneLabel);
        workzoneBox.setPadding(new Insets(18));
        workzoneBox.setMinHeight(60);
        applyWorkzoneStyle(COLOR_WORKZONE_BG, COLOR_LABEL);
        return workzoneBox;
    }

    private HBox buildNumbersRow(int[] numbers) {
        HBox row = new HBox(TILE_GAP);
        for (int i = 0; i < numbers.length; i++) {
            int index = i;
            int value = numbers[i];

            ToggleButton tile = new ToggleButton(String.valueOf(value));
            styleTile(tile, COLOR_NUM_ACTIVE, COLOR_NUM_TEXT);
            numberButtons.add(tile);

            tile.setOnAction(e -> onNumberClicked(index, value, tile));
            row.getChildren().add(tile);
        }
        return row;
    }

    private HBox buildOperatorsRow(String[] operators) {
        HBox row = new HBox(TILE_GAP);
        for (String op : operators) {
            ToggleButton tile = new ToggleButton(op);
            styleTile(tile, COLOR_OP_ACTIVE, COLOR_OP_TEXT);
            operatorButtons.add(tile);

            tile.setOnAction(e -> onOperatorClicked(op, tile));
            row.getChildren().add(tile);
        }
        return row;
    }

    // ---------------------------------------------------------------
    // Lógica de interacción
    // ---------------------------------------------------------------

    private void onNumberClicked(int index, int value, ToggleButton tile) {
        if (used[index]) {
            tile.setSelected(false);
            return;
        }

        if (firstNumber == null) {
            // Primer número del intento
            firstNumber = value;
            firstNumberButton = tile;
            tile.setSelected(true);
            tile.setStyle(tileStyle(COLOR_NUM_SELECTED, COLOR_NUM_TEXT));
            workzoneLabel.setText(String.valueOf(firstNumber));
            applyWorkzoneStyle(COLOR_WORKZONE_BG, "#2C2C2A");

        } else if (operator != null && secondNumber == null && tile != firstNumberButton) {
            // Segundo número: se completa la operación
            secondNumber = value;
            tile.setSelected(true);
            tile.setStyle(tileStyle(COLOR_NUM_SELECTED, COLOR_NUM_TEXT));
            evaluateOperation(index, tile);
        }
    }

    private void onOperatorClicked(String op, ToggleButton tile) {
        if (firstNumber == null || secondNumber != null) {
            return; // Necesita un primer número, y no se puede cambiar tras completar
        }
        operator = op;
        for (ToggleButton other : operatorButtons) {
            if (other != tile) other.setSelected(false);
        }
        tile.setSelected(true);
        workzoneLabel.setText(firstNumber + " " + op);
    }

    private void evaluateOperation(int secondIndex, ToggleButton secondTile) {
        Double result = compute(firstNumber, operator, secondNumber);
        boolean valid = result != null;

        String equation = firstNumber + " " + operator + " " + secondNumber + " = "
                + (valid ? formatResult(result) : "?");

        if (valid) {
            workzoneLabel.setText(equation + "  \u2713"); // check
            applyWorkzoneStyle(COLOR_WORKZONE_OK, COLOR_TEXT_OK);
            markUsed(firstNumberButton);
            markUsed(secondTile);
        } else {
            workzoneLabel.setText(equation + "  \u2717"); // X
            applyWorkzoneStyle(COLOR_WORKZONE_ERR, COLOR_TEXT_ERR);
        }
    }

    private Double compute(int a, String op, int b) {
        switch (op) {
            case "+": return (double) (a + b);
            case "\u2212": return (double) (a - b);
            case "\u00D7": return (double) (a * b);
            case "\u00F7":
                if (b == 0 || a % b != 0) return null; // división no exacta -> inválida
                return (double) (a / b);
            default: return null;
        }
    }

    private String formatResult(double value) {
        return (value == Math.floor(value)) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private void markUsed(ToggleButton tile) {
        int idx = numberButtons.indexOf(tile);
        if (idx >= 0) used[idx] = true;
        tile.setStyle(tileStyle(COLOR_NUM_USED, COLOR_NUM_TEXT));
        tile.setOpacity(0.55); // Reducción de carga cognitiva: se atenúa, no desaparece
        tile.setDisable(true);
    }

    /** Reinicia el intento actual sin tocar los números ya marcados como usados. */
    private void resetAttempt() {
        firstNumber = null;
        secondNumber = null;
        operator = null;
        firstNumberButton = null;

        for (int i = 0; i < numberButtons.size(); i++) {
            ToggleButton tile = numberButtons.get(i);
            tile.setSelected(false);
            if (!used[i]) {
                tile.setStyle(tileStyle(COLOR_NUM_ACTIVE, COLOR_NUM_TEXT));
            }
        }
        for (ToggleButton tile : operatorButtons) {
            tile.setSelected(false);
            tile.setStyle(tileStyle(COLOR_OP_ACTIVE, COLOR_OP_TEXT));
        }

        workzoneLabel.setText("Toca un número para empezar");
        applyWorkzoneStyle(COLOR_WORKZONE_BG, COLOR_LABEL);
    }

    // ---------------------------------------------------------------
    // Estilos
    // ---------------------------------------------------------------

    private void styleTile(ToggleButton tile, String bg, String textColor) {
        tile.setPrefSize(TILE_SIZE, TILE_SIZE);
        tile.setFont(Font.font("System", FontWeight.BOLD, 24));
        tile.setStyle(tileStyle(bg, textColor));
    }

    private String tileStyle(String bg, String textColor) {
        return "-fx-background-color: " + bg + ";" +
               "-fx-background-radius: 14;" +
               "-fx-text-fill: " + textColor + ";";
    }

    private void applyWorkzoneStyle(String bg, String textColor) {
        workzoneBox.setStyle(
                "-fx-background-color: " + bg + ";" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #A9A79E;" +
                "-fx-border-radius: 14;" +
                "-fx-border-width: 1.5;"
        );
        workzoneLabel.setTextFill(javafx.scene.paint.Color.web(textColor));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
