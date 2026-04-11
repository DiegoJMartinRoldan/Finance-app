package org.financeapp.controllers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.financeapp.data.dao.CategoryDao;
import org.financeapp.data.dao.FinanceTransactionDao;
import org.financeapp.domain.Account;
import org.financeapp.domain.Category;
import org.financeapp.domain.FinanceTransaction;
import org.financeapp.services.AccountService;
import org.financeapp.services.CategoryService;
import org.financeapp.services.FinanceTransactionService;
import org.financeapp.services.ServiceException;

public class TransactionController {

    @FXML private VBox transactionsContainer;

    private FinanceTransactionService txService;
    private CategoryService categoryService;
    private AccountService accountService;

    private final Map<Integer, String> accountNames = new HashMap<>();
    private final Map<Integer, String> categoryNames = new HashMap<>();

    private Runnable onDataChanged;
    private FinanceTransaction selectedTransaction;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    @FXML
    public void initialize() {
        txService = new FinanceTransactionService(new FinanceTransactionDao());
        categoryService = new CategoryService(new CategoryDao());
        accountService = new AccountService();
        refresh();
    }

    @FXML
    private void onAdd() {
        try {
            List<Account> accounts = accountService.getAll();
            List<Category> categories = categoryService.getAll();

            if (accounts.isEmpty()) {
                showError("Primero debes crear una cuenta.");
                return;
            }

            TransactionFormData form = showTransactionDialog(null, accounts, categories);

            if (form == null) {
                return;
            }

            txService.create(
                    form.type,
                    form.accountId,
                    form.toAccountId,
                    form.categoryId,
                    form.amount,
                    form.date,
                    form.description
            );

            refresh();
            if (onDataChanged != null) {
                onDataChanged.run();
            }

        } catch (NumberFormatException e) {
            showError("El importe debe ser un número válido.");
        } catch (ServiceException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onEdit() {
        if (selectedTransaction == null) {
            showError("Selecciona una transacción");
            return;
        }

        try {
            List<Account> accounts = accountService.getAll();
            List<Category> categories = categoryService.getAll();

            TransactionFormData form = showTransactionDialog(selectedTransaction, accounts, categories);

            if (form == null) {
                return;
            }

            txService.update(
                    selectedTransaction.getId(),
                    form.type,
                    form.accountId,
                    form.toAccountId,
                    form.categoryId,
                    form.amount,
                    form.date,
                    form.description
            );

            refresh();
            if (onDataChanged != null) {
                onDataChanged.run();
            }

        } catch (NumberFormatException e) {
            showError("El importe debe ser un número válido.");
        } catch (ServiceException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        if (selectedTransaction == null) {
            showError("Selecciona una transacción");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminación");
        confirm.setHeaderText("Eliminar transacción");
        confirm.setContentText("¿Seguro que quieres eliminar la transacción seleccionada?");

        ButtonType deleteButton = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(deleteButton, cancelButton);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != deleteButton) {
            return;
        }

        try {
            txService.delete(selectedTransaction.getId());
            selectedTransaction = null;
            refresh();
            if (onDataChanged != null) {
                onDataChanged.run();
            }
        } catch (ServiceException e) {
            showError(e.getMessage());
        }
    }

    public void refresh() {
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

            transactionsContainer.getChildren().clear();

            for (FinanceTransaction transaction : transactions) {
                transactionsContainer.getChildren().add(createTransactionCard(transaction));
            }

        } catch (ServiceException e) {
            showError(e.getMessage());
        }
    }

    private VBox createTransactionCard(FinanceTransaction transaction) {
        String date = transaction.getDate() != null ? transaction.getDate().format(DATE_FMT) : "";
        String description = transaction.getDescription() != null ? transaction.getDescription().trim() : "";

        String accountName = accountNames.getOrDefault(
                transaction.getAccountId(),
                "Cuenta " + transaction.getAccountId()
        );

        Label amountLabel = new Label(buildAmountText(transaction));
        amountLabel.getStyleClass().add("transaction-card-amount");
        amountLabel.getStyleClass().add(resolveAmountStyleClass(transaction));

        Label typeLabel = new Label(translateTransactionType(transaction.getType()));
        typeLabel.getStyleClass().add("transaction-card-type");

        String mainText;
        String metaText;

        if ("TRANSFER".equalsIgnoreCase(transaction.getType())) {
            String toAccountName = transaction.getToAccountId() != null
                    ? accountNames.getOrDefault(transaction.getToAccountId(), "Cuenta " + transaction.getToAccountId())
                    : "Sin destino";

            mainText = accountName + " → " + toAccountName;
            metaText = date;
        } else {
            String categoryName = transaction.getCategoryId() != null
                    ? categoryNames.getOrDefault(transaction.getCategoryId(), "Cat " + transaction.getCategoryId())
                    : "Sin categoría";

            mainText = categoryName;
            metaText = accountName + " · " + date;
        }

        Label mainLabel = new Label(mainText);
        mainLabel.getStyleClass().add("transaction-card-name");

        Label metaLabel = new Label(metaText);
        metaLabel.getStyleClass().add("transaction-card-meta");

        HBox topRow = new HBox(12, amountLabel, typeLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.getStyleClass().add("transaction-card-top");

        VBox box = new VBox(3);
        box.getChildren().addAll(topRow, mainLabel, metaLabel);

        if (!description.isBlank()) {
            Label descriptionLabel = new Label(description);
            descriptionLabel.getStyleClass().add("transaction-card-description");
            box.getChildren().add(descriptionLabel);
        }

        box.getStyleClass().add("transaction-card");
        box.setMaxWidth(Double.MAX_VALUE);

        if (selectedTransaction != null && selectedTransaction.getId() == transaction.getId()) {
            box.getStyleClass().add("transaction-card-selected");
        }

        box.setOnMouseClicked(event -> {
            selectedTransaction = transaction;
            refresh();
        });

        return box;
    }

    private TransactionFormData showTransactionDialog(
            FinanceTransaction transaction,
            List<Account> accounts,
            List<Category> categories
    ) {
        Dialog<TransactionFormData> dialog = new Dialog<>();
        dialog.setTitle(transaction == null ? "Nueva transacción" : "Editar transacción");

        ButtonType saveButton = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);
        dialog.getDialogPane().setPrefWidth(460);
        dialog.getDialogPane().setMinWidth(460);
        dialog.getDialogPane().setMaxWidth(460);
        dialog.setResizable(false);

        ComboBox<String> typeBox = new ComboBox<>(FXCollections.observableArrayList("INCOME", "EXPENSE", "TRANSFER"));
        ComboBox<Account> accountBox = new ComboBox<>(FXCollections.observableArrayList(accounts));
        ComboBox<Account> toAccountBox = new ComboBox<>(FXCollections.observableArrayList(accounts));
        ComboBox<Category> categoryBox = new ComboBox<>(FXCollections.observableArrayList(categories));
        TextField amountField = new TextField();
        DatePicker datePicker = new DatePicker(LocalDate.now());
        TextField descriptionField = new TextField();

        Label typeLabel = new Label("Tipo");
        Label accountLabel = new Label("Cuenta");
        Label toAccountLabel = new Label("Cuenta destino");
        Label categoryLabel = new Label("Categoría");
        Label amountLabel = new Label("Importe");
        Label dateLabel = new Label("Fecha");
        Label descriptionLabel = new Label("Descripción");

        typeBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : translateTransactionType(item));
            }
        });
        typeBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : translateTransactionType(item));
            }
        });

        accountBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        accountBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        toAccountBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        toAccountBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Account item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        categoryBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        categoryBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });

        if (transaction != null) {
            typeBox.setValue(transaction.getType());
            amountField.setText(String.valueOf(transaction.getAmount()));
            datePicker.setValue(transaction.getDate());
            descriptionField.setText(transaction.getDescription() != null ? transaction.getDescription() : "");

            for (Account account : accounts) {
                if (account.getId() == transaction.getAccountId()) {
                    accountBox.setValue(account);
                    break;
                }
            }

            if (transaction.getToAccountId() != null) {
                for (Account account : accounts) {
                    if (account.getId() == transaction.getToAccountId()) {
                        toAccountBox.setValue(account);
                        break;
                    }
                }
            }

            if (transaction.getCategoryId() != null) {
                for (Category category : categories) {
                    if (category.getId() == transaction.getCategoryId()) {
                        categoryBox.setValue(category);
                        break;
                    }
                }
            }
        } else {
            typeBox.setValue("EXPENSE");
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(typeLabel, 0, 0);
        grid.add(typeBox, 1, 0);

        grid.add(accountLabel, 0, 1);
        grid.add(accountBox, 1, 1);

        grid.add(toAccountLabel, 0, 2);
        grid.add(toAccountBox, 1, 2);

        grid.add(categoryLabel, 0, 3);
        grid.add(categoryBox, 1, 3);

        grid.add(amountLabel, 0, 4);
        grid.add(amountField, 1, 4);

        grid.add(dateLabel, 0, 5);
        grid.add(datePicker, 1, 5);

        grid.add(descriptionLabel, 0, 6);
        grid.add(descriptionField, 1, 6);

        // SOLO CAMBIO IMPORTANTE: DIALOG

        dialog.getDialogPane().setContent(grid);

        dialog.getDialogPane().setPrefWidth(480);
        dialog.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
        dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

        dialog.setResizable(false);

        dialog.setOnShown(e -> {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.setWidth(480);
            stage.setHeight(380); // 🔥 ajustado mejor
            stage.centerOnScreen();
        });

        Runnable updateVisibility = () -> {
            boolean isTransfer = "TRANSFER".equals(typeBox.getValue());

            setRowVisible(toAccountLabel, !isTransfer ? false : true);
            setRowVisible(toAccountBox, !isTransfer ? false : true);

            setRowVisible(categoryLabel, isTransfer ? false : true);
            setRowVisible(categoryBox, isTransfer ? false : true);

            if (isTransfer) {
                categoryBox.setValue(null);
            } else {
                toAccountBox.setValue(null);
            }
        };

        typeBox.valueProperty().addListener((obs, oldValue, newValue) -> updateVisibility.run());
        updateVisibility.run();

        dialog.setResultConverter(button -> {
            if (button != saveButton) {
                return null;
            }

            String type = typeBox.getValue();
            Account account = accountBox.getValue();
            Account toAccount = toAccountBox.getValue();
            Category category = categoryBox.getValue();

            if (type == null || type.isBlank()) {
                showError("Debes seleccionar el tipo de transacción.");
                return null;
            }

            if (account == null) {
                showError("Debes seleccionar una cuenta.");
                return null;
            }

            Integer toAccountId = null;
            Integer categoryId = null;

            if ("TRANSFER".equals(type)) {
                if (toAccount == null) {
                    showError("Debes seleccionar una cuenta destino.");
                    return null;
                }
                if (account.getId() == toAccount.getId()) {
                    showError("La cuenta de origen y destino no pueden ser la misma.");
                    return null;
                }
                toAccountId = toAccount.getId();
            } else {
                if (categories.isEmpty()) {
                    showError("Primero debes crear una categoría.");
                    return null;
                }
                if (category == null) {
                    showError("Debes seleccionar una categoría.");
                    return null;
                }
                categoryId = category.getId();
            }

            double amount;
            try {
                amount = Double.parseDouble(amountField.getText().replace(",", "."));
            } catch (NumberFormatException e) {
                showError("El importe debe ser un número válido.");
                return null;
            }

            LocalDate date = datePicker.getValue();
            String description = descriptionField.getText();

            return new TransactionFormData(
                    type,
                    account.getId(),
                    toAccountId,
                    categoryId,
                    amount,
                    date,
                    description
            );
        });

        Optional<TransactionFormData> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void setRowVisible(Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    private String buildAmountText(FinanceTransaction transaction) {
        String amount = formatAmount(transaction.getAmount());

        return switch (transaction.getType()) {
            case "INCOME" -> "+ " + amount;
            case "EXPENSE" -> "- " + amount;
            case "TRANSFER" -> "⇄ " + amount;
            default -> amount;
        };
    }

    private String resolveAmountStyleClass(FinanceTransaction transaction) {
        return switch (transaction.getType()) {
            case "INCOME" -> "transaction-card-income";
            case "EXPENSE" -> "transaction-card-expense";
            case "TRANSFER" -> "transaction-card-transfer";
            default -> "transaction-card-transfer";
        };
    }

    private String translateTransactionType(String type) {
        if (type == null) return "";

        return switch (type) {
            case "INCOME" -> "Ingreso";
            case "EXPENSE" -> "Gasto";
            case "TRANSFER" -> "Transferencia";
            default -> type;
        };
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String formatAmount(double amount) {
        return String.format("%.2f €", amount).replace(".", ",");
    }

    private static class TransactionFormData {
        String type;
        int accountId;
        Integer toAccountId;
        Integer categoryId;
        double amount;
        LocalDate date;
        String description;

        TransactionFormData(
                String type,
                int accountId,
                Integer toAccountId,
                Integer categoryId,
                double amount,
                LocalDate date,
                String description
        ) {
            this.type = type;
            this.accountId = accountId;
            this.toAccountId = toAccountId;
            this.categoryId = categoryId;
            this.amount = amount;
            this.date = date;
            this.description = description;
        }
    }
}