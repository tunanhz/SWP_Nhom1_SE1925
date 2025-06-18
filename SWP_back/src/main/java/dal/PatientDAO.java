package dal;

import model.Patient;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.logging.Logger;

public class PatientDAO {
    DBContext ad = new DBContext();

    public boolean updatePatient(int patientId, String fullName, String dob, String gender, String phone, String address) {
        String sql = """
                UPDATE [dbo].[Patient]
                SET [full_name] = ?,
                    [dob] = ?,
                    [gender] = ?,
                    [phone] = ?,
                    [address] = ?
                WHERE [patient_id] = ?;
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setNString(1, fullName);
            stmt.setNString(2, dob);
            stmt.setNString(3, gender);
            stmt.setNString(4, phone);
            stmt.setNString(5, address);
            stmt.setInt(6, patientId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<Patient> getAllPatientsByAccountPatientId(int accountPatientId, String fullName, String dob, String gender, int page, int pageSize) {
        String sql = """
                SELECT
                    p.patient_id,
                    p.full_name,
                    p.dob,
                    p.gender,
                    p.phone,
                    p.[address]
                FROM AccountPatient ap
                FULL OUTER JOIN Patient_AccountPatient pa
                    ON pa.account_patient_id = ap.account_patient_id
                FULL OUTER JOIN Patient p
                    ON p.patient_id = pa.patient_id
                WHERE ap.account_patient_id = ?
                    AND ap.status = 'Enable'
                    AND (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                    AND (? IS NULL OR
                        CONVERT(VARCHAR, p.dob, 120) COLLATE SQL_Latin1_General_CP1_CI_AI
                        LIKE CASE
                        WHEN ? LIKE '[0-9][0-9][0-9][0-9]' THEN ? + '%'
                        WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                        WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                        ELSE ? END)
                    AND (? IS NULL OR p.gender COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                ORDER BY p.patient_id
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;

        ArrayList<Patient> patients = new ArrayList<>();
        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setInt(1, accountPatientId);


            stmt.setNString(2, fullName);
            stmt.setNString(3, "%" + fullName + "%");


            stmt.setNString(4, dob);
            stmt.setNString(5, "%" + dob + "%");
            stmt.setNString(6, "%" + dob + "%");
            stmt.setNString(7, "%" + dob + "%");
            stmt.setNString(8, "%" + dob + "%");
            stmt.setNString(9, "%" + dob + "%");
            stmt.setNString(10, "%" + dob + "%");
            stmt.setNString(11, "%" + dob + "%");

            stmt.setNString(12, gender);
            stmt.setString(13, gender != null ? "%" + gender + "%" : null);

            int offset = (page - 1) * pageSize;
            stmt.setInt(14, offset);
            stmt.setInt(15, pageSize);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Patient patient = new Patient();
                patient.setId(rs.getInt("patient_id"));
                patient.setFullName(rs.getString("full_name"));
                patient.setDob(rs.getString("dob"));
                patient.setGender(rs.getString("gender"));
                patient.setPhone(rs.getString("phone"));
                patient.setAddress(rs.getString("address"));
                patients.add(patient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return patients;
    }

    public int countPatientByAccountPatientId(
            int accountPatientId,
            String fullName,
            String dob,
            String gender) {
        String query = """
                    SELECT COUNT(*) AS total
                    FROM AccountPatient ap
                FULL OUTER JOIN Patient_AccountPatient pa
                    ON pa.account_patient_id = ap.account_patient_id
                FULL OUTER JOIN Patient p
                    ON p.patient_id = pa.patient_id
                WHERE ap.account_patient_id = ?
                    AND ap.status = 'Enable'
                    AND (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                    AND (? IS NULL OR
                        CONVERT(VARCHAR, p.dob, 120) COLLATE SQL_Latin1_General_CP1_CI_AI
                        LIKE CASE
                        WHEN ? LIKE '[0-9][0-9][0-9][0-9]' THEN ? + '%'
                        WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                        WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                        ELSE ? END)
                    AND (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                """;

        try (PreparedStatement stmt = ad.getConnection().prepareStatement(query)) {
            // Account patient ID
            stmt.setInt(1, accountPatientId);

            // Full name filter
            stmt.setString(2, fullName);
            stmt.setString(3, fullName != null ? "%" + fullName + "%" : null);

            // dob year filter
            stmt.setString(4, dob);
            stmt.setString(5, "%" + dob + "%");
            stmt.setString(6, "%" + dob + "%");
            stmt.setString(7, "%" + dob + "%");
            stmt.setString(8, "%" + dob + "%");
            stmt.setString(9, "%" + dob + "%");
            stmt.setString(10, "%" + dob + "%");
            stmt.setString(11, "%" + dob + "%");

            // Gender filter
            stmt.setString(12, gender);
            stmt.setString(13, gender != null ? "%" + gender + "%" : null);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error counting patients: " + e.getMessage());
            throw new RuntimeException("Failed to count patients", e);
        }
    }

    public boolean insertPatient(String fullName, String dob, String gender, String phone, String address) {
        String sql = """
                INSERT INTO [dbo].[Patient]
                ([full_name], [dob], [gender], [phone], [address])
                VALUES (?, ?, ?, ?, ?);
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setNString(1, fullName);
            stmt.setNString(2, dob);
            stmt.setNString(3, gender);
            stmt.setNString(4, phone);
            stmt.setNString(5, address);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deletePatient(int patientId) {
        String sql = """
                DELETE FROM [dbo].[Patient]
                WHERE [patient_id] = ?;
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setInt(1, patientId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public static void main(String[] args) {
        PatientDAO patientDAO = new PatientDAO();
        ArrayList<Patient> patients = patientDAO.getAllPatientsByAccountPatientId(1, "", "", "", 1, 10);
        System.out.println(patients.size());
    }
}
