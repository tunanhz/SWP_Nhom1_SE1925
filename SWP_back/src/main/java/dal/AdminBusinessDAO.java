package dal;

import model.AccountStaff;
import model.ListOfMedicalService;
import model.Receptionist;

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Logger;

public class AdminBusinessDAO {
    private static final Logger LOGGER = Logger.getLogger(AdminBusinessDAO.class.getName());
    private final DBContext db = new DBContext();
    private static final String DEFAULT_IMAGE_URL = "https://res.cloudinary.com/dnoyqme5b/image/upload/v1752978933/avatars/1_xaytga.jpg";

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

    // Receptionist management methods
    public ArrayList<Receptionist> getReceptionists(String searchQuery, String statusFilter, int page, int pageSize) {
        ArrayList<Receptionist> receptionists = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM (SELECT ROW_NUMBER() OVER (ORDER BY r.receptionist_id) AS RowNum, ");
        sql.append("r.receptionist_id, r.full_name, r.phone, r.account_staff_id, ");
        sql.append("a.username, a.email, a.status ");
        sql.append("FROM Receptionist r ");
        sql.append("JOIN AccountStaff a ON r.account_staff_id = a.account_staff_id ");
        sql.append("WHERE a.role = 'Receptionist' ");
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append("AND (a.username LIKE ? OR a.email LIKE ? OR r.full_name LIKE ? OR r.phone LIKE ?) ");
        }
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            sql.append("AND a.status = ? ");
        }
        sql.append(") AS Result WHERE RowNum BETWEEN ? AND ?");

        try (Connection conn = getConnection()) {
            if (conn == null) {
                throw new SQLException("Database connection is null");
            }
            LOGGER.info("Connection established: " + conn.getMetaData().getURL());

            String searchParam = searchQuery != null && !searchQuery.trim().isEmpty() ? searchQuery.trim().replaceAll("\\s+", " ") : null;
            LOGGER.info("Search param: " + searchParam);

            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                if (searchParam != null && !searchParam.trim().isEmpty()) {
                    stmt.setString(paramIndex++, "%" + searchParam + "%");
                    stmt.setString(paramIndex++, "%" + searchParam + "%");
                    stmt.setString(paramIndex++, "%" + searchParam + "%");
                    stmt.setString(paramIndex++, "%" + searchParam + "%");
                }
                if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                    stmt.setString(paramIndex++, statusFilter);
                }
                int offset = (page - 1) * pageSize + 1;
                int limit = page * pageSize;
                stmt.setInt(paramIndex++, offset);
                stmt.setInt(paramIndex, limit);

