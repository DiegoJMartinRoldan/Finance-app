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
            System.out.println("Inicializando la base de datos...");
            Database.initialize();
            System.out.println("Base de datos Inicializada correctamente");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
            Scene scene = new Scene(loader.load(), 700, 450);

            stage.setTitle("Finance App");
            stage.setScene(scene);
            stage.show();
            var txService = new org.financeapp.services.FinanceTransactionService(
                    new org.financeapp.data.dao.FinanceTransactionDao()
            );

            // Ejemplo: gasto en la cuenta 1, categoría 1
            txService.create("EXPENSE", 1, null, 1, 12.50, java.time.LocalDate.now(), "Prueba café");


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("La base de datos no inicia correctamente");
        }



    }

    public static void main(String[] args) {
        launch(args);
    }
}