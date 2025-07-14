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
     * Create a new diagnosis record
     * @param diagnosisRequest the diagnosis data to create
     * @return the created Diagnosis object with generated ID
     * @throws SQLException if database operation fails
     */
    public Diagnosis createDiagnosis(DiagnosisRequestDTO diagnosisRequest) throws SQLException {
        String sql = "INSERT INTO Diagnosis (doctor_id, medicineRecord_id, conclusion, disease, treatmentPlan) VALUES (?, ?, ?, ?, ?)";

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
        String sql = "SELECT diagnosis_id, doctor_id, medicineRecord_id, conclusion, disease, treatmentPlan FROM Diagnosis WHERE diagnosis_id = ?";

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
                    diagnosis.setTreatmentPlan(rs.getString("treatmentPlan"));
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
     * Update an existing diagnosis record
     * @param diagnosisId the ID of the diagnosis to update
     * @param diagnosisRequest the updated diagnosis data
     * @return the updated Diagnosis object
     * @throws SQLException if database operation fails
     */
    public Diagnosis updateDiagnosis(int diagnosisId, DiagnosisRequestDTO diagnosisRequest) throws SQLException {
        String sql = "UPDATE Diagnosis SET doctor_id = ?, medicineRecord_id = ?, conclusion = ?, disease = ?, treatmentPlan = ? WHERE diagnosis_id = ?";

        try (PreparedStatement ps = dbContext.getConnection().prepareStatement(sql)) {
            ps.setInt(1, diagnosisRequest.getDoctorId());
            ps.setInt(2, diagnosisRequest.getMedicineRecordId());
            ps.setString(3, diagnosisRequest.getConclusion());
            ps.setString(4, diagnosisRequest.getDisease());
            ps.setString(5, diagnosisRequest.getTreatmentPlan());
            ps.setInt(6, diagnosisId);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating diagnosis failed, no rows affected.");
            }

            // Return the updated diagnosis
            Diagnosis diagnosis = new Diagnosis();
            diagnosis.setDiagnosisId(diagnosisId);
            diagnosis.setDoctorId(diagnosisRequest.getDoctorId());
            diagnosis.setMedicineRecordId(diagnosisRequest.getMedicineRecordId());
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
