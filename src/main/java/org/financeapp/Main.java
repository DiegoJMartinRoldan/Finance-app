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

            stage.setTitle("Finance App");
            stage.setScene(scene);
            stage.show();
            var txService = new org.financeapp.services.FinanceTransactionService(
                    new org.financeapp.data.dao.FinanceTransactionDao()
            );


        } catch (Exception e) {
            throw new RuntimeException("La base de datos no inicia correctamente");
        }



    }

    public static void main(String[] args) {
        launch(args);
    }
}