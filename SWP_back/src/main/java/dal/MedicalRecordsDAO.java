package dal;

import dto.MedicalRecordResponseDTO;
import dto.MedicalRecordRequestDTO;
import model.MedicineRecords;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.logging.Logger;

public class MedicalRecordsDAO {
    private static final Logger LOGGER = Logger.getLogger(MedicalRecordsDAO.class.getName());
    private final DBContext dbContext = new DBContext();

    /**
     * Get medical records for a specific doctor with pagination
     * @param doctorId the doctor's ID
     * @param page page number (1-based)
     * @param pageSize number of records per page
     * @return list of medical records
     */
    public ArrayList<MedicalRecordResponseDTO> getMedicalRecordsByDoctorId(int doctorId, int page, int pageSize) {
        ArrayList<MedicalRecordResponseDTO> recordsList = new ArrayList<>();

        String sql = """
                SELECT DISTINCT
                    mr.medicineRecord_id AS record_id,
                    p.full_name AS patient_name,
                    COALESCE(pr.prescription_date, a.appointment_datetime) AS record_date,
                    CASE 
                        WHEN d.diagnosis_id IS NOT NULL THEN 'Diagnosis'
                        WHEN er.exam_result_id IS NOT NULL THEN 'Examination'
                        WHEN pr.prescription_id IS NOT NULL THEN 'Prescription'
                        ELSE 'General Record'
                    END AS type,
                    CASE 
                        WHEN pr.status = 'Completed' THEN 'Completed'
                        WHEN pr.status = 'Pending' THEN 'In Progress'
                        WHEN a.status = 'Completed' THEN 'Completed'
                        WHEN a.status = 'Confirmed' THEN 'Active'
                        ELSE 'Active'
                    END AS status
                FROM MedicineRecords mr
                JOIN Patient p ON mr.patient_id = p.patient_id
                LEFT JOIN Diagnosis d ON mr.medicineRecord_id = d.medicineRecord_id AND d.doctor_id = ?
                LEFT JOIN ExamResult er ON mr.medicineRecord_id = er.medicineRecord_id AND er.doctor_id = ?
                LEFT JOIN Prescription pr ON mr.medicineRecord_id = pr.medicineRecord_id AND pr.doctor_id = ?
                LEFT JOIN Appointment a ON p.patient_id = a.patient_id AND a.doctor_id = ?
                WHERE (d.doctor_id = ? OR er.doctor_id = ? OR pr.doctor_id = ? OR a.doctor_id = ?)
                ORDER BY COALESCE(pr.prescription_date, a.appointment_datetime) DESC, mr.medicineRecord_id DESC
                OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                """;

        try {
            PreparedStatement ps = dbContext.getConnection().prepareStatement(sql);
            // Set all the doctor_id parameters
            for (int i = 1; i <= 8; i++) {
                ps.setInt(i, doctorId);
            }
            ps.setInt(9, (page - 1) * pageSize);
            ps.setInt(10, pageSize);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MedicalRecordResponseDTO record = new MedicalRecordResponseDTO();
                record.setRecordId(rs.getInt("record_id"));
                record.setPatientName(rs.getString("patient_name"));

                // Handle date conversion
                java.sql.Date sqlDate = rs.getDate("record_date");
                if (sqlDate != null) {
                    record.setDate(sqlDate.toLocalDate());
                } else {
                    record.setDate(LocalDate.now()); // fallback to current date
                }

                record.setType(rs.getString("type"));
                record.setStatus(rs.getString("status"));

                recordsList.add(record);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            LOGGER.severe("Error fetching medical records for doctor " + doctorId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return recordsList;
    }

    /**
     * Count total medical records for a specific doctor
     * @param doctorId the doctor's ID
     * @return total count of medical records
     */
    public int countMedicalRecordsByDoctorId(int doctorId) {
        String sql = """
                SELECT COUNT(DISTINCT mr.medicineRecord_id) AS total
                FROM MedicineRecords mr
                JOIN Patient p ON mr.patient_id = p.patient_id
                LEFT JOIN Diagnosis d ON mr.medicineRecord_id = d.medicineRecord_id AND d.doctor_id = ?
                LEFT JOIN ExamResult er ON mr.medicineRecord_id = er.medicineRecord_id AND er.doctor_id = ?
                LEFT JOIN Prescription pr ON mr.medicineRecord_id = pr.medicineRecord_id AND pr.doctor_id = ?
                LEFT JOIN Appointment a ON p.patient_id = a.patient_id AND a.doctor_id = ?
                WHERE (d.doctor_id = ? OR er.doctor_id = ? OR pr.doctor_id = ? OR a.doctor_id = ?)
                """;

        try {
            PreparedStatement ps = dbContext.getConnection().prepareStatement(sql);
            // Set all the doctor_id parameters
            for (int i = 1; i <= 8; i++) {
                ps.setInt(i, doctorId);
            }

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
            LOGGER.severe("Error counting medical records for doctor " + doctorId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Get all medical records for a specific doctor (without pagination)
     * @param doctorId the doctor's ID
     * @return list of all medical records
     */
    public ArrayList<MedicalRecordResponseDTO> getAllMedicalRecordsByDoctorId(int doctorId) {
        ArrayList<MedicalRecordResponseDTO> recordsList = new ArrayList<>();

        String sql = """
                SELECT DISTINCT
                    mr.medicineRecord_id AS record_id,
                    p.full_name AS patient_name,
                    COALESCE(pr.prescription_date, a.appointment_datetime) AS record_date,
                    CASE 
                        WHEN d.diagnosis_id IS NOT NULL THEN 'Diagnosis'
                        WHEN er.exam_result_id IS NOT NULL THEN 'Examination'
                        WHEN pr.prescription_id IS NOT NULL THEN 'Prescription'
                        ELSE 'General Record'
                    END AS type,
                    CASE 
                        WHEN pr.status = 'Completed' THEN 'Completed'
                        WHEN pr.status = 'Pending' THEN 'In Progress'
                        WHEN a.status = 'Completed' THEN 'Completed'
                        WHEN a.status = 'Confirmed' THEN 'Active'
                        ELSE 'Active'
                    END AS status
                FROM MedicineRecords mr
                JOIN Patient p ON mr.patient_id = p.patient_id
                LEFT JOIN Diagnosis d ON mr.medicineRecord_id = d.medicineRecord_id AND d.doctor_id = ?
                LEFT JOIN ExamResult er ON mr.medicineRecord_id = er.medicineRecord_id AND er.doctor_id = ?
                LEFT JOIN Prescription pr ON mr.medicineRecord_id = pr.medicineRecord_id AND pr.doctor_id = ?
                LEFT JOIN Appointment a ON p.patient_id = a.patient_id AND a.doctor_id = ?
                WHERE (d.doctor_id = ? OR er.doctor_id = ? OR pr.doctor_id = ? OR a.doctor_id = ?)
                ORDER BY COALESCE(pr.prescription_date, a.appointment_datetime) DESC, mr.medicineRecord_id DESC
                """;

        try {
            PreparedStatement ps = dbContext.getConnection().prepareStatement(sql);
            // Set all the doctor_id parameters
            for (int i = 1; i <= 8; i++) {
                ps.setInt(i, doctorId);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MedicalRecordResponseDTO record = new MedicalRecordResponseDTO();
                record.setRecordId(rs.getInt("record_id"));
                record.setPatientName(rs.getString("patient_name"));

                // Handle date conversion
                java.sql.Date sqlDate = rs.getDate("record_date");
                if (sqlDate != null) {
                    record.setDate(sqlDate.toLocalDate());
                } else {
                    record.setDate(LocalDate.now()); // fallback to current date
                }

                record.setType(rs.getString("type"));
                record.setStatus(rs.getString("status"));

                recordsList.add(record);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            LOGGER.severe("Error fetching all medical records for doctor " + doctorId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return recordsList;
    }

    /**
     * Test method to verify DAO functionality
     */
    public static void main(String[] args) {
        MedicalRecordsDAO dao = new MedicalRecordsDAO();

        // Test with a sample doctor ID
        int testDoctorId = 1;

        System.out.println("Testing MedicalRecordsDAO...");

        // Test count method
        int count = dao.countMedicalRecordsByDoctorId(testDoctorId);
        System.out.println("Total medical records for doctor " + testDoctorId + ": " + count);

        // Test paginated method
        ArrayList<MedicalRecordResponseDTO> recordsList = dao.getMedicalRecordsByDoctorId(testDoctorId, 1, 5);
        System.out.println("First 5 medical records:");
        for (MedicalRecordResponseDTO record : recordsList) {
            System.out.println("  " + record.toString());
        }

        System.out.println("MedicalRecordsDAO test completed.");
    }

    /**
     * Create a new medical record
     * @param medicalRecordRequest the medical record data to create
     * @return the created MedicineRecords object with generated ID
     * @throws SQLException if database operation fails
     */
    public MedicineRecords createMedicalRecord(MedicalRecordRequestDTO medicalRecordRequest) throws SQLException {
        String sql = "INSERT INTO MedicineRecords (patient_id) VALUES (?)";

        try (PreparedStatement ps = dbContext.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, medicalRecordRequest.getPatientId());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating medical record failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    MedicineRecords medicalRecord = new MedicineRecords();
                    medicalRecord.setMedicineRecordId(generatedKeys.getInt(1));
                    medicalRecord.setPatientId(medicalRecordRequest.getPatientId());
                    return medicalRecord;
                } else {
                    throw new SQLException("Creating medical record failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error creating medical record: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get a medical record by its ID
     * @param recordId the medical record ID
     * @return the MedicineRecords object or null if not found
     * @throws SQLException if database operation fails
     */
    public MedicineRecords getMedicalRecordById(int recordId) throws SQLException {
        String sql = "SELECT medicineRecord_id, patient_id FROM MedicineRecords WHERE medicineRecord_id = ?";

        try (PreparedStatement ps = dbContext.getConnection().prepareStatement(sql)) {
            ps.setInt(1, recordId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MedicineRecords medicalRecord = new MedicineRecords();
                    medicalRecord.setMedicineRecordId(rs.getInt("medicineRecord_id"));
                    medicalRecord.setPatientId(rs.getInt("patient_id"));
                    return medicalRecord;
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Error getting medical record by ID: " + e.getMessage());
            throw e;
        }
        return null;
    }

    /**
     * Update an existing medical record
     * @param recordId the ID of the medical record to update
     * @param medicalRecordRequest the updated medical record data
     * @return the updated MedicineRecords object
     * @throws SQLException if database operation fails
     */
    public MedicineRecords updateMedicalRecord(int recordId, MedicalRecordRequestDTO medicalRecordRequest) throws SQLException {
        String sql = "UPDATE MedicineRecords SET patient_id = ? WHERE medicineRecord_id = ?";

        try (PreparedStatement ps = dbContext.getConnection().prepareStatement(sql)) {
            ps.setInt(1, medicalRecordRequest.getPatientId());
            ps.setInt(2, recordId);

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating medical record failed, no rows affected.");
            }

            // Return the updated medical record
            MedicineRecords medicalRecord = new MedicineRecords();
            medicalRecord.setMedicineRecordId(recordId);
            medicalRecord.setPatientId(medicalRecordRequest.getPatientId());
            return medicalRecord;

        } catch (SQLException e) {
            LOGGER.severe("Error updating medical record: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Delete a medical record
     * @param recordId the ID of the medical record to delete
     * @return true if deletion was successful, false otherwise
     * @throws SQLException if database operation fails
     */
    public boolean deleteMedicalRecord(int recordId) throws SQLException {
        String sql = "DELETE FROM MedicineRecords WHERE medicineRecord_id = ?";

        try (PreparedStatement ps = dbContext.getConnection().prepareStatement(sql)) {
            ps.setInt(1, recordId);

            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            LOGGER.severe("Error deleting medical record: " + e.getMessage());
            throw e;
        }
    }
}
