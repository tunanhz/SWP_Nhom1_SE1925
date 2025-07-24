package dal;

import dto.PatientDTO;
import dto.ReceptionistDTO;
import model.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class PatientDAO {
    DBContext ad = new DBContext();

    public boolean updatePatient(int patientId, String fullName, String dob, String gender, String phone, String address) {
        String sql = """
                UPDATE [dbo].[Patient]
                SET [full_name] = ?,
                    [dob] = ?,
                    [gender] = ?,
                    [phone] = ?,
                    [address] = ?
                WHERE [patient_id] = ?;
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setNString(1, fullName);
            stmt.setNString(2, dob);
            stmt.setNString(3, gender);
            stmt.setNString(4, phone);
            stmt.setNString(5, address);
            stmt.setInt(6, patientId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePatientStatus(int patientId, String status) {
        String sql = """
                UPDATE [HealthCareSystem].[dbo].[Patient]
                SET [status] = ?
                WHERE [patient_id] = ?;
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setNString(1, status);
            stmt.setInt(2, patientId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<Patient> getAllPatientsByAccountPatientId(int accountPatientId, String fullName, String dob, String gender, int page, int pageSize) {
        Logger LOGGER = Logger.getLogger(this.getClass().getName());
        String sql = """
                SELECT
                    p.patient_id,
                    p.full_name,
                    p.dob,
                    p.gender,
                    p.phone,
                    p.[address],
                    p.status
                FROM AccountPatient ap
                FULL OUTER JOIN Patient_AccountPatient pa
                    ON pa.account_patient_id = ap.account_patient_id
                FULL OUTER JOIN Patient p
                    ON p.patient_id = pa.patient_id
                WHERE ap.account_patient_id = ?
                    AND ap.status = 'Enable'
                    AND p.status = 'Enable'
                    AND (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                    AND (? IS NULL OR CONVERT(VARCHAR, p.dob, 120) COLLATE SQL_Latin1_General_CP1_CI_AI
                                LIKE CASE
                                WHEN ? LIKE '[0-9][0-9][0-9][0-9]' THEN ? + '%'
                                WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                                WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                                ELSE ? END)
                    AND (? IS NULL OR p.gender COLLATE SQL_Latin1_General_CP1_CI_AI = ?)
                ORDER BY p.patient_id DESC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;

        ArrayList<Patient> patients = new ArrayList<>();
        try (PreparedStatement stmt = ad.getConnection().prepareStatement(sql)) {
            // Set parameters
            stmt.setInt(1, accountPatientId);

            // FullName
            stmt.setNString(2, fullName);
            stmt.setNString(3, fullName != null ? "%" + fullName + "%" : null);

            // DOB
            stmt.setNString(4, dob);
            stmt.setNString(5, dob);
            stmt.setNString(6, dob);
            stmt.setNString(7, dob);
            stmt.setNString(8, dob);
            stmt.setNString(9, dob);
            stmt.setNString(10, dob);
            stmt.setNString(11, dob);

            // Gender
            stmt.setNString(12, gender);
            stmt.setNString(13, gender);

            // Pagination
            int offset = (page - 1) * pageSize;
            stmt.setInt(14, offset);
            stmt.setInt(15, pageSize);

            // Execute query
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Patient patient = new Patient();
                patient.setId(rs.getInt("patient_id"));
                patient.setFullName(rs.getString("full_name"));
                patient.setDob(rs.getString("dob"));
                patient.setGender(rs.getString("gender"));
                patient.setPhone(rs.getString("phone"));
                patient.setAddress(rs.getString("address"));
                patient.setStatus(rs.getString("status"));
                patients.add(patient);
            }
            LOGGER.info("Fetched " + patients.size() + " patients for accountPatientId=" + accountPatientId);
        } catch (SQLException e) {
            LOGGER.severe("Error fetching patients for accountPatientId=" + accountPatientId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return patients;
    }

    public int countPatientByAccountPatientId(
            int accountPatientId,
            String fullName,
            String dob,
            String gender) {
        String query = """
                    SELECT COUNT(*) AS total
                    FROM AccountPatient ap
                FULL OUTER JOIN Patient_AccountPatient pa
                    ON pa.account_patient_id = ap.account_patient_id
                FULL OUTER JOIN Patient p
                    ON p.patient_id = pa.patient_id
                WHERE ap.account_patient_id = ?
                    AND ap.status = 'Enable'
                    AND p.status = 'Enable'
                    AND (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                    AND (? IS NULL OR
                        CONVERT(VARCHAR, p.dob, 120) COLLATE SQL_Latin1_General_CP1_CI_AI
                        LIKE CASE
                        WHEN ? LIKE '[0-9][0-9][0-9][0-9]' THEN ? + '%'
                        WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                        WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                        ELSE ? END)
                    AND (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                """;

        try (PreparedStatement stmt = ad.getConnection().prepareStatement(query)) {
            // Account patient ID
            stmt.setInt(1, accountPatientId);

            // Full name filter
            stmt.setString(2, fullName);
            stmt.setString(3, fullName != null ? "%" + fullName + "%" : null);

            // dob year filter
            stmt.setString(4, dob);
            stmt.setString(5, "%" + dob + "%");
            stmt.setString(6, "%" + dob + "%");
            stmt.setString(7, "%" + dob + "%");
            stmt.setString(8, "%" + dob + "%");
            stmt.setString(9, "%" + dob + "%");
            stmt.setString(10, "%" + dob + "%");
            stmt.setString(11, "%" + dob + "%");

            // Gender filter
            stmt.setString(12, gender);
            stmt.setString(13, gender != null ? "%" + gender + "%" : null);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error counting patients: " + e.getMessage());
            throw new RuntimeException("Failed to count patients", e);
        }
    }

    public boolean insertPatient(String fullName, String dob, String gender, String phone, String address, String status) {
        String sql = """
                INSERT INTO Patient (full_name, dob, gender, phone, address, status) VALUES
                (?, ?, ?, ?, ?, ?)
                """;
        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setNString(1, fullName);
            stmt.setNString(2, dob);
            stmt.setNString(3, gender);
            stmt.setNString(4, phone);
            stmt.setNString(5, address);
            stmt.setNString(6, status);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertAccountPatient(String fullName, String dob, String gender, String phone, String address, int account_id) {
        Patient p = getPatientByDetails(fullName, dob, gender, phone, address);
        String sql = """
                 INSERT INTO [dbo].[Patient_AccountPatient]
                ([patient_id],[account_patient_id])
                VALUES (?,?)
                """;
        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setInt(1, p.getId());
            stmt.setInt(2, account_id);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePatient(int patientId) {
        String sql = """
                UPDATE [dbo].[Patient]
                SET [status] = 'Disable'
                WHERE patient_id = ?;
                """;
        try (PreparedStatement stmt = ad.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean enablePatient(int patientId) {
        String sql = """
                UPDATE [dbo].[Patient]
                SET [status] = 'Enable'
                WHERE patient_id = ?;
                """;
        try (PreparedStatement stmt = ad.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Patient getPatientByDetails(String fullName, String dob, String gender, String phone, String address) {
        String sql = """
                SELECT [patient_id], [full_name], [dob], [gender], [phone], [address], [status]
                FROM [HealthCareSystem].[dbo].[Patient]
                WHERE [full_name] = ? AND [dob] = ? AND [gender] = ? AND [phone] = ? AND [address] = ?
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setNString(1, fullName);
            stmt.setNString(2, dob);
            stmt.setNString(3, gender);
            stmt.setNString(4, phone);
            stmt.setNString(5, address);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Patient patient = new Patient();
                patient.setId(rs.getInt("patient_id"));
                patient.setFullName(rs.getString("full_name"));
                patient.setDob(rs.getString("dob"));
                patient.setGender(rs.getString("gender"));
                patient.setPhone(rs.getString("phone"));
                patient.setAddress(rs.getString("address"));
                patient.setStatus(rs.getString("status"));
                return patient;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Patient getPatientByPatientId(int patientId) {
        String sql = """
                SELECT [patient_id]
                      ,[full_name]
                      ,[dob]
                      ,[gender]
                      ,[phone]
                      ,[address]
                      ,[status]
                FROM [Patient]
                WHERE [patient_id] = ?
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Patient patient = new Patient();
                patient.setId(rs.getInt("patient_id"));
                patient.setFullName(rs.getString("full_name"));
                patient.setDob(rs.getString("dob"));
                patient.setGender(rs.getString("gender"));
                patient.setPhone(rs.getString("phone"));
                patient.setAddress(rs.getString("address"));
                patient.setStatus(rs.getString("status"));
                return patient;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Method to get all patients for admin/staff view (not limited by account)
    public ArrayList<Patient> getAllPatients(String fullName, String dob, String gender, String status, int page, int pageSize) {
        Logger LOGGER = Logger.getLogger(this.getClass().getName());
        String sql = """
                 SELECT
                    p.patient_id,
                    p.full_name,
                    p.dob,
                    p.gender,
                    p.phone,
                    p.[address],
                    p.status
                FROM Patient p
                WHERE 
                    (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                    AND (? IS NULL OR CONVERT(VARCHAR, p.dob, 120) COLLATE SQL_Latin1_General_CP1_CI_AI
                                LIKE CASE
                                WHEN ? LIKE '[0-9][0-9][0-9][0-9]' THEN ? + '%'
                                WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                                WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                                ELSE ? END)
                    AND (? IS NULL OR p.gender COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                    AND (? IS NULL OR p.status COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                ORDER BY p.patient_id DESC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;

        ArrayList<Patient> patients = new ArrayList<>();
        try (PreparedStatement stmt = ad.getConnection().prepareStatement(sql)) {
            // FullName
            stmt.setNString(1, fullName);
            stmt.setNString(2, fullName != null ? "%" + fullName + "%" : null);

            // DOB
            stmt.setNString(3, dob);
            stmt.setNString(4, dob);
            stmt.setNString(5, dob);
            stmt.setNString(6, dob);
            stmt.setNString(7, dob);
            stmt.setNString(8, dob);
            stmt.setNString(9, dob);
            stmt.setNString(10, dob);

            // Gender
            stmt.setNString(11, gender);
            stmt.setNString(12, gender != null ? "%" + gender + "%" : null);

            stmt.setNString(13, status);
            stmt.setNString(14, status != null ? "%" + status + "%" : null);

            // Pagination
            int offset = (page - 1) * pageSize;
            stmt.setInt(15, offset);
            stmt.setInt(16, pageSize);

            // Execute query
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Patient patient = new Patient();
                patient.setId(rs.getInt("patient_id"));
                patient.setFullName(rs.getString("full_name"));
                patient.setDob(rs.getString("dob"));
                patient.setGender(rs.getString("gender"));
                patient.setPhone(rs.getString("phone"));
                patient.setAddress(rs.getString("address"));
                patient.setStatus(rs.getString("status"));
                patients.add(patient);
            }
            LOGGER.info("Fetched " + patients.size() + " patients (all patients view)");
        } catch (SQLException e) {
            LOGGER.severe("Error fetching all patients: " + e.getMessage());
            e.printStackTrace();
        }
        return patients;
    }

    // Method to count all patients for admin/staff view
    public int countAllPatients(String fullName, String dob, String gender, String status) {
        String query = """
                    SELECT COUNT(*) AS total
                    FROM Patient p
                    WHERE 
                        (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                        AND (? IS NULL OR
                            CONVERT(VARCHAR, p.dob, 120) COLLATE SQL_Latin1_General_CP1_CI_AI
                            LIKE CASE
                            WHEN ? LIKE '[0-9][0-9][0-9][0-9]' THEN ? + '%'
                            WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                            WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                            ELSE ? END)
                        AND (? IS NULL OR p.gender COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                        AND (? IS NULL OR p.status COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                """;

        try (PreparedStatement stmt = ad.getConnection().prepareStatement(query)) {
            // Full name filter
            stmt.setString(1, fullName);
            stmt.setString(2, fullName != null ? "%" + fullName + "%" : null);

            // dob year filter
            stmt.setString(3, dob);
            stmt.setString(4, dob);
            stmt.setString(5, dob);
            stmt.setString(6, dob);
            stmt.setString(7, dob);
            stmt.setString(8, dob);
            stmt.setString(9, dob);
            stmt.setString(10, dob);

            // Gender filter
            stmt.setString(11, gender);
            stmt.setString(12, gender != null ? "%" + gender + "%" : null);

            stmt.setNString(13, status);
            stmt.setNString(14, status != null ? "%" + status + "%" : null);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error counting all patients: " + e.getMessage());
            throw new RuntimeException("Failed to count all patients", e);
        }
    }

    public PatientDTO getPatientInfoByAccountPatientId(int accountPatientId) {
        String sql = """
                   SELECT [account_patient_id]
                          ,[img]
                          ,[status]
                   FROM [HealthCareSystem].[dbo].[AccountPatient] a
                   WHERE a.account_patient_id = ? AND a.status = 'Enable'
                """;

        try (Connection conn = ad.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountPatientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                PatientDTO patient = new PatientDTO();
                patient.setAccountStaffId(rs.getInt("account_patient_id"));
                patient.setImg(rs.getString("img"));
                return patient;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updatePatientIMG(int patientAccountId, String imgUrl) {
        String sql = """
                UPDATE [dbo].[AccountPatient]
                SET [img] = ?
                WHERE account_patient_id = ?
                """;
        try (PreparedStatement stmt = ad.getConnection().prepareStatement(sql)) {
            stmt.setNString(1, imgUrl);
            stmt.setInt(2, patientAccountId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(int accountPatientId, String currentPassword, String newPassword) throws SQLException {
        // Input validation
        if (currentPassword == null || newPassword == null || currentPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Passwords cannot be null or empty");
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }

        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            conn = ad.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Verify current password
            String checkPasswordSql = "SELECT password FROM [dbo].[AccountPatient] WHERE account_patient_id = ?";
            checkStmt = conn.prepareStatement(checkPasswordSql);
            checkStmt.setInt(1, accountPatientId);
            rs = checkStmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("No account found for ID: " + accountPatientId);
            }

            String storedPassword = rs.getString("password");
            // Note: Consider using proper password hashing comparison instead of plain text
            if (!storedPassword.equals(currentPassword)) {
                throw new SQLException("Current password is incorrect");
            }

            // Update password
            String updatePasswordSql = "UPDATE [dbo].[AccountPatient] SET password = ? WHERE account_patient_id = ?";
            updateStmt = conn.prepareStatement(updatePasswordSql);
            updateStmt.setString(1, newPassword); // Use setString instead of setNString
            updateStmt.setInt(2, accountPatientId);
            int rowsAffected = updateStmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new SQLException("Failed to update password");
            }

            conn.commit(); // Commit transaction
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw e; // Re-throw the SQLException
        } finally {
            // Close resources in reverse order
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (checkStmt != null) {
                try {
                    checkStmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (updateStmt != null) {
                try {
                    updateStmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ArrayList<Patient> getPatientsByAccountId(int accountPatientId) {
        Logger LOGGER = Logger.getLogger(this.getClass().getName());
        String sql = """
                SELECT p.patient_id, p.full_name
                FROM [HealthCareSystem].[dbo].[Patient] p
                INNER JOIN [HealthCareSystem].[dbo].[Patient_AccountPatient] pap 
                    ON p.patient_id = pap.patient_id
                INNER JOIN [HealthCareSystem].[dbo].[AccountPatient] ap
                    ON pap.account_patient_id = ap.account_patient_id
                WHERE pap.account_patient_id = ? 
                    AND p.status = 'Enable'
                    AND ap.status = 'Enable'
                ORDER BY p.patient_id DESC
                """;

        ArrayList<Patient> patients = new ArrayList<>();
        try (PreparedStatement stmt = ad.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, accountPatientId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Patient patient = new Patient();
                patient.setId(rs.getInt("patient_id"));
                patient.setFullName(rs.getString("full_name"));
                patients.add(patient);
            }
            LOGGER.info("Fetched " + patients.size() + " patients for accountPatientId=" + accountPatientId);
        } catch (SQLException e) {
            LOGGER.severe("Error fetching patients for accountPatientId=" + accountPatientId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return patients;
    }
    public static void main(String[] args) {
        PatientDAO patientDAO = new PatientDAO();
        ArrayList<Patient> patients = patientDAO.getAllPatients(null, null, "", "", 1, 50);
        System.out.println(patients.size());

        PatientDTO patientDTO = patientDAO.getPatientInfoByAccountPatientId(1);
        System.out.println(patientDTO);
        
        try {
            boolean p2 = patientDAO.updatePassword(1, "P@ss123", "11111111");
            System.out.println(p2);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public boolean createPatientAccount(
            String username,
            String password,
            String email,
            String img,
            String fullName,
            String dob,
            String gender,
            String phone,
            String address) {
        String insertAccountSql = """
            INSERT INTO [dbo].[AccountPatient]
                (username, password, email, img, status)
            VALUES (?, ?, ?, ?, 'Enable');
            """;
        String insertPatientSql = """
            INSERT INTO [dbo].[Patient]
                (full_name, dob, gender, phone, address, status)
            VALUES (?, ?, ?, ?, ?, 'Enable');
            """;
        String linkSql = """
            INSERT INTO [dbo].[Patient_AccountPatient]
                (patient_id, account_patient_id)
            VALUES (?, ?);
            """;
        Connection conn = null;
        try {
            conn = ad.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Insert AccountPatient
            PreparedStatement psAccount = conn.prepareStatement(insertAccountSql, PreparedStatement.RETURN_GENERATED_KEYS);
            psAccount.setNString(1, username);
            psAccount.setNString(2, password); // **Note**: Hash password before saving in real app
            psAccount.setNString(3, email);
            psAccount.setNString(4, img);
            psAccount.executeUpdate();
            ResultSet rsAcc = psAccount.getGeneratedKeys();
            if (!rsAcc.next()) throw new SQLException("Failed to get AccountPatient ID");
            int accountId = rsAcc.getInt(1);

            // 2. Insert Patient
            PreparedStatement psPatient = conn.prepareStatement(insertPatientSql, PreparedStatement.RETURN_GENERATED_KEYS);
            psPatient.setNString(1, fullName);
            psPatient.setNString(2, dob);
            psPatient.setNString(3, gender);
            psPatient.setNString(4, phone);
            psPatient.setNString(5, address);
            psPatient.executeUpdate();
            ResultSet rsPat = psPatient.getGeneratedKeys();
            if (!rsPat.next()) throw new SQLException("Failed to get Patient ID");
            int patientId = rsPat.getInt(1);

            // 3. Link Patient_AccountPatient
            PreparedStatement psLink = conn.prepareStatement(linkSql);
            psLink.setInt(1, patientId);
            psLink.setInt(2, accountId);
            psLink.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // === PHARMACIST ===
    public boolean createPharmacistAccount(
            String username,
            String password,
            String email,
            String img,
            String fullName,
            String phone,
            String eduLevel) {
        String insertAccountSql = """
            INSERT INTO [dbo].[AccountPharmacist]
                (username, password, email, img, status)
            VALUES (?, ?, ?, ?, 'Enable');
            """;
        String insertPharmacistSql = """
            INSERT INTO [dbo].[Pharmacist]
                (full_name, phone, eduLevel, account_pharmacist_id)
            VALUES (?, ?, ?, ?);
            """;

        Connection conn = null;
        try {
            conn = ad.getConnection();
            conn.setAutoCommit(false);

            // Insert AccountPharmacist
            PreparedStatement psAcc = conn.prepareStatement(insertAccountSql, PreparedStatement.RETURN_GENERATED_KEYS);
            psAcc.setNString(1, username);
            psAcc.setNString(2, password);
            psAcc.setNString(3, email);
            psAcc.setNString(4, img);
            psAcc.executeUpdate();
            ResultSet rsAcc = psAcc.getGeneratedKeys();
            if (!rsAcc.next()) throw new SQLException("Cannot get AccountPharmacist ID");
            int accountId = rsAcc.getInt(1);

            // Insert Pharmacist info
            PreparedStatement psPhar = conn.prepareStatement(insertPharmacistSql);
            psPhar.setNString(1, fullName);
            psPhar.setNString(2, phone);
            psPhar.setNString(3, eduLevel);
            psPhar.setInt(4, accountId);
            psPhar.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


    // === STAFF ===
    public boolean createStaffAccount(
            String username,
            String password,
            String email,
            String img,
            String role,
            String fullName,
            String phone,
            String department,
            String eduLevel) {

        String insertAccountSql = """
            INSERT INTO [dbo].[AccountStaff]
                (username, password, email, role, img, status)
            VALUES (?, ?, ?, ?, ?, 'Enable');
            """;

        // Tùy role mà chèn bảng khác
        String insertStaffSql;
        switch (role) {
            case "Doctor":
                insertStaffSql = "INSERT INTO Doctor(full_name, department, phone, eduLevel, account_staff_id) VALUES (?, ?, ?, ?, ?)";
                break;
            case "Nurse":
                insertStaffSql = "INSERT INTO Nurse(full_name, department, phone, eduLevel, account_staff_id) VALUES (?, ?, ?, ?, ?)";
                break;
            case "Receptionist":
                insertStaffSql = "INSERT INTO Receptionist(full_name, phone, account_staff_id) VALUES (?, ?, ?)";
                break;
            case "AdminSys":
                insertStaffSql = "INSERT INTO AdminSystem(full_name, department, phone, account_staff_id) VALUES (?, ?, ?, ?)";
                break;
            case "AdminBusiness":
                insertStaffSql = "INSERT INTO AdminBusiness(full_name, department, phone, account_staff_id) VALUES (?, ?, ?, ?)";
                break;
            default:
                throw new IllegalArgumentException("Invalid staff role: " + role);
        }

        Connection conn = null;
        try {
            conn = ad.getConnection();
            conn.setAutoCommit(false);

            // Insert AccountStaff
            PreparedStatement psAcc = conn.prepareStatement(insertAccountSql, PreparedStatement.RETURN_GENERATED_KEYS);
            psAcc.setNString(1, username);
            psAcc.setNString(2, password);
            psAcc.setNString(3, email);
            psAcc.setNString(4, role);
            psAcc.setNString(5, img);
            psAcc.executeUpdate();
            ResultSet rsAcc = psAcc.getGeneratedKeys();
            if (!rsAcc.next()) throw new SQLException("Cannot get AccountStaff ID");
            int accountId = rsAcc.getInt(1);

            // Insert vào bảng phụ theo role
            PreparedStatement psStaff = conn.prepareStatement(insertStaffSql);
            psStaff.setNString(1, fullName);
            if (role.equals("Receptionist")) {
                psStaff.setNString(2, phone);
                psStaff.setInt(3, accountId);
            } else if (role.startsWith("Admin")) {
                psStaff.setNString(2, department);
                psStaff.setNString(3, phone);
                psStaff.setInt(4, accountId);
            } else { // Doctor / Nurse
                psStaff.setNString(2, department);
                psStaff.setNString(3, phone);
                psStaff.setNString(4, eduLevel);
                psStaff.setInt(5, accountId);
            }
            psStaff.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }


}
