package dto;

public class TopDoctorDTO {
    private String doctorName;
    private String department;
    private double totalRevenue;

    public TopDoctorDTO(String doctorName, String department, double totalRevenue) {
        this.doctorName = doctorName;
        this.department = department;
        this.totalRevenue = totalRevenue;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public String getDepartment() {
        return department;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }
}
