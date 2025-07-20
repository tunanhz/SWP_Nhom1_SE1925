package model;

public class FeedbackOverview {
    private int totalFeedback;
    private double avgDoctorRating;
    private double negativeFeedbackPercentage;

    public FeedbackOverview(int totalFeedback, double avgDoctorRating, double negativeFeedbackPercentage) {
        this.totalFeedback = totalFeedback;
        this.avgDoctorRating = avgDoctorRating;
        this.negativeFeedbackPercentage = negativeFeedbackPercentage;
    }

    public int getTotalFeedback() {
        return totalFeedback;
    }

    public double getAvgDoctorRating() {
        return avgDoctorRating;
    }

    public double getNegativeFeedbackPercentage() {
        return negativeFeedbackPercentage;
    }

    @Override
    public String toString() {
        return "FeedbackOverview{" +
                "totalFeedback=" + totalFeedback +
                ", avgDoctorRating=" + avgDoctorRating +
                ", negativeFeedbackPercentage=" + negativeFeedbackPercentage +
                '}';
    }
}