package dto;

public class TopServiceDTO {
    private String serviceName;
    private String description;
    private int totalQuantity;
    private double totalServiceRevenue;

    public TopServiceDTO(String serviceName, String description, int totalQuantity, double totalServiceRevenue) {
        this.serviceName = serviceName;
        this.description = description;
        this.totalQuantity = totalQuantity;
        this.totalServiceRevenue = totalServiceRevenue;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getDescription() {
        return description;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public double getTotalServiceRevenue() {
        return totalServiceRevenue;
    }
}