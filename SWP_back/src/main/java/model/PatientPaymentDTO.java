package model;

public class PatientPaymentDTO {
    private int invoiceId;
    private int patientId;
    private String issueDate;
    private String invoiceStatus;
    private String serviceDetail;
    private String totalServiceCost;
    private String medicineDetail;
    private String totalMedicineCost;
    private String invoiceTotalAmount;

    public PatientPaymentDTO() {
    }

    public PatientPaymentDTO(int invoiceId, int patientId, String issueDate, String invoiceStatus, String serviceDetail, String totalServiceCost, String medicineDetail, String totalMedicineCost, String invoiceTotalAmount) {
        this.invoiceId = invoiceId;
        this.patientId = patientId;
        this.issueDate = issueDate;
        this.invoiceStatus = invoiceStatus;
        this.serviceDetail = serviceDetail;
        this.totalServiceCost = totalServiceCost;
        this.medicineDetail = medicineDetail;
        this.totalMedicineCost = totalMedicineCost;
        this.invoiceTotalAmount = invoiceTotalAmount;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(String issueDate) {
        this.issueDate = issueDate;
    }

    public String getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(String invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public String getServiceDetail() {
        return serviceDetail;
    }

    public void setServiceDetail(String serviceDetail) {
        this.serviceDetail = serviceDetail;
    }

    public String getTotalServiceCost() {
        return totalServiceCost;
    }

    public void setTotalServiceCost(String totalServiceCost) {
        this.totalServiceCost = totalServiceCost;
    }

    public String getMedicineDetail() {
        return medicineDetail;
    }

    public void setMedicineDetail(String medicineDetail) {
        this.medicineDetail = medicineDetail;
    }

    public String getTotalMedicineCost() {
        return totalMedicineCost;
    }

    public void setTotalMedicineCost(String totalMedicineCost) {
        this.totalMedicineCost = totalMedicineCost;
    }

    public String getInvoiceTotalAmount() {
        return invoiceTotalAmount;
    }

    public void setInvoiceTotalAmount(String invoiceTotalAmount) {
        this.invoiceTotalAmount = invoiceTotalAmount;
    }
}
