package com.nutriguide.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnectionFactory - gives us a database connection.
 * Reads the database URL, username, and password from database.properties file.
 */
public class DBConnectionFactory {

    private static Properties properties = new Properties();

    // This block runs once when the class is loaded
    static {
        try {
            // Load the database.properties file from the resources folder
            InputStream input = DBConnectionFactory.class.getClassLoader()
                    .getResourceAsStream("database.properties");

            if (input == null) {
                System.out.println("ERROR: database.properties file not found!");
            } else {
                properties.load(input);
                input.close();
                // Load the MySQL JDBC driver
                Class.forName(properties.getProperty("db.driver"));
            }
        } catch (IOException e) {
            System.out.println("Error reading database.properties: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL JDBC driver not found: " + e.getMessage());
        }
    }

    /**
     * Returns a new connection to the MySQL database.
     * Call this method whenever you need to talk to the database.
     */
    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.user");
        String password = properties.getProperty("db.password");
        return DriverManager.getConnection(url, user, password);
    }
}
