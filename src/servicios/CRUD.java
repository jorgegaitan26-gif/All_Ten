package servicios;

import java.util.List;

/**
 * Operaciones básicas de persistencia (crear, leer, actualizar y eliminar).
 *
 * @param <T> tipo de entidad que gestiona el DAO
 */
public interface CRUD<T> {

    /**
     * Registra una entidad.
     *
     * @param t entidad a registrar
     * @return mensaje con el resultado de la operación
     * @throws Exception si ocurre un error durante el registro
     */
    String create(T t) throws Exception;

    /**
     * Consulta una entidad por su identificador.
     *
     * @param <K> tipo del identificador
     * @param id identificador de la entidad
     * @return entidad encontrada o null si no existe
     * @throws Exception si ocurre un error durante la consulta
     */
    <K> T readone(K id) throws Exception;

    /**
     * Consulta todas las entidades.
     *
     * @return lista de entidades
     * @throws Exception si ocurre un error durante la consulta
     */
    List<T> readall() throws Exception;

    /**
     * Actualiza una entidad existente.
     *
     * @param t entidad con los datos nuevos (se identifica por su id)
     * @return mensaje con el resultado de la operación
     * @throws Exception si ocurre un error durante la actualización
     */
    String update(T t) throws Exception;

    /**
     * Elimina una entidad por su identificador.
     *
     * @param <K> tipo del identificador
     * @param id identificador de la entidad
     * @return mensaje con el resultado de la operación
     * @throws Exception si ocurre un error durante la eliminación
     */
    <K> String delete(K id) throws Exception;
}
