package dal;

import model.Appointment;
import model.AppointmentDTO;
import model.Patient;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Logger;

public class PatientAppointmentDAO {


    DBContext ad = new DBContext();

    public ArrayList<AppointmentDTO> getAppointmentsByAccountPatientId(
            int accountPatientId,
            String fullName,
            Timestamp appointmentDateTime,
            String status,
            int offset,
            int pageSize) {

        ArrayList<AppointmentDTO> appointments = new ArrayList<>();
        String query = """
            SELECT
                ap.account_patient_id,
                p.patient_id,
                a.doctor_id,
                a.appointment_id,
                a.receptionist_id,
                p.full_name,
                p.dob,
                p.gender,
                p.phone,
                p.[address],
                ap.email,
                ap.status AS account_status,
                a.appointment_datetime,
                a.[shift],
                a.status AS appointment_status,
                a.note
            FROM AccountPatient ap
            FULL OUTER JOIN Patient_AccountPatient pa
                ON pa.account_patient_id = ap.account_patient_id
            FULL OUTER JOIN Patient p
                ON p.patient_id = pa.patient_id
            FULL OUTER JOIN Appointment a
                ON a.patient_id = p.patient_id
            JOIN Doctor d on d.doctor_id = a.doctor_id
            WHERE ap.account_patient_id = ?
                AND a.appointment_datetime IS NOT NULL
                AND ap.status = 'Enable'
                AND (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                AND (? IS NULL OR YEAR(a.appointment_datetime) = ?)
                AND (? IS NULL OR a.status COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
            ORDER BY p.patient_id
            OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
    """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(query);

            // Account patient ID
            stmt.setInt(1, accountPatientId);

            // Full name filter
            stmt.setString(2, fullName);
            stmt.setString(3, fullName != null ? "%" + fullName + "%" : null);

            // Appointment year filter
            Integer year = null;
            if (appointmentDateTime != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(appointmentDateTime.getTime());
                year = cal.get(Calendar.YEAR);
            }
            stmt.setObject(4, year);
            stmt.setObject(5, year);
            // Status filter
            stmt.setString(6, status);
            stmt.setString(7, status != null ? "%" + status + "%" : null);

            // Pagination
            stmt.setInt(8, offset);
            stmt.setInt(9, pageSize);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                AppointmentDTO dto = new AppointmentDTO(
                        rs.getInt("account_patient_id"),
                        rs.getInt("patient_id"),
                        rs.getObject("doctor_id") != null ? rs.getInt("doctor_id") : null,
                        rs.getInt("appointment_id"),
                        rs.getObject("receptionist_id") != null ? rs.getInt("receptionist_id") : null,
                        rs.getString("full_name"),
                        rs.getDate("dob"),
                        rs.getString("gender"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("email"),
                        rs.getString("account_status"),
                        rs.getTimestamp("appointment_datetime"),
                        rs.getString("shift"),
                        rs.getString("appointment_status"),
                        rs.getString("note")
                );
                dto.includeDoctor();
                appointments.add(dto);
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error fetching appointments: " + e.getMessage());
            throw new RuntimeException("Failed to fetch appointments", e);
        }
        return appointments;
    }

    public int countAppointmentsByAccountPatientId(
            int accountPatientId,
            String fullName,
            Timestamp appointmentDateTime,
            String status) {
        String query = """
            SELECT COUNT(*) AS total
            FROM AccountPatient ap
            FULL OUTER JOIN Patient_AccountPatient pa
                ON pa.account_patient_id = ap.account_patient_id
            FULL OUTER JOIN Patient p
                ON p.patient_id = pa.patient_id
            FULL OUTER JOIN Appointment a
                ON a.patient_id = p.patient_id
            WHERE ap.account_patient_id = ?
                AND a.appointment_datetime IS NOT NULL
                AND ap.status = 'Enable'
                AND (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                AND (? IS NULL OR YEAR(a.appointment_datetime) = ?)
                AND (? IS NULL OR a.status COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
        """;

        try (PreparedStatement stmt = ad.getConnection().prepareStatement(query)) {
            // Account patient ID
            stmt.setInt(1, accountPatientId);

            // Full name filter
            stmt.setString(2, fullName);
            stmt.setString(3, fullName != null ? "%" + fullName + "%" : null);

            // Appointment year filter
            Integer year = null;
            if (appointmentDateTime != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(appointmentDateTime.getTime());
                year = cal.get(Calendar.YEAR);
            }
            stmt.setObject(4, year);
            stmt.setObject(5, year);

            // Status filter
            stmt.setString(6, status);
            stmt.setString(7, status != null ? "%" + status + "%" : null);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error counting appointments: " + e.getMessage());
            throw new RuntimeException("Failed to count appointments", e);
        }
    }



    public static void main(String[] args) {
        PatientAppointmentDAO dao = new PatientAppointmentDAO();

        // Call getAppointmentsByAccountPatientId
        ArrayList<AppointmentDTO> appointments = dao.getAppointmentsByAccountPatientId(
                1, "", null, "", 0, 8
        );
        System.out.println(appointments.size());
    }


}
