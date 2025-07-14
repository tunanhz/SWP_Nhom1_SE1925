package dal;

import dto.DiagnosisResponseDTO;
import dto.MedicalRecordResponseDTO;
import dto.PatientSearchDTO;
import dto.AppointmentSearchDTO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.logging.Logger;

public class DoctorSearchDAO {
    private static final Logger LOGGER = Logger.getLogger(DoctorSearchDAO.class.getName());
    private DBContext dbContext = new DBContext();

    /**
     * Get doctor ID by account staff ID
     */
    public int getDoctorIdByAccountStaffId(int accountStaffId) {
        String sql = "SELECT doctor_id FROM Doctor WHERE account_staff_id = ?";

        try {
            PreparedStatement ps = dbContext.getConnection().prepareStatement(sql);
            ps.setInt(1, accountStaffId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int doctorId = rs.getInt("doctor_id");
                rs.close();
                ps.close();
                return doctorId;
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            LOGGER.severe("Error getting doctor ID for account staff " + accountStaffId + ": " + e.getMessage());
            e.printStackTrace();
        }

        return -1; // Not found
    }

    /**
     * Search patients by name, phone, or patient ID
     */
    public ArrayList<PatientSearchDTO> searchPatients(String query, int doctorId) {
        ArrayList<PatientSearchDTO> patients = new ArrayList<>();

        String sql = """
                SELECT DISTINCT p.patient_id, p.full_name, p.dob, p.gender, p.phone, p.address, p.status
                FROM Patient p
                LEFT JOIN Appointment a ON p.patient_id = a.patient_id
                LEFT JOIN MedicineRecords mr ON p.patient_id = mr.patient_id
                LEFT JOIN Diagnosis d ON mr.medicineRecord_id = d.medicineRecord_id
                LEFT JOIN ExamResult er ON mr.medicineRecord_id = er.medicineRecord_id
                LEFT JOIN Prescription pr ON mr.medicineRecord_id = pr.medicineRecord_id
                WHERE (a.doctor_id = ? OR d.doctor_id = ? OR er.doctor_id = ? OR pr.doctor_id = ?)
                AND (
                    p.full_name LIKE ? OR 
                    p.phone LIKE ? OR 
                    CAST(p.patient_id AS VARCHAR) LIKE ?
                )
                ORDER BY p.full_name
                """;

        try {
            PreparedStatement ps = dbContext.getConnection().prepareStatement(sql);
            // Set doctor IDs
            ps.setInt(1, doctorId);
            ps.setInt(2, doctorId);
            ps.setInt(3, doctorId);
            ps.setInt(4, doctorId);
            // Set search patterns
            String searchPattern = "%" + query + "%";
            ps.setString(5, searchPattern);
            ps.setString(6, searchPattern);
            ps.setString(7, searchPattern);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                PatientSearchDTO patient = new PatientSearchDTO();
                patient.setPatientId(rs.getInt("patient_id"));
                patient.setFullName(rs.getString("full_name"));

                java.sql.Date sqlDate = rs.getDate("dob");
                if (sqlDate != null) {
                    patient.setDob(sqlDate.toLocalDate());
                }

                patient.setGender(rs.getString("gender"));
                patient.setPhone(rs.getString("phone"));
                patient.setAddress(rs.getString("address"));
                patient.setStatus(rs.getString("status"));

                patients.add(patient);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            LOGGER.severe("Error searching patients: " + e.getMessage());
            e.printStackTrace();
        }

        return patients;
    }

    /**
     * Search medical records by patient name or record ID
     */
    public ArrayList<MedicalRecordResponseDTO> searchMedicalRecords(String query, int doctorId) {
        ArrayList<MedicalRecordResponseDTO> records = new ArrayList<>();

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
                AND (
                    p.full_name LIKE ? OR 
                    CAST(mr.medicineRecord_id AS VARCHAR) LIKE ?
                )
                ORDER BY COALESCE(pr.prescription_date, a.appointment_datetime) DESC
                """;

        try {
            PreparedStatement ps = dbContext.getConnection().prepareStatement(sql);
            // Set doctor IDs
            for (int i = 1; i <= 8; i++) {
                ps.setInt(i, doctorId);
            }
            // Set search patterns
            String searchPattern = "%" + query + "%";
            ps.setString(9, searchPattern);
            ps.setString(10, searchPattern);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                MedicalRecordResponseDTO record = new MedicalRecordResponseDTO();
                record.setRecordId(rs.getInt("record_id"));
                record.setPatientName(rs.getString("patient_name"));

                java.sql.Date sqlDate = rs.getDate("record_date");
                if (sqlDate != null) {
                    record.setDate(sqlDate.toLocalDate());
                } else {
                    record.setDate(LocalDate.now());
                }

                record.setType(rs.getString("type"));
                record.setStatus(rs.getString("status"));

                records.add(record);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            LOGGER.severe("Error searching medical records: " + e.getMessage());
            e.printStackTrace();
        }

        return records;
    }

    /**
     * Search appointments by patient name or appointment date
     */
    public ArrayList<AppointmentSearchDTO> searchAppointments(String query, int doctorId) {
        ArrayList<AppointmentSearchDTO> appointments = new ArrayList<>();

        String sql = """
                SELECT 
                    a.appointment_id,
                    p.full_name AS patient_name,
                    a.appointment_datetime,
                    a.shift,
                    a.status,
                    a.note
                FROM Appointment a
                JOIN Patient p ON a.patient_id = p.patient_id
                WHERE a.doctor_id = ?
                AND (
                    p.full_name LIKE ? OR 
                    CAST(a.appointment_datetime AS VARCHAR) LIKE ? OR
                    CAST(a.appointment_id AS VARCHAR) LIKE ?
                )
                ORDER BY a.appointment_datetime DESC
                """;

        try {
            PreparedStatement ps = dbContext.getConnection().prepareStatement(sql);
            ps.setInt(1, doctorId);
            String searchPattern = "%" + query + "%";
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            ps.setString(4, searchPattern);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AppointmentSearchDTO appointment = new AppointmentSearchDTO();
                appointment.setAppointmentId(rs.getInt("appointment_id"));
                appointment.setPatientName(rs.getString("patient_name"));

                java.sql.Timestamp timestamp = rs.getTimestamp("appointment_datetime");
                if (timestamp != null) {
                    appointment.setAppointmentDatetime(timestamp.toLocalDateTime());
                }

                appointment.setShift(rs.getString("shift"));
                appointment.setStatus(rs.getString("status"));
                appointment.setNote(rs.getString("note"));

                appointments.add(appointment);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            LOGGER.severe("Error searching appointments: " + e.getMessage());
            e.printStackTrace();
        }

        return appointments;
    }

    /**
     * Search diagnosis by patient name or diagnosis type
     */
    public ArrayList<DiagnosisResponseDTO> searchDiagnosis(String query, int doctorId) {
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
                AND (
                    p.full_name LIKE ? OR 
                    d.disease LIKE ? OR 
                    d.conclusion LIKE ? OR
                    CAST(d.diagnosis_id AS VARCHAR) LIKE ?
                )
                ORDER BY pr.prescription_date DESC
                """;

        try {
            PreparedStatement ps = dbContext.getConnection().prepareStatement(sql);
            ps.setInt(1, doctorId);
            String searchPattern = "%" + query + "%";
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            ps.setString(4, searchPattern);
            ps.setString(5, searchPattern);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DiagnosisResponseDTO diagnosis = new DiagnosisResponseDTO();
                diagnosis.setDiagnosisId(rs.getInt("diagnosis_id"));
                diagnosis.setPatientName(rs.getString("patient_name"));

                java.sql.Date sqlDate = rs.getDate("diagnosis_date");
                if (sqlDate != null) {
                    diagnosis.setDate(sqlDate.toLocalDate());
                } else {
                    diagnosis.setDate(LocalDate.now());
                }

                diagnosis.setType(rs.getString("type"));
                diagnosis.setStatus(rs.getString("status"));

                diagnosisList.add(diagnosis);
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            LOGGER.severe("Error searching diagnosis: " + e.getMessage());
            e.printStackTrace();
        }

        return diagnosisList;
    }
}
