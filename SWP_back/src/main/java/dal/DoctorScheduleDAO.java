package dal;

import model.DoctorSchedule;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DoctorScheduleDAO {
    private static final Logger LOGGER = Logger.getLogger(DoctorScheduleDAO.class.getName());
    DBContext db = new DBContext();

    public DoctorSchedule createDoctorSchedule(DoctorSchedule schedule) throws SQLException {
        String sql = "{call sp_InsertDoctorSchedule(?, ?, ?, ?, ?)}";
        DoctorSchedule createdSchedule = null;
        Connection conn = null;
        CallableStatement stmt = null;

        try {
            conn = db.getConnection();
            stmt = conn.prepareCall(sql);

            stmt.setInt(1, schedule.getDoctorId());
            stmt.setDate(2, java.sql.Date.valueOf(schedule.getWorkingDate()));
            stmt.setString(3, schedule.getShift());
            stmt.setBoolean(4, schedule.isAvailable());
            stmt.setString(5, schedule.getNote() != null ? schedule.getNote() : null);

            LOGGER.info("Executing statement: " + sql);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String message = rs.getString("Message");
                LOGGER.info("Stored Procedure Message: " + message);
                if (message != null && message.contains("thành công")) {
                    createdSchedule = schedule;
                } else {
                    throw new SQLException("Stored Procedure failed: " + message);
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("SQL Error: " + e.getMessage());
            throw e;
        } finally {
            if (stmt != null) try {
                stmt.close();
            } catch (SQLException e) {
                LOGGER.severe(e.getMessage());
            }
            if (conn != null) try {
                conn.close();
            } catch (SQLException e) {
                LOGGER.severe(e.getMessage());
            }
        }
        return createdSchedule;
    }

    public List<String> getBookedAppointmentTimes(int doctorId, String date) throws SQLException {
        String sql = "SELECT CONVERT(VARCHAR, appointment_datetime, 108) AS appointment_time " +
                "FROM Appointment " +
                "WHERE doctor_id = ? AND CAST(appointment_datetime AS DATE) = ?";
        List<String> bookedTimes = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = db.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, doctorId);
            stmt.setString(2, date);

            LOGGER.info("Executing query: " + sql + " with doctorId=" + doctorId + ", date=" + date);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String time = rs.getString("appointment_time");
                // Truncate seconds if present (e.g., "08:30:00" -> "08:30")
                if (time != null && time.length() > 5) {
                    time = time.substring(0, 5);
                }
                bookedTimes.add(time);
            }
        } catch (SQLException e) {
            LOGGER.severe("SQL Error: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { LOGGER.severe(e.getMessage()); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { LOGGER.severe(e.getMessage()); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { LOGGER.severe(e.getMessage()); }
        }
        return bookedTimes;
    }

    public List<String> getWorkingDates(int doctorId) throws SQLException {
        String sql = "SELECT DISTINCT working_date " +
                "FROM DoctorSchedule " +
                "WHERE doctor_id = ? " +
                "AND working_date >= CAST(GETDATE() AS DATE) " +
                "ORDER BY working_date";
        List<String> workingDates = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = db.getConnection();
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, doctorId);

            LOGGER.info("Executing query: " + sql + " with doctorId=" + doctorId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Date workingDate = rs.getDate("working_date");
                if (workingDate != null) {
                    workingDates.add(new java.text.SimpleDateFormat("yyyy-MM-dd").format(workingDate));
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("SQL Error: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { LOGGER.severe(e.getMessage()); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { LOGGER.severe(e.getMessage()); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { LOGGER.severe(e.getMessage()); }
        }
        return workingDates;
    }
}