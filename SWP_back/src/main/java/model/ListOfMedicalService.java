package model;
public class ListOfMedicalService {
    private int serviceId;
    private String name;
    private String description;
    private double price;
    private String status;

    public ListOfMedicalService() {
    }

    public ListOfMedicalService(int serviceId, String name, String description, double price, String status) {
        this.serviceId = serviceId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ListOfMedicalService{" +
                "serviceId=" + serviceId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", status='" + status + '\'' +
                '}';
    }
}