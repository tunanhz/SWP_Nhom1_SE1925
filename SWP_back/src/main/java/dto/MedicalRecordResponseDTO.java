package dto;

import java.time.LocalDate;

public class MedicalRecordResponseDTO {
    private int recordId;
    private String patientName;
    private LocalDate date;
    private String type;
    private String status;

    public MedicalRecordResponseDTO() {
    }

    public MedicalRecordResponseDTO(int recordId, String patientName, LocalDate date, String type, String status) {
        this.recordId = recordId;
        this.patientName = patientName;
        this.date = date;
        this.type = type;
        this.status = status;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MedicalRecordResponseDTO{" +
                "recordId=" + recordId +
                ", patientName='" + patientName + '\'' +
                ", date=" + date +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
