package dto;

public class RevenueByDepartmentDTO {
    private String department;
    private double totalRevenueByDepartment;

    public RevenueByDepartmentDTO(String department, double totalRevenueByDepartment) {
        this.department = department;
        this.totalRevenueByDepartment = totalRevenueByDepartment;
    }

    public String getDepartment() {
        return department;
    }

    public double getTotalRevenueByDepartment() {
        return totalRevenueByDepartment;
    }
}
