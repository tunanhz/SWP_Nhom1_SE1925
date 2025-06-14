package model;


public class AccountStaff extends Account {
    private int accountStaffId;
    private String role;
    private String img;

    public AccountStaff() {
        super("", "", "", false); // Giá trị mặc định
    }

    public AccountStaff(int accountStaffId, String userName, String passWord, String role, String email, String img, boolean status) {
        super(userName, passWord, email, status);
        this.accountStaffId = accountStaffId;
        this.role = role;
        this.img = img;
    }

    public int getAccountStaffId() {
        return accountStaffId;
    }

    public void setAccountStaffId(int accountStaffId) {
        this.accountStaffId = accountStaffId;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }

    public String getPassWord() {
        return password;
    }

    public void setPassWord(String passWord) {
        this.password = passWord;
    }

    // Xóa getter thông thường getRole() để tránh xung đột
    // public String getRole() { return this.role; }

    public void setRole(String role) {
        this.role = role;
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

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public String getRole() {
        return this.role; // Triển khai phương thức abstract từ Account
    }

    @Override
    public void displayDashboard() {
        // Logic redirect dựa trên role (doctor, nurse, receptionist, etc.)
        if ("doctor".equals(this.role)) {
            System.out.println("Redirect to Doctor Dashboard");
        } else if ("nurse".equals(this.role)) {
            System.out.println("Redirect to Nurse Dashboard");
        } else {
            System.out.println("Redirect to Staff Dashboard");
        }
    }
}