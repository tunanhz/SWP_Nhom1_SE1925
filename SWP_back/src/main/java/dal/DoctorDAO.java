package dal;

import dto.WaitlistDTO;
import model.Doctor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

public class DoctorDAO {

    DBContext ad = new DBContext();

    public ArrayList<Doctor> getAllDoctors(int page, int size) {
        ArrayList<Doctor> l = new ArrayList<>();
        String xSql = """
                Select d.doctor_id, d.full_name, d.phone, d.eduLevel,d.department, a.email, a.[img]  from [dbo].[Doctor] d
                join [dbo].[AccountStaff] a on a.account_staff_id = d.account_staff_id
                where a.role = 'Doctor' and a.status = 'Enable'
                order by d.doctor_id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;
                """;

        try {
            PreparedStatement ps = ad.getConnection().prepareStatement(xSql);
            int offset = (page - 1) * size;
            ps.setInt(1, offset);
            ps.setInt(2, size);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                l.add(new Doctor(rs.getInt("doctor_id"), rs.getNString("full_name"),
                        rs.getNString("phone"), rs.getNString("eduLevel"),
                        rs.getNString("department"), rs.getNString("email"),
                        rs.getNString("img")));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return (l);
    }

    public ArrayList<Doctor> searchDoctorsByName(String name, int page, int size) {
        ArrayList<Doctor> doctors = new ArrayList<>();
        String sql = """
                 Select d.doctor_id, d.full_name, d.phone, d.eduLevel,d.department, a.email, a.[img]  from [dbo].[Doctor] d
                 join [dbo].[AccountStaff] a on a.account_staff_id = d.account_staff_id
                 where a.role = 'Doctor' and a.status = 'Enable' AND d.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?
                 order by d.doctor_id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY;
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            // Prepare search pattern (case-insensitive)
            stmt.setNString(1, "%" + name.trim() + "%");
            stmt.setInt(2, (page - 1) * size);
            stmt.setInt(3, size);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                doctors.add(new Doctor(rs.getInt("doctor_id"), rs.getNString("full_name"),
                        rs.getNString("phone"), rs.getNString("eduLevel"),
                        rs.getNString("department"), rs.getNString("email"),
                        rs.getNString("img")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return doctors;
    }

    public int getTotalDoctorsByName(String name) {
        String sql = """
                SELECT COUNT(*) FROM [dbo].[Doctor] d
                join [dbo].[AccountStaff] a on a.account_staff_id = d.account_staff_id 
                where a.role = 'Doctor' and a.status = 'Enable' AND d.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            stmt.setString(1, "%" + name.trim() + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalDoctors() {
        String sql = """
                   SELECT COUNT(*) from [dbo].[Doctor] d
                   join [dbo].[AccountStaff] a on a.account_staff_id = d.account_staff_id
                   where a.role = 'Doctor' and a.status = 'Enable'
                """;
        try {
            PreparedStatement ps = ad.getConnection().prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting doctors: " + e.getMessage());
        }
        return 0;
    }

    public Doctor getDoctorById(int id) {
        ArrayList<Doctor> l = new ArrayList<>();
        String xSql = """
                Select d.doctor_id, d.full_name, d.phone, d.eduLevel,d.department, a.email, a.[img]  from [dbo].[Doctor] d
                join [dbo].[AccountStaff] a on a.account_staff_id = d.account_staff_id
                where a.role = 'Doctor' and a.status = 'Enable' and d.doctor_id = ?                      
                """;
        Doctor d = null;
        try {
            PreparedStatement ps = ad.getConnection().prepareStatement(xSql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                d = new Doctor(rs.getInt("doctor_id"), rs.getNString("full_name"),
                        rs.getNString("phone"), rs.getNString("eduLevel"),
                        rs.getNString("department"), rs.getNString("email"),
                        rs.getNString("img"));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return d;
    }

    public ArrayList<Doctor> searchDoctorsByNameAndDepartment(String name, String department, int page, int size) {
        // Input validation
        if (page < 1 || size < 1) {
            throw new IllegalArgumentException("Page and size must be positive");
        }

        ArrayList<Doctor> doctors = new ArrayList<>();
        String sql = """
                    SELECT 
                        d.doctor_id, 
                        d.full_name, 
                        d.phone, 
                        d.eduLevel, 
                        d.department, 
                        a.email, 
                        a.[img]
                    FROM [dbo].[Doctor] d
                    JOIN [dbo].[AccountStaff] a ON a.account_staff_id = d.account_staff_id
                    WHERE 
                        a.role = 'Doctor' 
                        AND a.status = 'Enable' 
                        AND (? IS NULL OR d.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                        AND (? IS NULL OR d.department COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                    ORDER BY d.doctor_id 
                    OFFSET ? ROWS 
                    FETCH NEXT ? ROWS ONLY;
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            // Prepare parameters
            String namePattern = (name == null || name.trim().isEmpty()) ? null : "%" + name.trim() + "%";
            String deptPattern = (department == null || department.trim().isEmpty()) ? null : "%" + department.trim() + "%";

            // Set parameters for name
            stmt.setNString(1, namePattern);
            stmt.setNString(2, namePattern);

            // Set parameters for department
            stmt.setNString(3, deptPattern);
            stmt.setNString(4, deptPattern);

            // Set pagination parameters
            stmt.setInt(5, (page - 1) * size);
            stmt.setInt(6, size);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    doctors.add(new Doctor(
                            rs.getInt("doctor_id"),
                            rs.getNString("full_name"),
                            rs.getNString("phone"),
                            rs.getNString("eduLevel"),
                            rs.getNString("department"),
                            rs.getNString("email"),
                            rs.getNString("img")
                    ));
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error searching doctors: " + e.getMessage());
            throw new RuntimeException("Failed to search doctors", e);
        }

        return doctors;
    }

    public int getTotalDoctorsByNameAndDepartment(String name, String department) {
        String sql = """
                    SELECT COUNT(*) 
                    FROM [dbo].[Doctor] d
                    JOIN [dbo].[AccountStaff] a ON a.account_staff_id = d.account_staff_id
                    WHERE 
                        a.role = 'Doctor' 
                        AND a.status = 'Enable' 
                        AND (? IS NULL OR d.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                        AND (? IS NULL OR d.department COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?);
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);

            // Prepare parameters
            String namePattern = (name == null || name.trim().isEmpty()) ? null : "%" + name.trim() + "%";
            String deptPattern = (department == null || department.trim().isEmpty()) ? null : "%" + department.trim() + "%";

            stmt.setNString(1, namePattern);
            stmt.setNString(2, namePattern);
            stmt.setNString(3, deptPattern);
            stmt.setNString(4, deptPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error counting doctors: " + e.getMessage());
            throw new RuntimeException("Failed to count doctors", e);
        }
        return 0;
    }

    public ArrayList<String> getDepartments() {
        String sql = """
                    SELECT DISTINCT department 
                    FROM [dbo].[Doctor] d
                    JOIN [dbo].[AccountStaff] a ON a.account_staff_id = d.account_staff_id
                    WHERE a.role = 'Doctor' AND a.status = 'Enable'
                    ORDER BY department;
                """;
        ArrayList<String> departments = new ArrayList<>();
        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                departments.add(rs.getNString("department"));
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error fetching departments: " + e.getMessage());
            throw new RuntimeException("Failed to fetch departments", e);
        }
        return departments;
    }


    public ArrayList<Doctor> getAllDoctors() {
        ArrayList<Doctor> l = new ArrayList<>();
        String xSql = """
                Select d.doctor_id, d.full_name, d.phone, d.eduLevel, d.department, a.email, a.[img] 
                from [dbo].[Doctor] d
                join [dbo].[AccountStaff] a on a.account_staff_id = d.account_staff_id
                where a.role = 'Doctor' and a.status = 'Enable'
                order by d.doctor_id;
                """;

        try {
            PreparedStatement ps = ad.getConnection().prepareStatement(xSql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                l.add(new Doctor(rs.getInt("doctor_id"), rs.getNString("full_name"),
                        rs.getNString("phone"), rs.getNString("eduLevel"),
                        rs.getNString("department"), rs.getNString("email"),
                        rs.getNString("img")));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return l;
    }

    public ArrayList<Doctor> searchDoctorsByNameAndDepartment(String name, String department) {
        // Input validation


        ArrayList<Doctor> doctors = new ArrayList<>();
        String sql = """
                    SELECT 
                        d.doctor_id, 
                        d.full_name, 
                        d.phone, 
                        d.eduLevel, 
                        d.department, 
                        a.email, 
                        a.[img]
                    FROM [dbo].[Doctor] d
                    JOIN [dbo].[AccountStaff] a ON a.account_staff_id = d.account_staff_id
                    WHERE 
                        a.role = 'Doctor' 
                        AND a.status = 'Enable' 
                        AND (? IS NULL OR d.full_name COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                        AND (? IS NULL OR d.department COLLATE SQL_Latin1_General_CP1_CI_AI LIKE ?)
                    ORDER BY d.doctor_id 
                
                """;

        try {
            PreparedStatement stmt = ad.getConnection().prepareStatement(sql);
            // Prepare parameters
            String namePattern = (name == null || name.trim().isEmpty()) ? null : "%" + name.trim() + "%";
            String deptPattern = (department == null || department.trim().isEmpty()) ? null : "%" + department.trim() + "%";

            // Set parameters for name
            stmt.setNString(1, namePattern);
            stmt.setNString(2, namePattern);

            // Set parameters for department
            stmt.setNString(3, deptPattern);
            stmt.setNString(4, deptPattern);

            // Set pagination parameters

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    doctors.add(new Doctor(
                            rs.getInt("doctor_id"),
                            rs.getNString("full_name"),
                            rs.getNString("phone"),
                            rs.getNString("eduLevel"),
                            rs.getNString("department"),
                            rs.getNString("email"),
                            rs.getNString("img")
                    ));
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error searching doctors: " + e.getMessage());
            throw new RuntimeException("Failed to search doctors", e);
        }

        return doctors;
    }

    /**
     * Get doctor_id by account_staff_id
     * @param accountStaffId the account staff ID
     * @return doctor_id if found, -1 if not found
     */
    public int getDoctorByAccountStaffId(int accountStaffId) {
        String sql = "SELECT doctor_id FROM Doctor WHERE account_staff_id = ?";

        try (PreparedStatement stmt = ad.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, accountStaffId);
            Logger.getLogger(getClass().getName()).info("Executing SQL: " + sql + " with account_staff_id=" + accountStaffId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int doctorId = rs.getInt("doctor_id");
                Logger.getLogger(getClass().getName()).info("Found doctor_id=" + doctorId + " for account_staff_id=" + accountStaffId);
                return doctorId;
            } else {
                Logger.getLogger(getClass().getName()).warning("No doctor found for account_staff_id=" + accountStaffId);
                return -1;
            }
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error getting doctor by account_staff_id: " + e.getMessage());
            throw new RuntimeException("Failed to get doctor by account_staff_id", e);
        }
    }

    /**
     * Update waitlist status
     * @param waitlistId the waitlist ID
     * @param newStatus the new status
     * @return true if update successful, false otherwise
     */
    public boolean updateWaitlistStatus(int waitlistId, String newStatus) {
        String sql = "UPDATE Waitlist SET status = ? WHERE waitlist_id = ?";

        try (PreparedStatement stmt = ad.getConnection().prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, waitlistId);

            int rowsAffected = stmt.executeUpdate();
            Logger.getLogger(getClass().getName()).info("Updated waitlist " + waitlistId + " status to " + newStatus + ". Rows affected: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error updating waitlist status: " + e.getMessage());
            throw new RuntimeException("Failed to update waitlist status", e);
        }
    }

    /**
     * Get waitlist entries for a specific doctor
     * @param doctorId the doctor ID
     * @param page page number (1-based)
     * @param pageSize number of entries per page
     * @param sortBy field to sort by
     * @param sortOrder ASC or DESC
     * @return list of waitlist entries for the doctor
     */
    public ArrayList<WaitlistDTO> getWaitlistEntriesForDoctor(int doctorId, int page, int pageSize, String sortBy, String sortOrder) {
        ArrayList<WaitlistDTO> waitlistEntries = new ArrayList<>();

        String sql = """
            SELECT
                w.waitlist_id,
                COALESCE(p.full_name, 'Unknown Patient') AS patient_name,
                COALESCE(d.full_name, 'Unknown Doctor') AS doctor_name,
                r.room_name,
                w.registered_at,
                w.estimated_time,
                w.visittype,
                w.status
            FROM
                Waitlist w
                INNER JOIN Patient p ON w.patient_id = p.patient_id
                INNER JOIN Doctor d ON w.doctor_id = d.doctor_id
                LEFT JOIN Room r ON w.room_id = r.room_id
            WHERE
                w.doctor_id = ?
                AND w.status IN ('Waiting', 'InProgress', 'Skipped', 'Completed')
            """;

        String sortColumn;
        switch (sortBy != null ? sortBy.toLowerCase() : "waitlist_id") {
            case "registered_at":
                sortColumn = "w.registered_at";
                break;
            case "estimated_time":
                sortColumn = "w.estimated_time";
                break;
            case "status":
                sortColumn = "w.status";
                break;
            default:
                sortColumn = "w.waitlist_id";
        }

        String sortDirection = "DESC".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
        sql += " ORDER BY " + sortColumn + " " + sortDirection +
                " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

        try (PreparedStatement stmt = ad.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            stmt.setInt(2, (page - 1) * pageSize);
            stmt.setInt(3, pageSize);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                WaitlistDTO entry = new WaitlistDTO();
                entry.setWaitlistId(rs.getInt("waitlist_id"));
                entry.setPatientName(rs.getString("patient_name"));
                entry.setDoctorName(rs.getString("doctor_name"));
                entry.setRoomName(rs.getString("room_name"));
                Timestamp registeredAt = rs.getTimestamp("registered_at");
                entry.setRegisteredAt(registeredAt != null ? new Date(registeredAt.getTime()) : null);
                Timestamp estimatedTime = rs.getTimestamp("estimated_time");
                entry.setEstimatedTime(estimatedTime != null ? new Date(estimatedTime.getTime()) : null);
                entry.setVisitType(rs.getString("visittype"));
                entry.setStatus(rs.getString("status"));
                waitlistEntries.add(entry);
            }
            Logger.getLogger(getClass().getName()).info("Fetched " + waitlistEntries.size() + " waitlist entries for doctor " + doctorId);
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error getting waitlist entries for doctor: " + e.getMessage());
            throw new RuntimeException("Failed to get waitlist entries for doctor", e);
        }
        return waitlistEntries;
    }

    /**
     * Count waitlist entries for a specific doctor
     * @param doctorId the doctor ID
     * @return total count of waitlist entries for the doctor
     */
    public int countWaitlistEntriesForDoctor(int doctorId) {
        String sql = """
            SELECT COUNT(*) AS total
            FROM
                Waitlist w
                INNER JOIN Patient p ON w.patient_id = p.patient_id
                INNER JOIN Doctor d ON w.doctor_id = d.doctor_id
                LEFT JOIN Room r ON w.room_id = r.room_id
            WHERE
                w.doctor_id = ?
                AND w.status IN ('Waiting', 'InProgress', 'Skipped', 'Completed')
            """;

        try (PreparedStatement stmt = ad.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, doctorId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).severe("Error counting waitlist entries for doctor: " + e.getMessage());
            throw new RuntimeException("Failed to count waitlist entries for doctor", e);
        }
    }

    public static void main(String[] args) {
        DoctorDAO dao = new DoctorDAO();
        ArrayList<Doctor> l = dao.searchDoctorsByNameAndDepartment("", "", 1, 20);
        System.out.println(l.size());
    }


}

