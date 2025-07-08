package dal;

import dto.PatientRecordDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatientRecordDAO {
    private static final Logger LOGGER = Logger.getLogger(PatientRecordDAO.class.getName());
    private DBContext ad = new DBContext();

    // SQL query for account patient ID
    private static final String SELECT_PATIENT_RECORDS_BY_ACCOUNT =
            "SELECT p.patient_id, p.full_name AS patient_name, p.dob, p.gender, p.phone, p.address, p.status AS patient_status, " +
                    "mr.medicineRecord_id, " +
                    "d.diagnosis_id, d_doctor.full_name AS diagnosis_doctor_name, d.conclusion, d.disease, d.treatment_plan, pr.prescription_date AS diagnosis_date, " +
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
                    "JOIN Patient_AccountPatient pa ON pa.patient_id = p.patient_id " +
                    "JOIN AccountPatient ap ON ap.account_patient_id = pa.account_patient_id " +
                    "WHERE ap.account_patient_id = ? " +
                    "ORDER BY p.patient_id, mr.medicineRecord_id, d.diagnosis_id, er.exam_result_id, a.appointment_datetime, pr.prescription_id, m.medicine_id";

    // SQL query for patient ID
    private static final String SELECT_PATIENT_RECORDS_BY_PATIENT =
            "SELECT p.patient_id, p.full_name AS patient_name, p.dob, p.gender, p.phone, p.address, p.status AS patient_status, " +
                    "mr.medicineRecord_id, " +
                    "d.diagnosis_id, d_doctor.full_name AS diagnosis_doctor_name, d.conclusion, d.disease, d.treatment_plan, pr.prescription_date AS diagnosis_date, " +
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
                    "WHERE p.patient_id = ? " +
                    "ORDER BY p.patient_id, mr.medicineRecord_id, d.diagnosis_id, er.exam_result_id, a.appointment_datetime, pr.prescription_id, m.medicine_id";

    // Retrieve patient records by account patient ID
    public List<PatientRecordDTO> getPatientRecordsByAccountId(int accountPatientId) {
        List<PatientRecordDTO> records = new ArrayList<>();

        try (PreparedStatement preparedStatement = ad.getConnection().prepareStatement(SELECT_PATIENT_RECORDS_BY_ACCOUNT)) {
            preparedStatement.setInt(1, accountPatientId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    PatientRecordDTO record = mapResultSetToDTO(resultSet);
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing query for accountPatientId: " + accountPatientId, e);
        }

        return records;
    }

    // Retrieve patient records by account patient ID with filters and pagination
    public List<PatientRecordDTO> getPatientRecordsByAccountIdWithFilters(
            int accountPatientId,
            String patientName,
            LocalDate startDate,
            LocalDate endDate,
            String gender,
            int page,
            int size) {
        List<PatientRecordDTO> records = new ArrayList<>();

        // Build dynamic SQL query
        StringBuilder sql = new StringBuilder(
                "SELECT p.patient_id, p.full_name AS patient_name, p.dob, p.gender, p.phone, p.address, p.status AS patient_status, " +
                        "mr.medicineRecord_id, " +
                        "d.diagnosis_id, d_doctor.full_name AS diagnosis_doctor_name, d.conclusion, d.disease, d.treatment_plan, pr.prescription_date AS diagnosis_date, " +
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
                        "JOIN Patient_AccountPatient pa ON pa.patient_id = p.patient_id " +
                        "JOIN AccountPatient ap ON ap.account_patient_id = pa.account_patient_id " +
                        "WHERE ap.account_patient_id = ? AND a.status = 'Completed'"
        );

        List<Object> params = new ArrayList<>();
        params.add(accountPatientId);

        // Add filters
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

        // Add sorting
        sql.append(" ORDER BY p.patient_id, mr.medicineRecord_id, d.diagnosis_id, er.exam_result_id, a.appointment_datetime, pr.prescription_id, m.medicine_id");

        // Add pagination
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add((page - 1) * size);
        params.add(size);

        try (PreparedStatement preparedStatement = ad.getConnection().prepareStatement(sql.toString())) {
            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    PatientRecordDTO record = mapResultSetToDTO(resultSet);
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing query for accountPatientId: " + accountPatientId, e);
        }

        return records;
    }

    // Count patient records by account patient ID with filters
    public int countPatientRecordsByAccountIdWithFilters(
            int accountPatientId,
            String patientName,
            LocalDate startDate,
            LocalDate endDate,
            String gender) {
        int totalRecords = 0;

        // Build dynamic SQL query for counting
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
                        "JOIN Patient_AccountPatient pa ON pa.patient_id = p.patient_id " +
                        "JOIN AccountPatient ap ON ap.account_patient_id = pa.account_patient_id " +
                        "WHERE ap.account_patient_id = ? AND a.status = 'Completed'"
        );

        List<Object> params = new ArrayList<>();
        params.add(accountPatientId);

        // Add filters
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
            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                preparedStatement.setObject(i + 1, params.get(i));
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    totalRecords = resultSet.getInt("total");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting records for accountPatientId: " + accountPatientId, e);
        }

        return totalRecords;
    }

    // Retrieve patient records by patient ID
    public List<PatientRecordDTO> getPatientRecordsByPatientId(int patientId) {
        List<PatientRecordDTO> records = new ArrayList<>();

        try (PreparedStatement preparedStatement = ad.getConnection().prepareStatement(SELECT_PATIENT_RECORDS_BY_PATIENT)) {
            preparedStatement.setInt(1, patientId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    PatientRecordDTO record = mapResultSetToDTO(resultSet);
                    records.add(record);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing query for patientId: " + patientId, e);
        }

        return records;
    }

    // Helper method to map ResultSet to PatientRecordDTO
    private PatientRecordDTO mapResultSetToDTO(ResultSet resultSet) throws SQLException {
        PatientRecordDTO record = new PatientRecordDTO();

        // Patient information
        record.setPatientId(resultSet.getInt("patient_id"));
        record.setPatientName(resultSet.getString("patient_name"));
        record.setDob(resultSet.getObject("dob", LocalDate.class));
        record.setGender(resultSet.getString("gender"));
        record.setPhone(resultSet.getString("phone"));
        record.setAddress(resultSet.getString("address"));
        record.setPatientStatus(resultSet.getString("patient_status"));

        // Medicine Record
        record.setMedicineRecordId(resultSet.getInt("medicineRecord_id"));
        if (resultSet.wasNull()) {
            record.setMedicineRecordId(null);
        }

        // Diagnosis
        record.setDiagnosisId(resultSet.getInt("diagnosis_id"));
        if (resultSet.wasNull()) {
            record.setDiagnosisId(null);
        }
        record.setDiagnosisDoctorName(resultSet.getString("diagnosis_doctor_name"));
        record.setConclusion(resultSet.getString("conclusion"));
        record.setDisease(resultSet.getString("disease"));
        record.setTreatmentPlan(resultSet.getString("treatment_plan"));
        record.setDiagnosisDate(resultSet.getObject("diagnosis_date", LocalDate.class));

        // Exam Result
        record.setExamResultId(resultSet.getInt("exam_result_id"));
        if (resultSet.wasNull()) {
            record.setExamResultId(null);
        }
        record.setSymptoms(resultSet.getString("symptoms"));
        record.setPreliminaryDiagnosis(resultSet.getString("preliminary_diagnosis"));
        record.setExamDoctorName(resultSet.getString("exam_doctor_name"));
        record.setExamDate(resultSet.getObject("exam_date", LocalDate.class));

        // Appointment
        record.setAppointmentId(resultSet.getInt("appointment_id"));
        if (resultSet.wasNull()) {
            record.setAppointmentId(null);
        }
        record.setAppointmentDoctorName(resultSet.getString("appointment_doctor_name"));
        record.setAppointmentDatetime(resultSet.getObject("appointment_datetime", LocalDateTime.class));
        record.setShift(resultSet.getString("shift"));
        record.setAppointmentStatus(resultSet.getString("appointment_status"));

        // Prescription
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
        PatientRecordDAO dao = new PatientRecordDAO();
        List<PatientRecordDTO> list = dao.getPatientRecordsByAccountId(1);
        System.out.println("Records by accountPatientId: " + list.size());

        List<PatientRecordDTO> list1 = dao.getPatientRecordsByPatientId(1);
        System.out.println("Records by patientId: " + list1.size());

        // Test new method with filters and pagination
        List<PatientRecordDTO> filteredList = dao.getPatientRecordsByAccountIdWithFilters(
                1, "", LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 7, 31), "", 1, 10);
        System.out.println("Filtered records: " + filteredList.size());

        // Test count method
        int totalRecords = dao.countPatientRecordsByAccountIdWithFilters(
                1, "", LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 7, 31), "");
        System.out.println("Total records: " + totalRecords);
    }
}