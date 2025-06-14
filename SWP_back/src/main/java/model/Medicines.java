package model;
<<<<<<< HEAD

=======
>>>>>>> a8fc15e07df1e5c7b327c34684658fb816abc6da
public class Medicines {
    private int prescriptionInvoiceId;
    private int medicineId;
    private int quantity;
    private String dosage;

    public Medicines() {
    }

    public Medicines(int prescriptionInvoiceId, int medicineId, int quantity, String dosage) {
        this.prescriptionInvoiceId = prescriptionInvoiceId;
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.dosage = dosage;
    }

    public int getPrescriptionInvoiceId() {
        return prescriptionInvoiceId;
    }

    public void setPrescriptionInvoiceId(int prescriptionInvoiceId) {
        this.prescriptionInvoiceId = prescriptionInvoiceId;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }
}