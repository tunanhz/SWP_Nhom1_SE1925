package model;

public class Doctor {

    private int doctorId;
    private String fullName;
    private String phone;
    private String eduLevel;
    private String department;
    private String email;
    private String img;

    public Doctor() {
    }

    public Doctor(int doctorId, String fullName, String phone, String eduLevel, String department, String email, String img) {
        this.doctorId = doctorId;
        this.fullName = fullName;
        this.phone = phone;
        this.eduLevel = eduLevel;
        this.department = department;
        this.email = email;
        this.img = img;
    }

    public int getID() {
        return doctorId;
    }

    public void setID(int doctorId) {
        this.doctorId = doctorId;
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

    public String getEduLevel() {
        return eduLevel;
    }

    public void setEduLevel(String eduLevel) {
        this.eduLevel = eduLevel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

}

