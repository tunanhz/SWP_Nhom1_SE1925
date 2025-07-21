package dto;

public class AppointmentSummaryDTO {
    private int totalAppointments;
    private int completedAppointments;
    private int canceledAppointments;
    private int noShowAppointments;

    public AppointmentSummaryDTO(int totalAppointments, int completedAppointments,
                                 int canceledAppointments, int noShowAppointments) {
        this.totalAppointments = totalAppointments;
        this.completedAppointments = completedAppointments;
        this.canceledAppointments = canceledAppointments;
        this.noShowAppointments = noShowAppointments;
    }

    // Getters
    public int getTotalAppointments() {
        return totalAppointments;
    }

    public int getCompletedAppointments() {
        return completedAppointments;
    }

    public int getCanceledAppointments() {
        return canceledAppointments;
    }

    public int getNoShowAppointments() {
        return noShowAppointments;
    }

    public void setTotalAppointments(int totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public void setCompletedAppointments(int completedAppointments) {
        this.completedAppointments = completedAppointments;
    }

    public void setCanceledAppointments(int canceledAppointments) {
        this.canceledAppointments = canceledAppointments;
    }

    public void setNoShowAppointments(int noShowAppointments) {
        this.noShowAppointments = noShowAppointments;
    }
}