package dto;

public class MonthlyRevenueDTO {
    private int year;
    private int month;
    private double monthlyRevenue;

    public MonthlyRevenueDTO(int year, int month, double monthlyRevenue) {
        this.year = year;
        this.month = month;
        this.monthlyRevenue = monthlyRevenue;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public double getMonthlyRevenue() {
        return monthlyRevenue;
    }
}
