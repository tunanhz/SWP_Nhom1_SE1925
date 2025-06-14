package model;
public class Diagnosis {
    private int diagnosisId;
    private int doctorId;
    private int medicineRecordId;
    private String conclusion;
    private String disease;
    private String treatmentPlan;

    public Diagnosis() {
    }

    public Diagnosis(int diagnosisId, int doctorId, int medicineRecordId, String conclusion, 
                    String disease, String treatmentPlan) {
        this.diagnosisId = diagnosisId;
        this.doctorId = doctorId;
        this.medicineRecordId = medicineRecordId;
        this.conclusion = conclusion;
        this.disease = disease;
        this.treatmentPlan = treatmentPlan;
    }

    public int getDiagnosisId() {
        return diagnosisId;
    }

    public void setDiagnosisId(int diagnosisId) {
        this.diagnosisId = diagnosisId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public int getMedicineRecordId() {
        return medicineRecordId;
    }

    public void setMedicineRecordId(int medicineRecordId) {
        this.medicineRecordId = medicineRecordId;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public String getTreatmentPlan() {
        return treatmentPlan;
    }

    public void setTreatmentPlan(String treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }
}