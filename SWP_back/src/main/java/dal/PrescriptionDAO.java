package dal;


import model.PrescriptionDTO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PrescriptionDAO {
    DBContext ad = new DBContext();

    public ArrayList<PrescriptionDTO> getAllPrescriptions(int page, int size) {
        ArrayList<PrescriptionDTO> l = new ArrayList<>();
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
                l.add(new PrescriptionDTO(rs.getInt("prescription_id"), rs.getDate("prescription_date"),
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


    public ArrayList<PrescriptionDTO> getPrescriptionDetailById(int prescriptionId) {
        ArrayList<PrescriptionDTO> list = new ArrayList<>();
        String sql = """
                SELECT 
                    p.prescription_id,
                    p.prescription_date,
                    p.status,
                    pt.full_name AS patient_name,
                    d.full_name AS doctor_name,
                    m.name AS medicine_name,
                    ms.quantity,
                    ms.dosage,
                    m.price
                FROM Prescription p
                JOIN MedicineRecords mr ON p.medicineRecord_id = mr.medicineRecord_id
                JOIN Patient pt ON mr.patient_id = pt.patient_id
                JOIN Doctor d ON p.doctor_id = d.doctor_id
                JOIN PrescriptionInvoice pi ON pi.prescription_id = p.prescription_id
                JOIN Medicines ms ON ms.prescription_invoice_id = pi.prescription_invoice_id
                JOIN Medicine m ON ms.medicine_id = m.medicine_id
                WHERE p.prescription_id = ?;
                """;

        try {
            PreparedStatement ps = ad.getConnection().prepareStatement(sql);
            ps.setInt(1, prescriptionId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new PrescriptionDTO(
                        rs.getInt("prescription_id"),
                        rs.getDate("prescription_date"),
                        rs.getNString("status"),
                        rs.getNString("patient_name"),
                        rs.getNString("doctor_name"),
                        rs.getNString("medicine_name"),
                        rs.getInt("quantity"),
                        rs.getNString("dosage"),
                        rs.getDouble("price")
                ));
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

}