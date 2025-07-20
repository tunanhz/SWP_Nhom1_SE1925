package dto;

import dal.DoctorDAO;
import dal.FeedbackDAO;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PatientRecordDTO {
    // Thông tin bệnh nhân
    private Integer patientId;
    private String patientName;
    private LocalDate dob;
    private String gender;
    private String phone;
    private String address;
    private String patientStatus;

    // Hồ sơ bệnh án
    private Integer medicineRecordId;

    // Chẩn đoán
    private Integer diagnosisId;
    private String diagnosisDoctorName;
    private String conclusion;
    private String disease;
    private String treatmentPlan;
    private LocalDate diagnosisDate;

    // Kết quả khám
    private Integer examResultId;
    private String symptoms;
    private String preliminaryDiagnosis;
    private String examDoctorName;
    private LocalDate examDate;

    // Cuộc hẹn
    private Integer appointmentId;
    private String appointmentDoctorName;
    private LocalDateTime appointmentDatetime;
    private String shift;
    private String appointmentStatus;

    // Đơn thuốc
    private Integer prescriptionId;
    private String prescriptionDoctorName;
    private LocalDate prescriptionDate;
    private String prescriptionStatus;
    private String medicineName;
    private Integer medicineQuantity;
    private String medicineDosage;

    private boolean isFeedBack;

    // Constructor mặc định
    public PatientRecordDTO() {
    }

    // Getters và Setters
    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPatientStatus() {
        return patientStatus;
    }

    public void setPatientStatus(String patientStatus) {
        this.patientStatus = patientStatus;
    }

    public Integer getMedicineRecordId() {
        return medicineRecordId;
    }

    public void setMedicineRecordId(Integer medicineRecordId) {
        this.medicineRecordId = medicineRecordId;
    }

    public Integer getDiagnosisId() {
        return diagnosisId;
    }

    public void setDiagnosisId(Integer diagnosisId) {
        this.diagnosisId = diagnosisId;
    }

    public String getDiagnosisDoctorName() {
        return diagnosisDoctorName;
    }

    public void setDiagnosisDoctorName(String diagnosisDoctorName) {
        this.diagnosisDoctorName = diagnosisDoctorName;
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

    public LocalDate getDiagnosisDate() {
        return diagnosisDate;
    }

    public void setDiagnosisDate(LocalDate diagnosisDate) {
        this.diagnosisDate = diagnosisDate;
    }

    public int getExamResultId() {
        return examResultId;
    }

    public void setExamResultId(Integer examResultId) {
        this.examResultId = examResultId;
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

    public String getExamDoctorName() {
        return examDoctorName;
    }

    public void setExamDoctorName(String examDoctorName) {
        this.examDoctorName = examDoctorName;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getAppointmentDoctorName() {
        return appointmentDoctorName;
    }

    public void setAppointmentDoctorName(String appointmentDoctorName) {
        this.appointmentDoctorName = appointmentDoctorName;
    }

    public LocalDateTime getAppointmentDatetime() {
        return appointmentDatetime;
    }

    public void setAppointmentDatetime(LocalDateTime appointmentDatetime) {
        this.appointmentDatetime = appointmentDatetime;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public Integer getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Integer prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public String getPrescriptionDoctorName() {
        return prescriptionDoctorName;
    }

    public void setPrescriptionDoctorName(String prescriptionDoctorName) {
        this.prescriptionDoctorName = prescriptionDoctorName;
    }

    public LocalDate getPrescriptionDate() {
        return prescriptionDate;
    }

    public void setPrescriptionDate(LocalDate prescriptionDate) {
        this.prescriptionDate = prescriptionDate;
    }

    public String getPrescriptionStatus() {
        return prescriptionStatus;
    }

    public void setPrescriptionStatus(String prescriptionStatus) {
        this.prescriptionStatus = prescriptionStatus;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public Integer getMedicineQuantity() {
        return medicineQuantity;
    }

    public void setMedicineQuantity(Integer medicineQuantity) {
        this.medicineQuantity = medicineQuantity;
    }

    public String getMedicineDosage() {
        return medicineDosage;
    }

    public void setMedicineDosage(String medicineDosage) {
        this.medicineDosage = medicineDosage;
    }

    public void includeFeedback() {
        FeedbackDAO dao = new FeedbackDAO();
        if (patientId != null) {
            this.isFeedBack = dao.checkEligibility(this.patientId);
        }
    }

}