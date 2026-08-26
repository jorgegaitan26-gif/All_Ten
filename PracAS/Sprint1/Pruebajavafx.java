package Sprint1;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * Boceto (SCRUM-25) ampliado: tabla de progreso 1-10 + números + operadores +
 * paréntesis + controles (borrar último, reiniciar todo) + botón de resultado.
 *
 * A diferencia de la primera versión (que solo soportaba número-operador-número),
 * esta versión arma una EXPRESIÓN completa como en la referencia (Beast Academy):
 * el usuario va tocando números, operadores y paréntesis, y la expresión se
 * evalúa solo cuando toca "=".
 *
 * Aplica la colorimetría y metodología de los documentos del proyecto
 * (Gestalt, Ley de Fitts, carga cognitiva de Sweller).
 */
public class Pruebajavafx extends Application {

    // ----- Paleta -----
    private static final String COLOR_BG           = "#F1EFE8";
    private static final String COLOR_WORKZONE_BG  = "#FFFFFF";
    private static final String COLOR_WORKZONE_OK  = "#97C459";
    private static final String COLOR_WORKZONE_ERR = "#F09595";
    private static final String COLOR_NUM_ACTIVE    = "#85B7EB";
    private static final String COLOR_NUM_INCLUDED  = "#4A8FD6"; // en la expresión actual, sin confirmar
    private static final String COLOR_NUM_USED      = "#B5D4F4"; // ya usado en una operación resuelta
    private static final String COLOR_NUM_TEXT      = "#042C53";
    private static final String COLOR_OP_ACTIVE     = "#EF9F27";
    private static final String COLOR_OP_TEXT       = "#412402";
    private static final String COLOR_CTRL_ACTIVE   = "#6B7280"; // gris azulado para (, ), ⌫, ↺
    private static final String COLOR_CTRL_TEXT     = "#FFFFFF";
    private static final String COLOR_EQUALS_BG     = "#2C2C2A";
    private static final String COLOR_EQUALS_TEXT   = "#FFFFFF";
    private static final String COLOR_LABEL         = "#888780";
    private static final String COLOR_TEXT_OK       = "#173404";
    private static final String COLOR_TEXT_ERR      = "#4A0E0E";
    private static final String COLOR_ROW_BORDER    = "#DAD8CF";

    // ----- Tamaños (Ley de Fitts) -----
    private static final double TILE_SIZE = 96;
    private static final double TILE_GAP  = 12;

    /** Un token de la expresión: número (con su tile de origen) u operador/paréntesis. */
    private static class Token {
        String text;
        ToggleButton sourceTile; // solo para números; null para operadores/paréntesis
        Token(String text, ToggleButton sourceTile) { this.text = text; this.sourceTile = sourceTile; }
    }

    private final List<Token> expression = new ArrayList<>();
    private final List<ToggleButton> numberButtons = new ArrayList<>();
    private final boolean[] used = new boolean[4]; // números ya confirmados en una operación resuelta

    private final Label[] progressRows = new Label[11]; // índice 1..10
    private final boolean[] solved = new boolean[11];

    private Label workzoneLabel;
    private StackPane workzoneBox;

    @Override
    public void start(Stage stage) {
        int[] numbers = {6, 4, 2, 8};

        VBox root = new VBox(18);
        root.setPadding(new Insets(28));
        root.setAlignment(Pos.TOP_LEFT);
        root.setStyle("-fx-background-color: " + COLOR_BG + "; -fx-background-radius: 20;");
        root.setMaxWidth(480);

        root.getChildren().add(buildSectionLabel("progreso"));
        root.getChildren().add(buildProgressTable());

        root.getChildren().add(buildSectionLabel("zona de trabajo"));
        root.getChildren().add(buildWorkzone());

        root.getChildren().add(buildSectionLabel("números"));
        root.getChildren().add(buildNumbersRow(numbers));

        root.getChildren().add(buildSectionLabel("operaciones"));
        root.getChildren().add(buildOperatorsRow());

        root.getChildren().add(buildSectionLabel("controles"));
        root.getChildren().add(buildControlsRow());

        Scene scene = new Scene(root, 520, 820);
        stage.setTitle("Boceto: progreso, números, operadores, paréntesis y resultado");
        stage.setScene(scene);
        stage.show();
    }

