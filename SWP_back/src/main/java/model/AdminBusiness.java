package model;
public class AdminBusiness {
    private int adminId;
    private String fullName;
    private String department;
    private String phone;
    private int accountStaffId;

    public AdminBusiness() {
    }

    public AdminBusiness(int adminId, String fullName, String department, String phone, int accountStaffId) {
        this.adminId = adminId;
        this.fullName = fullName;
        this.department = department;
        this.phone = phone;
        this.accountStaffId = accountStaffId;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getAccountStaffId() {
        return accountStaffId;
    }

    public void setAccountStaffId(int accountStaffId) {
        this.accountStaffId = accountStaffId;
    }
}