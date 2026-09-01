package com.agenda.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String URL =
            "jdbc:mariadb://localhost:3306/agenda?allowPublicKeyRetrieval=true&useSsl=false";
    private static final String USER = "usuario1";
    private static final String PASS = "superpassword";
    private ConexionDB() {
    }
    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}