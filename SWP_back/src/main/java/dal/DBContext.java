package dal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AnhDT
 */
public class DBContext {
    protected Connection connection;

    public DBContext() {
        initializeConnection();
    }

    private void initializeConnection() {
        try {
            String user = "sa";
            String pass = "123";
            String url = "jdbc:sqlserver://localhost:1433;databaseName=HealthCareSystem;encrypt=true;trustServerCertificate=true";
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(url, user, pass);
            System.out.println("Connected to SQL Server successfully!");
        } catch (ClassNotFoundException e) {
            System.err.println("Driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initializeConnection();
            }
        } catch (SQLException e) {
            System.err.println("Error checking connection: " + e.getMessage());
            initializeConnection();
        }
        return connection;
    }

    // Loại bỏ phương thức closeConnection để tránh đóng kết nối thủ công
    // Kết nối sẽ được quản lý bởi ứng dụng hoặc connection pool

    private static DBContext instance = new DBContext();

    public static DBContext getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        DBContext ad = new DBContext();
    }
}