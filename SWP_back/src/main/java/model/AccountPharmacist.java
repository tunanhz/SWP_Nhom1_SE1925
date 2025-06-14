package model;
public class AccountPharmacist {
    private int accountPharmacistId;
    private String username;
    private String password;
    private String email;
    private boolean status;
    private String img;

    public AccountPharmacist() {
    }

    public AccountPharmacist(int accountPharmacistId, String username, String password, String email, boolean status, String img) {
        this.accountPharmacistId = accountPharmacistId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.status = status;
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
}