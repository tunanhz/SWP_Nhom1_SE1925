package model;
public class Distributor {
    private int distributorId;
    private String distributorName;
    private String address;
    private String distributorEmail;
    private String distributorPhone;

    public Distributor() {
    }

    public Distributor(int distributorId, String distributorName, String address,
                      String distributorEmail, String distributorPhone) {
        this.distributorId = distributorId;
        this.distributorName = distributorName;
        this.address = address;
        this.distributorEmail = distributorEmail;
        this.distributorPhone = distributorPhone;
    }

    public int getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(int distributorId) {
        this.distributorId = distributorId;
    }

    public String getDistributorName() {
        return distributorName;
    }

    public void setDistributorName(String distributorName) {
        this.distributorName = distributorName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDistributorEmail() {
        return distributorEmail;
    }

    public void setDistributorEmail(String distributorEmail) {
        this.distributorEmail = distributorEmail;
    }

    public String getDistributorPhone() {
        return distributorPhone;
    }

    public void setDistributorPhone(String distributorPhone) {
        this.distributorPhone = distributorPhone;
    }
}