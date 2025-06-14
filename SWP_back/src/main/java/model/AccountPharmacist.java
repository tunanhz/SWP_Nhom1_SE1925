package model;

public class AccountPharmacist extends Account {
    private int accountPharmacistId;
    private String img;

    public AccountPharmacist() {
        super("", "", "", false); // Giá trị mặc định
    }

    public AccountPharmacist(int accountPharmacistId, String username, String password, String email, boolean status, String img) {
        super(username, password, email, status);
        this.accountPharmacistId = accountPharmacistId;
        this.img = img;
    }

    public int getAccountPharmacistId() {
        return accountPharmacistId;
    }

    public void setAccountPharmacistId(int accountPharmacistId) {
        this.accountPharmacistId = accountPharmacistId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    @Override
    public String getRole() {
        return "pharmacist";
    }

    @Override
    public void displayDashboard() {
        // Logic redirect hoặc hiển thị dashboard pharmacist
    }
}