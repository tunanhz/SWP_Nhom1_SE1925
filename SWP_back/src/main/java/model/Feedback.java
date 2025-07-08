package model;

public class Feedback {
    private int patientId;
    private String content;
    private int serviceRating;
    private int doctorRating;
    private int receptionistRating;
    private int pharmacistRating;

    // Constructor
    public Feedback(int patientId, String content, int serviceRating, int doctorRating, int receptionistRating, int pharmacistRating) {
        this.patientId = patientId;
        this.content = content;
        this.serviceRating = serviceRating;
        this.doctorRating = doctorRating;
        this.receptionistRating = receptionistRating;
        this.pharmacistRating = pharmacistRating;
    }

    // Getters and Setters
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
}