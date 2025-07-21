package dal;

import dto.AppointmentReportDTO;
import dto.AppointmentSummaryDTO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class AdminBusinessReportDAO {
    DBContext ad = new DBContext();

    public AppointmentSummaryDTO getAppointmentSummary(String startDate, String endDate) {
        AppointmentSummaryDTO summary = null;
        String query = """
                SELECT 
                    COUNT(*) AS total_appointments,
                    SUM(CASE WHEN a.status = 'Completed' THEN 1 ELSE 0 END) AS completed_appointments,
                    SUM(CASE WHEN a.status = 'Cancelled' THEN 1 ELSE 0 END) AS canceled_appointments,
                    SUM(CASE WHEN a.status IN ('Pending', 'Confirmed') 
                             AND a.appointment_datetime < GETDATE() THEN 1 ELSE 0 END) AS no_show_appointments
                FROM Appointment a
                WHERE (? IS NULL OR a.appointment_datetime >= ?)
                    AND (? IS NULL OR a.appointment_datetime <= ?);
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(query);
            stmt.setString(1, startDate);
            stmt.setString(2, startDate);
            stmt.setString(3, endDate);
            stmt.setString(4, endDate);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                summary = new AppointmentSummaryDTO(
                        rs.getInt("total_appointments"),
                        rs.getInt("completed_appointments"),
                        rs.getInt("canceled_appointments"),
                        rs.getInt("no_show_appointments")
                );
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error fetching appointment summary: " + e.getMessage());
            throw new RuntimeException("Failed to fetch appointment summary", e);
        }
        return summary;
    }

    public ArrayList<AppointmentReportDTO> getAppointmentDetails(
            String startDate, String endDate, String status, String searchTerm, int page, int pageSize) {
        ArrayList<AppointmentReportDTO> appointments = new ArrayList<>();
        String query = """
                SELECT 
                    a.appointment_id,
                    p.patient_id,
                    p.full_name AS patient_name,
                    a.appointment_datetime,
                    a.shift,
                    a.note AS cancellation_reason,
                    a.status AS appointment_status,
                    d.doctor_id,
                    d.full_name AS doctor_name,
                    CASE 
                        WHEN a.status IN ('Pending', 'Confirmed') 
                             AND a.appointment_datetime < GETDATE() THEN 'Yes'
                        ELSE 'No'
                    END AS is_no_show
                FROM Appointment a
                LEFT JOIN Patient p ON a.patient_id = p.patient_id
                LEFT JOIN Doctor d ON a.doctor_id = d.doctor_id
                WHERE (? IS NULL OR  a.appointment_datetime >= ?)
                    AND (? IS NULL OR a.appointment_datetime <= ?)
                    AND (? IS NULL OR a.status COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                    AND (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? 
                         OR d.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                ORDER BY a.appointment_datetime DESC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(query);
            // Date range filters
            stmt.setString(1, startDate);
            stmt.setString(2, startDate);
            stmt.setString(3, endDate);
            stmt.setString(4, endDate);
            // Status filter
            boolean hasStatus = status != null && !status.trim().isEmpty();
            stmt.setString(5, hasStatus ? status.trim() : null);
            stmt.setString(6, hasStatus ? status.trim() : null);
            // Name search filter
            boolean hasSearchTerm = searchTerm != null && !searchTerm.trim().isEmpty();
            String searchQuery = hasSearchTerm ? "%" + searchTerm.trim().replaceAll("\\s+", " ") + "%" : null;
            stmt.setString(7, searchQuery);
            stmt.setString(8, searchQuery);
            stmt.setString(9, searchQuery);
            // Pagination
            int offset = (page - 1) * pageSize;
            stmt.setInt(10, offset);
            stmt.setInt(11, pageSize);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                AppointmentReportDTO dto = new AppointmentReportDTO(
                        rs.getInt("appointment_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getNString("patient_name"),
                        rs.getString("appointment_datetime"),
                        rs.getString("shift"),
                        rs.getNString("cancellation_reason"),
                        rs.getString("appointment_status"),
                        rs.getNString("doctor_name"),
                        rs.getString("is_no_show")
                );
                appointments.add(dto);
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error fetching appointment details: " + e.getMessage());
            throw new RuntimeException("Failed to fetch appointment details", e);
        }
        return appointments;
    }

    public int countAppointmentDetails(String startDate, String endDate, String status, String searchTerm) {
        int totalRecords = 0;
        String query = """
                SELECT 
                    COUNT(*) AS total_records
                FROM Appointment a
                LEFT JOIN Patient p ON a.patient_id = p.patient_id
                LEFT JOIN Doctor d ON a.doctor_id = d.doctor_id
                WHERE (? IS NULL OR a.appointment_datetime >= ?)
                    AND (? IS NULL OR a.appointment_datetime <= ?)
                    AND (? IS NULL OR a.status COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                    AND (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? 
                         OR d.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?);
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(query);
            // Date range filters
            stmt.setString(1, startDate);
            stmt.setString(2, startDate);
            stmt.setString(3, endDate);
            stmt.setString(4, endDate);
            // Status filter
            boolean hasStatus = status != null && !status.trim().isEmpty();
            stmt.setString(5, hasStatus ? status.trim() : null);
            stmt.setString(6, hasStatus ? status.trim() : null);
            // Name search filter
            boolean hasSearchTerm = searchTerm != null && !searchTerm.trim().isEmpty();
            String searchQuery = hasSearchTerm ? "%" + searchTerm.trim().replaceAll("\\s+", " ") + "%" : null;
            stmt.setString(7, searchQuery);
            stmt.setString(8, searchQuery);
            stmt.setString(9, searchQuery);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                totalRecords = rs.getInt("total_records");
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error counting appointment details: " + e.getMessage());
            throw new RuntimeException("Failed to count appointment details", e);
        }
        return totalRecords;
    }

    public static void main(String[] args) {
        AdminBusinessReportDAO dao = new AdminBusinessReportDAO();

        AppointmentSummaryDTO a = dao.getAppointmentSummary(null, null);
        System.out.println(a);

        ArrayList<AppointmentReportDTO> a1 = dao.getAppointmentDetails(null, null, null, null, 1, 50);
        System.out.println(a1.size());
        System.out.println(dao.countAppointmentDetails(null, null, null, null));


    }
}