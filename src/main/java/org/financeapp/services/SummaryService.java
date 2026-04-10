package org.financeapp.services;

import org.financeapp.data.dao.AccountDao;
import org.financeapp.data.dao.FinanceTransactionDao;
import org.financeapp.domain.Account;
import org.financeapp.domain.AccountSummary;
import org.financeapp.domain.GlobalSummary;

import java.util.List;

public class SummaryService {

    private final AccountDao accountDao;
    private final FinanceTransactionDao financeTransactionDao;

    public SummaryService() {
        this.accountDao = new AccountDao();
        this.financeTransactionDao = new FinanceTransactionDao();
    }

    public GlobalSummary getGlobalSummary() throws ServiceException {
        try {
            List<Account> accounts = accountDao.findAll();

            double totalBalance = 0;
            for (Account account : accounts) {
                totalBalance += accountDao.currentBalance(account.getId());
            }

            double totalIncome = accountDao.totalIncome();
            double totalExpense = accountDao.totalExpense();

            return new GlobalSummary(totalBalance, totalIncome, totalExpense);
        } catch (Exception e) {
            throw new ServiceException("No se pudo calcular el resumen global.", e);
        }
    }

    public AccountSummary getSummaryByAccount(int accountId) throws ServiceException {
        try {
            Account account = accountDao.findById(accountId);

            if (account == null) {
                throw new ServiceException("La cuenta no existe.");
            }

            double currentBalance = accountDao.currentBalance(accountId);
            double totalIncome = accountDao.totalIncomeByAccount(accountId);
            double totalExpense = accountDao.totalExpenseByAccount(accountId);

            double incomingTransfers = financeTransactionDao.totalIncomingTransfersByAccount(accountId);
            double outgoingTransfers = financeTransactionDao.totalOutgoingTransfersByAccount(accountId);
            double netTransfers = incomingTransfers - outgoingTransfers;

            return new AccountSummary(
                    account.getId(),
                    account.getName(),
                    currentBalance,
                    totalIncome,
                    totalExpense,
                    netTransfers
            );
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("No se pudo calcular el resumen de la cuenta.", e);
        }
    }

    public List<AccountSummary> getAllAccountSummaries() throws ServiceException {
        try {
            List<Account> accounts = accountDao.findAll();
            List<AccountSummary> summaries = new java.util.ArrayList<>();

            for (Account account : accounts) {
                double currentBalance = accountDao.currentBalance(account.getId());
                double totalIncome = accountDao.totalIncomeByAccount(account.getId());
                double totalExpense = accountDao.totalExpenseByAccount(account.getId());

                double incomingTransfers = financeTransactionDao.totalIncomingTransfersByAccount(account.getId());
                double outgoingTransfers = financeTransactionDao.totalOutgoingTransfersByAccount(account.getId());
                double netTransfers = incomingTransfers - outgoingTransfers;

                summaries.add(new AccountSummary(
                        account.getId(),
                        account.getName(),
                        currentBalance,
                        totalIncome,
                        totalExpense,
                        netTransfers
                ));
            }

            return summaries;
        } catch (Exception e) {
            throw new ServiceException("No se pudieron calcular los resúmenes por cuenta.", e);
        }
    }

    public double getTotalByCategoryId(int categoryId) throws ServiceException {
        if (categoryId <= 0) {
            throw new ServiceException("Categoría inválida.");
        }

        try {
            return financeTransactionDao.totalByCategoryId(categoryId);
        } catch (Exception e) {
            throw new ServiceException("No se pudo calcular el total de la categoría.", e);
        }
    }
}