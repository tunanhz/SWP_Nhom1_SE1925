package dal;


import model.Prescription;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;
public class PrescriptionDAO {
    DBContext ad = new DBContext();

    public ArrayList<Prescription> getAllPrescriptions(int page, int size) {
        ArrayList<Prescription> l = new ArrayList<>();
        String xSql = """
                      
                SELECT\s
                          p.prescription_id,
                          p.prescription_date,
                          p.status,
                          pt.full_name AS patient_name,
                      	pt.phone AS patient_phone,
                          d.full_name AS doctor_name,
                      	d.department AS doctor_department,
                      	d.phone AS doctor_Phone
                      FROM Prescription p
                      JOIN MedicineRecords mr ON p.medicineRecord_id = mr.medicineRecord_id
                      JOIN Patient pt ON mr.patient_id = pt.patient_id
                      JOIN Doctor d ON p.doctor_id = d.doctor_id
                      WHERE p.status = 'Pending'
                ORDER BY p.prescription_id
                                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;
                      """;

        try {
            PreparedStatement ps = ad.getConnection().prepareStatement(xSql);
            int offset = (page - 1) * size;
            ps.setInt(1, offset);
            ps.setInt(2, size);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                l.add(new Prescription(rs.getInt("prescription_id"), rs.getDate("prescription_date"),
                        rs.getNString("status"), rs.getNString("patient_name"),
                        rs.getNString("patient_phone"), rs.getNString("doctor_name"),
                        rs.getNString("doctor_department"), rs.getNString("doctor_Phone")));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return (l);
    }
}
