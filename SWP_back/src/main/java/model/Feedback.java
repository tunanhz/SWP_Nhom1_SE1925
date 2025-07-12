package model;

import dal.PatientDAO;

import java.sql.Timestamp;

public class Feedback {
    private int feedbackId;
    private int patientId;
    private String content;
    private int serviceRating;
    private int doctorRating;
    private int receptionistRating;
    private int pharmacistRating;
    private Double avgFeedback;
    private Timestamp createdAt;

    private Patient patient;

    public Feedback() {
    }

    public Feedback(int patientId, String content, int serviceRating, int doctorRating, int receptionistRating, int pharmacistRating) {
        this.patientId = patientId;
        this.content = content;
        this.serviceRating = serviceRating;
        this.doctorRating = doctorRating;
        this.receptionistRating = receptionistRating;
        this.pharmacistRating = pharmacistRating;
    }

    // Constructor
    public Feedback(int feedbackId, int patientId, String content, int serviceRating, int doctorRating,
                    int receptionistRating, int pharmacistRating, Double avgFeedback, Timestamp createdAt) {
        this.feedbackId = feedbackId;
        this.patientId = patientId;
        this.content = content;
        this.serviceRating = serviceRating;
        this.doctorRating = doctorRating;
        this.receptionistRating = receptionistRating;
        this.pharmacistRating = pharmacistRating;
        this.avgFeedback = avgFeedback;
        this.createdAt = createdAt;
    }

    // Getters and Setters
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

    public int getServiceRating() {
        return serviceRating;
    }

    public void setServiceRating(int serviceRating) {
        this.serviceRating = serviceRating;
    }

    public int getDoctorRating() {
        return doctorRating;
    }

    public void setDoctorRating(int doctorRating) {
        this.doctorRating = doctorRating;
    }

    public int getReceptionistRating() {
        return receptionistRating;
    }

    public void setReceptionistRating(int receptionistRating) {
        this.receptionistRating = receptionistRating;
    }

    public int getPharmacistRating() {
        return pharmacistRating;
    }

    public void setPharmacistRating(int pharmacistRating) {
        this.pharmacistRating = pharmacistRating;
    }

    public Double getAvgFeedback() {
        return avgFeedback;
    }

    public void setAvgFeedback(Double avgFeedback) {
        this.avgFeedback = avgFeedback;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Patient getPatient() {
        return patient;
    }

    public void includePatient() {
        PatientDAO dao = new PatientDAO();
        this.patient = dao.getPatientByPatientId(this.patientId);

    }
}