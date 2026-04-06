package org.financeapp.controllers;

import java.util.List;
import java.util.Optional;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.financeapp.domain.Account;
import org.financeapp.services.AccountService;
import org.financeapp.services.ServiceException;

public class AccountController {

  @FXML private TableView<Account> table;
  @FXML private TableColumn<Account, Integer> colId;
  @FXML private TableColumn<Account, String> colName;
  @FXML private TableColumn<Account, String> colType;
  @FXML private TableColumn<Account, Double> colInitialBalance;

  private AccountService service;

  @FXML
  public void initialize() {

    service = new AccountService();

    colId.setCellValueFactory(
        data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());

    colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));

    colType.setCellValueFactory(data -> {
      String type = data.getValue().getType();

      String translatedType;
      switch (type) {
        case "BANK":
          translatedType = "Banco";
          break;
        case "CASH":
          translatedType = "Efectivo";
          break;
        case "CARD":
          translatedType = "Tarjeta";
          break;
        default:
          translatedType = type;
      }

      return new SimpleStringProperty(translatedType);
    });

    colInitialBalance.setCellValueFactory(data -> {
      try {
        return new SimpleDoubleProperty(service.getCurrentBalance(data.getValue().getId())).asObject();
      } catch (Exception e) {
        return new SimpleDoubleProperty(data.getValue().getInitialBalance()).asObject();
      }
    });

    refresh();
  }

  @FXML
  private void onAdd() {
    AccountFormData form = showAccountDialog(null);

    if (form == null) {
      return;
    }

    try {
      service.create(form.name, form.type, form.initialBalance);
      refresh();
    } catch (ServiceException e) {
      showError(e.getMessage());
    }
  }

  @FXML
  private void onEdit() {
    Account selected = table.getSelectionModel().getSelectedItem();

    if (selected == null) {
      showError("Selecciona una cuenta");
      return;
    }

    AccountFormData form = showAccountDialog(selected);

    if (form == null) {
      return;
    }

    try {
      service.update(selected.getId(), form.name, form.type, form.initialBalance);
      refresh();
    } catch (ServiceException e) {
      showError(e.getMessage());
    }
  }

  @FXML
  private void onDelete() {
    Account selected = table.getSelectionModel().getSelectedItem();

    if (selected == null) {
      showError("Selecciona una cuenta");
      return;
    }

    try {
      service.delete(selected.getId());
      refresh();
    } catch (ServiceException e) {
      showError(e.getMessage());
    }
  }

  private void refresh() {
    try {
      List<Account> accounts = service.getAll();
      table.setItems(FXCollections.observableArrayList(accounts));
      table.refresh();
    } catch (ServiceException e) {
      showError(e.getMessage());
    }
  }

  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private AccountFormData showAccountDialog(Account account) {
    Dialog<AccountFormData> dialog = new Dialog<>();
    dialog.setTitle(account == null ? "Nueva cuenta" : "Editar cuenta");

    ButtonType saveButton = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButton, ButtonType.CANCEL);

    TextField nameField = new TextField();
    ComboBox<String> typeBox = new ComboBox<>();
    TextField balanceField = new TextField();

    typeBox.getItems().addAll("CASH", "BANK", "CARD");

    if (account != null) {
      nameField.setText(account.getName());
      typeBox.setValue(account.getType());
      balanceField.setText(String.valueOf(account.getInitialBalance()));
    } else {
      typeBox.setValue("CASH");
      balanceField.setText("0");
    }

    dialog
        .getDialogPane()
        .setContent(
            new javafx.scene.layout.VBox(
                10,
                new Label("Nombre"),
                nameField,
                new Label("Tipo"),
                typeBox,
                new Label("Saldo inicial"),
                balanceField));

    dialog.setResultConverter(
        button -> {
          if (button == saveButton) {
            try {
              double initialBalance = Double.parseDouble(balanceField.getText());
              return new AccountFormData(nameField.getText(), typeBox.getValue(), initialBalance);
            } catch (NumberFormatException e) {
              showError("El saldo inicial debe ser un número válido");
            }
          }
          return null;
        });

    Optional<AccountFormData> result = dialog.showAndWait();
    return result.orElse(null);
  }

  private static class AccountFormData {
    String name;
    String type;
    double initialBalance;

    AccountFormData(String name, String type, double initialBalance) {
      this.name = name;
      this.type = type;
      this.initialBalance = initialBalance;
    }
  }
}

