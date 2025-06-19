package dal;

import model.AccountPatient;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountPatientDAO {
    DBContext dbContext = DBContext.getInstance();

    public AccountPatient getAccountByUsernameAndPassword(String username, String password) {
        AccountPatient account = null;
        String sql = """
                SELECT * FROM [dbo].[AccountPatient] 
                WHERE username = ? AND password = ? AND status = 'Enable'
                """;

        try {
            PreparedStatement stmt = dbContext.getConnection().prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                account = new AccountPatient(
                        rs.getInt("account_patient_id"), rs.getString("username"), rs.getString("password"),
                        rs.getString("email"), rs.getBoolean("status"), rs.getString("img")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return account;
    }

    public void addPatient(AccountPatient patient) {
        String sql = """
                 INSERT INTO [dbo].[AccountPatient] (username, password, email, status, img)
                 VALUES (?, ?, ?, ?, ?)
                 """;

        try (PreparedStatement stmt = dbContext.getConnection().prepareStatement(sql)) {
            stmt.setString(1, patient.getUsername());
            stmt.setString(2, patient.getPassword());
            stmt.setString(3, patient.getEmail());
            stmt.setBoolean(4, patient.isStatus());
            stmt.setString(5, patient.getImg());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding patient: " + e.getMessage());
        }
    }

    public void addPatientWithStatus(AccountPatient patient, String status) {
        String sql = """
                     INSERT INTO [dbo].[AccountPatient] (username, password, email, status, img)
                     VALUES (?, ?, ?, ?, ?)
                     """;
        try (PreparedStatement stmt = dbContext.getConnection().prepareStatement(sql)) {
            stmt.setString(1, patient.getUsername());
            stmt.setString(2, patient.getPassword());
            stmt.setString(3, patient.getEmail());
            stmt.setString(4, status); // Gán "Enable" trực tiếp
            stmt.setString(5, patient.getImg());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding patient: " + e.getMessage());
        }
    }

    public boolean isUsernameExists(String username) {
        String sql = "SELECT * FROM [dbo].[AccountPatient] WHERE username = ?";
        try (PreparedStatement stmt = dbContext.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean isEmailExists(String email) {
        String sql = "SELECT * FROM [dbo].[AccountPatient] WHERE email = ?";
        try (PreparedStatement stmt = dbContext.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
        return false;
    }

    public static void main(String[] args) {
        AccountPatientDAO dao = new AccountPatientDAO();
        AccountPatient patient = dao.getAccountByUsernameAndPassword("phamvannha", "P@ss123");
        System.out.println(patient);
    }
}