public class ServiceOrderItem {
    private int serviceOrderItemId;
    private int serviceOrderId;
    private int serviceId;
    private int doctorId;

    public ServiceOrderItem() {
    }

    public ServiceOrderItem(int serviceOrderItemId, int serviceOrderId, int serviceId, int doctorId) {
        this.serviceOrderItemId = serviceOrderItemId;
        this.serviceOrderId = serviceOrderId;
        this.serviceId = serviceId;
        this.doctorId = doctorId;
    }

    public int getServiceOrderItemId() {
        return serviceOrderItemId;
    }

    public void setServiceOrderItemId(int serviceOrderItemId) {
        this.serviceOrderItemId = serviceOrderItemId;
    }

    public int getServiceOrderId() {
        return serviceOrderId;
    }

    public void setServiceOrderId(int serviceOrderId) {
        this.serviceOrderId = serviceOrderId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }
}