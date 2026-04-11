package org.financeapp.controllers;

import java.util.List;
import java.util.Optional;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.financeapp.data.dao.CategoryDao;
import org.financeapp.domain.Category;
import org.financeapp.services.CategoryService;
import org.financeapp.services.ServiceException;
import org.financeapp.services.SummaryService;

public class CategoryController {

  @FXML private VBox categoriesContainer;

  private CategoryService service;
  private SummaryService summaryService;
  private Runnable onDataChanged;

  private Category selectedCategory;

  public void setOnDataChanged(Runnable onDataChanged) {
    this.onDataChanged = onDataChanged;
  }

  @FXML
  public void initialize() {
    service = new CategoryService(new CategoryDao());
    summaryService = new SummaryService();
    refresh();
  }

  @FXML
  private void onAdd() {
    CategoryFormData form = showCategoryDialog("Nueva categoría", "", "EXPENSE");
    if (form == null) return;

    try {
      service.create(form.name, form.kind);
      refresh();
      if (onDataChanged != null) {
        onDataChanged.run();
      }
    } catch (ServiceException exception) {
      showError("No se pudo crear", exception.getMessage());
    }
  }

  @FXML
  private void onEdit() {
    if (selectedCategory == null) {
      showInfo("Selecciona una categoría para editar.");
      return;
    }

    CategoryFormData form =
            showCategoryDialog("Editar categoría", selectedCategory.getName(), selectedCategory.getKind());
    if (form == null) return;

    try {
      service.update(selectedCategory.getId(), form.name, form.kind);
      refresh();
      if (onDataChanged != null) {
        onDataChanged.run();
      }
    } catch (ServiceException exception) {
      showError("No se pudo actualizar", exception.getMessage());
    }
  }

  @FXML
  private void onDelete() {
    if (selectedCategory == null) {
      showInfo("Selecciona una categoría para eliminar.");
      return;
    }

    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
    confirm.setTitle("Confirmar eliminación");
    confirm.setHeaderText("Eliminar categoría");
    confirm.setContentText("¿Seguro que quieres eliminar \"" + selectedCategory.getName() + "\"?");

    ButtonType deleteButton = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButton = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
    confirm.getButtonTypes().setAll(deleteButton, cancelButton);

    Optional<ButtonType> result = confirm.showAndWait();
    if (result.isEmpty() || result.get() != deleteButton) return;

    try {
      service.delete(selectedCategory.getId());
      selectedCategory = null;
      refresh();
      if (onDataChanged != null) {
        onDataChanged.run();
      }
    } catch (ServiceException e) {
      showError("No se pudo eliminar", e.getMessage());
    }
  }

  public void refresh() {
    try {
      List<Category> categories = service.getAll();
      categoriesContainer.getChildren().clear();

      for (Category category : categories) {
        categoriesContainer.getChildren().add(createCategoryCard(category));
      }
    } catch (ServiceException e) {
      showError("Error cargando categorías", e.getMessage());
    }
  }

  private VBox createCategoryCard(Category category) {
    String prettyKind = prettyKind(category.getKind());

    Label nameLabel = new Label(category.getName());
    nameLabel.getStyleClass().add("category-card-name");

    Label typeLabel = new Label(prettyKind);
    if ("Ingreso".equals(prettyKind)) {
      typeLabel.getStyleClass().addAll("category-card-type", "category-card-type-income");
    } else {
      typeLabel.getStyleClass().addAll("category-card-type", "category-card-type-expense");
    }

    Label totalLabel;
    try {
      double total = summaryService.getTotalByCategoryId(category.getId());

      String totalText;
      if ("Ingreso".equals(prettyKind)) {
        totalText = "Total ingresado: " + formatAmount(total);
      } else {
        totalText = "Total gastado: " + formatAmount(total);
      }

      totalLabel = new Label(totalText);
      totalLabel.getStyleClass().add("category-card-total");
    } catch (ServiceException e) {
      totalLabel = new Label("Total no disponible");
      totalLabel.getStyleClass().add("category-card-total-muted");
    }

    VBox box = new VBox(6, nameLabel, typeLabel, totalLabel);
    box.setMaxWidth(Double.MAX_VALUE);
    box.getStyleClass().add("category-card");

    if (selectedCategory != null && selectedCategory.getId() == category.getId()) {
      box.getStyleClass().add("category-card-selected");
    }

    box.setOnMouseClicked(event -> {
      selectedCategory = category;
      refresh();
    });

    return box;
  }

  private CategoryFormData showCategoryDialog(
          String title, String initialName, String initialKind) {
    Dialog<CategoryFormData> dialog = new Dialog<>();
    dialog.setTitle(title);

    ButtonType saveButton = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
    ButtonType cancelButton = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
    dialog.getDialogPane().getButtonTypes().addAll(saveButton, cancelButton);
    dialog.getDialogPane().setPrefWidth(420);
    dialog.getDialogPane().setMinWidth(420);
    dialog.getDialogPane().setMaxWidth(420);

    TextField nameField = new TextField(initialName);
    ComboBox<String> kindBox = new ComboBox<>();
    kindBox.getItems().addAll("INCOME", "EXPENSE");
    kindBox.setValue(initialKind);

    kindBox.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : prettyKind(item));
      }
    });

    kindBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : prettyKind(item));
      }
    });

    GridPane grid = new GridPane();
    grid.setHgap(10);
    grid.setVgap(10);

    grid.add(new Label("Nombre"), 0, 0);
    grid.add(nameField, 1, 0);
    grid.add(new Label("Tipo"), 0, 1);
    grid.add(kindBox, 1, 1);

    // SOLO CAMBIO IMPORTANTE: DIALOG

    dialog.getDialogPane().setContent(grid);

    dialog.getDialogPane().setPrefWidth(400);
    dialog.getDialogPane().setPrefHeight(Region.USE_COMPUTED_SIZE);
    dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);

    dialog.setResizable(false);

    dialog.setOnShown(e -> {
      Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
      stage.setWidth(400);
      stage.setHeight(260);
      stage.centerOnScreen();
    });

    dialog.setResultConverter(
            btn -> {
              if (btn == saveButton) {
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

  private String prettyKind(String kind) {
    if ("INCOME".equalsIgnoreCase(kind)) return "Ingreso";
    if ("EXPENSE".equalsIgnoreCase(kind)) return "Gasto";
    return kind;
  }

  private String formatAmount(double amount) {
    return String.format("%.2f €", amount).replace(".", ",");
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