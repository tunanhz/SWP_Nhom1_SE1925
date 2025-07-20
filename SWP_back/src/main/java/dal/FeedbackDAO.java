package dal;

import dal.DBContext;
import model.Feedback;
import model.FeedbackOverview;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDAO {
    DBContext ad = new DBContext();

    // Kiểm tra tính hợp lệ của bệnh nhân
    public boolean checkEligibility(int patientId) {
        String sql = "SELECT COUNT(*) AS completed_appointments FROM Appointment a JOIN Feedback f ON f.patient_id = a.patient_id WHERE f.patient_id = ? AND status = 'Completed'";
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

    public ArrayList<Feedback> getFeedbackByFilters(Timestamp startDate, Timestamp endDate,
                                                    Double minAvgFeedback, Double maxAvgFeedback,
                                                    int page, int pageSize) {
        if (page < 1 || pageSize < 1) {
            throw new IllegalArgumentException("Page and pageSize must be positive");
        }

        ArrayList<Feedback> feedbackList = new ArrayList<>();
        String sql = "WITH FeedbackWithAvg AS (" +
                "    SELECT " +
                "        f.feedback_id, " +
                "        f.patient_id, " +
                "        f.content, " +
                "        f.feedback_rate_service, " +
                "        f.feedback_rate_doctor, " +
                "        f.feedback_rate_receptionist, " +
                "        f.feedback_rate_pharmacist, " +
                "        ROUND(CAST(f.feedback_rate_service + f.feedback_rate_doctor + " +
                "                   f.feedback_rate_receptionist + f.feedback_rate_pharmacist AS DECIMAL)/4, 1) AS avg_feedback, " +
                "        f.created_at " +
                "    FROM Feedback f" +
                ") " +
                "SELECT " +
                "    feedback_id, " +
                "    patient_id, " +
                "    content, " +
                "    feedback_rate_service, " +
                "    feedback_rate_doctor, " +
                "    feedback_rate_receptionist, " +
                "    feedback_rate_pharmacist, " +
                "    FORMAT(avg_feedback, 'N1') AS avg_feedback, " +
                "    created_at " +
                "FROM FeedbackWithAvg " +
                "WHERE (? IS NULL OR created_at >= ?) " +
                "AND (? IS NULL OR created_at <= ?) " +
                "AND (? IS NULL OR avg_feedback >= ?) " +
                "AND (? IS NULL OR avg_feedback <= ?) " +
                "ORDER BY feedback_id " +
                "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (PreparedStatement stmt = ad.getConnection().prepareStatement(sql)) {
            // Set parameters for created_at
            stmt.setTimestamp(1, startDate);
            stmt.setTimestamp(2, startDate != null ? startDate : endDate);
            stmt.setTimestamp(3, endDate);
            stmt.setTimestamp(4, endDate != null ? endDate : startDate);

            // Set parameters for avg_feedback
            stmt.setObject(5, minAvgFeedback, java.sql.Types.DOUBLE);
            stmt.setObject(6, minAvgFeedback, java.sql.Types.DOUBLE);
            stmt.setObject(7, maxAvgFeedback, java.sql.Types.DOUBLE);
            stmt.setObject(8, maxAvgFeedback, java.sql.Types.DOUBLE);

            // Set pagination parameters
            stmt.setInt(9, (page - 1) * pageSize);
            stmt.setInt(10, pageSize);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Double avgFeedback = rs.getString("avg_feedback") != null
                            ? Double.parseDouble(rs.getString("avg_feedback"))
                            : null;
                    Feedback feedback = new Feedback(
                            rs.getInt("feedback_id"),
                            rs.getInt("patient_id"),
                            rs.getString("content"),
                            rs.getInt("feedback_rate_service"),
                            rs.getInt("feedback_rate_doctor"),
                            rs.getInt("feedback_rate_receptionist"),
                            rs.getInt("feedback_rate_pharmacist"),
                            avgFeedback,
                            rs.getTimestamp("created_at")
                    );
                    feedback.includePatient();
                    feedbackList.add(feedback);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return feedbackList;
    }

    public FeedbackOverview getFeedbackOverview(Timestamp startDate, Timestamp endDate,
                                                Double minAvgFeedback, Double maxAvgFeedback) {
        // Kiểm tra tham số đầu vào
        if (minAvgFeedback != null && (minAvgFeedback < 1 || minAvgFeedback > 5)) {
            throw new IllegalArgumentException("minAvgFeedback must be between 1 and 5");
        }
        if (maxAvgFeedback != null && (maxAvgFeedback < 1 || maxAvgFeedback > 5)) {
            throw new IllegalArgumentException("maxAvgFeedback must be between 1 and 5");
        }
        if (minAvgFeedback != null && maxAvgFeedback != null && minAvgFeedback > maxAvgFeedback) {
            throw new IllegalArgumentException("minAvgFeedback cannot be greater than maxAvgFeedback");
        }

        String sql = "WITH FeedbackWithAvg AS (" +
                "    SELECT " +
                "        feedback_rate_doctor, " +
                "        created_at, " +
                "        ROUND(CAST(feedback_rate_service + feedback_rate_doctor + " +
                "                   feedback_rate_receptionist + feedback_rate_pharmacist AS DECIMAL)/4, 1) AS avg_feedback " +
                "    FROM Feedback" +
                ") " +
                "SELECT " +
                "    COUNT(*) AS total_feedback, " +
                "    COALESCE(ROUND(AVG(CAST(feedback_rate_doctor AS DECIMAL(4,2))), 1), 0) AS avg_doctor_rating, " +
                "    COALESCE(ROUND(100.0 * SUM(CASE WHEN feedback_rate_doctor <= 2 THEN 1 ELSE 0 END) / NULLIF(COUNT(*), 0), 1), 0) AS negative_feedback_percentage " +
                "FROM FeedbackWithAvg " +
                "WHERE (? IS NULL OR created_at >= ?) " +
                "AND (? IS NULL OR created_at <= ?) " +
                "AND (? IS NULL OR avg_feedback >= ?) " +
                "AND (? IS NULL OR avg_feedback <= ?)";

        try (Connection conn = ad.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            // Set parameters for created_at
            stmt.setTimestamp(1, startDate);
            stmt.setTimestamp(2, startDate);
            stmt.setTimestamp(3, endDate);
            stmt.setTimestamp(4, endDate);

            // Set parameters for avg_feedback
            stmt.setObject(5, minAvgFeedback, Types.DOUBLE);
            stmt.setObject(6, minAvgFeedback, Types.DOUBLE);
            stmt.setObject(7, maxAvgFeedback, Types.DOUBLE);
            stmt.setObject(8, maxAvgFeedback, Types.DOUBLE);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new FeedbackOverview(
                            rs.getInt("total_feedback"),
                            rs.getDouble("avg_doctor_rating"),
                            rs.getDouble("negative_feedback_percentage")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new FeedbackOverview(0, 0.0, 0.0);
    }

    public int countFeedbackByFilters(Timestamp startDate, Timestamp endDate,
                                      Double minAvgFeedback, Double maxAvgFeedback) {
        String sql = "WITH FeedbackWithAvg AS (" +
                "    SELECT " +
                "        ROUND(CAST(f.feedback_rate_service + f.feedback_rate_doctor + " +
                "                   f.feedback_rate_receptionist + f.feedback_rate_pharmacist AS DECIMAL)/4, 1) AS avg_feedback, " +
                "        f.created_at " +
                "    FROM Feedback f" +
                ") " +
                "SELECT COUNT(*) " +
                "FROM FeedbackWithAvg " +
                "WHERE (? IS NULL OR created_at >= ?) " +
                "AND (? IS NULL OR created_at <= ?) " +
                "AND (? IS NULL OR avg_feedback >= ?) " +
                "AND (? IS NULL OR avg_feedback <= ?)";

        try (PreparedStatement stmt = ad.getConnection().prepareStatement(sql)) {
            // Set parameters for created_at
            stmt.setTimestamp(1, startDate);
            stmt.setTimestamp(2, startDate != null ? startDate : endDate);
            stmt.setTimestamp(3, endDate);
            stmt.setTimestamp(4, endDate != null ? endDate : startDate);

            // Set parameters for avg_feedback
            stmt.setObject(5, minAvgFeedback, java.sql.Types.DOUBLE);
            stmt.setObject(6, minAvgFeedback, java.sql.Types.DOUBLE);
            stmt.setObject(7, maxAvgFeedback, java.sql.Types.DOUBLE);
            stmt.setObject(8, maxAvgFeedback, java.sql.Types.DOUBLE);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void main(String[] args) {
        FeedbackDAO feedbackDAO = new FeedbackDAO();
        ArrayList<Feedback> feedbackList = feedbackDAO.getFeedbackByFilters(null, null, null, null, 1, 50);
        System.out.println(feedbackList.size());

        FeedbackOverview feedbackOverview = feedbackDAO.getFeedbackOverview(null, null, null, null);
        System.out.println(feedbackOverview.toString());


    }
}