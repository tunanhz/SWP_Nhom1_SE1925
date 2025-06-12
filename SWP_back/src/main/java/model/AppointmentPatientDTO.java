package model;

public class AppointmentPatientDTO {
    private int accountPatientId;
    private int patientId;
    private int doctorId; // Nullable
    private int appointmentId;
    private int receptionistId;
    private String doctorName;
    private String appointmentDate;
    private String appointmentTime;
    private String daysUntilAppointment;
    private String message;
    private String shift;
    private String appointmentStatus;
    private String note;

    public AppointmentPatientDTO() {
    }

    public AppointmentPatientDTO(int accountPatientId, int patientId, int doctorId, int appointmentId,
                                 int receptionistId, String doctorName, String appointmentDate,
                                 String appointmentTime, String daysUntilAppointment, String message,
                                 String appointmentStatus, String shift, String note) {
        this.accountPatientId = accountPatientId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.receptionistId = receptionistId;
        this.doctorName = doctorName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.daysUntilAppointment = daysUntilAppointment;
        this.message = message;
        this.appointmentStatus = appointmentStatus;
        this.shift = shift;
        this.note = note;
    }

    public int getAccountPatientId() {
        return accountPatientId;
    }

    public void setAccountPatientId(int accountPatientId) {
        this.accountPatientId = accountPatientId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getReceptionistId() {
        return receptionistId;
    }

    public void setReceptionistId(int receptionistId) {
        this.receptionistId = receptionistId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getDaysUntilAppointment() {
        return daysUntilAppointment;
    }

    public void setDaysUntilAppointment(String daysUntilAppointment) {
        this.daysUntilAppointment = daysUntilAppointment;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getAppointmentStatus() {
        return appointmentStatus;
    }

    public void setAppointmentStatus(String appointmentStatus) {
        this.appointmentStatus = appointmentStatus;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
