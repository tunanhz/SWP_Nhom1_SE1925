package dal;

import model.AccountStaff;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Datnq
 */
public class AccountStaffDAO extends DBContext {
    public AccountStaff checkLogin(String username, String password) {
        String sql = "SELECT * FROM AccountStaff WHERE username = ? AND password = ? AND status = 1";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, username);
            st.setString(2, password);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return new AccountStaff(
                    rs.getInt("account_staff_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("email"),
                    rs.getString("img"),
                    rs.getBoolean("status")
                );
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return null;
    }
}