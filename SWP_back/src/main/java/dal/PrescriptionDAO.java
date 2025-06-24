package dal;


import model.PrescriptionDTO;

import java.sql.Connection;
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

                ORDER BY p.prescription_id
                                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;
                """;
//                      WHERE p.status = 'Pending'
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


    public boolean updatePrescriptionStatus(int prescriptionId, String status, int accountPharmacistId) {
        Connection conn = null;
        try {
            conn = ad.getConnection();
            conn.setAutoCommit(false);

            // Kiểm tra số lượng thuốc trong kho nếu trạng thái là Dispensed
            if (status.equals("Dispensed")) {
                String checkStockSql = """
                        SELECT m.medicine_id, m.quantity, ms.quantity AS required_quantity
                        FROM Medicines ms
                        JOIN Medicine m ON ms.medicine_id = m.medicine_id
                        JOIN PrescriptionInvoice pi ON ms.prescription_invoice_id = pi.prescription_invoice_id
                        WHERE pi.prescription_id = ?;
                        """;
                try (PreparedStatement ps = conn.prepareStatement(checkStockSql)) {
                    ps.setInt(1, prescriptionId);
                    ResultSet rs = ps.executeQuery();
                    boolean hasInsufficientStock = false;
                    while (rs.next()) {
                        int availableQuantity = rs.getInt("quantity");
                        int requiredQuantity = rs.getInt("required_quantity");
                        if (availableQuantity < requiredQuantity) {
                            hasInsufficientStock = true;
                            System.out.println("Insufficient stock for medicine_id " + rs.getInt("medicine_id") +
                                    ": Available = " + availableQuantity + ", Required = " + requiredQuantity);
                        }
                    }
                    rs.close();
                    if (hasInsufficientStock) {
                        conn.rollback();
                        return false;
                    }
                } catch (SQLException e) {
                    System.out.println("Error checking stock: " + e.getMessage());
                    conn.rollback();
                    return false;
                }
            }

            // Cập nhật trạng thái đơn thuốc
            String updatePrescriptionSql = """
                    UPDATE Prescription 
                    SET status = ? 
                    WHERE prescription_id = ?;
                    """;
            PreparedStatement psPrescription = conn.prepareStatement(updatePrescriptionSql);
            psPrescription.setString(1, status);
            psPrescription.setInt(2, prescriptionId);
            int rowsAffected = psPrescription.executeUpdate();
            psPrescription.close();

            // Nếu trạng thái là Dispensed, cập nhật số lượng thuốc trong kho
            if (status.equals("Dispensed") && rowsAffected > 0) {
                String updateStockSql = """
                        UPDATE m
                        SET m.quantity = m.quantity - ms.quantity
                        FROM Medicine m
                        INNER JOIN Medicines ms ON m.medicine_id = ms.medicine_id
                        INNER JOIN PrescriptionInvoice pi ON ms.prescription_invoice_id = pi.prescription_invoice_id
                        WHERE pi.prescription_id = ?;
                        
                        """;
                PreparedStatement psStock = conn.prepareStatement(updateStockSql);
                psStock.setInt(1, prescriptionId);
                psStock.executeUpdate();
                psStock.close();
            }

            // Ghi log hành động của dược sĩ
            if (rowsAffected > 0) {
                String logSql = """
                        INSERT INTO SystemLog_Pharmacist (account_pharmacist_id, action, action_type, log_time)
                        VALUES (?, ?, ?, GETDATE());
                        """;
                PreparedStatement psLog = conn.prepareStatement(logSql);
                psLog.setInt(1, accountPharmacistId);
                psLog.setString(2, "Updated prescription " + prescriptionId + " to " + status);
                psLog.setString(3, "Prescription_Update");
                psLog.executeUpdate();
                psLog.close();
            }

            conn.commit();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                System.out.println("Rollback failed: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing connection: " + e.getMessage());
            }
        }
    }


//    public boolean updatePrescriptionStatus(int prescriptionId, String status) {
//        String sql = """
//                UPDATE Prescription
//                SET status = ?
//                WHERE prescription_id = ?;
//                """;
//
//        try {
//            PreparedStatement ps = ad.getConnection().prepareStatement(sql);
//            ps.setString(1, status);
//            ps.setInt(2, prescriptionId);
//            int rowsAffected = ps.executeUpdate();
//            ps.close();
//            return rowsAffected > 0;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }



}