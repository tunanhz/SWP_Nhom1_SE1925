package dal;

import dto.ReceptionistCheckInDTO;
import dto.WaitlistDTO;
import model.Patient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;


public class ReceptionistDAO {
    private static final Logger LOGGER = Logger.getLogger(ReceptionistDAO.class.getName());
    private final DBContext db = new DBContext();

    public Connection getConnection() throws SQLException {
        return db.getConnection();
    }

    public ArrayList<ReceptionistCheckInDTO> getAppointmentsByStatus(
            String searchQuery, String startDate, String endDate, String status,
            int page, int pageSize, String sortBy, String sortOrder) {
        ArrayList<ReceptionistCheckInDTO> appointments = new ArrayList<>();

        String sql = """
            SELECT 
                a.appointment_id,
                COALESCE(p.full_name, 'Unknown Patient') AS patient_name,
                COALESCE(d.full_name, 'Unknown Doctor') AS doctor_name,
                a.appointment_datetime,
                a.shift,
                a.status,
                a.note
            FROM 
                Appointment a
                LEFT JOIN Patient p ON a.patient_id = p.patient_id
                LEFT JOIN Doctor d ON a.doctor_id = d.doctor_id
            WHERE 
                a.status IN ('Pending', 'Confirmed', 'Completed', 'Cancelled')
                AND (? IS NULL OR COALESCE(p.full_name, '') COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? OR COALESCE(d.full_name, '') COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                AND ( (? IS NULL AND ? IS NULL) OR a.appointment_datetime BETWEEN ? AND ? )
                AND (? IS NULL OR UPPER(a.status) = UPPER(?))
            """;

        String sortColumn;
        switch (sortBy != null ? sortBy.toLowerCase() : "appointment_id") {
            case "appointment_datetime":
                sortColumn = "a.appointment_datetime";
                break;
            case "status":
                sortColumn = "a.status";
                break;
            default:
                sortColumn = "a.appointment_id";
        }

        String sortDirection = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
        sql += " ORDER BY " + sortColumn + " " + sortDirection +
                " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            LOGGER.info("Executing SQL: " + sql.replace("\n", " ").replaceAll("\\s+", " "));
            String searchQueryParam = searchQuery != null && !searchQuery.trim().isEmpty()
                    ? searchQuery.trim().replaceAll("\\s+", " ") : null;
            stmt.setString(1, searchQueryParam);
            stmt.setString(2, searchQueryParam != null ? "%" + searchQueryParam + "%" : null);
            stmt.setString(3, searchQueryParam != null ? "%" + searchQueryParam + "%" : null);

            Timestamp startTimestamp = (startDate != null) ? Timestamp.valueOf(LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)) : null;
            Timestamp endTimestamp = (endDate != null) ? Timestamp.valueOf(LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)) : null;
            LOGGER.info("Converted timestamps: startDate=" + startTimestamp + ", endDate=" + endTimestamp);

            stmt.setObject(4, startTimestamp); // For IS NULL check
            stmt.setObject(5, endTimestamp);   // For IS NULL check
            stmt.setTimestamp(6, startTimestamp); // For BETWEEN start
            stmt.setTimestamp(7, endTimestamp);   // For BETWEEN end

            String statusParam = status != null && !status.trim().isEmpty() ? status : null;
            stmt.setString(8, statusParam);
            stmt.setString(9, statusParam != null ? statusParam : null);

            int offset = (page - 1) * pageSize;
            stmt.setInt(10, offset);
            stmt.setInt(11, pageSize);

            LOGGER.info("Parameters: searchQuery=" + searchQueryParam + ", startDate=" + startTimestamp +
                    ", endDate=" + endTimestamp + ", status=" + statusParam + ", offset=" + offset + ", pageSize=" + pageSize);

            // Debug query to verify data
            String debugSql = "SELECT COUNT(*) FROM Appointment WHERE appointment_datetime BETWEEN ? AND ?";
            try (PreparedStatement debugStmt = conn.prepareStatement(debugSql)) {
                debugStmt.setTimestamp(1, startTimestamp);
                debugStmt.setTimestamp(2, endTimestamp);
                ResultSet debugRs = debugStmt.executeQuery();
                if (debugRs.next()) {
                    LOGGER.info("Debug count (no joins): " + debugRs.getInt(1));
                }
            } catch (SQLException e) {
                LOGGER.severe("Debug SQL Error: " + e.getMessage());
            }

            // Debug to log filtered rows with joins
            String debugFilterSql = "SELECT a.appointment_datetime FROM Appointment a LEFT JOIN Patient p ON a.patient_id = p.patient_id LEFT JOIN Doctor d ON a.doctor_id = d.doctor_id WHERE a.appointment_datetime BETWEEN ? AND ?";
            try (PreparedStatement debugFilterStmt = conn.prepareStatement(debugFilterSql)) {
                debugFilterStmt.setTimestamp(1, startTimestamp);
                debugFilterStmt.setTimestamp(2, endTimestamp);
                ResultSet debugFilterRs = debugFilterStmt.executeQuery();
                while (debugFilterRs.next()) {
                    LOGGER.info("Debug filtered date with joins: " + debugFilterRs.getTimestamp(1));
                }
            } catch (SQLException e) {
                LOGGER.severe("Debug Filter SQL Error: " + e.getMessage());
            }

            ResultSet rs = stmt.executeQuery();
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                ReceptionistCheckInDTO appt = new ReceptionistCheckInDTO();
                appt.setAppointmentId(rs.getInt("appointment_id"));
                appt.setPatientName(rs.getString("patient_name"));
                appt.setDoctorName(rs.getString("doctor_name"));
                Timestamp timestamp = rs.getTimestamp("appointment_datetime");
                appt.setAppointmentDatetime(timestamp != null ? new Date(timestamp.getTime()) : null);
                appt.setShift(rs.getString("shift"));
                appt.setStatus(rs.getString("status"));
                appt.setNote(rs.getString("note"));
                appointments.add(appt);
                LOGGER.fine("Mapped appointment: ID=" + appt.getAppointmentId() + ", status=" + appt.getStatus());
            }
            LOGGER.info("Fetched " + rowCount + " rows, returned " + appointments.size() + " appointments");
        } catch (SQLException e) {
            LOGGER.severe("SQL Error: " + e.getMessage() + "\nSQL State: " + e.getSQLState() +
                    "\nError Code: " + e.getErrorCode());
            throw new RuntimeException("Failed to fetch appointments", e);
        }
        return appointments;
    }

    public int countAppointmentsByStatus(String searchQuery, String startDate, String endDate, String status) {
        String sql = """
            SELECT COUNT(*) AS total
            FROM 
                Appointment a
                LEFT JOIN Patient p ON a.patient_id = p.patient_id
                LEFT JOIN Doctor d ON a.doctor_id = d.doctor_id
            WHERE 
                a.status IN ('Pending', 'Confirmed', 'Completed', 'Cancelled')
                AND ( ? IS NULL OR COALESCE(p.full_name, '') COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? OR COALESCE(d.full_name, '') COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                AND ( (? IS NULL AND ? IS NULL) OR a.appointment_datetime BETWEEN ? AND ? )
                AND (? IS NULL OR UPPER(a.status) = UPPER(?))
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchQueryParam = searchQuery != null && !searchQuery.trim().isEmpty()
                    ? searchQuery.trim().replaceAll("\\s+", " ") : null;
            stmt.setString(1, searchQueryParam);
            stmt.setString(2, searchQueryParam != null ? "%" + searchQueryParam + "%" : null);
            stmt.setString(3, searchQueryParam != null ? "%" + searchQueryParam + "%" : null);

            Timestamp startTimestamp = (startDate != null) ? Timestamp.valueOf(LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)) : null;
            Timestamp endTimestamp = (endDate != null) ? Timestamp.valueOf(LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)) : null;
            LOGGER.info("Converted timestamps: startDate=" + startTimestamp + ", endDate=" + endTimestamp);

            stmt.setObject(4, startTimestamp); // For IS NULL check
            stmt.setObject(5, endTimestamp);   // For IS NULL check
            stmt.setTimestamp(6, startTimestamp); // For BETWEEN start
            stmt.setTimestamp(7, endTimestamp);   // For BETWEEN end

            String statusParam = status != null && !status.trim().isEmpty() ? status : null;
            stmt.setString(8, statusParam);
            stmt.setString(9, statusParam != null ? statusParam : null);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.severe("Error counting appointments: " + e.getMessage() + "\nSQL State: " + e.getSQLState() +
                    "\nError Code: " + e.getErrorCode());
            throw new RuntimeException("Failed to count appointments", e);
        }
    }

    public int getReceptionistByAccountStaffId(int accountStaffId) {
        String sql = "SELECT receptionist_id FROM Receptionist WHERE account_staff_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountStaffId);
            LOGGER.info("Executing SQL: " + sql + " with account_staff_id=" + accountStaffId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int receptionistId = rs.getInt("receptionist_id");
                LOGGER.info("Found receptionist_id=" + receptionistId + " for account_staff_id=" + accountStaffId);
                return receptionistId;
            } else {
                LOGGER.warning("No receptionist found for account_staff_id=" + accountStaffId);
                return -1; // Or throw an exception, depending on your use case
            }
        } catch (SQLException e) {
            LOGGER.severe("Error fetching receptionist for account_staff_id=" + accountStaffId + ": " +
                    e.getMessage() + "\nSQL State: " + e.getSQLState() + "\nError Code: " + e.getErrorCode());
            throw new RuntimeException("Failed to fetch receptionist by account_staff_id", e);
        }
    }

    public boolean checkInAppointment(int appointmentId, int receptionistId) {
        String sql = """
            UPDATE Appointment
            SET status = 'Confirmed', receptionist_id = ?
            WHERE appointment_id = ? AND status = 'Pending';
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, receptionistId);
            stmt.setInt(2, appointmentId);
            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;
            LOGGER.info("Check-in appointment ID " + appointmentId + " by receptionist ID " + receptionistId + ": " + (success ? "Success" : "Failed"));
            return success;
        } catch (SQLException e) {
            LOGGER.severe("Error checking in appointment ID " + appointmentId + ": " + e.getMessage() + "\nSQL State: " + e.getSQLState() +
                    "\nError Code: " + e.getErrorCode());
            throw new RuntimeException("Failed to check-in appointment", e);
        }
    }


    //waitlist
    public ArrayList<WaitlistDTO> getWaitlistEntries(
            String searchQuery, String startDate, String endDate, String status, String visitType,
            int page, int pageSize, String sortBy, String sortOrder) {
        ArrayList<WaitlistDTO> waitlistEntries = new ArrayList<>();

        String sql = """
            SELECT 
                w.waitlist_id,
                COALESCE(p.full_name, 'Unknown Patient') AS patient_name,
                COALESCE(d.full_name, 'Unknown Doctor') AS doctor_name,
                r.room_name,
                w.registered_at,
                w.estimated_time,
                w.visittype,
                w.status
            FROM 
                Waitlist w
                INNER JOIN Patient p ON w.patient_id = p.patient_id
                INNER JOIN Doctor d ON w.doctor_id = d.doctor_id
                LEFT JOIN Room r ON w.room_id = r.room_id
            WHERE 
                w.status IN ('Waiting', 'InProgress', 'Skipped', 'Completed')
                AND (? IS NULL OR COALESCE(p.full_name, '') COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? 
                    OR COALESCE(d.full_name, '') COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?
                    OR COALESCE(r.room_name, '') COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                AND ( (? IS NULL AND ? IS NULL) OR w.registered_at BETWEEN ? AND ? )
                AND (? IS NULL OR UPPER(w.status) = UPPER(?))
                AND (? IS NULL OR UPPER(w.visittype) = UPPER(?))
            """;

        String sortColumn;
        switch (sortBy != null ? sortBy.toLowerCase() : "waitlist_id") {
            case "registered_at":
                sortColumn = "w.registered_at";
                break;
            case "estimated_time":
                sortColumn = "w.estimated_time";
                break;
            case "status":
                sortColumn = "w.status";
                break;
            default:
                sortColumn = "w.waitlist_id";
        }

        String sortDirection = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
        sql += " ORDER BY " + sortColumn + " " + sortDirection +
                " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            LOGGER.info("Executing Waitlist SQL: " + sql.replace("\n", " ").replaceAll("\\s+", " "));
            String searchQueryParam = searchQuery != null && !searchQuery.trim().isEmpty()
                    ? searchQuery.trim().replaceAll("\\s+", " ") : null;
            stmt.setString(1, searchQueryParam);
            stmt.setString(2, searchQueryParam != null ? "%" + searchQueryParam + "%" : null);
            stmt.setString(3, searchQueryParam != null ? "%" + searchQueryParam + "%" : null);
            stmt.setString(4, searchQueryParam != null ? "%" + searchQueryParam + "%" : null);

            Timestamp startTimestamp = (startDate != null) ? Timestamp.valueOf(LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)) : null;
            Timestamp endTimestamp = (endDate != null) ? Timestamp.valueOf(LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)) : null;
            LOGGER.info("Converted waitlist timestamps: startDate=" + startTimestamp + ", endDate=" + endTimestamp);

            stmt.setObject(5, startTimestamp);
            stmt.setObject(6, endTimestamp);
            stmt.setTimestamp(7, startTimestamp);
            stmt.setTimestamp(8, endTimestamp);

            String statusParam = status != null && !status.trim().isEmpty() ? status : null;
            stmt.setString(9, statusParam);
            stmt.setString(10, statusParam != null ? statusParam : null);

            String visitTypeParam = visitType != null && !visitType.trim().isEmpty() ? visitType : null;
            stmt.setString(11, visitTypeParam);
            stmt.setString(12, visitTypeParam != null ? visitTypeParam : null);

            int offset = (page - 1) * pageSize;
            stmt.setInt(13, offset);
            stmt.setInt(14, pageSize);

            LOGGER.info("Waitlist Parameters: searchQuery=" + searchQueryParam + ", startDate=" + startTimestamp +
                    ", endDate=" + endTimestamp + ", status=" + statusParam + ", visitType=" + visitTypeParam +
                    ", offset=" + offset + ", pageSize=" + pageSize);

            ResultSet rs = stmt.executeQuery();
            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                WaitlistDTO entry = new WaitlistDTO();
                entry.setWaitlistId(rs.getInt("waitlist_id"));
                entry.setPatientName(rs.getString("patient_name"));
                entry.setDoctorName(rs.getString("doctor_name"));
                entry.setRoomName(rs.getString("room_name"));
                Timestamp registeredAt = rs.getTimestamp("registered_at");
                entry.setRegisteredAt(registeredAt != null ? new Date(registeredAt.getTime()) : null);
                Timestamp estimatedTime = rs.getTimestamp("estimated_time");
                entry.setEstimatedTime(estimatedTime != null ? new Date(estimatedTime.getTime()) : null);
                entry.setVisitType(rs.getString("visittype"));
                entry.setStatus(rs.getString("status"));
                waitlistEntries.add(entry);
                LOGGER.fine("Mapped waitlist entry: ID=" + entry.getWaitlistId() + ", status=" + entry.getStatus());
            }
            LOGGER.info("Fetched " + rowCount + " waitlist rows, returned " + waitlistEntries.size() + " entries");
        } catch (SQLException e) {
            LOGGER.severe("Waitlist SQL Error: " + e.getMessage() + "\nSQL State: " + e.getSQLState() +
                    "\nError Code: " + e.getErrorCode());
            throw new RuntimeException("Failed to fetch waitlist entries", e);
        }
        return waitlistEntries;
    }

    public int countWaitlistEntries(String searchQuery, String startDate, String endDate, String status, String visitType) {
        String sql = """
            SELECT COUNT(*) AS total
            FROM 
                Waitlist w
                INNER JOIN Patient p ON w.patient_id = p.patient_id
                INNER JOIN Doctor d ON w.doctor_id = d.doctor_id
                LEFT JOIN Room r ON w.room_id = r.room_id
            WHERE 
                w.status IN ('Waiting', 'InProgress', 'Skipped', 'Completed')
                AND ( ? IS NULL OR COALESCE(p.full_name, '') COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? 
                    OR COALESCE(d.full_name, '') COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?
                    OR COALESCE(r.room_name, '') COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                AND ( (? IS NULL AND ? IS NULL) OR w.registered_at BETWEEN ? AND ? )
                AND (? IS NULL OR UPPER(w.status) = UPPER(?))
                AND (? IS NULL OR UPPER(w.visittype) = UPPER(?))
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchQueryParam = searchQuery != null && !searchQuery.trim().isEmpty()
                    ? searchQuery.trim().replaceAll("\\s+", " ") : null;
            stmt.setString(1, searchQueryParam);
            stmt.setString(2, searchQueryParam != null ? "%" + searchQueryParam + "%" : null);
            stmt.setString(3, searchQueryParam != null ? "%" + searchQueryParam + "%" : null);
            stmt.setString(4, searchQueryParam != null ? "%" + searchQueryParam + "%" : null);

            Timestamp startTimestamp = (startDate != null) ? Timestamp.valueOf(LocalDateTime.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)) : null;
            Timestamp endTimestamp = (endDate != null) ? Timestamp.valueOf(LocalDateTime.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)) : null;

            stmt.setObject(5, startTimestamp);
            stmt.setObject(6, endTimestamp);
            stmt.setTimestamp(7, startTimestamp);
            stmt.setTimestamp(8, endTimestamp);

            String statusParam = status != null && !status.trim().isEmpty() ? status : null;
            stmt.setString(9, statusParam);
            stmt.setString(10, statusParam != null ? statusParam : null);

            String visitTypeParam = visitType != null && !visitType.trim().isEmpty() ? visitType : null;
            stmt.setString(11, visitTypeParam);
            stmt.setString(12, visitTypeParam != null ? visitTypeParam : null);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.severe("Error counting waitlist entries: " + e.getMessage() + "\nSQL State: " + e.getSQLState() +
                    "\nError Code: " + e.getErrorCode());
            throw new RuntimeException("Failed to count waitlist entries", e);
        }
    }

    public boolean updateEstimatedTime(int waitlistId, String estimatedTimeStr) {
        String sql = """
            UPDATE Waitlist
            SET estimated_time = ?
            WHERE waitlist_id = ? AND visittype = 'Initial' AND status = 'Waiting';
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            Timestamp estimatedTime = (estimatedTimeStr != null) ?
                    Timestamp.valueOf(LocalDateTime.parse(estimatedTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)) : null;
            stmt.setTimestamp(1, estimatedTime);
            stmt.setInt(2, waitlistId);
            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;
            LOGGER.info("Update estimated_time for waitlist ID " + waitlistId + ": " + (success ? "Success" : "Failed"));
            return success;
        } catch (SQLException e) {
            LOGGER.severe("Error updating estimated_time for waitlist ID " + waitlistId + ": " + e.getMessage() +
                    "\nSQL State: " + e.getSQLState() + "\nError Code: " + e.getErrorCode());
            throw new RuntimeException("Failed to update estimated_time", e);
        }
    }

    //book be half
    public ArrayList<Patient> getPatients(
            String searchQuery, String dob, String gender,
            int page, int pageSize, String sortBy, String sortOrder) {
        ArrayList<Patient> patients = new ArrayList<>();

        String sql = """
            SELECT 
                patient_id,
                full_name,
                dob,
                gender,
                phone,
                address
            FROM 
                Patient
            WHERE 
                status = 'Enable'
                AND (? IS NULL OR COALESCE(full_name, '') COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? 
                    OR COALESCE(phone, '') COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                AND (? IS NULL OR CONVERT(VARCHAR, dob, 23) LIKE ? + '%')
                AND (? IS NULL OR gender = ?)
            """;

        String sortColumn;
        switch (sortBy != null ? sortBy.toLowerCase() : "patient_id") {
            case "full_name":
                sortColumn = "full_name";
                break;
            case "dob":
                sortColumn = "dob";
                break;
            case "phone":
                sortColumn = "phone";
                break;
            default:
                sortColumn = "patient_id";
        }

        String sortDirection = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
        sql += " ORDER BY " + sortColumn + " " + sortDirection +
                " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy");
        inputFormat.setLenient(false);
        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd");

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            LOGGER.info("Executing SQL: " + sql.replace("\n", " ").replaceAll("\\s+", " "));
            String searchQueryParam = searchQuery != null && !searchQuery.trim().isEmpty()
                    ? searchQuery.trim().replaceAll("\\s+", " ") : null;
            stmt.setString(1, searchQueryParam);
            stmt.setString(2, searchQueryParam != null ? "%" + searchQueryParam + "%" : null);
            stmt.setString(3, searchQueryParam != null ? "%" + searchQueryParam + "%" : null);

            String dobParam = (dob != null && !dob.trim().isEmpty()) ? dob.trim() : null;
            stmt.setString(4, dobParam);
            stmt.setString(5, dobParam != null ? dobParam : null);

            String genderParam = (gender != null && !gender.trim().isEmpty() &&
                    !"All Gender".equalsIgnoreCase(gender)) ? gender : null;
            stmt.setString(6, genderParam);
            stmt.setString(7, genderParam);

            int offset = (page - 1) * pageSize;
            stmt.setInt(8, offset);
            stmt.setInt(9, pageSize);

            LOGGER.info("Parameters: searchQuery=" + searchQueryParam + ", dob=" + dobParam +
                    ", gender=" + genderParam + ", offset=" + offset + ", pageSize=" + pageSize);

            ResultSet rs = stmt.executeQuery();
            int rowCount = 0;
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
            while (rs.next()) {
                rowCount++;
                Patient patient = new Patient();
                patient.setId(rs.getInt("patient_id"));
                patient.setFullName(rs.getString("full_name"));
                java.sql.Date dbDob = rs.getDate("dob");
                patient.setDob(dbDob != null ? outputFormat.format(dbDob) : null);
                patient.setGender(rs.getString("gender"));
                patient.setPhone(rs.getString("phone"));
                patient.setAddress(rs.getString("address"));
                patients.add(patient);
                LOGGER.fine("Mapped patient: ID=" + patient.getId() + ", Name=" + patient.getFullName());
            }
            LOGGER.info("Fetched " + rowCount + " rows, returned " + patients.size() + " patients");
        } catch (SQLException e) {
            LOGGER.severe("SQL Error: " + e.getMessage() + "\nSQL State: " + e.getSQLState() +
                    "\nError Code: " + e.getErrorCode());
            throw new RuntimeException("Failed to fetch patients", e);
        }
        return patients;
    }

    public int countPatients(String searchQuery, String dob, String gender) {
        String sql = """
            SELECT COUNT(*) AS total
            FROM 
                Patient
            WHERE 
                status = 'Enable'
                AND (? IS NULL OR COALESCE(full_name, '') COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? 
                    OR COALESCE(phone, '') COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                AND (? IS NULL OR CONVERT(VARCHAR, dob, 23) LIKE ? + '%')
                AND (? IS NULL OR gender = ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            String searchQueryParam = searchQuery != null && !searchQuery.trim().isEmpty()
                    ? searchQuery.trim().replaceAll("\\s+", " ") : null;
            stmt.setString(1, searchQueryParam);
            stmt.setString(2, searchQueryParam != null ? "%" + searchQueryParam + "%" : null);
            stmt.setString(3, searchQueryParam != null ? "%" + searchQueryParam + "%" : null);

            String dobParam = (dob != null && !dob.trim().isEmpty()) ? dob.trim() : null;
            stmt.setString(4, dobParam);
            stmt.setString(5, dobParam != null ? dobParam : null);

            String genderParam = (gender != null && !gender.trim().isEmpty() &&
                    !"All Gender".equalsIgnoreCase(gender)) ? gender : null;
            stmt.setString(6, genderParam);
            stmt.setString(7, genderParam);

            LOGGER.info("Parameters for count: searchQuery=" + searchQueryParam + ", dob=" + dobParam +
                    ", gender=" + genderParam);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            LOGGER.severe("Error counting patients: " + e.getMessage() + "\nSQL State: " + e.getSQLState() +
                    "\nError Code: " + e.getErrorCode());
            throw new RuntimeException("Failed to count patients", e);
        }
    }

    public boolean addPatient(Patient patient) throws SQLException {
        String sql = """
            INSERT INTO [dbo].[Patient] ([full_name], [dob], [gender], [phone], [address], [status])
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, patient.getFullName());
            stmt.setDate(2, patient.getDob() != null ? java.sql.Date.valueOf(patient.getDob()) : null);
            stmt.setString(3, patient.getGender());
            stmt.setString(4, patient.getPhone());
            stmt.setString(5, patient.getAddress());
            stmt.setString(6, "Enable"); // Default status
            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;
            LOGGER.info("Adding patient: Name=" + patient.getFullName() + ", Success=" + success);
            return success;
        } catch (SQLException e) {
            LOGGER.severe("Error adding patient: " + e.getMessage() + "\nSQL State: " + e.getSQLState() +
                    "\nError Code: " + e.getErrorCode());
            throw new RuntimeException("Failed to add patient", e);
        }
    }



    public static void main(String[] args) {
        ReceptionistDAO dao = new ReceptionistDAO();
        String startDate = "2025-06-01T00:00:00"; // Start of the range
        String endDate = "2025-06-30T23:59:59";   // End of the range
        ArrayList<ReceptionistCheckInDTO> appointments = dao.getAppointmentsByStatus(null, startDate, endDate, null, 1, 10, "appointment_id", "ASC");
        System.out.println("Appointments: " + appointments.size());
        for (ReceptionistCheckInDTO appt : appointments) {
            System.out.println("ID: " + appt.getAppointmentId() + ", Date: " + appt.getAppointmentDatetime() + ", Status: " + appt.getStatus());
        }
        int totalCount = dao.countAppointmentsByStatus(null, startDate, endDate, null);
        System.out.println("Total count: " + totalCount);

        //test_waitlist
        ArrayList<WaitlistDTO> waitlistEntries = dao.getWaitlistEntries(null, startDate, endDate, null, null, 1, 10, "waitlist_id", "ASC");
        System.out.println("Waitlist Entries: " + waitlistEntries.size());
        for (WaitlistDTO entry : waitlistEntries) {
            System.out.println("ID: " + entry.getWaitlistId() + ", Registered At: " + entry.getRegisteredAt() + ", Status: " + entry.getStatus());
        }
        int totalWaitlistCount = dao.countWaitlistEntries(null, startDate, endDate, null, null);
        System.out.println("Total waitlist count: " + totalWaitlistCount);
    }

}