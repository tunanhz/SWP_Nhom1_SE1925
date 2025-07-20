package dal;

import dto.MedicineDTO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MedicineDAO {

    DBContext ad = new DBContext();



    public ArrayList<MedicineDTO> getMedicinesByPage(int page, int size, String usage, String warehouseName) {
        ArrayList<MedicineDTO> medicines = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
                SELECT
                    m.medicine_id,
                    m.name,
                    m.ingredient,
                    m.usage,
                    m.preservation,
                    m.quantity,
                    m.manuDate,
                    m.expDate,
                    m.price,
                    w.name AS warehouse_name,
                    w.location AS warehouse_location,
                    c.categoryName
                FROM Medicine m
                JOIN Warehouse w ON m.warehouse_id = w.warehouse_id
                LEFT JOIN Category c ON m.category_id = c.category_id
                WHERE 1=1
                """);

        // Thêm điều kiện lọc
        ArrayList<Object> params = new ArrayList<>();
        if (usage != null && !usage.trim().isEmpty()) {
            sql.append(" AND LOWER(m.usage) LIKE LOWER(?)");
            params.add("%" + usage + "%");
        }
        if (warehouseName != null && !warehouseName.trim().isEmpty()) {
            sql.append(" AND LOWER(w.name) LIKE LOWER(?)");
            params.add("%" + warehouseName + "%");
        }

        sql.append(" ORDER BY m.medicine_id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try {
            PreparedStatement ps = ad.getConnection().prepareStatement(sql.toString());
            // Gán tham số cho điều kiện lọc
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            // Gán tham số cho phân trang
            int offset = (page - 1) * size;
            ps.setInt(params.size() + 1, offset);
            ps.setInt(params.size() + 2, size);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                medicines.add(new MedicineDTO(
                        rs.getInt("medicine_id"),
                        rs.getNString("name"),
                        rs.getNString("ingredient"),
                        rs.getNString("usage"),
                        rs.getNString("preservation"),
                        rs.getInt("quantity"),
                        rs.getDate("manuDate"),
                        rs.getDate("expDate"),
                        rs.getDouble("price"),
                        rs.getNString("warehouse_name"),
                        rs.getNString("warehouse_location")
                ));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return medicines;
    }

    public ArrayList<String> getWarehouseNames() {
        ArrayList<String> warehouseNames = new ArrayList<>();
        String sql = "SELECT DISTINCT name FROM Warehouse ORDER BY name";
        try {
            PreparedStatement ps = ad.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                warehouseNames.add(rs.getNString("name"));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return warehouseNames;
    }

    // Lấy danh sách usage duy nhất
    public ArrayList<String> getUniqueUsages() {
        ArrayList<String> usages = new ArrayList<>();
        String sql = "SELECT DISTINCT usage FROM Medicine WHERE usage IS NOT NULL ORDER BY usage";
        try {
            PreparedStatement ps = ad.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                usages.add(rs.getNString("usage"));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usages;
    }

//    public ArrayList<MedicineDTO> getMedicinesByPage(int page, int size) {
//        ArrayList<MedicineDTO> medicines = new ArrayList<>();
//        String sql = """
//                SELECT
//                    m.medicine_id,
//                    m.name,
//                    m.ingredient,
//                    m.usage,
//                    m.preservation,
//                    m.quantity,
//                    m.manuDate,
//                    m.expDate,
//                    m.price,
//                    w.name AS warehouse_name,
//                    w.location AS warehouse_location,
//                    c.categoryName
//                FROM Medicine m
//                JOIN Warehouse w ON m.warehouse_id = w.warehouse_id
//                LEFT JOIN Category c ON m.category_id = c.category_id
//                ORDER BY m.medicine_id
//                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;
//                """;
//
//        try {
//            PreparedStatement ps = ad.getConnection().prepareStatement(sql);
//            int offset = (page - 1) * size;
//            ps.setInt(1, offset);
//            ps.setInt(2, size);
//            ResultSet rs = ps.executeQuery();
//
//            while (rs.next()) {
//                medicines.add(new MedicineDTO(
//                        rs.getInt("medicine_id"),
//                        rs.getNString("name"),
//                        rs.getNString("ingredient"),
//                        rs.getNString("usage"),
//                        rs.getNString("preservation"),
//                        rs.getInt("quantity"),
//                        rs.getDate("manuDate"),
//                        rs.getDate("expDate"),
//                        rs.getDouble("price"),
//                        rs.getNString("warehouse_name"),
//                        rs.getNString("warehouse_location")
//                ));
//            }
//
//            rs.close();
//            ps.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return medicines;
//    }

    public MedicineDTO getMedicineById(int medicine_id) {
        MedicineDTO medicine = null;
        String sql = """
                    SELECT
                        m.medicine_id,
                        m.name,
                        m.quantity,
                        m.price,
                        w.name AS warehouse_name
                    FROM Medicine m
                    JOIN Warehouse w ON m.warehouse_id = w.warehouse_id
                    WHERE m.medicine_id = ?
                """;

        try {
            PreparedStatement ps = ad.getConnection().prepareStatement(sql);
            ps.setInt(1, medicine_id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                medicine = new MedicineDTO(
                        rs.getInt("medicine_id"),
                        rs.getNString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("price"),
                        rs.getNString("warehouse_name")
                );
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return medicine;
    }


    public boolean updateMedicineQuantity(int medicineId, int newQuantity) {
        String sql = """
                UPDATE Medicine
                SET quantity = ?
                WHERE medicine_id = ?;
                """;

        try {
            PreparedStatement ps = ad.getConnection().prepareStatement(sql);
            ps.setInt(1, newQuantity);
            ps.setInt(2, medicineId);
            int rowsAffected = ps.executeUpdate();
            ps.close();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ArrayList<MedicineDTO> searchMedicinesByName(String name) {
        ArrayList<MedicineDTO> medicines = new ArrayList<>();
        String sql = """
                SELECT
                    m.medicine_id,
                    m.name,
                    m.ingredient,
                    m.usage,
                    m.preservation,
                    m.quantity,
                    m.manuDate,
                    m.expDate,
                    m.price,
                    w.name AS warehouse_name,
                    w.location AS warehouse_location,
                    c.categoryName
                FROM Medicine m
                JOIN Warehouse w ON m.warehouse_id = w.warehouse_id
                LEFT JOIN Category c ON m.category_id = c.category_id
                WHERE LOWER(m.name) LIKE LOWER(?)
                ORDER BY m.medicine_id;
                """;

        try {
            PreparedStatement ps = ad.getConnection().prepareStatement(sql);
            ps.setString(1, "%" + name + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                medicines.add(new MedicineDTO(
                        rs.getInt("medicine_id"),
                        rs.getNString("name"),
                        rs.getNString("ingredient"),
                        rs.getNString("usage"),
                        rs.getNString("preservation"),
                        rs.getInt("quantity"),
                        rs.getDate("manuDate"),
                        rs.getDate("expDate"),
                        rs.getDouble("price"),
                        rs.getNString("warehouse_name"),
                        rs.getNString("warehouse_location")
                ));
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return medicines;
    }
}