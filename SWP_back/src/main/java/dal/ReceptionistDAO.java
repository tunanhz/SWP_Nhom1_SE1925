package dal;

import dto.PatientPaymentDTO;
import dto.ReceptionistCheckInDTO;
import dto.ReceptionistDTO;
import dto.WaitlistDTO;
import model.Appointment;
import model.Patient;
import model.Receptionist;
import model.Waitlist;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ReceptionistDAO {
    private static final Logger LOGGER = Logger.getLogger(ReceptionistDAO.class.getName());
    private final DBContext db = new DBContext();
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    private Appointment getAppointmentById(int appointmentId, Connection conn) throws SQLException {
        String sql = """
                    SELECT 
                        a.appointment_id,
                        a.patient_id,
                        a.doctor_id,
                        a.appointment_datetime,
                        a.shift,
                        a.status,
                        a.note
                    FROM 
                        Appointment a
                    WHERE 
                        a.appointment_id = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            LOGGER.info("Executing SQL to get appointment: " + sql + " with appointment_id=" + appointmentId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Appointment appt = new Appointment();
                appt.setAppointmentId(rs.getInt("appointment_id"));
                appt.setPatientId(rs.getInt("patient_id"));
                appt.setDoctorId(rs.getInt("doctor_id"));
                Timestamp timestamp = rs.getTimestamp("appointment_datetime");
                appt.setAppointmentDatetime(timestamp != null ? new Date(timestamp.getTime()) : null);
                appt.setShift(rs.getString("shift"));
                appt.setStatus(rs.getString("status"));
                appt.setNote(rs.getString("note"));
                LOGGER.info("Found appointment: ID=" + appt.getAppointmentId());
                return appt;
            } else {
                LOGGER.warning("No appointment found for appointment_id=" + appointmentId);
                return null;
            }
        }
    }

    // Get room_id from DoctorSchedule based on doctor_id, appointment_datetime, and shift
    private Integer getRoomIdForAppointment(int doctorId, Date appointmentDatetime, String shift, Connection conn) throws SQLException {
        String sql = """
                    SELECT 
                        ds.room_id
                    FROM 
                        Appointment a
                        INNER JOIN Doctor d ON a.doctor_id = d.doctor_id
                        INNER JOIN DoctorSchedule ds ON a.doctor_id = ds.doctor_id
                            AND CAST(a.appointment_datetime AS DATE) = ds.working_date
                            AND a.shift = ds.shift
                    WHERE 
                        a.doctor_id = ?
                        AND a.appointment_datetime = ?
                        AND a.shift = ?
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            stmt.setTimestamp(2, new Timestamp(appointmentDatetime.getTime()));
            stmt.setString(3, shift);
            LOGGER.info("Executing SQL to get room_id: " + sql + " with doctor_id=" + doctorId +
                    ", appointment_datetime=" + appointmentDatetime + ", shift=" + shift);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int roomId = rs.getInt("room_id");
                LOGGER.info("Found room_id=" + roomId);
                return roomId;
            } else {
                LOGGER.warning("No room_id found for doctor_id=" + doctorId + ", appointment_datetime=" +
                        appointmentDatetime + ", shift=" + shift);
                return null;
            }
        }
    }

    // Add record to Waitlist
    private boolean addToWaitlist(Waitlist waitlist, Connection conn) throws SQLException {
        String sql = """
                    INSERT INTO [dbo].[Waitlist]
                        ([patient_id], [doctor_id], [room_id], [registered_at], [estimated_time], [visittype], [status])
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, waitlist.getPatientId());
            stmt.setInt(2, waitlist.getDoctorId());
            stmt.setInt(3, waitlist.getRoomId());
            String registeredAtStr = waitlist.getRegisteredAt();
            String estimatedTimeStr = waitlist.getEstimatedTime();
            LOGGER.info("Adding to waitlist: registered_at=" + registeredAtStr + ", estimated_time=" + estimatedTimeStr);
            stmt.setTimestamp(4, Timestamp.valueOf(registeredAtStr));
            stmt.setTimestamp(5, Timestamp.valueOf(estimatedTimeStr));
            stmt.setString(6, waitlist.getVisittype());
            stmt.setString(7, waitlist.getStatus());

            LOGGER.info("Executing SQL to add waitlist: patient_id=" + waitlist.getPatientId() +
                    ", doctor_id=" + waitlist.getDoctorId() + ", room_id=" + waitlist.getRoomId());

            int rowsAffected = stmt.executeUpdate();
            boolean success = rowsAffected > 0;
            LOGGER.info("Add to waitlist: " + (success ? "Success" : "Failed"));
            return success;
        }
    }

    // Updated checkInAppointment to update Appointment and add to Waitlist
    public boolean checkInAppointment(int appointmentId, int receptionistId) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Step 1: Update Appointment status to Confirmed
            String updateSql = """
                        UPDATE Appointment
                        SET status = 'Confirmed', receptionist_id = ?
                        WHERE appointment_id = ? AND status = 'Pending'
                    """;
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setInt(1, receptionistId);
                updateStmt.setInt(2, appointmentId);
                int rowsAffected = updateStmt.executeUpdate();
                if (rowsAffected == 0) {
                    LOGGER.warning("Failed to update appointment ID " + appointmentId +
                            ": Appointment not found or not in Pending status");
                    conn.rollback();
                    return false;
                }
                LOGGER.info("Updated appointment ID " + appointmentId + " to Confirmed");
            }

            // Step 2: Get Appointment details
            Appointment appointment = getAppointmentById(appointmentId, conn);
            if (appointment == null) {
                LOGGER.warning("Appointment not found for ID " + appointmentId);
                conn.rollback();
                return false;
            }

            // Step 3: Get room_id from DoctorSchedule
            Integer roomId = getRoomIdForAppointment(
                    appointment.getDoctorId(),
                    appointment.getAppointmentDatetime(),
                    appointment.getShift(),
                    conn
            );
            if (roomId == null) {
                LOGGER.warning("No room_id found for appointment ID " + appointmentId);
                conn.rollback();
                return false;
            }

            // Step 4: Create Waitlist entry
            Waitlist waitlist = new Waitlist();
            waitlist.setPatientId(appointment.getPatientId());
            waitlist.setDoctorId(appointment.getDoctorId());
            waitlist.setRoomId(roomId);
            waitlist.setRegisteredAt(LocalDateTime.now().format(TIMESTAMP_FORMATTER));
            waitlist.setEstimatedTime(new Timestamp(appointment.getAppointmentDatetime().getTime())
                    .toLocalDateTime().format(TIMESTAMP_FORMATTER));
            waitlist.setVisittype("Initial");
            waitlist.setStatus("Waiting");

            if (!addToWaitlist(waitlist, conn)) {
                LOGGER.warning("Failed to add to waitlist for appointment ID " + appointmentId);
                conn.rollback();
                return false;
            }

            conn.commit(); // Commit transaction
            LOGGER.info("Check-in successful for appointment ID " + appointmentId);
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.info("Transaction rolled back for appointment ID " + appointmentId);
                } catch (SQLException rollbackEx) {
                    LOGGER.severe("Error during rollback: " + rollbackEx.getMessage() +
                            "\nSQL State: " + rollbackEx.getSQLState() + "\nError Code: " + rollbackEx.getErrorCode());
                }
            }
            LOGGER.severe("Error checking in appointment ID " + appointmentId + ": " + e.getMessage() +
                    "\nSQL State: " + e.getSQLState() + "\nError Code: " + e.getErrorCode());
            throw new RuntimeException("Failed to check-in appointment: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    if (!conn.isClosed()) {
                        conn.setAutoCommit(true);
                        conn.close();
                        LOGGER.info("Connection closed successfully for appointment ID " + appointmentId);
                    }
                } catch (SQLException e) {
                    LOGGER.severe("Error closing connection: " + e.getMessage() +
                            "\nSQL State: " + e.getSQLState() + "\nError Code: " + e.getErrorCode());
                }
            }
        }
    }

//    public boolean checkInAppointment(int appointmentId, int receptionistId) {
//        String sql = """
//            UPDATE Appointment
//            SET status = 'Confirmed', receptionist_id = ?
//            WHERE appointment_id = ? AND status = 'Pending';
//        """;
//
//        try (Connection conn = db.getConnection();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setInt(1, receptionistId);
//            stmt.setInt(2, appointmentId);
//            int rowsAffected = stmt.executeUpdate();
//            boolean success = rowsAffected > 0;
//            LOGGER.info("Check-in appointment ID " + appointmentId + " by receptionist ID " + receptionistId + ": " + (success ? "Success" : "Failed"));
//            return success;
//        } catch (SQLException e) {
//            LOGGER.severe("Error checking in appointment ID " + appointmentId + ": " + e.getMessage() + "\nSQL State: " + e.getSQLState() +
//                    "\nError Code: " + e.getErrorCode());
//            throw new RuntimeException("Failed to check-in appointment", e);
//        }
//    }


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

    public Appointment createAppointment(Appointment appointment, int receptionistId) throws SQLException {
        String sql = "{call CreateAppointment(?, ?, ?, ?, ?, ?)}";
        Appointment createdAppointment = null;
        Connection conn = null;
        CallableStatement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.prepareCall(sql);

            stmt.setInt(1, appointment.getDoctorId());
            stmt.setInt(2, appointment.getPatientId());
            stmt.setTimestamp(3, new Timestamp(appointment.getAppointmentDatetime().getTime()));
            stmt.setInt(4, receptionistId);
            stmt.setString(5, appointment.getNote());
            stmt.registerOutParameter(6, java.sql.Types.NVARCHAR);

            LOGGER.info("Executing stored procedure: " + sql + " with parameters: doctorId=" + appointment.getDoctorId() +
                    ", patientId=" + appointment.getPatientId() + ", datetime=" + appointment.getAppointmentDatetime() +
                    ", receptionistId=" + receptionistId + ", note=" + appointment.getNote());
            stmt.execute();

            String message = stmt.getString(6);
            LOGGER.info("Stored Procedure Message: " + message);

            if (message != null && message.contains("thành công")) {
                createdAppointment = appointment;
            } else {
                throw new SQLException("Stored Procedure failed: " + message);
            }
        } catch (SQLException e) {
            LOGGER.severe("SQL Error in createAppointment: " + e.getMessage() + ", SQL State: " + e.getSQLState() +
                    ", Error Code: " + e.getErrorCode());
            throw e;
        } finally {
            if (stmt != null) try {
                stmt.close();
            } catch (SQLException e) {
                LOGGER.severe("Error closing statement: " + e.getMessage());
            }
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.severe("Error closing connection: " + e.getMessage());
            }
        }
        return createdAppointment;
    }

    public List<ReceptionistCheckInDTO> getTop3AppointmentsPerDay(String startDate, String endDate) {
        List<ReceptionistCheckInDTO> appointments = new ArrayList<>();
        String sql = "SELECT DISTINCT appointment_date, appointment_id, patient_name, doctor_name, appointment_datetime, shift, status, note " +
                "FROM (SELECT CONVERT(date, a.appointment_datetime) AS appointment_date, a.appointment_id AS appointment_id, " +
                "COALESCE(p.full_name, 'Unknown Patient') AS patient_name, COALESCE(d.full_name, 'Unknown Doctor') AS doctor_name, " +
                "a.appointment_datetime, a.shift, a.status, a.note, " +
                "ROW_NUMBER() OVER (PARTITION BY CONVERT(date, a.appointment_datetime) ORDER BY a.appointment_datetime) AS row_num " +
                "FROM Appointment a " +
                "LEFT JOIN Patient p ON a.patient_id = p.patient_id " +
                "LEFT JOIN Doctor d ON a.doctor_id = d.doctor_id " +
                "WHERE a.appointment_datetime BETWEEN ? AND ? " +
                "AND a.status = 'Pending') AS ranked_appointments " +
                "WHERE row_num <= 3 " +
                "ORDER BY appointment_date, appointment_datetime";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, startDate + " 00:00:00");
            stmt.setString(2, endDate + " 23:59:59");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ReceptionistCheckInDTO appointment = new ReceptionistCheckInDTO();
                appointment.setAppointmentId(rs.getInt("appointment_id"));
                appointment.setPatientName(rs.getString("patient_name"));
                appointment.setDoctorName(rs.getString("doctor_name"));
                Timestamp timestamp = rs.getTimestamp("appointment_datetime");
                appointment.setAppointmentDatetime(timestamp != null ? new Date(timestamp.getTime()) : null);
                appointment.setShift(rs.getString("shift"));
                appointment.setStatus(rs.getString("status"));
                appointment.setNote(rs.getString("note"));
                appointments.add(appointment);
            }
        } catch (SQLException e) {
            Logger.getLogger(ReceptionistDAO.class.getName()).log(Level.SEVERE, "Error fetching top 3 pending appointments per day", e);
            throw new RuntimeException("Failed to fetch top 3 appointments", e);
        }
        return appointments;
    }

    public List<WaitlistDTO> getTop3WaitlistEntriesPerDay(String startDate, String endDate) {
        List<WaitlistDTO> waitlistEntries = new ArrayList<>();
        String sql = """
                    SELECT DISTINCT waitlist_date, waitlist_id, patient_name, doctor_name, room_name, estimated_time, visittype, status
                    FROM (
                        SELECT 
                            CONVERT(date, w.estimated_time) AS waitlist_date,
                            w.waitlist_id AS waitlist_id,
                            COALESCE(p.full_name, 'Unknown Patient') AS patient_name,
                            COALESCE(d.full_name, 'Unknown Doctor') AS doctor_name,
                            COALESCE(r.room_name, 'Unknown Room') AS room_name,
                            w.estimated_time,
                            w.visittype,
                            w.status,
                            ROW_NUMBER() OVER (PARTITION BY CONVERT(date, w.estimated_time) ORDER BY w.estimated_time) AS row_num
                        FROM Waitlist w
                        INNER JOIN Patient p ON w.patient_id = p.patient_id
                        INNER JOIN Doctor d ON w.doctor_id = d.doctor_id
                        LEFT JOIN Room r ON w.room_id = r.room_id
                        WHERE w.estimated_time BETWEEN ? AND ?
                        AND w.visittype = 'Initial'
                        AND w.status = 'Waiting'
                    ) AS ranked_waitlist
                    WHERE row_num <= 3
                    ORDER BY waitlist_date, estimated_time
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, startDate + " 00:00:00");
            stmt.setString(2, endDate + " 23:59:59");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                WaitlistDTO entry = new WaitlistDTO();
                entry.setWaitlistId(rs.getInt("waitlist_id"));
                entry.setPatientName(rs.getString("patient_name"));
                entry.setDoctorName(rs.getString("doctor_name"));
                entry.setRoomName(rs.getString("room_name"));
                Timestamp timestamp = rs.getTimestamp("estimated_time");
                entry.setEstimatedTime(timestamp != null ? new Date(timestamp.getTime()) : null);
                entry.setVisitType(rs.getString("visittype"));
                entry.setStatus(rs.getString("status"));
                waitlistEntries.add(entry);
            }
            LOGGER.info("Fetched " + waitlistEntries.size() + " top 3 waitlist entries for date range: " + startDate + " to " + endDate);
        } catch (SQLException e) {
            LOGGER.severe("Error fetching top 3 waitlist entries per day: " + e.getMessage() +
                    "\nSQL State: " + e.getSQLState() + "\nError Code: " + e.getErrorCode());
            throw new RuntimeException("Failed to fetch top 3 waitlist entries", e);
        }
        return waitlistEntries;
    }

    public ReceptionistDTO getReceptionistInfoByAccountStaffId(int accountStaffId) throws SQLException {
        String sql = """
                    SELECT r.receptionist_id, r.full_name, r.phone, r.account_staff_id, a.img
                    FROM [dbo].[Receptionist] r
                    JOIN [dbo].[AccountStaff] a ON r.account_staff_id = a.account_staff_id
                    WHERE r.account_staff_id = ? AND a.status = 'Enable'
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountStaffId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                ReceptionistDTO receptionist = new ReceptionistDTO();
                receptionist.setReceptionistId(rs.getInt("receptionist_id"));
                receptionist.setFullName(rs.getString("full_name"));
                receptionist.setPhone(rs.getString("phone"));
                receptionist.setAccountStaffId(rs.getInt("account_staff_id"));
                receptionist.setImg(rs.getString("img"));
                LOGGER.info("Fetched receptionist for account_staff_id=" + accountStaffId);
                return receptionist;
            }
            LOGGER.warning("No receptionist found for account_staff_id=" + accountStaffId);
            return null;
        } catch (SQLException e) {
            LOGGER.severe("Error fetching receptionist: " + e.getMessage());
            throw e;
        }
    }

    public void updateReceptionistProfile(int receptionistId, String fullName, String phone, String imgUrl) throws SQLException {
        if (phone != null && !phone.matches("^0[0-9]{9}$")) {
            throw new SQLException("Phone number must be 10 digits starting with 0");
        }

        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            if (fullName != null || phone != null) {
                String updateReceptionistSql = """
                            UPDATE [dbo].[Receptionist]
                            SET full_name = COALESCE(?, full_name), phone = COALESCE(?, phone)
                            WHERE receptionist_id = ?
                        """;
                try (PreparedStatement stmt = conn.prepareStatement(updateReceptionistSql)) {
                    stmt.setString(1, fullName);
                    stmt.setString(2, phone);
                    stmt.setInt(3, receptionistId);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected == 0) {
                        LOGGER.warning("No receptionist found with receptionist_id=" + receptionistId);
                        throw new SQLException("No receptionist found");
                    }
                    LOGGER.info("Receptionist profile updated: receptionist_id=" + receptionistId);
                }
            }

            if (imgUrl != null) {
                String updateAccountStaffSql = """
                            UPDATE [dbo].[AccountStaff]
                            SET img = ?
                            WHERE account_staff_id = (SELECT account_staff_id FROM [dbo].[Receptionist] WHERE receptionist_id = ?)
                        """;
                try (PreparedStatement stmt = conn.prepareStatement(updateAccountStaffSql)) {
                    stmt.setString(1, imgUrl);
                    stmt.setInt(2, receptionistId);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected == 0) {
                        LOGGER.warning("No account_staff found for receptionist_id=" + receptionistId);
                        throw new SQLException("No AccountStaff found for receptionist");
                    }
                    LOGGER.info("Image updated in AccountStaff for receptionist_id=" + receptionistId);
                }
            }

            conn.commit();
            LOGGER.info("Receptionist profile updated successfully for receptionist_id=" + receptionistId);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.info("Transaction rolled back for receptionist_id=" + receptionistId);
                } catch (SQLException rollbackEx) {
                    LOGGER.severe("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            LOGGER.severe("Error updating receptionist profile: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.severe("Error closing connection: " + e.getMessage());
                }
            }
        }
    }

    public void updatePassword(int accountStaffId, String currentPassword, String newPassword) throws SQLException {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Kiểm tra mật khẩu hiện tại
            String checkPasswordSql = """
                        SELECT password FROM [dbo].[AccountStaff] WHERE account_staff_id = ?
                    """;
            try (PreparedStatement checkStmt = conn.prepareStatement(checkPasswordSql)) {
                checkStmt.setInt(1, accountStaffId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        LOGGER.warning("No account found with account_staff_id=" + accountStaffId);
                        throw new SQLException("No account found");
                    }
                    String storedPassword = rs.getString("password");
                    if (!BCrypt.checkpw(currentPassword, storedPassword)) {
                        LOGGER.warning("Current password mismatch for account_staff_id=" + accountStaffId);
                        throw new SQLException("Current password is incorrect");
                    }
                }
            }
            String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(12));
            String updatePasswordSql = """
                        UPDATE [dbo].[AccountStaff]
                        SET password = ?
                        WHERE account_staff_id = ?
                    """;
            try (PreparedStatement stmt = conn.prepareStatement(updatePasswordSql)) {
                stmt.setString(1, hashedNewPassword);
                stmt.setInt(2, accountStaffId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    LOGGER.warning("No account updated for account_staff_id=" + accountStaffId);
                    throw new SQLException("Failed to update password");
                }
                LOGGER.info("Password updated successfully for account_staff_id=" + accountStaffId);
            }

            conn.commit();
            LOGGER.info("Password update transaction committed for account_staff_id=" + accountStaffId);
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    LOGGER.info("Transaction rolled back for account_staff_id=" + accountStaffId);
                } catch (SQLException rollbackEx) {
                    LOGGER.severe("Error during rollback: " + rollbackEx.getMessage());
                }
            }
            LOGGER.severe("Error updating password: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.severe("Error closing connection: " + e.getMessage());
                }
            }
        }
    }


    //Payment - Patient
    public ArrayList<PatientPaymentDTO> getPatientInvoices(String fromDate, String toDate, String status, int page, int pageSize) {
        String sql = """
                SELECT 
                    i.invoice_id,
                    p.patient_id,
                    CONVERT(DATE, i.issue_date) as issue_date,
                    i.status AS invoice_status,
                    COALESCE(sv.service_details, 'Không có dịch vụ') AS service_details,
                    COALESCE(sv.total_service_cost, 0) AS total_service_cost,
                    COALESCE(med.medicine_details, 'Không có thuốc') AS medicine_details,
                    COALESCE(med.total_medicine_cost, 0) AS total_medicine_cost,
                    (COALESCE(sv.total_service_cost, 0) + COALESCE(med.total_medicine_cost, 0)) AS total_cost
                FROM Invoice i
                JOIN Patient p ON i.patient_id = p.patient_id
                LEFT JOIN (
                    SELECT 
                        si.invoice_id,
                        STRING_AGG(
                            CONCAT(lms.name, ': ', si.quantity, ' x ', si.unit_price, ' = ', si.total_price), 
                            '; '
                        ) AS service_details,
                        SUM(si.total_price) AS total_service_cost
                    FROM ServiceInvoice si
                    JOIN ServiceOrderItem soi ON si.service_order_item_id = soi.service_order_item_id
                    JOIN ListOfMedicalService lms ON soi.service_id = lms.service_id
                    GROUP BY si.invoice_id
                ) sv ON i.invoice_id = sv.invoice_id
                LEFT JOIN (
                    SELECT 
                        pi.invoice_id,
                        STRING_AGG(
                            CONCAT(m.name, ': ', med.quantity, ' x ', m.price, ' = ', (med.quantity * m.price)), 
                            '; '
                        ) AS medicine_details,
                        SUM(med.quantity * m.price) AS total_medicine_cost
                    FROM PrescriptionInvoice pi    
                    JOIN Medicines med ON pi.prescription_invoice_id = med.prescription_invoice_id
                    JOIN Medicine m ON med.medicine_id = m.medicine_id
                    GROUP BY pi.invoice_id
                ) med ON i.invoice_id = med.invoice_id
                WHERE
                    p.status = 'Enable'
                    AND (? IS NULL OR i.issue_date >= ?)
                    AND (? IS NULL OR i.issue_date <= ?)
                    AND (? IS NULL OR i.status COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                ORDER BY i.issue_date desc
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;
                """;

        ArrayList<PatientPaymentDTO> invoices = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set date range parameters
            stmt.setString(1, fromDate);
            stmt.setString(2, fromDate);
            stmt.setString(3, toDate);
            stmt.setString(4, toDate);

            // Set status parameters
            stmt.setString(5, status);
            stmt.setString(6, status != null ? "%" + status + "%" : null);

            // Set pagination parameters
            int offset = (page - 1) * pageSize;
            stmt.setInt(7, offset);
            stmt.setInt(8, pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PatientPaymentDTO invoice = new PatientPaymentDTO();
                    invoice.setInvoiceId(rs.getInt("invoice_id"));
                    invoice.setPatientId(rs.getInt("patient_id"));
                    invoice.setIssueDate(rs.getString("issue_date"));
                    invoice.setInvoiceStatus(rs.getString("invoice_status"));
                    invoice.setServiceDetail(rs.getString("service_details"));
                    invoice.setTotalServiceCost(rs.getString("total_service_cost"));
                    invoice.setMedicineDetail(rs.getString("medicine_details"));
                    invoice.setTotalMedicineCost(rs.getString("total_medicine_cost"));
                    invoice.setInvoiceTotalAmount(rs.getString("total_cost"));
                    invoice.includePatient();
                    invoices.add(invoice);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    public int getTotalInvoices(String fromDate, String toDate, String status) {
        String sql = """
                SELECT COUNT(*)
                FROM Invoice i
                JOIN Patient p ON i.patient_id = p.patient_id
                WHERE
                    p.status = 'Enable'
                    AND (? IS NULL OR i.issue_date >= ?)
                    AND (? IS NULL OR i.issue_date <= ?)
                    AND (? IS NULL OR i.status COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set date range parameters
            stmt.setString(1, fromDate);
            stmt.setString(2, fromDate);
            stmt.setString(3, toDate);
            stmt.setString(4, toDate);

            // Set status parameters
            stmt.setString(5, status);
            stmt.setString(6, status != null ? "%" + status + "%" : null);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Consider logging the error properly or throwing a custom exception
        }
        return 0;
    }

    public boolean updateInvoice(int invoiceId) {
        String sql = """
                UPDATE [dbo].[Invoice]
                SET [status] = 'Paid'
                WHERE invoice_id = ?
                """;
        try {
            PreparedStatement stmt = getConnection().prepareStatement(sql);
            stmt.setInt(1, invoiceId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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
        ArrayList<PatientPaymentDTO> a = dao.getPatientInvoices(null, null, null, 1, 50);
        System.out.println(a.size());

    }

}