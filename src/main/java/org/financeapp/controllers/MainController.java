package org.financeapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.financeapp.domain.GlobalSummary;
import org.financeapp.services.ServiceException;
import org.financeapp.services.SummaryService;

public class MainController {

    @FXML private Label labelTotalBalance;
    @FXML private Label labelotalIncome;
    @FXML private Label labelTotalExpense;

    @FXML private Button buttontAccounts;
    @FXML private Button buttontTransactions;
    @FXML private Button buttontCategories;

    @FXML private VBox accountsPage;
    @FXML private VBox transactionsPage;
    @FXML private VBox categoriesPage;

    @FXML private AccountController accountsViewController;
    @FXML private TransactionController transactionsViewController;
    @FXML private CategoryController categoriesViewController;

    private SummaryService summaryService;

    @FXML
    public void initialize() {
        summaryService = new SummaryService();

        Runnable refreshAllAction = this::refreshAll;

        if (accountsViewController != null) {
            accountsViewController.setOnDataChanged(refreshAllAction);
        }

        if (transactionsViewController != null) {
            transactionsViewController.setOnDataChanged(refreshAllAction);
        }

        if (categoriesViewController != null) {
            categoriesViewController.setOnDataChanged(refreshAllAction);
        }

        refreshAll();
        showAccounts();
    }

    public void refreshAll() {
        refreshSummary();

        if (accountsViewController != null) {
            accountsViewController.refresh();
        }

        if (transactionsViewController != null) {
            transactionsViewController.refresh();
        }

        if (categoriesViewController != null) {
            categoriesViewController.refresh();
        }
    }

    @FXML
    private void showAccounts() {
        setActivePage(accountsPage);
        setActiveButton(buttontAccounts);
    }

    @FXML
    private void showTransactions() {
        setActivePage(transactionsPage);
        setActiveButton(buttontTransactions);
    }

    @FXML
    private void showCategories() {
        setActivePage(categoriesPage);
        setActiveButton(buttontCategories);
    }

    private void setActivePage(VBox page) {
        accountsPage.setVisible(false);
        accountsPage.setManaged(false);

        transactionsPage.setVisible(false);
        transactionsPage.setManaged(false);

        categoriesPage.setVisible(false);
        categoriesPage.setManaged(false);

        page.setVisible(true);
        page.setManaged(true);
    }

    private void setActiveButton(Button activeButton) {
        buttontAccounts.getStyleClass().remove("nav-button-active");
        buttontTransactions.getStyleClass().remove("nav-button-active");
        buttontCategories.getStyleClass().remove("nav-button-active");

        if (!buttontAccounts.getStyleClass().contains("nav-button")) {
            buttontAccounts.getStyleClass().add("nav-button");
        }
        if (!buttontTransactions.getStyleClass().contains("nav-button")) {
            buttontTransactions.getStyleClass().add("nav-button");
        }
        if (!buttontCategories.getStyleClass().contains("nav-button")) {
            buttontCategories.getStyleClass().add("nav-button");
        }

        activeButton.getStyleClass().add("nav-button-active");
    }

    private void refreshSummary() {
        try {
            GlobalSummary summary = summaryService.getGlobalSummary();

            double totalBalance = summary.getTotalBalance();

            labelTotalBalance.setText(formatAmount(totalBalance));
            labelotalIncome.setText(formatAmount(summary.getTotalIncome()));
            labelTotalExpense.setText(formatAmount(summary.getTotalExpense()));

            labelTotalBalance.getStyleClass().removeAll(
                    "summary-balance-positive",
                    "summary-balance-negative",
                    "summary-balance-neutral"
            );

            if (totalBalance > 0.009) {
                labelTotalBalance.getStyleClass().add("summary-balance-positive");
            } else if (totalBalance < -0.009) {
                labelTotalBalance.getStyleClass().add("summary-balance-negative");
            } else {
                labelTotalBalance.getStyleClass().add("summary-balance-neutral");
            }

        } catch (ServiceException e) {
            labelTotalBalance.setText("Error");
            labelotalIncome.setText("Error");
            labelTotalExpense.setText("Error");
        }
    }

    private String formatAmount(double amount) {
        return String.format("%.2f €", amount).replace(".", ",");
    }
}