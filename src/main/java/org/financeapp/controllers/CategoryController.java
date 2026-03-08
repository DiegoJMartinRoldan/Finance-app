package org.financeapp.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.financeapp.data.dao.CategoryDao;
import org.financeapp.domain.Category;
import org.financeapp.services.CategoryService;
import org.financeapp.services.ServiceException;

import java.util.List;
import java.util.Optional;

public class CategoryController {

    @FXML private TableView<Category> table;
    @FXML private TableColumn<Category, Integer> colId;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TableColumn<Category, String> colKind;

    private CategoryService service;

    @FXML
    public void initialize() {
        service = new CategoryService(new CategoryDao());

        // Columnas (forma explícita y clara)
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colKind.setCellValueFactory(data -> new SimpleStringProperty(prettyKind(data.getValue().getKind())));

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Doble click para editar
        table.setRowFactory(tv -> {
            TableRow<Category> row = new TableRow<>();
            row.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !row.isEmpty()) {
                    onEdit();
                }
            });
            return row;
        });

        refresh();
    }

    @FXML
    private void onAdd() {
        CategoryFormData form = showCategoryDialog("Nueva categoría", "", "EXPENSE");
        if (form == null) return;

        try {
            service.create(form.name, form.kind);
            refresh();
        } catch (ServiceException exception) {
            showError("No se pudo crear", exception.getMessage());
        }
    }

    @FXML
    private void onEdit() {
        Category selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Selecciona una categoría para editar.");
            return;
        }

        CategoryFormData form = showCategoryDialog("Editar categoría", selected.getName(), selected.getKind());
        if (form == null) return;

        try {
            service.update(selected.getId(), form.name, form.kind);
            refresh();
        } catch (ServiceException exceptione) {
            showError("No se pudo actualizar", exceptione.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        Category selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showInfo("Selecciona una categoría para eliminar.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar eliminacion");
        confirm.setHeaderText("Eliminar categoría");
        confirm.setContentText("Seguro que quieres eliminar \"" + selected.getName() + "\"?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            service.delete(selected.getId());
            refresh();
        } catch (ServiceException e) {
            showError("No se pudo eliminar", e.getMessage());
        }
    }

    private void refresh() {
        try {
            List<Category> categories = service.getAll();
            table.setItems(FXCollections.observableArrayList(categories));
        } catch (ServiceException e) {
            showError("Error cargando categorías", e.getMessage());
        }
    }

    private CategoryFormData showCategoryDialog(String title, String initialName, String initialKind) {
        Dialog<CategoryFormData> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField nameField = new TextField(initialName);
        ComboBox<String> kindBox = new ComboBox<>();
        kindBox.getItems().addAll("INCOME", "EXPENSE");
        kindBox.setValue(initialKind);

        // Layout simple del diálogo
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Tipo:"), 0, 1);
        grid.add(kindBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == ButtonType.OK) {
                return new CategoryFormData(nameField.getText(), kindBox.getValue());
            }
            return null;
        });

        return dialog.showAndWait().orElse(null);
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Info");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // Idiomas
    private String prettyKind(String kind) {
        if ("INCOME".equalsIgnoreCase(kind)) return "Ingreso";
        if ("EXPENSE".equalsIgnoreCase(kind)) return "Gasto";
        return kind;
    }

    private static class CategoryFormData {
        final String name;
        final String kind;

        CategoryFormData(String name, String kind) {
            this.name = name;
            this.kind = kind;
        }
    }
}