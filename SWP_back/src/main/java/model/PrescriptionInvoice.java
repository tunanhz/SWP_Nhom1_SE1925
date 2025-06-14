package model;
public class PrescriptionInvoice {
    private int prescriptionInvoiceId;
    private int invoiceId;
    private int pharmacistId;
    private int prescriptionId;

    public PrescriptionInvoice() {
    }

    public PrescriptionInvoice(int prescriptionInvoiceId, int invoiceId, int pharmacistId, int prescriptionId) {
        this.prescriptionInvoiceId = prescriptionInvoiceId;
        this.invoiceId = invoiceId;
        this.pharmacistId = pharmacistId;
        this.prescriptionId = prescriptionId;
    }

    public int getPrescriptionInvoiceId() {
        return prescriptionInvoiceId;
    }

    public void setPrescriptionInvoiceId(int prescriptionInvoiceId) {
        this.prescriptionInvoiceId = prescriptionInvoiceId;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getPharmacistId() {
        return pharmacistId;
    }

    public void setPharmacistId(int pharmacistId) {
        this.pharmacistId = pharmacistId;
    }

    public int getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
    }
}