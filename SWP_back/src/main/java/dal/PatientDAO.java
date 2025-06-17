package dal;

import model.Patient;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

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

    public ArrayList<Patient> getAllPatientsByAccountPatientId(int accountPatientId, String fullName, String dob, String gender, int offset, int pageSize) {
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
                    AND p.gender = ?
                ORDER BY p.patient_id
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;

        ArrayList<Patient> patients = new ArrayList<>();
        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setInt(1, accountPatientId);

            // Handle full_name parameter
            if (fullName == null || fullName.trim().isEmpty()) {
                stmt.setNull(2, Types.NVARCHAR);
                stmt.setNull(3, Types.NVARCHAR);
            } else {
                stmt.setNString(2, fullName);
                stmt.setNString(3, "%" + fullName + "%");
            }

            // Handle dob parameter
            if (dob == null || dob.trim().isEmpty()) {
                stmt.setNull(4, Types.NVARCHAR);
                stmt.setNull(5, Types.NVARCHAR);
                stmt.setNull(6, Types.NVARCHAR);
                stmt.setNull(7, Types.NVARCHAR);
                stmt.setNull(8, Types.NVARCHAR);
                stmt.setNull(9, Types.NVARCHAR);
                stmt.setNull(10, Types.NVARCHAR);
            } else {
                stmt.setNString(4, dob);
                stmt.setNString(5, dob);
                stmt.setNString(6, dob);
                stmt.setNString(7, dob);
                stmt.setNString(8, dob);
                stmt.setNString(9, dob);
                stmt.setNString(10, dob);
            }

            stmt.setNString(11, gender);
            stmt.setInt(12, offset);
            stmt.setInt(13, pageSize);

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

}
