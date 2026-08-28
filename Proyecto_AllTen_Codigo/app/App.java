package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        // La aplicación inicia en el menú principal
        Parent root = FXMLLoader.load(
            getClass().getResource("/view/MainMenuView.fxml")
        );
        Scene scene = new Scene(root, 1000, 700);

        stage.setScene(scene);
        stage.setTitle("All Ten");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}