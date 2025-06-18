package dal;

import model.DoctorSchedule;
import java.sql.*;
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
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { LOGGER.severe(e.getMessage()); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { LOGGER.severe(e.getMessage()); }
        }
        return createdSchedule;
    }
}