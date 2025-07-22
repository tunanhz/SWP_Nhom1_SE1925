package dto;

public class RevenueByTypeDTO {
    private String paymentType;
    private double totalRevenueByType;

    public RevenueByTypeDTO(String paymentType, double totalRevenueByType) {
        this.paymentType = paymentType;
        this.totalRevenueByType = totalRevenueByType;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public double getTotalRevenueByType() {
        return totalRevenueByType;
    }
}
