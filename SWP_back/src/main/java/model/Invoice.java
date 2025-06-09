public class Invoice {
    private int invoiceId;
    private int patientId;
    private int medicineRecordId;
    private String issueDate;
    private double totalAmount;
    private String status;

    public Invoice() {
    }

    public Invoice(int invoiceId, int patientId, int medicineRecordId, String issueDate, 
                  double totalAmount, String status) {
        this.invoiceId = invoiceId;
        this.patientId = patientId;
        this.medicineRecordId = medicineRecordId;
        this.issueDate = issueDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getMedicineRecordId() {
        return medicineRecordId;
    }

    public void setMedicineRecordId(int medicineRecordId) {
        this.medicineRecordId = medicineRecordId;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}