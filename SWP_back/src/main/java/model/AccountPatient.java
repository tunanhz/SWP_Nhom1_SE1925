package model;

public class AccountPatient extends Account {
    private int accountPatientId;

    public AccountPatient() {
        super("", "", "", false);
    }

    public AccountPatient(int accountPatientId, String username, String password, String email, boolean status) {
        super(username, password, email, status);
        this.accountPatientId = accountPatientId;
    }

    public int getAccountPatientId() {
        return accountPatientId;
    }

    public void setAccountPatientId(int accountPatientId) {
        this.accountPatientId = accountPatientId;
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

    @Override
    public String getRole() {
        return "patient"; // Triển khai phương thức abstract
    }

    @Override
    public void displayDashboard() {
        // Logic redirect hoặc hiển thị dashboard patient, ví dụ:
        // System.out.println("Redirect to Patient Dashboard");
    }
}