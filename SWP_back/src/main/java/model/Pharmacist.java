package model;
public class Pharmacist {
    private int pharmacistId;
    private String fullName;
    private String phone;
    private int accountPharmacistId;

    public Pharmacist() {
    }

    public Pharmacist(int pharmacistId, String fullName, String phone, int accountPharmacistId) {
        this.pharmacistId = pharmacistId;
        this.fullName = fullName;
        this.phone = phone;
        this.accountPharmacistId = accountPharmacistId;
    }

    public int getPharmacistId() {
        return pharmacistId;
    }

    public void setPharmacistId(int pharmacistId) {
        this.pharmacistId = pharmacistId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getAccountPharmacistId() {
        return accountPharmacistId;
    }

    public void setAccountPharmacistId(int accountPharmacistId) {
        this.accountPharmacistId = accountPharmacistId;
    }
}