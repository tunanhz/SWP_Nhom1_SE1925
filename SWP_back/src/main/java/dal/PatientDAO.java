package dal;

import java.sql.PreparedStatement;
import java.sql.SQLException;

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
}
