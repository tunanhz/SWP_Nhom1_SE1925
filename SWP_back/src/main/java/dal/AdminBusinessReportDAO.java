package dal;

import dto.AppointmentReportDTO;
import dto.AppointmentSummaryDTO;
import dto.PatientRecordDTO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminBusinessReportDAO {
    DBContext ad = new DBContext();

    //Appointment
    public AppointmentSummaryDTO getAppointmentSummary(String startDate, String endDate) {
        AppointmentSummaryDTO summary = null;
        String query = """
                SELECT 
                    COUNT(*) AS total_appointments,
                    SUM(CASE WHEN a.status = 'Completed' THEN 1 ELSE 0 END) AS completed_appointments,
                    SUM(CASE WHEN a.status = 'Cancelled' THEN 1 ELSE 0 END) AS canceled_appointments,
                    SUM(CASE WHEN a.status IN ('Pending', 'Confirmed') 
                             AND a.appointment_datetime < GETDATE() THEN 1 ELSE 0 END) AS no_show_appointments
                FROM Appointment a
                WHERE (? IS NULL OR a.appointment_datetime >= ?)
                    AND (? IS NULL OR a.appointment_datetime <= ?);
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(query);
            stmt.setString(1, startDate);
            stmt.setString(2, startDate);
            stmt.setString(3, endDate);
            stmt.setString(4, endDate);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                summary = new AppointmentSummaryDTO(
                        rs.getInt("total_appointments"),
                        rs.getInt("completed_appointments"),
                        rs.getInt("canceled_appointments"),
                        rs.getInt("no_show_appointments")
                );
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error fetching appointment summary: " + e.getMessage());
            throw new RuntimeException("Failed to fetch appointment summary", e);
        }
        return summary;
    }

    public ArrayList<AppointmentReportDTO> getAppointmentDetails(
            String startDate, String endDate, String status, String searchTerm, int page, int pageSize) {
        ArrayList<AppointmentReportDTO> appointments = new ArrayList<>();
        String query = """
                SELECT 
                    a.appointment_id,
                    p.patient_id,
                    p.full_name AS patient_name,
                    a.appointment_datetime,
                    a.shift,
                    a.note AS cancellation_reason,
                    a.status AS appointment_status,
                    d.doctor_id,
                    d.full_name AS doctor_name,
                    CASE 
                        WHEN a.status IN ('Pending', 'Confirmed') 
                             AND a.appointment_datetime < GETDATE() THEN 'Yes'
                        ELSE 'No'
                    END AS is_no_show
                FROM Appointment a
                LEFT JOIN Patient p ON a.patient_id = p.patient_id
                LEFT JOIN Doctor d ON a.doctor_id = d.doctor_id
                WHERE (? IS NULL OR  a.appointment_datetime >= ?)
                    AND (? IS NULL OR a.appointment_datetime <= ?)
                    AND (? IS NULL OR a.status COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                    AND (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? 
                         OR d.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                ORDER BY a.appointment_datetime DESC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(query);
            // Date range filters
            stmt.setString(1, startDate);
            stmt.setString(2, startDate);
            stmt.setString(3, endDate);
            stmt.setString(4, endDate);
            // Status filter
            boolean hasStatus = status != null && !status.trim().isEmpty();
            stmt.setString(5, hasStatus ? status.trim() : null);
            stmt.setString(6, hasStatus ? status.trim() : null);
            // Name search filter
            boolean hasSearchTerm = searchTerm != null && !searchTerm.trim().isEmpty();
            String searchQuery = hasSearchTerm ? "%" + searchTerm.trim().replaceAll("\\s+", " ") + "%" : null;
            stmt.setString(7, searchQuery);
            stmt.setString(8, searchQuery);
            stmt.setString(9, searchQuery);
            // Pagination
            int offset = (page - 1) * pageSize;
            stmt.setInt(10, offset);
            stmt.setInt(11, pageSize);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                AppointmentReportDTO dto = new AppointmentReportDTO(
                        rs.getInt("appointment_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getNString("patient_name"),
                        rs.getString("appointment_datetime"),
                        rs.getString("shift"),
                        rs.getNString("cancellation_reason"),
                        rs.getString("appointment_status"),
                        rs.getNString("doctor_name"),
                        rs.getString("is_no_show")
                );
                appointments.add(dto);
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error fetching appointment details: " + e.getMessage());
            throw new RuntimeException("Failed to fetch appointment details", e);
        }
        return appointments;
    }

    public int countAppointmentDetails(String startDate, String endDate, String status, String searchTerm) {
        int totalRecords = 0;
        String query = """
                SELECT 
                    COUNT(*) AS total_records
                FROM Appointment a
                LEFT JOIN Patient p ON a.patient_id = p.patient_id
                LEFT JOIN Doctor d ON a.doctor_id = d.doctor_id
                WHERE (? IS NULL OR a.appointment_datetime >= ?)
                    AND (? IS NULL OR a.appointment_datetime <= ?)
                    AND (? IS NULL OR a.status COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                    AND (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ? 
                         OR d.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?);
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(query);
            // Date range filters
            stmt.setString(1, startDate);
            stmt.setString(2, startDate);
            stmt.setString(3, endDate);
            stmt.setString(4, endDate);
            // Status filter
            boolean hasStatus = status != null && !status.trim().isEmpty();
            stmt.setString(5, hasStatus ? status.trim() : null);
            stmt.setString(6, hasStatus ? status.trim() : null);
            // Name search filter
            boolean hasSearchTerm = searchTerm != null && !searchTerm.trim().isEmpty();
            String searchQuery = hasSearchTerm ? "%" + searchTerm.trim().replaceAll("\\s+", " ") + "%" : null;
            stmt.setString(7, searchQuery);
            stmt.setString(8, searchQuery);
            stmt.setString(9, searchQuery);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                totalRecords = rs.getInt("total_records");
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error counting appointment details: " + e.getMessage());
            throw new RuntimeException("Failed to count appointment details", e);
        }
        return totalRecords;
    }

    //Patient Record
    public List<PatientRecordDTO> getPatientRecordsWithFilters(
            String patientName, LocalDate startDate, LocalDate endDate, String gender, int page, int size) {
        List<PatientRecordDTO> records = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.patient_id, p.full_name AS patient_name, p.dob, p.gender, p.phone, p.address, p.status AS patient_status, " +
                        "mr.medicineRecord_id, d.diagnosis_id, d_doctor.full_name AS diagnosis_doctor_name, d.conclusion, d.disease, d.treatment_plan, pr.prescription_date AS diagnosis_date, " +
                        "er.exam_result_id, er.symptoms, er.preliminary_diagnosis, er_doctor.full_name AS exam_doctor_name, pr.prescription_date AS exam_date, " +
                        "a.appointment_id, a_doctor.full_name AS appointment_doctor_name, a.appointment_datetime, a.shift, a.status AS appointment_status, " +
                        "pr.prescription_id, pr_doctor.full_name AS prescription_doctor_name, pr.prescription_date, pr.status AS prescription_status, " +
                        "m.name AS medicine_name, med.quantity AS medicine_quantity, med.dosage AS medicine_dosage " +
                        "FROM Patient p " +
                        "LEFT JOIN MedicineRecords mr ON p.patient_id = mr.patient_id " +
                        "LEFT JOIN Diagnosis d ON mr.medicineRecord_id = d.medicineRecord_id " +
                        "LEFT JOIN Doctor d_doctor ON d.doctor_id = d_doctor.doctor_id " +
                        "LEFT JOIN ExamResult er ON mr.medicineRecord_id = er.medicineRecord_id " +
                        "LEFT JOIN Doctor er_doctor ON er.doctor_id = er_doctor.doctor_id " +
                        "LEFT JOIN Appointment a ON p.patient_id = a.patient_id " +
                        "LEFT JOIN Doctor a_doctor ON a.doctor_id = a_doctor.doctor_id " +
                        "LEFT JOIN Prescription pr ON mr.medicineRecord_id = pr.medicineRecord_id " +
                        "LEFT JOIN Doctor pr_doctor ON pr.doctor_id = pr_doctor.doctor_id " +
                        "LEFT JOIN PrescriptionInvoice pi ON pr.prescription_id = pi.prescription_id " +
                        "LEFT JOIN Medicines med ON pi.prescription_invoice_id = med.prescription_invoice_id " +
                        "LEFT JOIN Medicine m ON med.medicine_id = m.medicine_id " +
                        "WHERE a.status = 'Completed'"
        );

        List<Object> params = new ArrayList<>();
        if (patientName != null && !patientName.trim().isEmpty()) {
            sql.append(" AND (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)");
            params.add("%" + patientName.trim() + "%");
            params.add("%" + patientName.trim() + "%");
        }
        if (startDate != null) {
            sql.append(" AND a.appointment_datetime >= ?");
            params.add(startDate.atStartOfDay());
        }
        if (endDate != null) {
            sql.append(" AND a.appointment_datetime <= ?");
            params.add(endDate.atTime(23, 59, 59));
        }
        if (gender != null && !gender.trim().isEmpty()) {
            sql.append(" AND p.gender = ?");
            params.add(gender.trim());
        }

        sql.append(" ORDER BY p.patient_id, mr.medicineRecord_id, d.diagnosis_id, er.exam_result_id, a.appointment_datetime, pr.prescription_id, m.medicine_id");
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add((page - 1) * size);
        params.add(size);

        try (PreparedStatement preparedStatement = ad.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(mapResultSetToDTO(resultSet));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return records;
    }

    public int countPatientRecordsWithFilters(
            String patientName, LocalDate startDate, LocalDate endDate, String gender) {
        int totalRecords = 0;
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) AS total " +
                        "FROM Patient p " +
                        "LEFT JOIN MedicineRecords mr ON p.patient_id = mr.patient_id " +
                        "LEFT JOIN Diagnosis d ON mr.medicineRecord_id = d.medicineRecord_id " +
                        "LEFT JOIN Doctor d_doctor ON d.doctor_id = d_doctor.doctor_id " +
                        "LEFT JOIN ExamResult er ON mr.medicineRecord_id = er.medicineRecord_id " +
                        "LEFT JOIN Doctor er_doctor ON er.doctor_id = er_doctor.doctor_id " +
                        "LEFT JOIN Appointment a ON p.patient_id = a.patient_id " +
                        "LEFT JOIN Doctor a_doctor ON a.doctor_id = a_doctor.doctor_id " +
                        "LEFT JOIN Prescription pr ON mr.medicineRecord_id = pr.medicineRecord_id " +
                        "LEFT JOIN Doctor pr_doctor ON pr.doctor_id = pr_doctor.doctor_id " +
                        "LEFT JOIN PrescriptionInvoice pi ON pr.prescription_id = pi.prescription_id " +
                        "LEFT JOIN Medicines med ON pi.prescription_invoice_id = med.prescription_invoice_id " +
                        "LEFT JOIN Medicine m ON med.medicine_id = m.medicine_id " +
                        "WHERE a.status = 'Completed'"
        );

        List<Object> params = new ArrayList<>();
        if (patientName != null && !patientName.trim().isEmpty()) {
            sql.append(" AND (? IS NULL OR p.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)");
            params.add("%" + patientName.trim() + "%");
            params.add("%" + patientName.trim() + "%");
        }
        if (startDate != null) {
            sql.append(" AND a.appointment_datetime >= ?");
            params.add(startDate.atStartOfDay());
        }
        if (endDate != null) {
            sql.append(" AND a.appointment_datetime <= ?");
            params.add(endDate.atTime(23, 59, 59));
        }
        if (gender != null && !gender.trim().isEmpty()) {
            sql.append(" AND p.gender = ?");
            params.add(gender.trim());
        }

        try (PreparedStatement preparedStatement = ad.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    totalRecords = resultSet.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalRecords;
    }

    private PatientRecordDTO mapResultSetToDTO(ResultSet resultSet) throws SQLException {
        PatientRecordDTO record = new PatientRecordDTO();
        record.setPatientId(resultSet.getInt("patient_id"));
        record.setPatientName(resultSet.getString("patient_name"));
        record.setDob(resultSet.getObject("dob", LocalDate.class));
        record.setGender(resultSet.getString("gender"));
        record.setPhone(resultSet.getString("phone"));
        record.setAddress(resultSet.getString("address"));
        record.setPatientStatus(resultSet.getString("patient_status"));
        record.setMedicineRecordId(resultSet.getInt("medicineRecord_id"));
        if (resultSet.wasNull()) {
            record.setMedicineRecordId(null);
        }
        record.setDiagnosisId(resultSet.getInt("diagnosis_id"));
        if (resultSet.wasNull()) {
            record.setDiagnosisId(null);
        }
        record.setDiagnosisDoctorName(resultSet.getString("diagnosis_doctor_name"));
        record.setConclusion(resultSet.getString("conclusion"));
        record.setDisease(resultSet.getString("disease"));
        record.setTreatmentPlan(resultSet.getString("treatment_plan"));
        record.setDiagnosisDate(resultSet.getObject("diagnosis_date", LocalDate.class));
        record.setExamResultId(resultSet.getInt("exam_result_id"));
        if (resultSet.wasNull()) {
            record.setExamResultId(null);
        }
        record.setSymptoms(resultSet.getString("symptoms"));
        record.setPreliminaryDiagnosis(resultSet.getString("preliminary_diagnosis"));
        record.setExamDoctorName(resultSet.getString("exam_doctor_name"));
        record.setExamDate(resultSet.getObject("exam_date", LocalDate.class));
        record.setAppointmentId(resultSet.getInt("appointment_id"));
        if (resultSet.wasNull()) {
            record.setAppointmentId(null);
        }
        record.setAppointmentDoctorName(resultSet.getString("appointment_doctor_name"));
        record.setAppointmentDatetime(resultSet.getObject("appointment_datetime", LocalDateTime.class));
        record.setShift(resultSet.getString("shift"));
        record.setAppointmentStatus(resultSet.getString("appointment_status"));
        record.setPrescriptionId(resultSet.getInt("prescription_id"));
        if (resultSet.wasNull()) {
            record.setPrescriptionId(null);
        }
        record.setPrescriptionDoctorName(resultSet.getString("prescription_doctor_name"));
        record.setPrescriptionDate(resultSet.getObject("prescription_date", LocalDate.class));
        record.setPrescriptionStatus(resultSet.getString("prescription_status"));
        record.setMedicineName(resultSet.getString("medicine_name"));
        record.setMedicineQuantity(resultSet.getInt("medicine_quantity"));
        if (resultSet.wasNull()) {
            record.setMedicineQuantity(null);
        }
        record.setMedicineDosage(resultSet.getString("medicine_dosage"));
        record.includeFeedback();
        return record;
    }

    public static void main(String[] args) {
        AdminBusinessReportDAO dao = new AdminBusinessReportDAO();

        AppointmentSummaryDTO a = dao.getAppointmentSummary(null, null);
        System.out.println(a);

        ArrayList<AppointmentReportDTO> a1 = dao.getAppointmentDetails(null, null, null, null, 1, 50);
        System.out.println(a1.size());
        System.out.println(dao.countAppointmentDetails(null, null, null, null));


        // Test count method
        int totalRecords = dao.countPatientRecordsWithFilters(
                "", LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 7, 31), "");
        System.out.println("Total records: " + totalRecords);

        // Test new method with filters and pagination
        List<PatientRecordDTO> filteredList = dao.getPatientRecordsWithFilters(
                "", LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 7, 31), "", 1, 5);
        System.out.println("Filtered records: " + filteredList.size());

    }
}