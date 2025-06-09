public class Payment {
    private int paymentId;
    private double amount;
    private String paymentType;
    private int invoiceId;
    private String paymentDate;
    private String status;

    public Payment() {
    }

    public Payment(int paymentId, double amount, String paymentType, int invoiceId, 
                  String paymentDate, String status) {
        this.paymentId = paymentId;
        this.amount = amount;
        this.paymentType = paymentType;
        this.invoiceId = invoiceId;
        this.paymentDate = paymentDate;
        this.status = status;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}