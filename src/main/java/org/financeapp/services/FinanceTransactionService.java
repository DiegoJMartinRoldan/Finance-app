package org.financeapp.services;

import org.financeapp.data.dao.FinanceTransactionDao;
import org.financeapp.domain.FinanceTransaction;

import java.time.LocalDate;
import java.util.List;

public class FinanceTransactionService {

    private final FinanceTransactionDao financeTransactionDao;

    public FinanceTransactionService(FinanceTransactionDao dao) {

        this.financeTransactionDao = dao;
    }




    public List<FinanceTransaction> getAll() throws ServiceException {
        try {
            return financeTransactionDao.findAll();
        } catch (Exception e) {
            throw new ServiceException("No se pudieron cargar las transacciones.", e);
        }
    }

    public void create(String type, int accountId, Integer toAccountId, Integer categoryId, double amount, LocalDate date, String description)
            throws ServiceException {

        validate(type, accountId, toAccountId, categoryId, amount, date);

        String desc = normalizeDescription(description);

        FinanceTransaction transaction = new FinanceTransaction(0, type, accountId, toAccountId, categoryId, amount, date, desc);

        try {
            financeTransactionDao.insert(transaction);
        } catch (Exception e) {
            throw new ServiceException("No se pudo crear la transacción.", e);
        }
    }

    public void update(int id, String type, int accountId, Integer toAccountId, Integer categoryId, double amount, LocalDate date, String description)
            throws ServiceException {

        if (id <= 0) throw new ServiceException("Transacción inválida (id).");
        validate(type, accountId, toAccountId, categoryId, amount, date);

        String desc = normalizeDescription(description);

        FinanceTransaction transaction = new FinanceTransaction(
                id,
                type,
                accountId,
                toAccountId,
                categoryId,
                amount,
                date,
                desc
        );

        try {
            int rows = financeTransactionDao.update(transaction);
            if (rows == 0) throw new ServiceException("La transacción no existe.");
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("No se pudo actualizar la transacción.", e);
        }
    }

    public void delete(int id) throws ServiceException {
        if (id <= 0) throw new ServiceException("Transacción inválida (id).");

        try {
            int rows = financeTransactionDao.deleteById(id);
            if (rows == 0) throw new ServiceException("La transacción no existe o ya fue eliminada.");
        } catch (ServiceException exception) {
            throw exception;
        } catch (Exception e) {
            throw new ServiceException("No se pudo eliminar la transacción.", e);
        }
    }



    // Validaciones

    private void validate(String type, int accountId, Integer toAccountId, Integer categoryId, double amount, LocalDate date) throws ServiceException {
        if (type == null || type.isBlank()) throw new ServiceException("El tipo de transacción es obligatorio.");
        if (!type.equals("INCOME") && !type.equals("EXPENSE") && !type.equals("TRANSFER")) {
            throw new ServiceException("Tipo de transacción no válido.");
        }

        if (accountId <= 0) throw new ServiceException("Cuenta inválida.");
        if (amount <= 0.0) throw new ServiceException("El importe debe ser mayor que 0.");
        if (Double.isNaN(amount) || Double.isInfinite(amount)) throw new ServiceException("Importe no válido.");
        if (date == null) throw new ServiceException("La fecha es obligatoria.");

        if (type.equals("INCOME") || type.equals("EXPENSE")) {
            if (categoryId == null || categoryId <= 0) throw new ServiceException("Categoría inválida.");
            if (toAccountId != null) throw new ServiceException("Un ingreso o gasto no puede tener cuenta destino.");
        }

        if (type.equals("TRANSFER")) {
            if (toAccountId == null || toAccountId <= 0) throw new ServiceException("La cuenta destino es obligatoria en una transferencia.");
            if (accountId == toAccountId) throw new ServiceException("La cuenta origen y destino no pueden ser la misma.");
            if (categoryId != null) throw new ServiceException("Una transferencia no puede tener categoría.");
        }

    }

    private String normalizeDescription(String description) {
        if (description == null) return "";
        return description.trim();
    }
}