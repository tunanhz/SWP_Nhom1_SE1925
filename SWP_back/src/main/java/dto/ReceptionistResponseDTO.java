package dto;

public class ReceptionistResponseDTO {
    private int receptionistId;
    private String fullName;
    private String phone;
    private int accountStaffId;
    private String username;
    private String email;
    private String img;
    private String status;

    public ReceptionistResponseDTO(int receptionistId, String fullName, String phone, int accountStaffId,
                                   String username, String email, String img, String status) {
        this.receptionistId = receptionistId;
        this.fullName = fullName;
        this.phone = phone;
        this.accountStaffId = accountStaffId;
        this.username = username;
        this.email = email;
        this.img = img;
        this.status = status;
    }

    public ReceptionistResponseDTO() {
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}