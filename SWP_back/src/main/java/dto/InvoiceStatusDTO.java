package dto;

public class InvoiceStatusDTO {
    private String status;
    private int invoiceCount;
    private double totalAmount;
    private double percentage;

    public InvoiceStatusDTO(String status, int invoiceCount, double totalAmount, double percentage) {
        this.status = status;
        this.invoiceCount = invoiceCount;
        this.totalAmount = totalAmount;
        this.percentage = percentage;
    }

    public String getStatus() {
        return status;
    }

    public int getInvoiceCount() {
        return invoiceCount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getPercentage() {
        return percentage;
    }
}