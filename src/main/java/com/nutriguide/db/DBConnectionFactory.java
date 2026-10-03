package com.nutriguide.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnectionFactory handles database connections using a properties file.
 * This class establishes the connection via DriverManager without hardcoding credentials.
 */
public class DBConnectionFactory {

    private static Properties properties = new Properties();

    static {
        try (InputStream input = DBConnectionFactory.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find database.properties");
            } else {
                properties.load(input);
                Class.forName(properties.getProperty("db.driver"));
            }
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Provides a new Connection object to the database.
     * @return Connection object.
     * @throws SQLException If a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        );
    }
}
