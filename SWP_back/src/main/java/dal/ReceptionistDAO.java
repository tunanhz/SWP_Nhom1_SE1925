package dal;

import dto.ReceptionistCheckInDTO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
    }
}