package org.financeapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.financeapp.data.db.Database;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            Database.initialize();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Scene scene = new Scene(loader.load(), 700, 450);

            scene.getStylesheets().add(
                    getClass().getResource("/styles/style.css").toExternalForm()
            );

            stage.setTitle("Finance App");
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al iniciar la aplicación.", e);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}