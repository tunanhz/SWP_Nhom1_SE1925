package model;
public class ImportInfo {
    private int importId;
    private int distributorId;
    private int medicineId;
    private String importDate;
    private int importAmount;

    public ImportInfo() {
    }

    public ImportInfo(int importId, int distributorId, int medicineId, String importDate, int importAmount) {
        this.importId = importId;
        this.distributorId = distributorId;
        this.medicineId = medicineId;
        this.importDate = importDate;
        this.importAmount = importAmount;
    }

    public int getImportId() {
        return importId;
    }

    public void setImportId(int importId) {
        this.importId = importId;
    }

    public int getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(int distributorId) {
        this.distributorId = distributorId;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public String getImportDate() {
        return importDate;
    }

    public void setImportDate(String importDate) {
        this.importDate = importDate;
    }

    public int getImportAmount() {
        return importAmount;
    }

    public void setImportAmount(int importAmount) {
        this.importAmount = importAmount;
    }
}