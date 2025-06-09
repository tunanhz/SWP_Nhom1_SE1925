public class Feedback {
    private int feedbackId;
    private int patientId;
    private String content;
    private String createdAt;

    public Feedback() {
    }

    public Feedback(int feedbackId, int patientId, String content, String createdAt) {
        this.feedbackId = feedbackId;
        this.patientId = patientId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(int feedbackId) {
        this.feedbackId = feedbackId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}