package org.financeapp.services;

import org.financeapp.data.dao.AccountDao;
import org.financeapp.domain.Account;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class AccountService {

    // Valores válidos para type
    private static final Set<String> ALLOWED_TYPES = Set.of("CASH", "BANK", "CARD");
    private final AccountDao accountDao;

    public AccountService() {
        this.accountDao = new AccountDao();
    }

    public List<Account> getAll() throws ServiceException {
        try {
            return accountDao.findAll();
        } catch (Exception exception) {
            throw new ServiceException("No se puede encontrar ninguna cuenta", exception);
        }

    }


    public Account getAccountById(int id) throws ServiceException {
        if (id <= 0) {
            throw new ServiceException("Cuent no válida");

        }
        try {
            Account account =  accountDao.findById(id);
            if (account == null) {
                throw  new ServiceException("La cuenta no eexiste");
            }
            return account;
        } catch (Exception exception) {
            throw new ServiceException("No se encuentra una cuenta con este id", exception);
        }

    }

    public void create(String name, String type, double initialBalance) throws ServiceException {
        String newName = normalizeName(name);
        String newType = normalizeType(type);

        validateName(newName);
        validateType(newType);
        validateInitialBalance(initialBalance);

        try {
            accountDao.create(new Account(0, newName, newType, initialBalance));
        } catch (Exception exception) {
            throw new ServiceException("No se pudo crear la cuenta.", exception);
        }
    }

    public void update(int id, String name, String type, double initialBalance) throws ServiceException {
        if (id <= 0) {
            throw new ServiceException("Cuenta inválida.");
        }

        String newName = normalizeName(name);
        String newType = normalizeType(type);

        validateName(newName);
        validateType(newType);
        validateInitialBalance(initialBalance);

        try {
            Account currentAccount = accountDao.findById(id);
            if (currentAccount == null) {
                throw new ServiceException("La cuenta no existe.");
            }

            boolean updatedAccount = accountDao.update(new Account(id, newName, newType, initialBalance));
            if (!updatedAccount) {
                throw new ServiceException("No se pudo actualizar la cuenta.");
            }
        } catch (ServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ServiceException("No se pudo actualizar la cuenta.", exception);
        }
    }


    public void delete(int id) throws ServiceException {
        if (id <= 0) {
            throw new ServiceException("Cuenta inválida.");
        }

        try {
            boolean deletedAccount = accountDao.deleteById(id);
            if (!deletedAccount) {
                throw new ServiceException("La cuenta no existe o ya fue eliminada.");
            }
        } catch (SQLException exception) {
            throw new ServiceException("Error al eliminar la cuenta.", exception);
        } catch (Exception exception) {
            throw new ServiceException("Error al eliminar la cuenta.", exception);
        }
    }

    public double getCurrentBalance(int accountId) throws Exception {
        Account account = accountDao.findById(accountId);

        if (account == null) {
            throw new IllegalArgumentException("Account not found with id: " + accountId);
        }

        double transactionsSum = accountDao.allAccountTransactions(accountId);

        return account.getInitialBalance() + transactionsSum;
    }


    // Vlidaciones
    private String normalizeName(String name) {
        return name == null ? "" : name.trim();
    }

    private String normalizeType(String type) {
        return type == null ? "" : type.trim().toUpperCase();
    }

    private void validateName(String name) throws ServiceException {
        if (name.isBlank()) {
            throw new ServiceException("El nombre de la cuenta no puede estar vacio");
        }
        if (name.length() > 50) {
            throw new ServiceException("El nombre de la cuenta es demasiado largo");
        }
    }

    private void validateType(String type) throws ServiceException {
        if (!ALLOWED_TYPES.contains(type)) {
            throw new ServiceException("Tipo de cuenta inválido.");
        }
    }

    private void validateInitialBalance(double initialBalance) throws ServiceException {
        if (initialBalance < 0) {
            throw new ServiceException("El saldo inicial no puede ser negativo.");
        }
    }




}