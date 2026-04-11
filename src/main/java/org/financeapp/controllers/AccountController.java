package org.financeapp.controllers;

import java.util.List;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.financeapp.domain.Account;
import org.financeapp.services.AccountService;
import org.financeapp.services.ServiceException;
import org.financeapp.services.SummaryService;

public class AccountController {

  // Contenedor dinámico de las tarjetas de cuentas.
  @FXML private VBox accountsContainer;

  private AccountService service;
  private SummaryService summaryService;

  // Callback para actualizar los datos
  private Runnable onDataChanged;

  // Para guardar la cuenta seleccionada en la vista
  private Account selectedAccount;

  public void setOnDataChanged(Runnable onDataChanged) {
    this.onDataChanged = onDataChanged;
  }

  @FXML
  public void initialize() {
    service = new AccountService();
    summaryService = new SummaryService();
    refresh();
  }

  @FXML
  private void onAdd() {

    // Abre el diálogo con el usuario.
    AccountFormData form = showAccountDialog(null);

    // Si el usuario cancela, volver atrás.
    if (form == null) {
      return;
    }

    // Crear cuenta
    try {
      service.create(
              form.name,
              form.type,
              Double.parseDouble(form.initialBalance.replace(",", "."))
      );
      refresh();
      if (onDataChanged != null) {
        onDataChanged.run();
      }
    } catch (Exception e) {
      showError("No se pudo crear la cuenta.");
    }
  }

  // Editar cuenta
  @FXML
  private void onEdit() {
    if (selectedAccount == null) {
      showError("Selecciona una cuenta");
      return;
    }

    AccountFormData form = showAccountDialog(selectedAccount);

    if (form == null) {
      return;
    }

    try {
      service.update(
              selectedAccount.getId(),
              form.name,
              form.type,
              Double.parseDouble(form.initialBalance.replace(",", "."))
      );
      refresh();
      if (onDataChanged != null) {
        onDataChanged.run();
      }
    } catch (Exception e) {
      showError("No se pudo actualizar la cuenta.");
    }
  }

  // ELiminar cuenta
  @FXML
  private void onDelete() {
    if (selectedAccount == null) {
      showError("Selecciona una cuenta");
      return;
    }

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Confirmar eliminación");
    confirm.setHeaderText("Eliminar cuenta");
    confirm.setContentText("¿Seguro que quieres eliminar \"" + selectedAccount.getName() + "\"?");

    ButtonType deleteButton = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButton = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
    confirm.getButtonTypes().setAll(deleteButton, cancelButton);

    Optional<ButtonType> result = confirm.showAndWait();
    if (result.isEmpty() || result.get() != deleteButton) {
      return;
    }

    try {
      service.delete(selectedAccount.getId());
      selectedAccount = null;
      refresh();
      if (onDataChanged != null) {
        onDataChanged.run();
      }
    } catch (ServiceException e) {
      showError(e.getMessage());
    }
  }

  // REfrescar cuenta
  public void refresh() {
    try {
      List<Account> accounts = service.getAll();
      accountsContainer.getChildren().clear();

      for (Account account : accounts) {
        accountsContainer.getChildren().add(createAccountCard(account));
      }

    } catch (ServiceException e) {
      showError(e.getMessage());
    }
  }
  //Metodo encargado de construir la tarjeta JavaFX

