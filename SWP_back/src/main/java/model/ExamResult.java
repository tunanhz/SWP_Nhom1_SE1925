package model;
public class ExamResult {
    private int examResultId;
    private int medicineRecordId;
    private String symptoms;
    private String preliminaryDiagnosis;
    private int doctorId;

    public ExamResult() {
    }

    public ExamResult(int examResultId, int medicineRecordId, String symptoms, 
                     String preliminaryDiagnosis, int doctorId) {
        this.examResultId = examResultId;
        this.medicineRecordId = medicineRecordId;
        this.symptoms = symptoms;
        this.preliminaryDiagnosis = preliminaryDiagnosis;
        this.doctorId = doctorId;
    }

    public int getExamResultId() {
        return examResultId;
    }

    public void setExamResultId(int examResultId) {
        this.examResultId = examResultId;
    }

    public int getMedicineRecordId() {
        return medicineRecordId;
    }

    public void setMedicineRecordId(int medicineRecordId) {
        this.medicineRecordId = medicineRecordId;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getPreliminaryDiagnosis() {
        return preliminaryDiagnosis;
    }

    public void setPreliminaryDiagnosis(String preliminaryDiagnosis) {
        this.preliminaryDiagnosis = preliminaryDiagnosis;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }
}