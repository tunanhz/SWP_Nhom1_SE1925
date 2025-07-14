package dto;

public class MedicalRecordRequestDTO {
    private int patientId;

    public MedicalRecordRequestDTO() {
    }

    public MedicalRecordRequestDTO(int patientId) {
        this.patientId = patientId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    @Override
    public String toString() {
        return "MedicalRecordRequestDTO{" +
                "patientId=" + patientId +
                '}';
    }
}
