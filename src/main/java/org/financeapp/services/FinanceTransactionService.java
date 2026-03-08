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

    public void create(int accountId, int categoryId, double amount, LocalDate date, String description)
            throws ServiceException {

        validate(accountId, categoryId, amount, date);

        String desc = normalizeDescription(description);

        FinanceTransaction transaction = new FinanceTransaction(0, accountId, categoryId, amount, date, desc);

        try {
            financeTransactionDao.insert(transaction);
        } catch (Exception e) {
            throw new ServiceException("No se pudo crear la transacción.", e);
        }
    }

    public void update(int id, int accountId, int categoryId, double amount, LocalDate date, String description)
            throws ServiceException {

        if (id <= 0) throw new ServiceException("Transacción inválida (id).");
        validate(accountId, categoryId, amount, date);

        String desc = normalizeDescription(description);

        FinanceTransaction transaction = new FinanceTransaction(
                id,
                accountId,
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

    private void validate(int accountId, int categoryId, double amount, LocalDate date) throws ServiceException {
        if (accountId <= 0) throw new ServiceException("Cuenta inválida.");
        if (categoryId <= 0) throw new ServiceException("Categoría inválida.");
        if (amount == 0.0) throw new ServiceException("El importe no puede ser 0.");
        if (Double.isNaN(amount) || Double.isInfinite(amount)) throw new ServiceException("Importe no válido.");
        if (date == null) throw new ServiceException("La fecha es obligatoria.");

    }

    private String normalizeDescription(String description) {
        if (description == null) return "";
        return description.trim();
    }
}