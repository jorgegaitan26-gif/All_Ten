package co.edu.poli.Allten.DAO;

import java.sql.Connection;

@FunctionalInterface
interface ConnectionProvider {
    Connection getConnection() throws Exception;
}
