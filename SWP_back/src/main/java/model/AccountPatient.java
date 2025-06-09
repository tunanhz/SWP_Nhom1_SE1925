public class AccountPatient {
    private int accountPatientId;
    private String username;
    private String password;
    private String email;
    private boolean status;

    public AccountPatient() {
    }

    public AccountPatient(int accountPatientId, String username, String password, String email, boolean status) {
        this.accountPatientId = accountPatientId;
        this.username = username;
        this.password = password;
        this.email = email;
        this.status = status;
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
}