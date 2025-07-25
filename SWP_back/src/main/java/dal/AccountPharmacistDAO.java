package dal;

import model.AccountPharmacist;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountPharmacistDAO {
    DBContext dbContext = DBContext.getInstance();

    public AccountPharmacist getAccountByUsernameAndPassword(String username, String password) {
        AccountPharmacist account = null;
        String sql = """
                SELECT * FROM [dbo].[AccountPharmacist] 
                WHERE username = ? AND password = ? AND status = 'Enable'
                """;

        try {
            PreparedStatement stmt = dbContext.getConnection().prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                account = new AccountPharmacist(
                        rs.getInt("account_pharmacist_id"), rs.getString("username"), rs.getString("password"),
                        rs.getString("email"), rs.getBoolean("status"), rs.getString("img")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return account;
    }

    public boolean isEmailExists(String email) {
        String sql = "SELECT * FROM [dbo].[AccountPharmacist] WHERE email = ?";
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
            UPDATE [dbo].[AccountPharmacist]
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

    public AccountPharmacist getAccountByUsername(String username) {
        AccountPharmacist account = null;
        String sql = """
                SELECT * FROM [dbo].[AccountPharmacist] 
                WHERE username = ? AND status = 'Enable'
                """;

        try {
            PreparedStatement stmt = dbContext.getConnection().prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                account = new AccountPharmacist(
                        rs.getInt("account_pharmacist_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email"),
                        rs.getBoolean("status"),
                        rs.getString("img")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching pharmacist account by username: " + e.getMessage(), e);
        }

        return account;
    }
    public static void main(String[] args) {
        AccountPharmacistDAO dao = new AccountPharmacistDAO();
        //AccountPharmacist pharmacist = dao.getAccountByUsernameAndPassword("phamthuphuong", "P@ss2024");
        AccountPharmacist pharmacist = dao.getAccountByUsername("phamthuphuong");
        System.out.println(pharmacist);
    }
}