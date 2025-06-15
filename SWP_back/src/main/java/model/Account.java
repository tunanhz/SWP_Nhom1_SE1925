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
}