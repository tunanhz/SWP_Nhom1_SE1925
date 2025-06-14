package model;
import java.util.Date;


public class MedicineDTO {
    private int medicineId;
    private String name;
    private int unitId;
    private int categoryId;
    private String ingredient;
    private String usage;
    private String preservation;
    private Date manuDate;
    private String expDate;
    private int quantity;
    private double price;
    private int warehouseId;

    public MedicineDTO() {
    }

    public MedicineDTO(int medicineId, String name, int unitId, int categoryId, String ingredient,
                    String usage, String preservation, Date manuDate, String expDate,
                    int quantity, double price, int warehouseId) {
        this.medicineId = medicineId;
        this.name = name;
        this.unitId = unitId;
        this.categoryId = categoryId;
        this.ingredient = ingredient;
        this.usage = usage;
        this.preservation = preservation;
        this.manuDate = manuDate;
        this.expDate = expDate;
        this.quantity = quantity;
        this.price = price;
        this.warehouseId = warehouseId;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getIngredient() {
        return ingredient;
    }

    public void setIngredient(String ingredient) {
        this.ingredient = ingredient;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getPreservation() {
        return preservation;
    }

    public void setPreservation(String preservation) {
        this.preservation = preservation;
    }

    public Date getManuDate() {
        return manuDate;
    }

    public void setManuDate(Date manuDate) {
        this.manuDate = manuDate;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(int warehouseId) {
        this.warehouseId = warehouseId;
    }

    private String warehouseName;

    public MedicineDTO(int medicineId, String name, int quantity, double price, String warehouseName) {
        this.medicineId = medicineId;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.warehouseName = warehouseName;
    }
}