package dto;

public class ResetPasswordRequestDTO {
    private String email;

    public ResetPasswordRequestDTO() {
    }

    public ResetPasswordRequestDTO(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
