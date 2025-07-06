package dal;

import dto.DoctorScheduleDTO;
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


    public List<DoctorScheduleDTO> getDoctorSchedules(String doctorId, String startDate, String endDate, String shift, String department) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT ds.schedule_id, d.full_name AS doctor_name, d.department AS doctor_department, " +
                        "ds.working_date, ds.shift, r.room_name, r.department AS room_department, " +
                        "CASE ds.is_available WHEN 1 THEN 'Available' ELSE 'Not Available' END AS availability, ds.note " +
                        "FROM DoctorSchedule ds " +
                        "JOIN Doctor d ON ds.doctor_id = d.doctor_id " +
                        "LEFT JOIN Room r ON ds.room_id = r.room_id " +
                        "JOIN AccountStaff a ON d.account_staff_id = a.account_staff_id " +
                        "WHERE a.status = 'Enable'"
        );

        List<Object> params = new ArrayList<>();
        if (doctorId != null && !doctorId.isEmpty()) {
            sql.append(" AND ds.doctor_id = ?");
            params.add(Integer.parseInt(doctorId));
        }
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            sql.append(" AND ds.working_date BETWEEN ? AND ?");
            params.add(startDate);
            params.add(endDate);
        }
        if (shift != null && !shift.isEmpty()) {
            sql.append(" AND ds.shift = ?");
            params.add(shift);
        }
        if (department != null && !department.isEmpty()) {
            sql.append(" AND d.department = ?");
            params.add(department);
        }
        sql.append(" ORDER BY ds.working_date, ds.shift, d.full_name");

        List<DoctorScheduleDTO> schedules = new ArrayList<>();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = db.getConnection();
            stmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            LOGGER.info("Executing query: " + sql.toString());
            rs = stmt.executeQuery();

            while (rs.next()) {
                DoctorScheduleDTO schedule = new DoctorScheduleDTO();
                schedule.setScheduleId(rs.getInt("schedule_id"));
                schedule.setDoctorName(rs.getString("doctor_name"));
                schedule.setDoctorDepartment(rs.getString("doctor_department"));
                schedule.setWorkingDate(rs.getString("working_date"));
                schedule.setShift(rs.getString("shift"));
                schedule.setRoomName(rs.getString("room_name"));
                schedule.setRoomDepartment(rs.getString("room_department"));
                schedule.setAvailability(rs.getString("availability"));
                schedule.setNote(rs.getString("note"));
                schedules.add(schedule);
            }
        } catch (SQLException e) {
            LOGGER.severe("SQL Error: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { LOGGER.severe(e.getMessage()); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { LOGGER.severe(e.getMessage()); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { LOGGER.severe(e.getMessage()); }
        }
        return schedules;
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