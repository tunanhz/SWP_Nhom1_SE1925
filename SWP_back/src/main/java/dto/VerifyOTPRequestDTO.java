package dto;

public class VerifyOTPRequestDTO {
    private String email;
    private String otp;
    private String password;
    private String confirmPassword;

    public VerifyOTPRequestDTO() {
    }

    public VerifyOTPRequestDTO(String email, String otp, String password, String confirmPassword) {
        this.email = email;
        this.otp = otp;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
