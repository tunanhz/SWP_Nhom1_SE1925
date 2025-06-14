package model;
public class ServiceOrder {
    private int serviceOrderId;
    private int doctorId;
    private String orderDate;
    private int medicineRecordId;

    public ServiceOrder() {
    }

    public ServiceOrder(int serviceOrderId, int doctorId, String orderDate, int medicineRecordId) {
        this.serviceOrderId = serviceOrderId;
        this.doctorId = doctorId;
        this.orderDate = orderDate;
        this.medicineRecordId = medicineRecordId;
    }

    public int getServiceOrderId() {
        return serviceOrderId;
    }

    public void setServiceOrderId(int serviceOrderId) {
        this.serviceOrderId = serviceOrderId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public int getMedicineRecordId() {
        return medicineRecordId;
    }

    public void setMedicineRecordId(int medicineRecordId) {
        this.medicineRecordId = medicineRecordId;
    }
}