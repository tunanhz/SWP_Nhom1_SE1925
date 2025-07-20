package dal;

import model.ListOfMedicalService;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Logger;

public class AdminBusinessDAO {
    private static final Logger LOGGER = Logger.getLogger(AdminBusinessDAO.class.getName());
    private final DBContext db = new DBContext();

    public Connection getConnection() throws SQLException {
        return db.getConnection();
    }

    public ArrayList<ListOfMedicalService> getServices(String searchQuery, Double minPrice, Double maxPrice,
                                                       int page, int pageSize, String sortBy, String sortOrder) {
        ArrayList<ListOfMedicalService> services = new ArrayList<>();
        String baseSql = """
            SELECT service_id, name, description, price, status
            FROM ListOfMedicalService
            WHERE status = 'Enable'
            AND (? IS NULL OR name LIKE ? OR description LIKE ?)
            AND (? IS NULL OR price >= ?)
            AND (? IS NULL OR price <= ?)
            ORDER BY 
                CASE 
                    WHEN ? = 'service_id' THEN CAST(service_id AS VARCHAR(10))
                    WHEN ? = 'name' THEN name
                    WHEN ? = 'price' THEN CAST(price AS VARCHAR(10))
                    ELSE CAST(service_id AS VARCHAR(10))
                END
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
            """;

        try (Connection conn = db.getConnection()) {
            if (conn == null) {
                throw new SQLException("Database connection is null");
            }
            LOGGER.info("Connection established: " + conn.getMetaData().getURL());

            String sortColumn = sortBy != null && !sortBy.trim().isEmpty() ? sortBy.toLowerCase() : "service_id";
            LOGGER.info("Using sortColumn: " + sortColumn);

            try (PreparedStatement stmt = conn.prepareStatement(baseSql)) {
                String searchParam = searchQuery != null && !searchQuery.trim().isEmpty() ? searchQuery.trim().replaceAll("\\s+", " ") : null;
                LOGGER.info("Search param: " + searchParam);
                stmt.setString(1, searchParam);
                stmt.setString(2, searchParam != null ? "%" + searchParam + "%" : null);
                stmt.setString(3, searchParam != null ? "%" + searchParam + "%" : null);

                LOGGER.info("minPrice: " + minPrice + ", maxPrice: " + maxPrice);
                stmt.setObject(4, minPrice);
                stmt.setObject(5, minPrice);
                stmt.setObject(6, maxPrice);
                stmt.setObject(7, maxPrice);

                stmt.setString(8, sortColumn);
                stmt.setString(9, sortColumn);
                stmt.setString(10, sortColumn);

                int offset = (page - 1) * pageSize;
                LOGGER.info("Offset: " + offset + ", pageSize: " + pageSize);
                stmt.setInt(11, offset);
                stmt.setInt(12, pageSize);

                LOGGER.info("Executing query with parameters set");
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ListOfMedicalService service = new ListOfMedicalService();
                        service.setServiceId(rs.getInt("service_id"));
                        service.setName(rs.getString("name"));
                        service.setDescription(rs.getString("description"));
                        service.setPrice(rs.getDouble("price"));
                        service.setStatus(rs.getString("status"));
                        services.add(service);
                    }
                }
            }
            LOGGER.info("Fetched " + services.size() + " services");
        } catch (SQLException e) {
            LOGGER.severe("SQL Error: " + e.getMessage() + ", SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode() +
                    ", Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
            throw new RuntimeException("Failed to fetch services", e);
        }
        return services;
    }

    public int countServices(String searchQuery, Double minPrice, Double maxPrice) {
        String sql = """
            SELECT COUNT(*) AS total
            FROM ListOfMedicalService
            WHERE status = 'Enable'
            AND (? IS NULL OR name LIKE ? OR description LIKE ?)
            AND (? IS NULL OR price >= ?)
            AND (? IS NULL OR price <= ?)
            """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchParam = searchQuery != null && !searchQuery.trim().isEmpty() ? searchQuery.trim().replaceAll("\\s+", " ") : null;
            stmt.setString(1, searchParam);
            stmt.setString(2, searchParam != null ? "%" + searchParam + "%" : null);
            stmt.setString(3, searchParam != null ? "%" + searchParam + "%" : null);

            stmt.setObject(4, minPrice);
            stmt.setObject(5, minPrice);
            stmt.setObject(6, maxPrice);
            stmt.setObject(7, maxPrice);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.severe("Error counting services: " + e.getMessage() + ", SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode());
            throw new RuntimeException("Failed to count services", e);
        }
    }

    // Check if name already exists (excluding the current service during update)
    private boolean isNameExists(String name, Integer excludeServiceId) {
        String sql = "SELECT COUNT(*) FROM ListOfMedicalService WHERE name = ? AND (? IS NULL OR service_id != ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setObject(2, excludeServiceId);
            stmt.setObject(3, excludeServiceId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.severe("Error checking name existence: " + e.getMessage() + ", SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode());
            throw new RuntimeException("Failed to check name existence", e);
        }
    }

    public boolean createService(ListOfMedicalService service) {
        if (isNameExists(service.getName(), null)) {
            LOGGER.warning("Duplicate name detected: " + service.getName());
            return false; // Return false to indicate failure due to duplicate
        }

        String sql = "INSERT INTO ListOfMedicalService (name, description, price, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, service.getName());
            stmt.setString(2, service.getDescription());
            stmt.setDouble(3, service.getPrice());
            stmt.setString(4, service.getStatus() != null ? service.getStatus() : "Enable");
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.severe("Error creating service: " + e.getMessage() + ", SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode());
            throw new RuntimeException("Failed to create service", e);
        }
    }

    public boolean updateService(ListOfMedicalService service) {
        // Check if the new name is already in use by another service
        if (isNameExists(service.getName(), service.getServiceId())) {
            LOGGER.warning("Duplicate name detected: " + service.getName());
            return false; // Return false to indicate failure due to duplicate
        }

        String sql = "UPDATE ListOfMedicalService SET name = ?, description = ?, price = ?, status = ? WHERE service_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, service.getName());
            stmt.setString(2, service.getDescription());
            stmt.setDouble(3, service.getPrice());
            stmt.setString(4, service.getStatus());
            stmt.setInt(5, service.getServiceId());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.severe("Error updating service: " + e.getMessage() + ", SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode());
            throw new RuntimeException("Failed to update service", e);
        }
    }

    public boolean deleteService(int serviceId) {
        String sql = "UPDATE ListOfMedicalService SET status = 'Disable' WHERE service_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, serviceId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.severe("Error deleting service: " + e.getMessage() + ", SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode());
            throw new RuntimeException("Failed to delete service", e);
        }
    }
}