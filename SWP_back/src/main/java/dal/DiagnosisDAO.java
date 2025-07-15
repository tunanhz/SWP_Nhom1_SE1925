package dal;

import dto.DiagnosisResponseDTO;
import dto.DiagnosisRequestDTO;
import model.Diagnosis;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.logging.Logger;

public class DiagnosisDAO {
    private static final Logger LOGGER = Logger.getLogger(DiagnosisDAO.class.getName());
    private final DBContext dbContext = new DBContext();

    /**
     * Get diagnosis records for a specific doctor with pagination
     * @param doctorId the doctor's ID
     * @param page page number (1-based)
     * @param pageSize number of records per page
     * @return list of diagnosis records
     */
    public ArrayList<DiagnosisResponseDTO> getDiagnosisByDoctorId(int doctorId, int page, int pageSize) {
        ArrayList<DiagnosisResponseDTO> diagnosisList = new ArrayList<>();

        String sql = """
                SELECT 
                    d.diagnosis_id,
                    p.full_name AS patient_name,
                    pr.prescription_date AS diagnosis_date,
                    d.disease AS type,
                    CASE 
                        WHEN pr.status = 'Completed' THEN 'Completed'
                        WHEN pr.status = 'Pending' THEN 'In Progress'
                        ELSE 'Active'
                    END AS status
                FROM Diagnosis d
                JOIN MedicineRecords mr ON d.medicineRecord_id = mr.medicineRecord_id
                JOIN Patient p ON mr.patient_id = p.patient_id
                LEFT JOIN Prescription pr ON mr.medicineRecord_id = pr.medicineRecord_id
                WHERE d.doctor_id = ?
                ORDER BY pr.prescription_date DESC, d.diagnosis_id DESC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;

        try {
            PreparedStatement ps = dbContext.getConnection().prepareStatement(sql);
            ps.setInt(1, doctorId);
            ps.setInt(2, (page - 1) * pageSize);
            ps.setInt(3, pageSize);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DiagnosisResponseDTO diagnosis = new DiagnosisResponseDTO();
                diagnosis.setDiagnosisId(rs.getInt("diagnosis_id"));
                diagnosis.setPatientName(rs.getString("patient_name"));

                // Handle date conversion
                java.sql.Date sqlDate = rs.getDate("diagnosis_date");
                if (sqlDate != null) {
                    diagnosis.setDate(sqlDate.toLocalDate());
                } else {
                    diagnosis.setDate(LocalDate.now()); // fallback to current date
                }

                diagnosis.setType(rs.getString("type"));
                diagnosis.setStatus(rs.getString("status"));

                diagnosisList.add(diagnosis);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            LOGGER.severe("Error fetching diagnosis records for doctor " + doctorId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return diagnosisList;
    }

    /**
     * Count total diagnosis records for a specific doctor
     * @param doctorId the doctor's ID
     * @return total count of diagnosis records
     */
    public int countDiagnosisByDoctorId(int doctorId) {
        String sql = """
                SELECT COUNT(*) AS total
                FROM Diagnosis d
                JOIN MedicineRecords mr ON d.medicineRecord_id = mr.medicineRecord_id
                WHERE d.doctor_id = ?
                """;

        try {
            PreparedStatement ps = dbContext.getConnection().prepareStatement(sql);
            ps.setInt(1, doctorId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total");
                rs.close();
                ps.close();
                return total;
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            LOGGER.severe("Error counting diagnosis records for doctor " + doctorId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Get all diagnosis records for a specific doctor (without pagination)
     * @param doctorId the doctor's ID
     * @return list of all diagnosis records
     */
    public ArrayList<DiagnosisResponseDTO> getAllDiagnosisByDoctorId(int doctorId) {
        ArrayList<DiagnosisResponseDTO> diagnosisList = new ArrayList<>();

        String sql = """
                SELECT 
                    d.diagnosis_id,
                    p.full_name AS patient_name,
                    pr.prescription_date AS diagnosis_date,
                    d.disease AS type,
                    CASE 
                        WHEN pr.status = 'Completed' THEN 'Completed'
                        WHEN pr.status = 'Pending' THEN 'In Progress'
                        ELSE 'Active'
                    END AS status
                FROM Diagnosis d
                JOIN MedicineRecords mr ON d.medicineRecord_id = mr.medicineRecord_id
                JOIN Patient p ON mr.patient_id = p.patient_id
                LEFT JOIN Prescription pr ON mr.medicineRecord_id = pr.medicineRecord_id
                WHERE d.doctor_id = ?
                ORDER BY pr.prescription_date DESC, d.diagnosis_id DESC
                """;

        try {
            PreparedStatement ps = dbContext.getConnection().prepareStatement(sql);
            ps.setInt(1, doctorId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DiagnosisResponseDTO diagnosis = new DiagnosisResponseDTO();
                diagnosis.setDiagnosisId(rs.getInt("diagnosis_id"));
                diagnosis.setPatientName(rs.getString("patient_name"));

                // Handle date conversion
                java.sql.Date sqlDate = rs.getDate("diagnosis_date");
                if (sqlDate != null) {
                    diagnosis.setDate(sqlDate.toLocalDate());
                } else {
                    diagnosis.setDate(LocalDate.now()); // fallback to current date
                }

                diagnosis.setType(rs.getString("type"));
                diagnosis.setStatus(rs.getString("status"));

                diagnosisList.add(diagnosis);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            LOGGER.severe("Error fetching all diagnosis records for doctor " + doctorId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return diagnosisList;
    }

    /**
     * Test method to verify DAO functionality
     */
    public static void main(String[] args) {
        DiagnosisDAO dao = new DiagnosisDAO();

        // Test with a sample doctor ID
        int testDoctorId = 1;

        System.out.println("Testing DiagnosisDAO...");

        // Test count method
        int count = dao.countDiagnosisByDoctorId(testDoctorId);
        System.out.println("Total diagnosis records for doctor " + testDoctorId + ": " + count);

        // Test paginated method
        ArrayList<DiagnosisResponseDTO> diagnosisList = dao.getDiagnosisByDoctorId(testDoctorId, 1, 5);
        System.out.println("First 5 diagnosis records:");
        for (DiagnosisResponseDTO diagnosis : diagnosisList) {
            System.out.println("  " + diagnosis.toString());
        }

        System.out.println("DiagnosisDAO test completed.");
    }

    /**
     * Validate if a doctor has access to a medical record
     * @param doctorId the doctor's ID
     * @param medicineRecordId the medical record ID
     * @return true if doctor has access, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean validateDoctorMedicalRecordAccess(int doctorId, int medicineRecordId) throws SQLException {
        String sql = """
                SELECT COUNT(*) as access_count
                FROM MedicineRecords mr
                JOIN Patient p ON mr.patient_id = p.patient_id
                WHERE mr.medicineRecord_id = ?
                AND (
                    EXISTS (SELECT 1 FROM Appointment a WHERE a.patient_id = p.patient_id AND a.doctor_id = ?)
                    OR EXISTS (SELECT 1 FROM Diagnosis d WHERE d.medicineRecord_id = mr.medicineRecord_id AND d.doctor_id = ?)
                    OR EXISTS (SELECT 1 FROM ExamResult er WHERE er.medicineRecord_id = mr.medicineRecord_id AND er.doctor_id = ?)
                    OR EXISTS (SELECT 1 FROM Prescription pr WHERE pr.medicineRecord_id = mr.medicineRecord_id AND pr.doctor_id = ?)
                )
                """;

        try (PreparedStatement ps = dbContext.getConnection().prepareStatement(sql)) {
            ps.setInt(1, medicineRecordId);
            ps.setInt(2, doctorId);
            ps.setInt(3, doctorId);
            ps.setInt(4, doctorId);
            ps.setInt(5, doctorId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("access_count") > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error validating doctor medical record access: " + e.getMessage());
            throw e;
        }
        return false;
    }

    /**
     * Get patient information for a medical record
     * @param medicineRecordId the medical record ID
     * @return patient information or null if not found
     * @throws SQLException if database operation fails
     */
    public String getPatientInfoForMedicalRecord(int medicineRecordId) throws SQLException {
        String sql = """
                SELECT p.full_name, p.dob, p.gender, p.phone
                FROM MedicineRecords mr
                JOIN Patient p ON mr.patient_id = p.patient_id
                WHERE mr.medicineRecord_id = ?
                """;

        try (PreparedStatement ps = dbContext.getConnection().prepareStatement(sql)) {
            ps.setInt(1, medicineRecordId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return String.format("Patient: %s, DOB: %s, Gender: %s, Phone: %s",
                            rs.getString("full_name"),
                            rs.getDate("dob"),
                            rs.getString("gender"),
                            rs.getString("phone"));
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error getting patient info for medical record: " + e.getMessage());
            throw e;
        }
        return null;
    }

    /**
     * Create a new diagnosis record with business validation
     * @param diagnosisRequest the diagnosis data to create
     * @return the created Diagnosis object with generated ID
     * @throws SQLException if database operation fails
     */
    public Diagnosis createDiagnosis(DiagnosisRequestDTO diagnosisRequest) throws SQLException {
        // Validate that the doctor has access to the medical record
        if (!validateDoctorMedicalRecordAccess(diagnosisRequest.getDoctorId(), diagnosisRequest.getMedicineRecordId())) {
            throw new SQLException("Doctor does not have access to the specified medical record");
        }
        String sql = "INSERT INTO Diagnosis (doctor_id, medicineRecord_id, conclusion, disease, treatment_plan) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = dbContext.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, diagnosisRequest.getDoctorId());
            ps.setInt(2, diagnosisRequest.getMedicineRecordId());
            ps.setString(3, diagnosisRequest.getConclusion());
            ps.setString(4, diagnosisRequest.getDisease());
            ps.setString(5, diagnosisRequest.getTreatmentPlan());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating diagnosis failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Diagnosis diagnosis = new Diagnosis();
                    diagnosis.setDiagnosisId(generatedKeys.getInt(1));
                    diagnosis.setDoctorId(diagnosisRequest.getDoctorId());
                    diagnosis.setMedicineRecordId(diagnosisRequest.getMedicineRecordId());
                    diagnosis.setConclusion(diagnosisRequest.getConclusion());
                    diagnosis.setDisease(diagnosisRequest.getDisease());
                    diagnosis.setTreatmentPlan(diagnosisRequest.getTreatmentPlan());
                    return diagnosis;
                } else {
                    throw new SQLException("Creating diagnosis failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error creating diagnosis: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get a diagnosis by its ID
     * @param diagnosisId the diagnosis ID
     * @return the Diagnosis object or null if not found
     * @throws SQLException if database operation fails
     */
    public Diagnosis getDiagnosisById(int diagnosisId) throws SQLException {
        String sql = "SELECT diagnosis_id, doctor_id, medicineRecord_id, conclusion, disease, treatment_plan FROM Diagnosis WHERE diagnosis_id = ?";

        try (PreparedStatement ps = dbContext.getConnection().prepareStatement(sql)) {
            ps.setInt(1, diagnosisId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Diagnosis diagnosis = new Diagnosis();
                    diagnosis.setDiagnosisId(rs.getInt("diagnosis_id"));
                    diagnosis.setDoctorId(rs.getInt("doctor_id"));
                    diagnosis.setMedicineRecordId(rs.getInt("medicineRecord_id"));
                    diagnosis.setConclusion(rs.getString("conclusion"));
                    diagnosis.setDisease(rs.getString("disease"));
                    diagnosis.setTreatmentPlan(rs.getString("treatment_plan"));
                    return diagnosis;
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error getting diagnosis by ID: " + e.getMessage());
            throw e;
        }
        return null;
    }

    /**
     * Update an existing diagnosis record with business validation
     * @param diagnosisId the ID of the diagnosis to update
     * @param diagnosisRequest the updated diagnosis data
     * @return the updated Diagnosis object
     * @throws SQLException if database operation fails
     */
    public Diagnosis updateDiagnosis(int diagnosisId, DiagnosisRequestDTO diagnosisRequest) throws SQLException {
        // Validate that the diagnosis exists and belongs to the doctor
        Diagnosis existingDiagnosis = getDiagnosisById(diagnosisId);
        if (existingDiagnosis == null) {
            throw new SQLException("Diagnosis not found");
        }

        if (existingDiagnosis.getDoctorId() != diagnosisRequest.getDoctorId()) {
            throw new SQLException("Doctor does not have permission to update this diagnosis");
        }

        // Business rule: Medical record cannot be changed during update
        if (existingDiagnosis.getMedicineRecordId() != diagnosisRequest.getMedicineRecordId()) {
            throw new SQLException("Medical record cannot be changed when updating a diagnosis");
        }
        // Only update allowed fields: conclusion, disease, treatmentPlan
        // Medical record and doctor cannot be changed
        String sql = "UPDATE Diagnosis SET conclusion = ?, disease = ?, treatment_plan = ? WHERE diagnosis_id = ? AND doctor_id = ?";

        try (PreparedStatement ps = dbContext.getConnection().prepareStatement(sql)) {
            ps.setString(1, diagnosisRequest.getConclusion());
            ps.setString(2, diagnosisRequest.getDisease());
            ps.setString(3, diagnosisRequest.getTreatmentPlan());
            ps.setInt(4, diagnosisId);
            ps.setInt(5, diagnosisRequest.getDoctorId()); // Ensure only the original doctor can update

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating diagnosis failed, no rows affected.");
            }

            // Return the updated diagnosis with original doctor_id and medicineRecord_id
            Diagnosis diagnosis = new Diagnosis();
            diagnosis.setDiagnosisId(diagnosisId);
            diagnosis.setDoctorId(existingDiagnosis.getDoctorId()); // Keep original doctor
            diagnosis.setMedicineRecordId(existingDiagnosis.getMedicineRecordId()); // Keep original medical record
            diagnosis.setConclusion(diagnosisRequest.getConclusion());
            diagnosis.setDisease(diagnosisRequest.getDisease());
            diagnosis.setTreatmentPlan(diagnosisRequest.getTreatmentPlan());
            return diagnosis;

        } catch (SQLException e) {
            LOGGER.severe("Error updating diagnosis: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Delete a diagnosis record
     * @param diagnosisId the ID of the diagnosis to delete
     * @return true if deletion was successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean deleteDiagnosis(int diagnosisId) throws SQLException {
        String sql = "DELETE FROM Diagnosis WHERE diagnosis_id = ?";

        try (PreparedStatement ps = dbContext.getConnection().prepareStatement(sql)) {
            ps.setInt(1, diagnosisId);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            LOGGER.severe("Error deleting diagnosis: " + e.getMessage());
            throw e;
        }
    }
}
