package dal;

import model.MedicineDTO;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MedicineDAO {

    DBContext ad = new DBContext();

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
}
