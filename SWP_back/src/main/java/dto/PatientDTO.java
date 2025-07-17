package dto;

public class PatientDTO {

    private int accountStaffId;
    private String img;

    public PatientDTO() {
    }

    public PatientDTO(int accountStaffId, String img) {
        this.accountStaffId = accountStaffId;
        this.img = img;
    }


    public int getAccountStaffId() {
        return accountStaffId;
    }

    public void setAccountStaffId(int accountStaffId) {
        this.accountStaffId = accountStaffId;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    @Override
    public String toString() {
        return "PatientDTO{" +
                "accountStaffId=" + accountStaffId +
                ", img='" + img + '\'' +
                '}';
    }
}
