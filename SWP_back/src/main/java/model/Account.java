package model;

public abstract class Account {
    protected String username;
    protected String password;
    protected String email;
    protected boolean status;

    public Account(String username, String password, String email, boolean status) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.status = status;
    }

    public abstract String getRole();

    public abstract void displayDashboard();

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