  private VBox createAccountCard(Account account) throws ServiceException {
    var summary = summaryService.getSummaryByAccount(account.getId());

    // nombre
    Label nameLabel = new Label(account.getName());
    nameLabel.getStyleClass().add("account-card-name");

    // tipo
    Label typeLabel = new Label(prettyType(account.getType()));
    typeLabel.getStyleClass().add("account-card-type");

    // balance
    Label balanceLabel = new Label(formatAmount(summary.getCurrentBalance()));
    balanceLabel.getStyleClass().add("account-card-balance");

    Label balanceTextLabel = new Label("Saldo actual");
    balanceTextLabel.getStyleClass().add("account-card-balance-label");

    // ingreso
    Label incomeLabel = new Label("+ " + formatAmount(summary.getTotalIncome()));
    incomeLabel.getStyleClass().add("account-card-income");

    // gasto
    Label expenseLabel = new Label("- " + formatAmount(summary.getTotalExpense()));
    expenseLabel.getStyleClass().add("account-card-expense");

    // transferencias
    Label transferLabel = new Label("⇄ " + formatSignedAmount(summary.getNetTransfers()));
    if (summary.getNetTransfers() > 0.009) {
      transferLabel.getStyleClass().add("account-card-transfer-positive");
    } else if (summary.getNetTransfers() < -0.009) {
      transferLabel.getStyleClass().add("account-card-transfer-negative");
    } else {
      transferLabel.getStyleClass().add("account-card-transfer-neutral");
    }

    // Box que contiene las cuentas
    HBox totalsBox = new HBox(20, incomeLabel, expenseLabel, transferLabel);
    totalsBox.getStyleClass().add("account-card-totals");

    VBox box = new VBox(2, nameLabel, typeLabel, balanceLabel, balanceTextLabel, totalsBox);
    box.getStyleClass().add("account-card");
    box.setMaxWidth(Double.MAX_VALUE);

    // Validación sobre la cuenta seleccionada
    if (selectedAccount != null && selectedAccount.getId() == account.getId()) {
      box.getStyleClass().add("account-card-selected");
    }

    // Cambio en la selección con el ratón de alguna de las cuentas
    box.setOnMouseClicked(event -> {
      selectedAccount = account;
      refresh();
    });

    return box;
  }

  // Formulario para crear una cuenta.
  private AccountFormData showAccountDialog(Account account) {
    Dialog<AccountFormData> dialog = new Dialog<>();
    dialog.setTitle(account == null ? "Nueva cuenta" : "Editar cuenta");

    ButtonType saveButton = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButton = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);

    TextField nameField = new TextField();
    ComboBox<String> typeBox = new ComboBox<>();
    TextField balanceField = new TextField();

    typeBox.getItems().addAll("CASH", "BANK", "CARD");

    typeBox.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : prettyType(item));
      }
    });

    typeBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : prettyType(item));
      }
    });

    if (account != null) {
      nameField.setText(account.getName());
      typeBox.setValue(account.getType());
      balanceField.setText(String.valueOf(account.getInitialBalance()));
    } else {
      typeBox.setValue("CASH");
      balanceField.setText("0");
    }

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    grid.add(new Label("Nombre"), 0, 0);
    grid.add(nameField, 1, 0);
    grid.add(new Label("Tipo"), 0, 1);
    grid.add(typeBox, 1, 1);
    grid.add(new Label("Saldo inicial"), 0, 2);
    grid.add(balanceField, 1, 2);

    dialog.getDialogPane().setContent(grid);
    dialog.getDialogPane().setPrefWidth(430);
    dialog.getDialogPane().setMinWidth(430);
    dialog.getDialogPane().setMaxWidth(430);

    dialog.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
    dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

    dialog.setResizable(false);

    dialog.setOnShown(e -> {
      Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
      stage.setWidth(430);
      stage.setHeight(320);
      stage.centerOnScreen();
    });

    dialog.setResultConverter(button -> {
      if (button == saveButton) {
        return new AccountFormData(
                nameField.getText(),
                typeBox.getValue(),
                balanceField.getText()
        );
      }
      return null;
    });

    Optional<AccountFormData> result = dialog.showAndWait();
    return result.orElse(null);
  }

  // Mostrar errores
  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  // Traduccion de los tipos
  private String prettyType(String type) {
    if ("BANK".equalsIgnoreCase(type)) return "Banco";
    if ("CASH".equalsIgnoreCase(type)) return "Efectivo";
    if ("CARD".equalsIgnoreCase(type)) return "Tarjeta";
    return type;
  }

  // Formato monetario establecido en euros.
  private String formatAmount(double amount) {
    return String.format("%.2f €", amount).replace(".", ",");
  }
  // Signo de + o -
  private String formatSignedAmount(double amount) {
    String formatted = formatAmount(Math.abs(amount));
    return amount >= 0 ? "+ " + formatted : "- " + formatted;
  }

  private static class AccountFormData {
    String name;
    String type;
    String initialBalance;

    AccountFormData(String name, String type, String initialBalance) {
      this.name = name;
      this.type = type;
      this.initialBalance = initialBalance;
    }
  }
}