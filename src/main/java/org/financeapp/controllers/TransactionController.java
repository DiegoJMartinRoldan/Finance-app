package org.financeapp.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.financeapp.data.dao.CategoryDao;
import org.financeapp.data.dao.FinanceTransactionDao;
import org.financeapp.domain.Account;
import org.financeapp.domain.Category;
import org.financeapp.domain.FinanceTransaction;
import org.financeapp.services.AccountService;
import org.financeapp.services.CategoryService;
import org.financeapp.services.FinanceTransactionService;
import org.financeapp.services.ServiceException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TransactionController {

    @FXML
    private ListView<FinanceTransaction> list;

    private FinanceTransactionService txService;
    private CategoryService categoryService;
    private AccountService accountService;

    private Map<Integer, String> accountNames = new HashMap<>();
    private Map<Integer, String> categoryNames = new HashMap<>();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        txService = new FinanceTransactionService(new FinanceTransactionDao());
        categoryService = new CategoryService(new CategoryDao());
        accountService = new AccountService();

        list.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(FinanceTransaction item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    String date = item.getDate() != null ? item.getDate().format(DATE_FMT) : "";
                    String desc = item.getDescription() != null ? item.getDescription() : "";

                    String accountName = accountNames.getOrDefault(item.getAccountId(), "Cuenta " + item.getAccountId());
                    String typeName = translateTransactionType(item.getType());

                    String text = date + " | " + typeName + " | " + accountName;

                    if (item.getToAccountId() != null) {
                        String toAccountName = accountNames.getOrDefault(item.getToAccountId(), "Cuenta " + item.getToAccountId());
                        text += " -> " + toAccountName;
                    }

                    if (item.getCategoryId() != null) {
                        String categoryName = categoryNames.getOrDefault(item.getCategoryId(), "Cat " + item.getCategoryId());
                        text += " | " + categoryName;
                    }

                    text += " | " + item.getAmount() + " € | " + desc;

                    setText(text);
                }
            }
        });

        refresh();
    }

    @FXML
    private void onAdd() {
        try {
            List<Account> accounts = accountService.getAll();
            List<Category> categories = categoryService.getAll();

            if (accounts.isEmpty()) {
                showError("Error", "Primero debes crear una cuenta.");
                return;
            }

            if (categories.isEmpty()) {
                showError("Error", "Primero debes crear una categoría.");
                return;
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Nueva transacción");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList("INCOME", "EXPENSE", "TRANSFER"));
            ComboBox<Account> accountBox = new ComboBox<>(FXCollections.observableArrayList(accounts));
            ComboBox<Account> toAccountBox = new ComboBox<>(FXCollections.observableArrayList(accounts));
            ComboBox<Category> categoryBox = new ComboBox<>(FXCollections.observableArrayList(categories));
            TextField amountField = new TextField();
            DatePicker datePicker = new DatePicker(LocalDate.now());
            TextField descriptionField = new TextField();

            typeBox.setPromptText("Selecciona tipo");
            accountBox.setPromptText("Selecciona cuenta");
            toAccountBox.setPromptText("Selecciona cuenta destino");
            categoryBox.setPromptText("Selecciona categoría");
            amountField.setPromptText("Importe");
            descriptionField.setPromptText("Descripción");

            dialog.getDialogPane().setContent(new VBox(
                    10,
                    new Label("Tipo"),
                    typeBox,
                    new Label("Cuenta"),
                    accountBox,
                    new Label("Cuenta destino"),
                    toAccountBox,
                    new Label("Categoría"),
                    categoryBox,
                    new Label("Importe"),
                    amountField,
                    new Label("Fecha"),
                    datePicker,
                    new Label("Descripción"),
                    descriptionField
            ));

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                String selectedType = typeBox.getValue();
                Account selectedAccount = accountBox.getValue();
                Account selectedToAccount = toAccountBox.getValue();
                Category selectedCategory = categoryBox.getValue();

                if (selectedType == null || selectedType.isBlank()) {
                    showError("Error", "Debes seleccionar el tipo de transacción.");
                    return;
                }

                if (selectedAccount == null) {
                    showError("Error", "Debes seleccionar una cuenta.");
                    return;
                }

                Integer toAccountId = null;
                Integer categoryId = null;

                if (selectedType.equals("TRANSFER")) {
                    if (selectedToAccount == null) {
                        showError("Error", "Debes seleccionar una cuenta destino.");
                        return;
                    }
                    toAccountId = selectedToAccount.getId();
                } else {
                    if (selectedCategory == null) {
                        showError("Error", "Debes seleccionar una categoría.");
                        return;
                    }
                    categoryId = selectedCategory.getId();
                }

                double amount = Double.parseDouble(amountField.getText().replace(",", "."));
                LocalDate date = datePicker.getValue();
                String description = descriptionField.getText();

                txService.create(
                        selectedType,
                        selectedAccount.getId(),
                        toAccountId,
                        categoryId,
                        amount,
                        date,
                        description
                );

                refresh();
            }

        } catch (NumberFormatException e) {
            showError("Error", "El importe debe ser un número válido.");
        } catch (ServiceException e) {
            showError("Error", e.getMessage());
        }
    }

    @FXML
    private void onEdit() {
        FinanceTransaction selected = list.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Error", "Selecciona una transacción para editar.");
            return;
        }

        try {
            List<Account> accounts = accountService.getAll();
            List<Category> categories = categoryService.getAll();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Editar transacción");
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList("INCOME", "EXPENSE", "TRANSFER"));
            ComboBox<Account> accountBox = new ComboBox<>(FXCollections.observableArrayList(accounts));
            ComboBox<Account> toAccountBox = new ComboBox<>(FXCollections.observableArrayList(accounts));
            ComboBox<Category> categoryBox = new ComboBox<>(FXCollections.observableArrayList(categories));
            TextField amountField = new TextField(String.valueOf(selected.getAmount()));
            DatePicker datePicker = new DatePicker(selected.getDate());
            TextField descriptionField = new TextField(selected.getDescription());

            typeBox.setValue(selected.getType());

            for (Account account : accounts) {
                if (account.getId() == selected.getAccountId()) {
                    accountBox.setValue(account);
                    break;
                }
            }

            if (selected.getToAccountId() != null) {
                for (Account account : accounts) {
                    if (account.getId() == selected.getToAccountId()) {
                        toAccountBox.setValue(account);
                        break;
                    }
                }
            }

            if (selected.getCategoryId() != null) {
                for (Category category : categories) {
                    if (category.getId() == selected.getCategoryId()) {
                        categoryBox.setValue(category);
                        break;
                    }
                }
            }

            dialog.getDialogPane().setContent(new VBox(
                    10,
                    new Label("Tipo"),
                    typeBox,
                    new Label("Cuenta"),
                    accountBox,
                    new Label("Cuenta destino"),
                    toAccountBox,
                    new Label("Categoría"),
                    categoryBox,
                    new Label("Importe"),
                    amountField,
                    new Label("Fecha"),
                    datePicker,
                    new Label("Descripción"),
                    descriptionField
            ));

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                String selectedType = typeBox.getValue();
                Account selectedAccount = accountBox.getValue();
                Account selectedToAccount = toAccountBox.getValue();
                Category selectedCategory = categoryBox.getValue();

                if (selectedType == null || selectedType.isBlank()) {
                    showError("Error", "Debes seleccionar el tipo de transacción.");
                    return;
                }

                if (selectedAccount == null) {
                    showError("Error", "Debes seleccionar una cuenta.");
                    return;
                }

                Integer toAccountId = null;
                Integer categoryId = null;

                if (selectedType.equals("TRANSFER")) {
                    if (selectedToAccount == null) {
                        showError("Error", "Debes seleccionar una cuenta destino.");
                        return;
                    }
                    toAccountId = selectedToAccount.getId();
                } else {
                    if (selectedCategory == null) {
                        showError("Error", "Debes seleccionar una categoría.");
                        return;
                    }
                    categoryId = selectedCategory.getId();
                }

                double amount = Double.parseDouble(amountField.getText().replace(",", "."));
                LocalDate date = datePicker.getValue();
                String description = descriptionField.getText();

                txService.update(
                        selected.getId(),
                        selectedType,
                        selectedAccount.getId(),
                        toAccountId,
                        categoryId,
                        amount,
                        date,
                        description
                );

                refresh();
            }

        } catch (NumberFormatException e) {
            showError("Error", "El importe debe ser un número válido.");
        } catch (ServiceException e) {
            showError("Error", e.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        FinanceTransaction selected = list.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("Error", "Selecciona una transacción para eliminar.");
            return;
        }

        try {
            txService.delete(selected.getId());
            refresh();
        } catch (ServiceException e) {
            showError("Error", e.getMessage());
        }
    }

    private void refresh() {
        try {
            List<Account> accounts = accountService.getAll();
            List<Category> categories = categoryService.getAll();
            List<FinanceTransaction> transactions = txService.getAll();

            accountNames.clear();
            for (Account account : accounts) {
                accountNames.put(account.getId(), account.getName());
            }

            categoryNames.clear();
            for (Category category : categories) {
                categoryNames.put(category.getId(), category.getName());
            }

            list.setItems(FXCollections.observableArrayList(transactions));
            list.refresh();
        } catch (ServiceException e) {
            showError("Error", e.getMessage());
        }
    }
    private String translateTransactionType(String type) {
        if (type == null) {
            return "";
        }

        return switch (type) {
            case "INCOME" -> "Ingreso";
            case "EXPENSE" -> "Gasto";
            case "TRANSFER" -> "Transferencia";
            default -> type;
        };
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}