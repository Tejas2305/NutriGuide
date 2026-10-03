package com.nutriguide.main;

import com.nutriguide.db.DBConnectionFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class NutriGuideApplication {
    public static void main(String[] args) {
        System.out.println("----------------------------------------");
        System.out.println("Starting NutriGuide Application Phase 1 Test...");
        
        try (Connection conn = DBConnectionFactory.getConnection()) {
            if (conn != null) {
                System.out.println("JDBC Connection to MySQL was successful!");
                
                // Execute SELECT 1
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT 1")) {
                    if (rs.next()) {
                        System.out.println("Database test query (SELECT 1) returned: " + rs.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("JDBC Connection failed.");
            System.err.println("Error Message: " + e.getMessage());
            System.err.println("SQLState: " + e.getSQLState());
            System.err.println("VendorError: " + e.getErrorCode());
        }
        System.out.println("----------------------------------------");
    }
}
