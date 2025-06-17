package dal;

import model.Appointment;

import java.sql.*;
import java.util.logging.Logger;

public class AppointmentDAO {
    DBContext ad = new DBContext();

    // ID bệnh nhân
    public Appointment getAppointmentByPatientId(int patientId) {
        Appointment appointment = null;
        String sql = """
                SELECT  [appointment_id]
                        ,[doctor_id]
                        ,[patient_id]
                        ,[appointment_datetime]
                        ,[receptionist_id]
                        ,[shift]
                        ,[status]
                        ,[note]
                FROM [HealthCareSystem].[dbo].[Appointment]
                WHERE patient_id = ?
                     """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                appointment = new Appointment(rs.getInt("appointment_id"),
                        rs.getInt("doctor_id"),
                        rs.getInt("patient_id"),
                        rs.getTimestamp("appointment_datetime"),
                        rs.getInt("receptionist_id"),
                        rs.getString("shift"),
                        rs.getString("status"),
                        rs.getString("note")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return appointment;
    }

    // Hàm lấy cuộc hẹn theo doctor_id
    public Appointment getAppointmentByDoctorId(int doctorId) {
        Appointment appointment = null;
        String sql = """
            SELECT  [appointment_id]
                    ,[doctor_id]
                    ,[patient_id]
                    ,[appointment_datetime]
                    ,[receptionist_id]
                    ,[shift]
                    ,[status]
                    ,[note]
            FROM [HealthCareSystem].[dbo].[Appointment]
            WHERE doctor_id = ?
                 """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setInt(1, doctorId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                appointment = new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("doctor_id"),
                        rs.getInt("patient_id"),
                        rs.getTimestamp("appointment_datetime"),
                        rs.getInt("receptionist_id"),
                        rs.getString("shift"),
                        rs.getString("status"),
                        rs.getString("note")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return appointment;
    }

    // Hàm lấy cuộc hẹn theo appointment_id
    public Appointment getAppointmentByAppointmentId(int appointmentId) {
        Appointment appointment = null;
        String sql = """
            SELECT  [appointment_id]
                    ,[doctor_id]
                    ,[patient_id]
                    ,[appointment_datetime]
                    ,[receptionist_id]
                    ,[shift]
                    ,[status]
                    ,[note]
            FROM [HealthCareSystem].[dbo].[Appointment]
            WHERE appointment_id = ?
                 """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setInt(1, appointmentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                appointment = new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("doctor_id"),
                        rs.getInt("patient_id"),
                        rs.getTimestamp("appointment_datetime"),
                        rs.getInt("receptionist_id"),
                        rs.getString("shift"),
                        rs.getString("status"),
                        rs.getString("note")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return appointment;
    }

    // Hàm lấy cuộc hẹn theo receptionist_id
    public Appointment getAppointmentByReceptionistId(int receptionistId) {
        Appointment appointment = null;
        String sql = """
            SELECT  [appointment_id]
                    ,[doctor_id]
                    ,[patient_id]
                    ,[appointment_datetime]
                    ,[receptionist_id]
                    ,[shift]
                    ,[status]
                    ,[note]
            FROM [HealthCareSystem].[dbo].[Appointment]
            WHERE receptionist_id = ?
                 """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setInt(1, receptionistId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                appointment = new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("doctor_id"),
                        rs.getInt("patient_id"),
                        rs.getTimestamp("appointment_datetime"),
                        rs.getInt("receptionist_id"),
                        rs.getString("shift"),
                        rs.getString("status"),
                        rs.getString("note")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return appointment;
    }

    public static void main(String[] args) {
        AppointmentDAO dao = new AppointmentDAO();
        Appointment appointment = dao.getAppointmentByPatientId(18);
        System.out.println(appointment);
    }



    private static final Logger LOGGER = Logger.getLogger(AppointmentDAO.class.getName());

    public Appointment createAppointment(Appointment appointment) throws SQLException {
        String sql = "{call CreateAppointment(?, ?, ?, ?, ?, ?)}";
        Appointment createdAppointment = null;
        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = ad.getConnection();
            stmt = conn.prepareCall(sql);

            stmt.setInt(1, appointment.getDoctorId());
            stmt.setInt(2, appointment.getPatientId());
            stmt.setTimestamp(3, new Timestamp(appointment.getAppointmentDatetime().getTime()));
            stmt.setString(4, appointment.getShift());
            stmt.setString(5, appointment.getNote());
            stmt.setObject(6, appointment.getReceptionistId() != -1 ? appointment.getReceptionistId() : null, Types.INTEGER);

            LOGGER.info("Executing statement: " + sql);
            boolean hasResult = stmt.execute();
            if (hasResult) {
                rs = stmt.getResultSet();
                if (rs.next()) {
                    createdAppointment = new Appointment();
                    createdAppointment.setAppointmentId(rs.getInt("appointment_id"));
                    createdAppointment.setDoctorId(rs.getInt("doctor_id"));
                    createdAppointment.setPatientId(rs.getInt("patient_id"));
                    createdAppointment.setAppointmentDatetime(rs.getTimestamp("appointment_datetime"));
                    createdAppointment.setShift(rs.getString("shift"));
                    createdAppointment.setStatus(rs.getString("status"));
                    createdAppointment.setNote(rs.getString("note"));
                    createdAppointment.setDoctorName(rs.getString("doctor_name"));
                    createdAppointment.setPatientName(rs.getString("patient_name"));
                }
            }

            if (createdAppointment == null) {
                throw new SQLException("Failed to create appointment, no result returned.");
            }
        } catch (SQLException e) {
            LOGGER.severe("SQL Error: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { LOGGER.severe(e.getMessage()); }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { LOGGER.severe(e.getMessage()); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { LOGGER.severe(e.getMessage()); }
        }
        return createdAppointment;
    }

}
