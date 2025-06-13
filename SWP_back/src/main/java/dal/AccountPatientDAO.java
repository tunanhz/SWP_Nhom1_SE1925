package dal;

import model.AccountPatient;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author Datnq
 */
public class AccountPatientDAO extends DBContext {
    public AccountPatient checkLogin(String username, String password) {
        String sql = "SELECT * FROM AccountPatient WHERE username = ? AND password = ? AND status = 1";
        try {
            PreparedStatement st = connection.prepareStatement(sql);
            st.setString(1, username);
            st.setString(2, password);
            ResultSet rs = st.executeQuery();
            if (rs.next()) {
                return new AccountPatient(
                    rs.getInt("account_patient_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getBoolean("status")
                );
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return null;
    }
}