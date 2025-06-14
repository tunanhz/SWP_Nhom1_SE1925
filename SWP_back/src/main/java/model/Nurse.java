package model;
public class Nurse {
    private int nurseId;
    private String fullName;
    private String department;
    private String phone;
    private String eduLevel;
    private int accountStaffId;

    public Nurse() {
    }

    public Nurse(int nurseId, String fullName, String department, String phone, String eduLevel, int accountStaffId) {
        this.nurseId = nurseId;
        this.fullName = fullName;
        this.department = department;
        this.phone = phone;
        this.eduLevel = eduLevel;
        this.accountStaffId = accountStaffId;
    }

    public int getNurseId() {
        return nurseId;
    }

    public void setNurseId(int nurseId) {
        this.nurseId = nurseId;
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

    public String getEduLevel() {
        return eduLevel;
    }

    public void setEduLevel(String eduLevel) {
        this.eduLevel = eduLevel;
    }

    public int getAccountStaffId() {
        return accountStaffId;
    }

    public void setAccountStaffId(int accountStaffId) {
        this.accountStaffId = accountStaffId;
    }
}