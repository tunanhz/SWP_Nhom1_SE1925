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
                                                       int page, int pageSize, String sortBy, String sortOrder, String statusFilter) {
        ArrayList<ListOfMedicalService> services = new ArrayList<>();
        StringBuilder baseSql = new StringBuilder();
        baseSql.append("SELECT * FROM (SELECT ROW_NUMBER() OVER (ORDER BY ");
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            baseSql.append("CASE WHEN ? = 'price' THEN price ELSE service_id END ");
            if (sortOrder != null && sortOrder.equalsIgnoreCase("DESC")) {
                baseSql.append("DESC");
            } else {
                baseSql.append("ASC");
            }
        } else {
            baseSql.append("service_id"); // Mặc định ORDER BY service_id
        }
        baseSql.append(") AS RowNum, service_id, name, description, price, status FROM ListOfMedicalService WHERE 1=1 ");
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            baseSql.append("AND (? IS NULL OR name LIKE ? OR description LIKE ?) ");
        }
        baseSql.append("AND (? IS NULL OR price >= ?) AND (? IS NULL OR price <= ?) ");
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            baseSql.append("AND status = ? ");
        }
        baseSql.append(") AS Result WHERE RowNum BETWEEN ? AND ?");

        try (Connection conn = getConnection()) {
            if (conn == null) {
                throw new SQLException("Database connection is null");
            }
            LOGGER.info("Connection established: " + conn.getMetaData().getURL());

            String searchParam = searchQuery != null && !searchQuery.trim().isEmpty() ? searchQuery.trim().replaceAll("\\s+", " ") : null;
            LOGGER.info("Search param: " + searchParam);

            try (PreparedStatement stmt = conn.prepareStatement(baseSql.toString())) {
                int paramIndex = 1;
                if (sortBy != null && !sortBy.trim().isEmpty()) {
                    stmt.setString(paramIndex++, sortBy);
                }

                if (searchParam != null && !searchParam.trim().isEmpty()) {
                    stmt.setString(paramIndex++, searchParam);
                    stmt.setString(paramIndex++, "%" + searchParam + "%");
                    stmt.setString(paramIndex++, "%" + searchParam + "%");
                }

                LOGGER.info("minPrice: " + minPrice + ", maxPrice: " + maxPrice);
                stmt.setObject(paramIndex++, minPrice);
                stmt.setObject(paramIndex++, minPrice);
                stmt.setObject(paramIndex++, maxPrice);
                stmt.setObject(paramIndex++, maxPrice);

                if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                    stmt.setString(paramIndex++, statusFilter);
                }

                int offset = (page - 1) * pageSize + 1;
                int limit = page * pageSize;
                stmt.setInt(paramIndex++, offset);
                stmt.setInt(paramIndex, limit);

                long startTime = System.currentTimeMillis();
                LOGGER.info("Executing SQL: " + baseSql.toString().replace("?", "{}").formatted(
                        sortBy, searchParam, searchParam != null ? "%" + searchParam + "%" : null,
                        searchParam != null ? "%" + searchParam + "%" : null, minPrice, minPrice,
                        maxPrice, maxPrice, statusFilter, offset, limit));
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
                LOGGER.info("Query execution time: " + (System.currentTimeMillis() - startTime) + "ms");
                LOGGER.info("Fetched " + services.size() + " services");
            }
        } catch (SQLException e) {
            LOGGER.severe("SQL Error: " + e.getMessage() + ", SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode() +
                    ", Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch services", e);
        }
        return services;
    }

    public int countServices(String searchQuery, Double minPrice, Double maxPrice, String statusFilter) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) AS total FROM ListOfMedicalService WHERE 1=1 ");
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append("AND (? IS NULL OR name LIKE ? OR description LIKE ?) ");
        }
        sql.append("AND (? IS NULL OR price >= ?) AND (? IS NULL OR price <= ?) ");
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            sql.append("AND status = ? ");
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            String searchParam = searchQuery != null && !searchQuery.trim().isEmpty() ? searchQuery.trim().replaceAll("\\s+", " ") : null;
            if (searchParam != null && !searchParam.trim().isEmpty()) {
                stmt.setString(paramIndex++, searchParam);
                stmt.setString(paramIndex++, "%" + searchParam + "%");
                stmt.setString(paramIndex++, "%" + searchParam + "%");
            }

            stmt.setObject(paramIndex++, minPrice);
            stmt.setObject(paramIndex++, minPrice);
            stmt.setObject(paramIndex++, maxPrice);
            stmt.setObject(paramIndex++, maxPrice);

            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                stmt.setString(paramIndex, statusFilter);
            }

            long startTime = System.currentTimeMillis();
            ResultSet rs = stmt.executeQuery();
            LOGGER.info("Count query execution time: " + (System.currentTimeMillis() - startTime) + "ms");
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.severe("Error counting services: " + e.getMessage() + ", SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode());
            throw new RuntimeException("Failed to count services", e);
        }
    }

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
            return false;
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
        if (isNameExists(service.getName(), service.getServiceId())) {
            LOGGER.warning("Duplicate name detected: " + service.getName());
            return false;
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

    public ListOfMedicalService getServiceById(int serviceId) {
        String sql = "SELECT service_id, name, description, price, status FROM ListOfMedicalService WHERE service_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, serviceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ListOfMedicalService service = new ListOfMedicalService();
                    service.setServiceId(rs.getInt("service_id"));
                    service.setName(rs.getString("name"));
                    service.setDescription(rs.getString("description"));
                    service.setPrice(rs.getDouble("price"));
                    service.setStatus(rs.getString("status"));
                    return service;
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching service by ID: " + e.getMessage() + ", SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode());
            throw new RuntimeException("Failed to fetch service by ID", e);
        }
        return null;
    }
}