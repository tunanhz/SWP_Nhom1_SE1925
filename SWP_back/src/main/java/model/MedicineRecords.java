public class MedicineRecords {
    private int medicineRecordId;
    private int patientId;

    public MedicineRecords() {
    }

    public MedicineRecords(int medicineRecordId, int patientId) {
        this.medicineRecordId = medicineRecordId;
        this.patientId = patientId;
    }

    public int getMedicineRecordId() {
        return medicineRecordId;
    }

    public void setMedicineRecordId(int medicineRecordId) {
        this.medicineRecordId = medicineRecordId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }
}