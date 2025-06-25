package dal;

import model.AccountStaff;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountStaffDAO {
    DBContext dbContext = DBContext.getInstance();

    public AccountStaff getAccountByUsernameAndPassword(String username, String password) {
        AccountStaff account = null;
        String sql = """
                SELECT * FROM [dbo].[AccountStaff] 
                WHERE userName = ? AND passWord = ? AND status = 'Enable'
                """;

        try {
            PreparedStatement stmt = dbContext.getConnection().prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                account = new AccountStaff(
                        rs.getInt("account_staff_id"), rs.getString("username"), rs.getString("password"),
                        rs.getString("role"), rs.getString("email"), rs.getString("img"), rs.getBoolean("status")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return account;
    }

    public boolean isEmailExists(String email) {
        String sql = "SELECT * FROM [dbo].[AccountStaff] WHERE email = ?";
        try (PreparedStatement stmt = dbContext.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage(), e);
        }
    }

    public void updatePassword(String email, String password) {
        String sql = """
            UPDATE [dbo].[AccountStaff]
            SET password = ?
            WHERE email = ?
        """;
        try (PreparedStatement stmt = dbContext.getConnection().prepareStatement(sql)) {
            stmt.setString(1, password);
            stmt.setString(2, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating password: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        AccountStaffDAO dao = new AccountStaffDAO();
        AccountStaff staff = dao.getAccountByUsernameAndPassword("doquocdat", "P@ss123");
        System.out.println(staff);
    }
}