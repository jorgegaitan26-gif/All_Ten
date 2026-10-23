package co.edu.poli.Allten.DAO;

import java.sql.SQLException;
import java.util.List;

/**
 * Operaciones CRUD comunes para las entidades persistidas.
 *
 * @param <T> tipo de entidad administrada por el DAO
 */
public interface CRUD<T> {

    /** Crea una entidad y devuelve la instancia con su identificador asignado. */
    T create(T entity) throws SQLException;

    /** Busca una entidad por su identificador o devuelve {@code null} si no existe. */
    T readone(int id) throws SQLException;

    /** Devuelve todas las entidades. */
    List<T> readall() throws SQLException;

    /** Actualiza una entidad; indica si la base de datos reportó cambios. */
    boolean update(T entity) throws SQLException;

    /** Elimina una entidad; devuelve {@code false} si no existe. */
    boolean delete(int id) throws SQLException;
}
