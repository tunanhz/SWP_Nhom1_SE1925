public class Appointment {
    private int appointmentId;
    private int doctorId;
    private int patientId;
    private String appointmentDatetime;
    private int receptionistId;
    private String shift;
    private String status;
    private String note;

    public Appointment() {
    }

    public Appointment(int appointmentId, int doctorId, int patientId, String appointmentDatetime, 
                      int receptionistId, String shift, String status, String note) {
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.appointmentDatetime = appointmentDatetime;
        this.receptionistId = receptionistId;
        this.shift = shift;
        this.status = status;
        this.note = note;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getAppointmentDatetime() {
        return appointmentDatetime;
    }

    public void setAppointmentDatetime(String appointmentDatetime) {
        this.appointmentDatetime = appointmentDatetime;
    }

    public int getReceptionistId() {
        return receptionistId;
    }

    public void setReceptionistId(int receptionistId) {
        this.receptionistId = receptionistId;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}