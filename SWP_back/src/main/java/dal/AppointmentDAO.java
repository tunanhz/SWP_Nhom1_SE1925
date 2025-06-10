package dal;

import model.Appointment;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

}
