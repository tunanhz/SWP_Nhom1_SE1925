public class ServiceInvoice {
    private int serviceInvoiceId;
    private int invoiceId;
    private int serviceOrderItemId;
    private int quantity;
    private double unitPrice;
    private double totalPrice;

    public ServiceInvoice() {
    }

    public ServiceInvoice(int serviceInvoiceId, int invoiceId, int serviceOrderItemId, 
                         int quantity, double unitPrice, double totalPrice) {
        this.serviceInvoiceId = serviceInvoiceId;
        this.invoiceId = invoiceId;
        this.serviceOrderItemId = serviceOrderItemId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    public int getServiceInvoiceId() {
        return serviceInvoiceId;
    }

    public void setServiceInvoiceId(int serviceInvoiceId) {
        this.serviceInvoiceId = serviceInvoiceId;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getServiceOrderItemId() {
        return serviceOrderItemId;
    }

    public void setServiceOrderItemId(int serviceOrderItemId) {
        this.serviceOrderItemId = serviceOrderItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}