    // ---------------------------------------------------------------
    // Construcción de secciones
    // ---------------------------------------------------------------

    private Label buildSectionLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", 13));
        label.setTextFill(Color.web(COLOR_LABEL));
        return label;
    }

    private GridPane buildProgressTable() {
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(8);
        for (int i = 1; i <= 5; i++) {
            grid.add(buildProgressRow(i), 0, i - 1);
            grid.add(buildProgressRow(i + 5), 1, i - 1);
        }
        return grid;
    }

    private HBox buildProgressRow(int number) {
        Label eqLabel = new Label("\u2014");
        eqLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        eqLabel.setTextFill(Color.web(COLOR_LABEL));
        eqLabel.setPadding(new Insets(0, 0, 0, 12));
        eqLabel.setPrefWidth(120);
        eqLabel.setMinHeight(40);
        eqLabel.setAlignment(Pos.CENTER_LEFT);
        eqLabel.setStyle("-fx-background-color: #FFFFFF; -fx-background-radius: 10 0 0 10;");

        Label numLabel = new Label(String.valueOf(number));
        numLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        numLabel.setTextFill(Color.web("#2C2C2A"));
        numLabel.setMinSize(40, 40);
        numLabel.setAlignment(Pos.CENTER);
        numLabel.setStyle("-fx-background-color: #EDEBE3; -fx-background-radius: 0 10 10 0; -fx-border-color: " + COLOR_ROW_BORDER + "; -fx-border-width: 0 0 0 1;");

        HBox row = new HBox(eqLabel, numLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        progressRows[number] = eqLabel;
        return row;
    }

    private StackPane buildWorkzone() {
        workzoneLabel = new Label("Toca números, operadores o paréntesis");
        workzoneLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        workzoneLabel.setTextFill(Color.web(COLOR_LABEL));
        workzoneLabel.setWrapText(true);

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

    private HBox buildOperatorsRow() {
        HBox row = new HBox(TILE_GAP);
        String[] operators = {"+", "\u2212", "\u00D7", "\u00F7"}; // + − × ÷
        for (String op : operators) {
            Button tile = new Button(op);
            styleStaticTile(tile, COLOR_OP_ACTIVE, COLOR_OP_TEXT);
            tile.setOnAction(e -> appendToken(op, null));
            row.getChildren().add(tile);
        }
        return row;
    }

    /** Paréntesis, borrar último, reiniciar todo y botón de resultado ("="). */
    private HBox buildControlsRow() {
        HBox row = new HBox(TILE_GAP);

        Button open = new Button("(");
        styleStaticTile(open, COLOR_CTRL_ACTIVE, COLOR_CTRL_TEXT);
        open.setOnAction(e -> appendToken("(", null));

        Button close = new Button(")");
        styleStaticTile(close, COLOR_CTRL_ACTIVE, COLOR_CTRL_TEXT);
        close.setOnAction(e -> appendToken(")", null));

        Button backspace = new Button("\u232B"); // ⌫
        styleStaticTile(backspace, COLOR_CTRL_ACTIVE, COLOR_CTRL_TEXT);
        backspace.setOnAction(e -> removeLastToken());

        Button reset = new Button("\u21BA"); // ↺
        styleStaticTile(reset, COLOR_CTRL_ACTIVE, COLOR_CTRL_TEXT);
        reset.setOnAction(e -> clearExpression());

        Button equals = new Button("=");
        styleStaticTile(equals, COLOR_EQUALS_BG, COLOR_EQUALS_TEXT);
        equals.setOnAction(e -> evaluateExpression());

        row.getChildren().addAll(open, close, backspace, reset, equals);
        return row;
    }

    // ---------------------------------------------------------------
    // Construcción de la expresión
    // ---------------------------------------------------------------

    private void onNumberClicked(int index, int value, ToggleButton tile) {
        if (used[index] || tile.isSelected()) return; // ya resuelto, o ya incluido en la expresión actual
        appendToken(String.valueOf(value), tile);
        tile.setSelected(true);
        tile.setStyle(tileStyle(COLOR_NUM_INCLUDED, COLOR_NUM_TEXT));
    }

    private void appendToken(String text, ToggleButton sourceTile) {
        expression.add(new Token(text, sourceTile));
        refreshWorkzoneText();
    }

    private void removeLastToken() {
        if (expression.isEmpty()) return;
        Token last = expression.remove(expression.size() - 1);
        if (last.sourceTile != null) {
            int idx = numberButtons.indexOf(last.sourceTile);
            last.sourceTile.setSelected(false);
            if (idx >= 0 && !used[idx]) {
                last.sourceTile.setStyle(tileStyle(COLOR_NUM_ACTIVE, COLOR_NUM_TEXT));
            }
        }
        refreshWorkzoneText();
    }

    /** Reinicia la expresión actual (no afecta números ya resueltos en la tabla de progreso). */
    private void clearExpression() {
        for (Token t : expression) {
            if (t.sourceTile != null) {
                int idx = numberButtons.indexOf(t.sourceTile);
                t.sourceTile.setSelected(false);
                if (idx >= 0 && !used[idx]) {
                    t.sourceTile.setStyle(tileStyle(COLOR_NUM_ACTIVE, COLOR_NUM_TEXT));
                }
            }
        }
        expression.clear();
        refreshWorkzoneText();
        applyWorkzoneStyle(COLOR_WORKZONE_BG, COLOR_LABEL);
    }

    private void refreshWorkzoneText() {
        if (expression.isEmpty()) {
            workzoneLabel.setText("Toca números, operadores o paréntesis");
            applyWorkzoneStyle(COLOR_WORKZONE_BG, COLOR_LABEL);
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (Token t : expression) sb.append(t.text).append(' ');
        workzoneLabel.setText(sb.toString().trim());
        applyWorkzoneStyle(COLOR_WORKZONE_BG, "#2C2C2A");
    }

    // ---------------------------------------------------------------
    // Evaluación ("=")
    // ---------------------------------------------------------------

    private void evaluateExpression() {
        if (expression.isEmpty()) return;

        List<String> tokens = new ArrayList<>();
        for (Token t : expression) tokens.add(t.text);

        Double result;
        try {
            result = new ExpressionParser(tokens).parse();
        } catch (RuntimeException ex) {
            result = null; // expresión inválida (paréntesis sin cerrar, división no exacta, etc.)
        }

        String exprText = workzoneLabel.getText();

        if (result != null) {
            workzoneLabel.setText(exprText + " = " + formatResult(result) + "  \u2713");
            applyWorkzoneStyle(COLOR_WORKZONE_OK, COLOR_TEXT_OK);

            for (Token t : expression) {
                if (t.sourceTile != null) markUsed(t.sourceTile);
            }
            markProgress(result);
            expression.clear();
        } else {
            workzoneLabel.setText(exprText + "  \u2717");
            applyWorkzoneStyle(COLOR_WORKZONE_ERR, COLOR_TEXT_ERR);
        }
    }

    private void markProgress(double result) {
        int value = (int) Math.round(result);
        if (Math.abs(result - value) > 1e-9) return; // no es un entero exacto
        if (value < 1 || value > 10) return;
        if (solved[value]) return;

        solved[value] = true;
        Label row = progressRows[value];
        row.setText(workzoneLabel.getText().replace("  \u2713", ""));
        row.setTextFill(Color.web(COLOR_TEXT_OK));
        row.setStyle("-fx-background-color: " + COLOR_WORKZONE_OK + "; -fx-background-radius: 10 0 0 10;");
    }

    private String formatResult(double value) {
        return (value == Math.floor(value)) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private void markUsed(ToggleButton tile) {
        int idx = numberButtons.indexOf(tile);
        if (idx >= 0) used[idx] = true;
        tile.setStyle(tileStyle(COLOR_NUM_USED, COLOR_NUM_TEXT));
        tile.setOpacity(0.55);
        tile.setDisable(true);
    }

    // ---------------------------------------------------------------
    // Parser de expresiones: soporta + - × ÷ y paréntesis, con precedencia
    // ---------------------------------------------------------------

    private static class ExpressionParser {
        private final List<String> tokens;
        private int pos = 0;

        ExpressionParser(List<String> tokens) { this.tokens = tokens; }

        double parse() {
            double result = parseExpression();
            if (pos != tokens.size()) throw new RuntimeException("Tokens sobrantes");
            return result;
        }

        // expression = term (( + | - ) term)*
        private double parseExpression() {
            double value = parseTerm();
            while (pos < tokens.size() && ("+".equals(tokens.get(pos)) || "\u2212".equals(tokens.get(pos)))) {
                String op = tokens.get(pos++);
                double rhs = parseTerm();
                value = "+".equals(op) ? value + rhs : value - rhs;
            }
            return value;
        }

        // term = factor (( × | ÷ ) factor)*
        private double parseTerm() {
            double value = parseFactor();
            while (pos < tokens.size() && ("\u00D7".equals(tokens.get(pos)) || "\u00F7".equals(tokens.get(pos)))) {
                String op = tokens.get(pos++);
                double rhs = parseFactor();
                if ("\u00D7".equals(op)) {
                    value = value * rhs;
                } else {
                    if (rhs == 0) throw new RuntimeException("División entre cero");
                    value = value / rhs;
                }
            }
            return value;
        }

        // factor = NUMBER | '(' expression ')'
        private double parseFactor() {
            if (pos >= tokens.size()) throw new RuntimeException("Expresión incompleta");
            String tok = tokens.get(pos);
            if ("(".equals(tok)) {
                pos++;
                double value = parseExpression();
                if (pos >= tokens.size() || !")".equals(tokens.get(pos))) throw new RuntimeException("Falta ')'");
                pos++;
                return value;
            }
            try {
                double value = Double.parseDouble(tok);
                pos++;
                return value;
            } catch (NumberFormatException ex) {
                throw new RuntimeException("Token inesperado: " + tok);
            }
        }
    }

    // ---------------------------------------------------------------
    // Estilos
    // ---------------------------------------------------------------

    private void styleTile(ToggleButton tile, String bg, String textColor) {
        tile.setPrefSize(TILE_SIZE, TILE_SIZE);
        tile.setFont(Font.font("System", FontWeight.BOLD, 24));
        tile.setStyle(tileStyle(bg, textColor));
    }

    private void styleStaticTile(Button tile, String bg, String textColor) {
        tile.setPrefSize(TILE_SIZE, TILE_SIZE);
        tile.setFont(Font.font("System", FontWeight.BOLD, 24));
        tile.setStyle(tileStyle(bg, textColor));
    }

    private String tileStyle(String bg, String textColor) {
        return "-fx-background-color: " + bg + "; -fx-background-radius: 14; -fx-text-fill: " + textColor + ";";
    }

    private void applyWorkzoneStyle(String bg, String textColor) {
        workzoneBox.setStyle(
                "-fx-background-color: " + bg + ";" +
                "-fx-background-radius: 14;" +
                "-fx-border-color: #A9A79E;" +
                "-fx-border-radius: 14;" +
                "-fx-border-width: 1.5;"
        );
        workzoneLabel.setTextFill(Color.web(textColor));
    }

    public static void main(String[] args) {
        launch(args);
    }
}