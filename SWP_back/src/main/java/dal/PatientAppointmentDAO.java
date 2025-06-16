package dal;

import model.*;

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
            String appointmentDateTime,
            String status,
            int page,
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
                        JOIN AccountStaff acs on acs.account_staff_id = d.account_staff_id
                        WHERE ap.account_patient_id = ?
                            AND a.appointment_datetime IS NOT NULL
                            AND ap.status = 'Enable'
                            AND acs.status = 'Enable'
                            AND (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                            AND (? IS NULL OR CONVERT(VARCHAR, a.appointment_datetime, 120) COLLATE SQL_Latin1_General_CP1_CI_AI
                                LIKE CASE
                                WHEN ? LIKE '[0-9][0-9][0-9][0-9]' THEN ? + '%'
                                WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                                WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                                ELSE ? END)
                            AND (? IS NULL OR a.status COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                        ORDER BY p.patient_id
                        OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(query);

            // Account patient ID
            stmt.setInt(1, accountPatientId);

            // Full name filter
            boolean hasName = fullName != null && !fullName.trim().isEmpty();
            String nameQuery = hasName ? fullName.trim().replaceAll("\\s+", " ") : null;
            stmt.setString(2, nameQuery);
            stmt.setString(3, nameQuery != null ? "%" + nameQuery + "%" : null);

            // Appointment
            stmt.setString(4, appointmentDateTime);
            stmt.setString(5, "%" + appointmentDateTime + "%");
            stmt.setString(6, "%" + appointmentDateTime + "%");
            stmt.setString(7, "%" + appointmentDateTime + "%");
            stmt.setString(8, "%" + appointmentDateTime + "%");
            stmt.setString(9, "%" + appointmentDateTime + "%");
            stmt.setString(10, "%" + appointmentDateTime + "%");
            stmt.setString(11, "%" + appointmentDateTime + "%");

            // Status filter
            stmt.setString(12, status);
            stmt.setString(13, status != null ? "%" + status + "%" : null);

            // Pagination
            int offset = (page - 1) * pageSize;
            stmt.setInt(14, offset);
            stmt.setInt(15, pageSize);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                AppointmentDTO dto = new AppointmentDTO(
                        rs.getInt("account_patient_id"),
                        rs.getInt("patient_id"),
                        rs.getObject("doctor_id") != null ? rs.getInt("doctor_id") : null,
                        rs.getInt("appointment_id"),
                        rs.getObject("receptionist_id") != null ? rs.getInt("receptionist_id") : null,
                        rs.getNString("full_name"),
                        rs.getDate("dob"),
                        rs.getString("gender"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("email"),
                        rs.getString("account_status"),
                        rs.getString("appointment_datetime"),
                        rs.getString("shift"),
                        rs.getString("appointment_status"),
                        rs.getNString("note")
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
            String appointmentDateTime,
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
                    JOIN Doctor d on d.doctor_id = a.doctor_id
                    JOIN AccountStaff acs on acs.account_staff_id = d.account_staff_id
                    WHERE ap.account_patient_id = ?
                        AND a.appointment_datetime IS NOT NULL
                        AND ap.status = 'Enable'
                        AND acs.status = 'Enable'
                        AND (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                        AND (? IS NULL OR CONVERT(VARCHAR, a.appointment_datetime, 120) COLLATE SQL_Latin1_General_CP1_CI_AI
                                LIKE CASE
                                WHEN ? LIKE '[0-9][0-9][0-9][0-9]' THEN ? + '%'
                                WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                                WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                                ELSE ? END)
                        AND (? IS NULL OR a.status COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                """;

        try (PreparedStatement stmt = ad.getConnection().prepareStatement(query)) {
            // Account patient ID
            stmt.setInt(1, accountPatientId);

            // Full name filter
            stmt.setString(2, fullName);
            stmt.setString(3, fullName != null ? "%" + fullName + "%" : null);

            // Appointment year filter
            stmt.setObject(4, appointmentDateTime);
            stmt.setString(5, "%" + appointmentDateTime + "%");
            stmt.setString(6, "%" + appointmentDateTime + "%");
            stmt.setString(7, "%" + appointmentDateTime + "%");
            stmt.setString(8, "%" + appointmentDateTime + "%");
            stmt.setString(9, "%" + appointmentDateTime + "%");
            stmt.setString(10, "%" + appointmentDateTime + "%");
            stmt.setString(11, "%" + appointmentDateTime + "%");

            // Status filter
            stmt.setString(12, status);
            stmt.setString(13, status != null ? "%" + status + "%" : null);

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

    public AppointmentDTO getAppointmentsByAppointmentId(int id) {
        String xSql = """
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
                JOIN AccountStaff acs on acs.account_staff_id = d.account_staff_id
                WHERE a.appointment_id = ?
                    AND a.appointment_datetime IS NOT NULL
                    AND ap.status = 'Enable'
                    AND acs.status = 'Enable'
                """;
        AppointmentDTO a = null;
        try {
            PreparedStatement ps = ad.getConnection().prepareStatement(xSql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                a = new AppointmentDTO(
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
                        rs.getString("appointment_datetime"),
                        rs.getString("shift"),
                        rs.getString("appointment_status"),
                        rs.getString("note")
                );
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return a;
    }

    public ArrayList<AppointmentPatientDTO> getThreeAppointmentsUpcoming(int accountPatientId) {
        ArrayList<AppointmentPatientDTO> appointmentDTOS = new ArrayList<>();

        String query = """
                                                                SELECT TOP (3)
                                                                ap.account_patient_id,
                                                                p.patient_id,
                                                                a.doctor_id,
                                                                a.appointment_id,
                                                                a.receptionist_id,
                                                                d.full_name AS doctorName,
                                                            	CONVERT(varchar, a.appointment_datetime, 103) AS appointment_date,
                                                                CONVERT(varchar, a.appointment_datetime, 108) AS appointment_time,
                                                                DATEDIFF(DAY, GETDATE(), a.appointment_datetime) AS days_until_appointment,
                                                                CAST(DATEDIFF(DAY, GETDATE(), a.appointment_datetime) AS varchar) + ' days left, at ' + CONVERT(varchar, a.appointment_datetime, 108) AS message,
                                                                a.[shift],
                                                                a.status AS appointment_status,
                                                                a.note
                                                            FROM AccountPatient ap
                                                            JOIN Patient_AccountPatient pa
                                                                ON pa.account_patient_id = ap.account_patient_id
                                                            JOIN Patient p
                                                                ON p.patient_id = pa.patient_id
                                                            JOIN Appointment a
                                                                ON a.patient_id = p.patient_id
                                                            JOIN Doctor d on a.doctor_id = d.doctor_id
                                                            JOIN AccountStaff acs on acs.account_staff_id = d.account_staff_id
                                                            WHERE ap.account_patient_id = ?
                                                                AND a.appointment_datetime IS NOT NULL
                                                                AND ap.status = 'Enable'
                                                                AND acs.status = 'Enable'
                                                            	AND a.status = 'Confirmed'
                                                            	AND a.appointment_datetime > GETDATE()
                                                            	ORDER BY a.appointment_datetime ASC
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(query);

            // Account patient ID
            stmt.setInt(1, accountPatientId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                AppointmentPatientDTO dto = new AppointmentPatientDTO(
                        rs.getInt("account_patient_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getInt("appointment_id"),
                        rs.getInt("receptionist_id"),
                        rs.getString("doctorName"),
                        rs.getString("appointment_date"),
                        rs.getString("appointment_time"),
                        rs.getString("days_until_appointment"),
                        rs.getString("message"),
                        rs.getString("appointment_status"),
                        rs.getString("shift"),
                        rs.getString("note")
                );
                appointmentDTOS.add(dto);
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error fetching appointments: " + e.getMessage());
            throw new RuntimeException("Failed to fetch appointments", e);
        }
        return appointmentDTOS;
    }

    public boolean deleteAppointmentById(int appointmentId) {
        String sql = """
                DELETE FROM [dbo].[Appointment]
                WHERE [appointment_id] = ?
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setInt(1, appointmentId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        PatientAppointmentDAO dao = new PatientAppointmentDAO();
        AppointmentDTO a = dao.getAppointmentsByAppointmentId(2);
        System.out.println(a);

        ArrayList<AppointmentPatientDTO> appointments = dao.getThreeAppointmentsUpcoming(1);
        System.out.println(appointments.size());
    }


}