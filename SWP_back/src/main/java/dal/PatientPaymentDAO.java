package dal;

import model.PatientPaymentDTO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PatientPaymentDAO {

    DBContext ad = new DBContext();

    public ArrayList<PatientPaymentDTO> getPatientInvoicesByPatientId(int patientId) {
        String sql = """
                SELECT 
                    i.invoice_id,
                    i.issue_date,
                    i.status AS invoice_status,
                    COALESCE(sv.service_details, 'Không có dịch vụ') AS service_details,
                    COALESCE(sv.total_service_cost, 0) AS total_service_cost,
                    COALESCE(med.medicine_details, 'Không có thuốc') AS medicine_details,
                    COALESCE(med.total_medicine_cost, 0) AS total_medicine_cost,
                    i.total_amount AS invoice_total_amount
                FROM Invoice i
                LEFT JOIN (
                    SELECT 
                        si.invoice_id,
                        STRING_AGG(
                            CONCAT(lms.name, ': ', si.quantity, ' x ', si.unit_price, ' = ', si.total_price), 
                            '; '
                        ) AS service_details,
                        SUM(si.total_price) AS total_service_cost
                    FROM ServiceInvoice si
                    JOIN ServiceOrderItem soi ON si.service_order_item_id = soi.service_order_item_id
                    JOIN ListOfMedicalService lms ON soi.service_id = lms.service_id
                    GROUP BY si.invoice_id
                ) sv ON i.invoice_id = sv.invoice_id
                LEFT JOIN (
                    SELECT 
                        pi.invoice_id,
                        STRING_AGG(
                            CONCAT(m.name, ': ', med.quantity, ' x ', m.price, ' = ', (med.quantity * m.price)), 
                            '; '
                        ) AS medicine_details,
                        SUM(med.quantity * m.price) AS total_medicine_cost
                    FROM PrescriptionInvoice pi    
                    JOIN Medicines med ON pi.prescription_invoice_id = med.prescription_invoice_id
                    JOIN Medicine m ON med.medicine_id = m.medicine_id
                    GROUP BY pi.invoice_id
                ) med ON i.invoice_id = med.invoice_id
                WHERE i.patient_id = ?
                    AND i.status IN ('Pending', 'Paid')
                ORDER BY i.issue_date DESC
                """;

        ArrayList<PatientPaymentDTO> invoices = new ArrayList<>();
        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setInt(1, patientId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PatientPaymentDTO invoice = new PatientPaymentDTO();
                invoice.setInvoiceId(rs.getInt("invoice_id"));
                invoice.setIssueDate(rs.getString("issue_date"));
                invoice.setInvoiceStatus(rs.getString("invoice_status"));
                invoice.setServiceDetail(rs.getString("service_details"));
                invoice.setTotalServiceCost(rs.getString("total_service_cost"));
                invoice.setMedicineDetail(rs.getString("medicine_details"));
                invoice.setTotalMedicineCost(rs.getString("total_medicine_cost"));
                invoice.setInvoiceTotalAmount(rs.getString("invoice_total_amount"));
                invoices.add(invoice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    public ArrayList<PatientPaymentDTO> getPatientInvoicesByAccountId(int accountPatientId) {
        String sql = """
                SELECT 
                    i.invoice_id,
                    p.patient_id,
                    i.issue_date,
                    i.status AS invoice_status,
                    COALESCE(sv.service_details, 'Không có dịch vụ') AS service_details,
                    COALESCE(sv.total_service_cost, 0) AS total_service_cost,
                    COALESCE(med.medicine_details, 'Không có thuốc') AS medicine_details,
                    COALESCE(med.total_medicine_cost, 0) AS total_medicine_cost,
                    i.total_amount AS invoice_total_amount
                FROM Invoice i
                JOIN Patient p ON i.patient_id = p.patient_id
                JOIN Patient_AccountPatient pa ON pa.patient_id = p.patient_id
                JOIN AccountPatient ap ON ap.account_patient_id = pa.account_patient_id
                LEFT JOIN (
                    SELECT 
                        si.invoice_id,
                        STRING_AGG(
                            CONCAT(lms.name, ': ', si.quantity, ' x ', si.unit_price, ' = ', si.total_price), 
                            '; '
                        ) AS service_details,
                        SUM(si.total_price) AS total_service_cost
                    FROM ServiceInvoice si
                    JOIN ServiceOrderItem soi ON si.service_order_item_id = soi.service_order_item_id
                    JOIN ListOfMedicalService lms ON soi.service_id = lms.service_id
                    GROUP BY si.invoice_id
                ) sv ON i.invoice_id = sv.invoice_id
                LEFT JOIN (
                    SELECT 
                        pi.invoice_id,
                        STRING_AGG(
                            CONCAT(m.name, ': ', med.quantity, ' x ', m.price, ' = ', (med.quantity * m.price)), 
                            '; '
                        ) AS medicine_details,
                        SUM(med.quantity * m.price) AS total_medicine_cost
                    FROM PrescriptionInvoice pi    
                    JOIN Medicines med ON pi.prescription_invoice_id = med.prescription_invoice_id
                    JOIN Medicine m ON med.medicine_id = m.medicine_id
                    GROUP BY pi.invoice_id
                ) med ON i.invoice_id = med.invoice_id
                WHERE i.status IN ('Pending', 'Paid')
                    AND ap.account_patient_id = ?
                    AND ap.status = 'Enable'
                    AND p.status = 'Enable'
                ORDER BY i.issue_date DESC
                """;

        ArrayList<PatientPaymentDTO> invoices = new ArrayList<>();
        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setInt(1, accountPatientId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PatientPaymentDTO invoice = new PatientPaymentDTO();
                invoice.setInvoiceId(rs.getInt("invoice_id"));
                invoice.setPatientId(rs.getInt("patient_id"));
                invoice.setIssueDate(rs.getString("issue_date"));
                invoice.setInvoiceStatus(rs.getString("invoice_status"));
                invoice.setServiceDetail(rs.getString("service_details"));
                invoice.setTotalServiceCost(rs.getString("total_service_cost"));
                invoice.setMedicineDetail(rs.getString("medicine_details"));
                invoice.setTotalMedicineCost(rs.getString("total_medicine_cost"));
                invoice.setInvoiceTotalAmount(rs.getString("invoice_total_amount"));
                invoices.add(invoice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    public static void main(String[] args) {
        PatientPaymentDAO appointmentDAO = new PatientPaymentDAO();
        ArrayList<PatientPaymentDTO> a = appointmentDAO.getPatientInvoicesByAccountId(1);
        System.out.println(a.size());
    }
}
