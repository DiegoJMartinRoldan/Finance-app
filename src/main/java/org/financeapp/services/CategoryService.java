package org.financeapp.services;

import org.financeapp.data.dao.CategoryDao;
import org.financeapp.domain.Category;
import org.financeapp.services.ServiceException;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

public class CategoryService {

    // Lista de valores validos para kind. (Para el apartado de métodos de validación más abajo)
    private static final Set<String> ALLOWED_KINDS = Set.of("INCOME", "EXPENSE");

    // Instancia del dao en el service.
    private final CategoryDao categoryDao;


    // Constructor del service
    public CategoryService(CategoryDao categoryDao) {
        this.categoryDao = categoryDao;
    }



    //Read
    public List<Category> getAll() throws ServiceException {
        try {
            return categoryDao.findAll();
        } catch (Exception exception) {
            throw new ServiceException("No se pudieron cargar las categorías.", exception);
        }
    }

    // Create
    public void create(String name, String kind) throws ServiceException {
        // Metodo normalizeName definido abajo
        String newName = normalizeName(name);
        String newKind = normalizeKind(kind);

        // Metodo abajo en validaciones
        validateName(newName);
        validateKind(newKind);

        try {
            // Comprobación del nombre vacío
            if (categoryDao.existsByName(newName)) {
                throw new ServiceException("Ya existe una categoría con ese nombre");
            }

            categoryDao.insert(new Category(0, newName, newKind));
        } catch (ServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ServiceException("No se pudo crear la categoría", exception);
        }
    }


    // Update
    public void update(int id, String name, String kind) throws ServiceException {
        if (id <= 0) throw new ServiceException("Categoría inválida, el id no puede ser negativo ni 0.");

        // Validación con el metodo normalizeName abajo definido. No quiero espacios en blanco.
        String cleanName = normalizeName(name);
        String cleanKind = normalizeKind(kind);

        validateName(cleanName);
        validateKind(cleanKind);

        try {
            Category currentCategory = categoryDao.findById(id);
            if (currentCategory == null) {
                throw new ServiceException("La categoría no existe.");
            }

            // Evitar duplicados: si cambia el nombre y el nuevo nombre ya existe, error.
            if (!currentCategory.getName().equalsIgnoreCase(cleanName) && categoryDao.existsByName(cleanName)) {
                throw new ServiceException("Ya existe otra categoría con ese nombre.");
            }

            // La actualización se produce aquí.
            int modifications = categoryDao.update(new Category(id, cleanName, cleanKind));
            if (modifications == 0) {
                throw new ServiceException("No se pudo actualizar la categoría.");
            }
        } catch (ServiceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ServiceException("No se pudo actualizar la categoría.", exception);
        }
    }


    // Delete
    public void delete(int id) throws ServiceException {
        if (id <= 0) throw new ServiceException("Categoría inválida.");

        try {
            int modifications = categoryDao.deleteById(id);
            if (modifications == 0) {
                throw new ServiceException("La categoría no existe o ya fue eliminada.");
            }
        } catch (SQLException exception) {
            // FK constraint: hay transacciones asociadas
            if (CategoryDao.foreignKeyFail(exception)) {
                throw new ServiceException("No se puede eliminar: hay transacciones asociadas a esta categoría.");
            }
            throw new ServiceException("Error al eliminar la categoría.", exception);
        } catch (Exception e) {
            throw new ServiceException("Error al eliminar la categoría.", e);
        }
    }


    // Validaciones sencillas.


    private String normalizeName(String name) {
        return name == null ? "" : name.trim();
    }

    private String normalizeKind(String kind) {
        return kind == null ? "" : kind.trim().toUpperCase();
    }

    private void validateName(String name) throws ServiceException {
        if (name.isBlank()) {
            throw new ServiceException("El nombre de la categoría no puede estar vacío.");
        }
        if (name.length() > 50) {
            throw new ServiceException("El nombre de la categoría es demasiado largo");
        }
    }

    private void validateKind(String kind) throws ServiceException {
        if (!ALLOWED_KINDS.contains(kind)) {
            throw new ServiceException("Tipo de categoría inválido.");
        }
    }
}