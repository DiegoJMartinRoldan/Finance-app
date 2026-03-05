package org.financeapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.financeapp.data.db.Database;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        try {
            System.out.println("Inicializando la base de datos...");
            Database.initialize();
            System.out.println("Base de datos Inicializada correctamente");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("La base de datos no inicia correctamente");
        }

        Label label = new Label("Finance App - FUNCIONA");
        StackPane root = new StackPane(label);

        Scene scene = new Scene(root, 500, 300);

        stage.setTitle("Finance App");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}