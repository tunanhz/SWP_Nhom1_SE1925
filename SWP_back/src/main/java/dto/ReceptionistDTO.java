package dto;

public class ReceptionistDTO {
    private int receptionistId;
    private String fullName;
    private String phone;
    private int accountStaffId;
    private String img;

    public ReceptionistDTO() {
    }

    public ReceptionistDTO(int receptionistId, String fullName, String phone, int accountStaffId, String img) {
        this.receptionistId = receptionistId;
        this.fullName = fullName;
        this.phone = phone;
        this.accountStaffId = accountStaffId;
        this.img = img;
    }

    public int getReceptionistId() {
        return receptionistId;
    }

    public void setReceptionistId(int receptionistId) {
        this.receptionistId = receptionistId;
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

    public int getAccountStaffId() {
        return accountStaffId;
    }

    public void setAccountStaffId(int accountStaffId) {
        this.accountStaffId = accountStaffId;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}