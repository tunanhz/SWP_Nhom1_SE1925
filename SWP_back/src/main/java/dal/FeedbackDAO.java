package dal;

import dal.DBContext;
import model.Feedback;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FeedbackDAO {
    DBContext ad = new DBContext();

    // Kiểm tra tính hợp lệ của bệnh nhân
    public boolean checkEligibility(int patientId) {
        String sql = "SELECT COUNT(*) AS completed_appointments FROM Appointment a JOIN Feedback f ON f.patient_id = a.appointment_id WHERE f.patient_id = ? AND status = 'Completed'";
        try (PreparedStatement stmt = ad.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("completed_appointments") > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lưu feedback
    public boolean saveFeedback(Feedback feedback) throws SQLException {
        String sql = "INSERT INTO Feedback (patient_id, content, feedback_rate_service, feedback_rate_doctor, feedback_rate_receptionist, feedback_rate_pharmacist, created_at) VALUES (?, ?, ?, ?, ?, ?, GETDATE())";
        try (PreparedStatement stmt = ad.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, feedback.getPatientId());
            stmt.setString(2, feedback.getContent());
            stmt.setInt(3, feedback.getServiceRating());
            stmt.setInt(4, feedback.getDoctorRating());
            stmt.setInt(5, feedback.getReceptionistRating());
            stmt.setInt(6, feedback.getPharmacistRating());
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // Kiểm tra xem bệnh nhân đã gửi feedback chưa
//    public boolean hasFeedback(int patientId) {
//        String sql = "SELECT COUNT(*) AS feedback_count FROM Feedback WHERE patient_id = ?";
//        try (PreparedStatement stmt = ad.getConnection().prepareStatement(sql)) {
//            stmt.setInt(1, patientId);
//            ResultSet rs = stmt.executeQuery();
//            if (rs.next()) {
//                return rs.getInt("feedback_count") > 0;
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    public static void main(String[] args) {
        FeedbackDAO feedbackDAO = new FeedbackDAO();
        boolean a = feedbackDAO.checkEligibility(2);
        System.out.println(a);
    }
}