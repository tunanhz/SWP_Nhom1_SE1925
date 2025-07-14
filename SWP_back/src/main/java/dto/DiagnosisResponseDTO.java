package dto;

import java.time.LocalDate;

public class DiagnosisResponseDTO {
    private int diagnosisId;
    private String patientName;
    private LocalDate date;
    private String type;
    private String status;

    public DiagnosisResponseDTO() {
    }

    public DiagnosisResponseDTO(int diagnosisId, String patientName, LocalDate date, String type, String status) {
        this.diagnosisId = diagnosisId;
        this.patientName = patientName;
        this.date = date;
        this.type = type;
        this.status = status;
    }

    public int getDiagnosisId() {
        return diagnosisId;
    }

    public void setDiagnosisId(int diagnosisId) {
        this.diagnosisId = diagnosisId;
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
        return "DiagnosisResponseDTO{" +
                "diagnosisId=" + diagnosisId +
                ", patientName='" + patientName + '\'' +
                ", date=" + date +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
