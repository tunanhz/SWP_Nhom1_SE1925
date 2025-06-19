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

    public static void main(String[] args) {
        AccountPatientDAO dao = new AccountPatientDAO();
        AccountPatient patient = dao.getAccountByUsernameAndPassword("phamvannha", "P@ss123");
        System.out.println(patient);
    }
}