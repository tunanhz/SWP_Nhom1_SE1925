package dal;

import dto.PatientPaymentDTO;

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

    public ArrayList<PatientPaymentDTO> getPatientInvoicesByAccountId(int accountPatientId, String issueDate, String status, int page, int pageSize) {
        String sql = """
                SELECT 
                    i.invoice_id,
                    p.patient_id,
                    CONVERT(DATE, i.issue_date) as issue_date,
                    i.status AS invoice_status,
                    COALESCE(sv.service_details, 'Không có dịch vụ') AS service_details,
                    COALESCE(sv.total_service_cost, 0) AS total_service_cost,
                    COALESCE(med.medicine_details, 'Không có thuốc') AS medicine_details,
                    COALESCE(med.total_medicine_cost, 0) AS total_medicine_cost,
                    (COALESCE(sv.total_service_cost, 0) + COALESCE(med.total_medicine_cost, 0)) AS total_cost
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
                    AND (? IS NULL OR CONVERT(VARCHAR, i.issue_date, 120) COLLATE SQL_Latin1_General_CP1_CI_AI
                            LIKE CASE
                            WHEN ? LIKE '[0-9][0-9][0-9][0-9]' THEN ? + '%'
                            WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                            WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                            ELSE ? END)
                    AND (? IS NULL OR i.status COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                ORDER BY i.issue_date desc
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;
                """;

        ArrayList<PatientPaymentDTO> invoices = new ArrayList<>();
        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setInt(1, accountPatientId);

            stmt.setString(2, issueDate);
            stmt.setString(3, "%" + issueDate + "%");
            stmt.setString(4, "%" + issueDate + "%");
            stmt.setString(5, "%" + issueDate + "%");
            stmt.setString(6, "%" + issueDate + "%");
            stmt.setString(7, "%" + issueDate + "%");
            stmt.setString(8, "%" + issueDate + "%");
            stmt.setString(9, "%" + issueDate + "%");

            // Status filter
            stmt.setString(10, status);
            stmt.setString(11, status != null ? "%" + status + "%" : null);

            // Pagination
            int offset = (page - 1) * pageSize;
            stmt.setInt(12, offset);
            stmt.setInt(13, pageSize);

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
                invoice.setInvoiceTotalAmount(rs.getString("total_cost"));
                invoice.includePatient();
                invoices.add(invoice);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    public int getTotalInvoices(int accountPatientId, String issueDate, String status) {
        String sql = """
                SELECT COUNT(*)
                FROM Invoice i
                JOIN Patient p ON i.patient_id = p.patient_id
                JOIN Patient_AccountPatient pa ON pa.patient_id = p.patient_id
                JOIN AccountPatient ap ON ap.account_patient_id = pa.account_patient_id
                WHERE i.status IN ('Pending', 'Paid')
                    AND ap.account_patient_id = ?
                    AND ap.status = 'Enable'
                    AND p.status = 'Enable'
                    AND (? IS NULL OR CONVERT(VARCHAR, i.issue_date, 120) COLLATE SQL_Latin1_General_CP1_CI_AI
                            LIKE CASE
                            WHEN ? LIKE '[0-9][0-9][0-9][0-9]' THEN ? + '%'
                            WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                            WHEN ? LIKE '[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]' THEN ? + '%'
                            ELSE ? END)
                    AND (? IS NULL OR i.status COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                """;
        try (PreparedStatement stmt = ad.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, accountPatientId);

            stmt.setString(2, issueDate);
            stmt.setString(3, "%" + issueDate + "%");
            stmt.setString(4, "%" + issueDate + "%");
            stmt.setString(5, "%" + issueDate + "%");
            stmt.setString(6, "%" + issueDate + "%");
            stmt.setString(7, "%" + issueDate + "%");
            stmt.setString(8, "%" + issueDate + "%");
            stmt.setString(9, "%" + issueDate + "%");

            stmt.setString(10, status);
            stmt.setString(11, status != null ? "%" + status + "%" : null);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean updateInvoice(int invoiceId) {
        String sql = """
                UPDATE [dbo].[Invoice]
                SET [status] = 'Paid'
                WHERE invoice_id = ?
                """;
        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setInt(1, invoiceId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        PatientPaymentDAO dao = new PatientPaymentDAO();
        ArrayList<PatientPaymentDTO> invoices = dao.getPatientInvoicesByAccountId(1, null, null, 1, 6);
        System.out.println(invoices.size());

        int a = dao.getTotalInvoices(1, null, null);
        System.out.println(a);
    }
}