                long startTime = System.currentTimeMillis();
                LOGGER.info("Executing SQL: " + sql.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Receptionist receptionist = new Receptionist();
                        receptionist.setReceptionistId(rs.getInt("receptionist_id"));
                        receptionist.setFullName(rs.getString("full_name"));
                        receptionist.setPhone(rs.getString("phone"));
                        receptionist.setAccountStaffId(rs.getInt("account_staff_id"));

                        AccountStaff account = new AccountStaff();
                        account.setAccountStaffId(rs.getInt("account_staff_id"));
                        account.setUserName(rs.getString("username"));
                        account.setEmail(rs.getString("email"));
                        account.setStatus(rs.getString("status").equals("Enable"));
                        receptionist.setAccountStaffId(rs.getInt("account_staff_id"));
                        receptionists.add(receptionist);
                    }
                }
                LOGGER.info("Query execution time: " + (System.currentTimeMillis() - startTime) + "ms");
                LOGGER.info("Fetched " + receptionists.size() + " receptionists");
            }
        } catch (SQLException e) {
            LOGGER.severe("SQL Error: " + e.getMessage() + ", SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode() +
                    ", Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "N/A"));
            throw new RuntimeException("Failed to fetch receptionists", e);
        }
        return receptionists;
    }

    public int countReceptionists(String searchQuery, String statusFilter) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) AS total FROM Receptionist r ");
        sql.append("JOIN AccountStaff a ON r.account_staff_id = a.account_staff_id ");
        sql.append("WHERE a.role = 'Receptionist' ");
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append("AND (a.username LIKE ? OR a.email LIKE ? OR r.full_name LIKE ? OR r.phone LIKE ?) ");
        }
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            sql.append("AND a.status = ? ");
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            String searchParam = searchQuery != null && !searchQuery.trim().isEmpty() ? searchQuery.trim().replaceAll("\\s+", " ") : null;
            if (searchParam != null && !searchParam.trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + searchParam + "%");
                stmt.setString(paramIndex++, "%" + searchParam + "%");
                stmt.setString(paramIndex++, "%" + searchParam + "%");
                stmt.setString(paramIndex++, "%" + searchParam + "%");
            }
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
            LOGGER.severe("Error counting receptionists: " + e.getMessage() + ", SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode());
            throw new RuntimeException("Failed to count receptionists", e);
        }
    }

    public boolean isUniqueField(String fieldName, String value, Integer excludeAccountStaffId) throws SQLException {
        String table = fieldName.equals("phone") ? "r" : "a";
        String sql = "SELECT COUNT(*) FROM AccountStaff a LEFT JOIN Receptionist r ON a.account_staff_id = r.account_staff_id " +
                "WHERE " + table + "." + fieldName + " = ? AND (? IS NULL OR a.account_staff_id != ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.setObject(2, excludeAccountStaffId);
            stmt.setObject(3, excludeAccountStaffId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
            return true;
        } catch (SQLException e) {
            LOGGER.severe("Error checking unique " + fieldName + ": " + e.getMessage() + ", SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode());
            throw e;
        }
    }

    public boolean createReceptionist(AccountStaff account, Receptionist receptionist) throws SQLException {
        // Check uniqueness of username, email, and phone individually
        if (!isUniqueField("username", account.getUserName(), null)) {
            LOGGER.warning("Duplicate username detected: " + account.getUserName());
            throw new SQLException("Username already exists");
        }
        if (!isUniqueField("email", account.getEmail(), null)) {
            LOGGER.warning("Duplicate email detected: " + account.getEmail());
            throw new SQLException("Email already exists");
        }
        if (!isUniqueField("phone", receptionist.getPhone(), null)) {
            LOGGER.warning("Duplicate phone detected: " + receptionist.getPhone());
            throw new SQLException("Phone already exists");
        }

        Connection conn = null;
        PreparedStatement stmtAccount = null;
        PreparedStatement stmtReceptionist = null;
        ResultSet generatedKeys = null;

        try {
            conn = getConnection();
            if (conn == null) {
                throw new SQLException("Database connection is null");
            }
            conn.setAutoCommit(false);

            // Insert into AccountStaff with default image URL
            String sqlAccount = "INSERT INTO AccountStaff (username, password, role, email, img, status) " +
                    "VALUES (?, ?, 'Receptionist', ?, ?, ?)";
            stmtAccount = conn.prepareStatement(sqlAccount, Statement.RETURN_GENERATED_KEYS);
            stmtAccount.setString(1, account.getUserName());
            stmtAccount.setString(2, account.getPassWord());
            stmtAccount.setString(3, account.getEmail());
            stmtAccount.setString(4, DEFAULT_IMAGE_URL);
            stmtAccount.setString(5, account.isStatus() ? "Enable" : "Disable");
            int rowsAffected = stmtAccount.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Failed to create AccountStaff");
            }

            // Get generated account_staff_id
            generatedKeys = stmtAccount.getGeneratedKeys();
            if (generatedKeys.next()) {
                int accountStaffId = generatedKeys.getInt(1);
                receptionist.setAccountStaffId(accountStaffId);

                // Insert into Receptionist
                String sqlReceptionist = "INSERT INTO Receptionist (full_name, phone, account_staff_id) " +
                        "VALUES (?, ?, ?)";
                stmtReceptionist = conn.prepareStatement(sqlReceptionist);
                stmtReceptionist.setString(1, receptionist.getFullName());
                stmtReceptionist.setString(2, receptionist.getPhone());
                stmtReceptionist.setInt(3, accountStaffId);
                rowsAffected = stmtReceptionist.executeUpdate();

                if (rowsAffected == 0) {
                    throw new SQLException("Failed to create Receptionist");
                }

                conn.commit();
                return true;
            } else {
                throw new SQLException("Failed to retrieve generated account_staff_id");
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.severe("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            LOGGER.severe("Error creating receptionist: " + e.getMessage() + ", SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode());
            throw e;
        } finally {
            if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException e) { /* ignored */ }
            if (stmtReceptionist != null) try { stmtReceptionist.close(); } catch (SQLException e) { /* ignored */ }
            if (stmtAccount != null) try { stmtAccount.close(); } catch (SQLException e) { /* ignored */ }
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /* ignored */ }
        }
    }

    public boolean updateReceptionist(AccountStaff account, Receptionist receptionist) throws SQLException {
        // Check uniqueness of username, email, and phone individually
        if (!isUniqueField("username", account.getUserName(), account.getAccountStaffId())) {
            LOGGER.warning("Duplicate username detected: " + account.getUserName());
            throw new SQLException("Username already exists");
        }
        if (!isUniqueField("email", account.getEmail(), account.getAccountStaffId())) {
            LOGGER.warning("Duplicate email detected: " + account.getEmail());
            throw new SQLException("Email already exists");
        }
        if (!isUniqueField("phone", receptionist.getPhone(), account.getAccountStaffId())) {
            LOGGER.warning("Duplicate phone detected: " + receptionist.getPhone());
            throw new SQLException("Phone already exists");
        }

        Connection conn = null;
        PreparedStatement stmtAccount = null;
        PreparedStatement stmtReceptionist = null;

        try {
            conn = getConnection();
            if (conn == null) {
                throw new SQLException("Database connection is null");
            }
            conn.setAutoCommit(false);

            // Update AccountStaff (excluding img)
            String sqlAccount = "UPDATE AccountStaff SET username = ?, password = ?, email = ?, status = ? " +
                    "WHERE account_staff_id = ?";
            stmtAccount = conn.prepareStatement(sqlAccount);
            stmtAccount.setString(1, account.getUserName());
            stmtAccount.setString(2, account.getPassWord());
            stmtAccount.setString(3, account.getEmail());
            stmtAccount.setString(4, account.isStatus() ? "Enable" : "Disable");
            stmtAccount.setInt(5, account.getAccountStaffId());
            int rowsAffected = stmtAccount.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Failed to update AccountStaff");
            }

            // Update Receptionist
            String sqlReceptionist = "UPDATE Receptionist SET full_name = ?, phone = ? WHERE account_staff_id = ?";
            stmtReceptionist = conn.prepareStatement(sqlReceptionist);
            stmtReceptionist.setString(1, receptionist.getFullName());
            stmtReceptionist.setString(2, receptionist.getPhone());
            stmtReceptionist.setInt(3, receptionist.getAccountStaffId());
            rowsAffected = stmtReceptionist.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Failed to update Receptionist");
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    LOGGER.severe("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            LOGGER.severe("Error updating receptionist: " + e.getMessage() + ", SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode());
            throw e;
        } finally {
            if (stmtReceptionist != null) try { stmtReceptionist.close(); } catch (SQLException e) { /* ignored */ }
            if (stmtAccount != null) try { stmtAccount.close(); } catch (SQLException e) { /* ignored */ }
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /* ignored */ }
        }
    }

    public boolean deleteReceptionist(int accountStaffId) {
        String sql = "UPDATE AccountStaff SET status = 'Disable' WHERE account_staff_id = ? AND role = 'Receptionist'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountStaffId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            LOGGER.severe("Error deleting receptionist: " + e.getMessage() + ", SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode());
            throw new RuntimeException("Failed to delete receptionist", e);
        }
    }

    public Receptionist getReceptionistById(int accountStaffId) {
        String sql = "SELECT r.receptionist_id, r.full_name, r.phone, r.account_staff_id, " +
                "a.username, a.password, a.email, a.img, a.status " +
                "FROM Receptionist r " +
                "JOIN AccountStaff a ON r.account_staff_id = a.account_staff_id " +
                "WHERE r.account_staff_id = ? AND a.role = 'Receptionist'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountStaffId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Receptionist receptionist = new Receptionist();
                    receptionist.setReceptionistId(rs.getInt("receptionist_id"));
                    receptionist.setFullName(rs.getString("full_name"));
                    receptionist.setPhone(rs.getString("phone"));
                    receptionist.setAccountStaffId(rs.getInt("account_staff_id"));

                    AccountStaff account = new AccountStaff();
                    account.setAccountStaffId(rs.getInt("account_staff_id"));
                    account.setUserName(rs.getString("username"));
                    account.setPassWord(rs.getString("password"));
                    account.setEmail(rs.getString("email"));
                    account.setImg(rs.getString("img"));
                    account.setStatus(rs.getString("status").equals("Enable"));
                    account.setRole("Receptionist");

                    return receptionist;
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching receptionist by ID: " + e.getMessage() + ", SQLState: " + e.getSQLState() + ", ErrorCode: " + e.getErrorCode());
            throw new RuntimeException("Failed to fetch receptionist by ID", e);
        }
        return null;
    }
}