package dal;

import model.AccountPatient;
import model.AccountPharmacist;
import model.AccountStaff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AccountDAO {
    private final AccountPatientDAO patientDAO = new AccountPatientDAO();
    private final AccountStaffDAO staffDAO = new AccountStaffDAO();
    private final AccountPharmacistDAO pharmacistDAO = new AccountPharmacistDAO();
    private static final String PATIENT = "patient";
    private static final String STAFF = "staff";
    private static final String PHARMACIST = "pharmacist";
    private final DBContext dbContext = new DBContext();

    public String isEmailExists(String email) {
        int count = 0;
        String accountType = null;

        if (patientDAO.isEmailExists(email)) {
            accountType = PATIENT;
            count++;
        }
        if (staffDAO.isEmailExists(email)) {
            accountType = STAFF;
            count++;
        }
        if (pharmacistDAO.isEmailExists(email)) {
            accountType = PHARMACIST;
            count++;
        }

        if (count > 1) {
            throw new RuntimeException("Email exists in multiple accounts");
        }
        return accountType;
    }

    public void updatePassword(String email, String password, String accountType) {
        switch (accountType) {
            case PATIENT:
                patientDAO.updatePassword(email, password);
                break;
            case STAFF:
                staffDAO.updatePassword(email, password);
                break;
            case PHARMACIST:
                pharmacistDAO.updatePassword(email, password);
                break;
            default:
                throw new RuntimeException("Invalid account type");
        }
    }

    public ArrayList<AccountPatient> getAllPatientAccounts() {
        ArrayList<AccountPatient> patients = new ArrayList<>();
        String sql = """
                SELECT account_patient_id, username, email, img, status
                FROM AccountPatient
                WHERE status = 'Enable';
                """;

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AccountPatient patient = new AccountPatient();
                patient.setAccountPatientId(rs.getInt("account_patient_id"));
                patient.setUsername(rs.getString("username"));
                patient.setEmail(rs.getString("email"));
                patient.setImg(rs.getString("img"));
                patient.setStatus(rs.getString("status").equals("Enable"));
                patients.add(patient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch patient accounts");
        }
        return patients;
    }

    public ArrayList<AccountStaff> getAllStaffAccounts() {
        ArrayList<AccountStaff> staff = new ArrayList<>();
        String sql = """
                SELECT account_staff_id, username, role, email, img, status
                FROM AccountStaff
                WHERE status = 'Enable';
                """;

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AccountStaff account = new AccountStaff();
                account.setAccountStaffId(rs.getInt("account_staff_id"));
                account.setUserName(rs.getString("username"));
                account.setRole(rs.getString("role"));
                account.setEmail(rs.getString("email"));
                account.setImg(rs.getString("img"));
                account.setStatus(rs.getString("status").equals("Enable"));
                staff.add(account);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch staff accounts");
        }
        return staff;
    }
    public ArrayList<AccountPharmacist> getAllPharmacistAccounts() {
        ArrayList<AccountPharmacist> pharmacists = new ArrayList<>();
        String sql = """
                SELECT account_pharmacist_id, username, email, status, img
                FROM AccountPharmacist
                WHERE status = 'Enable';
                """;

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AccountPharmacist pharmacist = new AccountPharmacist();
                pharmacist.setAccountPharmacistId(rs.getInt("account_pharmacist_id"));
                pharmacist.setUsername(rs.getString("username"));
                pharmacist.setEmail(rs.getString("email"));
                pharmacist.setStatus(rs.getString("status").equals("Enable"));
                pharmacist.setImg(rs.getString("img"));
                pharmacists.add(pharmacist);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch pharmacist accounts");
        }
        return pharmacists;
    }

    public AccountPatient getPatientAccountById(int id) {
        String sql = """
                SELECT account_patient_id, username, email, img, status
                FROM AccountPatient
                WHERE account_patient_id = ? AND status = 'Enable';
                """;

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AccountPatient patient = new AccountPatient();
                    patient.setAccountPatientId(rs.getInt("account_patient_id"));
                    patient.setUsername(rs.getString("username"));
                    patient.setEmail(rs.getString("email"));
                    patient.setImg(rs.getString("img"));
                    patient.setStatus(rs.getString("status").equals("Enable"));
                    return patient;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch patient account by ID");
        }
        return null;
    }

    public AccountStaff getStaffAccountById(int id) {
        String sql = """
                SELECT account_staff_id, username, role, email, img, status
                FROM AccountStaff
                WHERE account_staff_id = ? AND status = 'Enable';
                """;

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AccountStaff staff = new AccountStaff();
                    staff.setAccountStaffId(rs.getInt("account_staff_id"));
                    staff.setUserName(rs.getString("username"));
                    staff.setRole(rs.getString("role"));
                    staff.setEmail(rs.getString("email"));
                    staff.setImg(rs.getString("img"));
                    staff.setStatus(rs.getString("status").equals("Enable"));
                    return staff;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch staff account by ID");
        }
        return null;
    }


    public AccountPharmacist getPharmacistAccountById(int id) {
        String sql = """
                SELECT account_pharmacist_id, username, email, status, img
                FROM AccountPharmacist
                WHERE account_pharmacist_id = ? AND status = 'Enable';
                """;

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AccountPharmacist pharmacist = new AccountPharmacist();
                    pharmacist.setAccountPharmacistId(rs.getInt("account_pharmacist_id"));
                    pharmacist.setUsername(rs.getString("username"));
                    pharmacist.setEmail(rs.getString("email"));
                    pharmacist.setStatus(rs.getString("status").equals("Enable"));
                    pharmacist.setImg(rs.getString("img"));
                    return pharmacist;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch pharmacist account by ID");
        }
        return null;
    }

    public void addPatientAccount(AccountPatient patient, String fullName, String dob, String gender, String phone, String address) {
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);

            // Thêm vào AccountPatient
            String sqlAccount = """
                    INSERT INTO AccountPatient (username, password, email, img, status)
                    VALUES (?, ?, ?, ?, ?);
                    """;
            try (PreparedStatement psAccount = conn.prepareStatement(sqlAccount, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psAccount.setString(1, patient.getUsername());
                psAccount.setString(2, patient.getPassword());
                psAccount.setString(3, patient.getEmail());
                psAccount.setString(4, patient.getImg() != null ? patient.getImg() : "");
                psAccount.setString(5, patient.isStatus() ? "Enable" : "Disable");
                psAccount.executeUpdate();

                try (ResultSet rs = psAccount.getGeneratedKeys()) {
                    if (rs.next()) {
                        patient.setAccountPatientId(rs.getInt(1));
                    }
                }
            }

            // Thêm vào Patient
            String sqlPatient = """
                    INSERT INTO Patient (full_name, dob, gender, phone, address, status)
                    VALUES (?, ?, ?, ?, ?, ?);
                    """;
            try (PreparedStatement psPatient = conn.prepareStatement(sqlPatient, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psPatient.setString(1, fullName);
                psPatient.setString(2, dob);
                psPatient.setString(3, gender);
                psPatient.setString(4, phone);
                psPatient.setString(5, address);
                psPatient.setString(6, patient.isStatus() ? "Enable" : "Disable");
                psPatient.executeUpdate();

                try (ResultSet rs = psPatient.getGeneratedKeys()) {
                    if (rs.next()) {
                        int patientId = rs.getInt(1);
                        // Thêm vào Patient_AccountPatient
                        String sqlLink = """
                                INSERT INTO Patient_AccountPatient (patient_id, account_patient_id)
                                VALUES (?, ?);
                                """;
                        try (PreparedStatement psLink = conn.prepareStatement(sqlLink)) {
                            psLink.setInt(1, patientId);
                            psLink.setInt(2, patient.getAccountPatientId());
                            psLink.executeUpdate();
                        }
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new RuntimeException("Failed to add patient account: " + e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
//    public void addStaffAccount(AccountStaff staff) {
//        String sql = """
//                INSERT INTO AccountStaff (username, password, role, email, img, status)
//                VALUES (?, ?, ?, ?, ?, ?);
//                """;
//
//        try (Connection conn = dbContext.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
//
//            ps.setString(1, staff.getUserName());
//            ps.setString(2, staff.getPassWord() != null ? staff.getPassWord() : "defaultPassword");
//            ps.setString(3, staff.getRole());
//            ps.setString(4, staff.getEmail());
//            ps.setString(5, staff.getImg());
//            ps.setString(6, staff.isStatus() ? "Enable" : "Disable");
//            ps.executeUpdate();
//
//            // Set generated ID
//            try (ResultSet rs = ps.getGeneratedKeys()) {
//                if (rs.next()) {
//                    staff.setAccountStaffId(rs.getInt(1));
//                }
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            throw new RuntimeException("Failed to add staff account");
//        }
//    }

    public void addStaffAccount(AccountStaff staff, String fullName, String phone, String department, String eduLevel) {
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);

            // Thêm vào AccountStaff
            String sqlAccount = """
                    INSERT INTO AccountStaff (username, password, role, email, img, status)
                    VALUES (?, ?, ?, ?, ?, ?);
                    """;
            try (PreparedStatement psAccount = conn.prepareStatement(sqlAccount, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psAccount.setString(1, staff.getUserName());
                psAccount.setString(2, staff.getPassWord());
                psAccount.setString(3, staff.getRole());
                psAccount.setString(4, staff.getEmail());
                psAccount.setString(5, staff.getImg());
                psAccount.setString(6, staff.isStatus() ? "Enable" : "Disable");
                psAccount.executeUpdate();

                try (ResultSet rs = psAccount.getGeneratedKeys()) {
                    if (rs.next()) {
                        staff.setAccountStaffId(rs.getInt(1));
                    }
                }
            }

            // Thêm vào bảng tương ứng dựa trên role
            String sqlRole;
            switch (staff.getRole()) {
                case "Doctor":
                    sqlRole = """
                            INSERT INTO Doctor (full_name, department, phone, eduLevel, account_staff_id)
                            VALUES (?, ?, ?, ?, ?);
                            """;
                    break;
                case "Nurse":
                    sqlRole = """
                            INSERT INTO Nurse (full_name, department, phone, eduLevel, account_staff_id)
                            VALUES (?, ?, ?, ?, ?);
                            """;
                    break;
                case "Receptionist":
                    sqlRole = """
                            INSERT INTO Receptionist (full_name, phone, account_staff_id)
                            VALUES (?, ?, ?);
                            """;
                    break;
                case "AdminBusiness":
                    sqlRole = """
                            INSERT INTO AdminBusiness (full_name, department, phone, account_staff_id)
                            VALUES (?, ?, ?, ?);
                            """;
                    break;
                case "AdminSys":
                    sqlRole = """
                            INSERT INTO AdminSystem (full_name, department, phone, account_staff_id)
                            VALUES (?, ?, ?, ?);
                            """;
                    break;
                default:
                    throw new RuntimeException("Invalid staff role");
            }

            try (PreparedStatement psRole = conn.prepareStatement(sqlRole)) {
                if (staff.getRole().equals("Receptionist")) {
                    psRole.setString(1, fullName);
                    psRole.setString(2, phone);
                    psRole.setInt(3, staff.getAccountStaffId());
                } else {
                    psRole.setString(1, fullName);
                    psRole.setString(2, department);
                    psRole.setString(3, phone);
                    if (!staff.getRole().equals("AdminBusiness") && !staff.getRole().equals("AdminSys")) {
                        psRole.setString(4, eduLevel);
                        psRole.setInt(5, staff.getAccountStaffId());
                    } else {
                        psRole.setInt(4, staff.getAccountStaffId());
                    }
                }
                psRole.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new RuntimeException("Failed to add staff account");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    public void addPharmacistAccount(AccountPharmacist pharmacist) {
//        String sql = """
//                INSERT INTO AccountPharmacist (username, password, email, status, img)
//                VALUES (?, ?, ?, ?, ?);
//                """;
//
//        try (Connection conn = dbContext.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
//
//            ps.setString(1, pharmacist.getUsername());
//            ps.setString(2, pharmacist.getPassword() != null ? pharmacist.getPassword() : "defaultPassword");
//            ps.setString(3, pharmacist.getEmail());
//            ps.setString(4, pharmacist.isStatus() ? "Enable" : "Disable");
//            ps.setString(5, pharmacist.getImg());
//            ps.executeUpdate();
//
//            // Set generated ID
//            try (ResultSet rs = ps.getGeneratedKeys()) {
//                if (rs.next()) {
//                    pharmacist.setAccountPharmacistId(rs.getInt(1));
//                }
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//            throw new RuntimeException("Failed to add pharmacist account");
//        }
//    }

    public void addPharmacistAccount(AccountPharmacist pharmacist, String fullName, String phone, String eduLevel) {
        Connection conn = null;
        try {
            conn = dbContext.getConnection();
            conn.setAutoCommit(false);

            // Thêm vào AccountPharmacist
            String sqlAccount = """
                    INSERT INTO AccountPharmacist (username, password, email, status, img)
                    VALUES (?, ?, ?, ?, ?);
                    """;
            try (PreparedStatement psAccount = conn.prepareStatement(sqlAccount, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psAccount.setString(1, pharmacist.getUsername());
                psAccount.setString(2, pharmacist.getPassword());
                psAccount.setString(3, pharmacist.getEmail());
                psAccount.setString(4, pharmacist.isStatus() ? "Enable" : "Disable");
                psAccount.setString(5, pharmacist.getImg());
                psAccount.executeUpdate();

                try (ResultSet rs = psAccount.getGeneratedKeys()) {
                    if (rs.next()) {
                        pharmacist.setAccountPharmacistId(rs.getInt(1));
                    }
                }
            }

            // Thêm vào Pharmacist
            String sqlPharmacist = """
                    INSERT INTO Pharmacist (full_name, phone, eduLevel, account_pharmacist_id)
                    VALUES (?, ?, ?, ?);
                    """;
            try (PreparedStatement psPharmacist = conn.prepareStatement(sqlPharmacist)) {
                psPharmacist.setString(1, fullName);
                psPharmacist.setString(2, phone);
                psPharmacist.setString(3, eduLevel);
                psPharmacist.setInt(4, pharmacist.getAccountPharmacistId());
                psPharmacist.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            throw new RuntimeException("Failed to add pharmacist account");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean updatePatientAccount(int id, AccountPatient patient) {
        String sql = """
                UPDATE AccountPatient
                SET username = ?, email = ?, img = ?, status = ?
                WHERE account_patient_id = ? AND status = 'Enable';
                """;

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, patient.getUsername());
            ps.setString(2, patient.getEmail());
            ps.setString(3, patient.getImg());
            ps.setString(4, patient.isStatus() ? "Enable" : "Disable");
            ps.setInt(5, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update patient account");
        }
    }

    public boolean updateStaffAccount(int id, AccountStaff staff) {
        String sql = """
                UPDATE AccountStaff
                SET username = ?, email = ?, img = ?, role = ?, status = ?
                WHERE account_staff_id = ? AND status = 'Enable';
                """;

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, staff.getUserName());
            ps.setString(2, staff.getEmail());
            ps.setString(3, staff.getImg());
            ps.setString(4, staff.getRole());
            ps.setString(5, staff.isStatus() ? "Enable" : "Disable");
            ps.setInt(6, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update staff account");
        }
    }

    public boolean updatePharmacistAccount(int id, AccountPharmacist pharmacist) {
        String sql = """
                UPDATE AccountPharmacist
                SET username = ?, email = ?, img = ?, status = ?
                WHERE account_pharmacist_id = ? AND status = 'Enable';
                """;

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pharmacist.getUsername());
            ps.setString(2, pharmacist.getEmail());
            ps.setString(3, pharmacist.getImg());
            ps.setString(4, pharmacist.isStatus() ? "Enable" : "Disable");
            ps.setInt(5, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update pharmacist account");
        }
    }

    public boolean disablePatientAccount(int id) {
        String sql = """
                UPDATE AccountPatient
                SET status = 'Disable'
                WHERE account_patient_id = ? AND status = 'Enable';
                """;

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to disable patient account");
        }
    }

    public boolean disableStaffAccount(int id) {
        String sql = """
                UPDATE AccountStaff
                SET status = 'Disable'
                WHERE account_staff_id = ? AND status = 'Enable';
                """;

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to disable staff account");
        }
    }

    public boolean disablePharmacistAccount(int id) {
        String sql = """
                UPDATE AccountPharmacist
                SET status = 'Disable'
                WHERE account_pharmacist_id = ? AND status = 'Enable';
                """;

        try (Connection conn = dbContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to disable pharmacist account");
        }
    }

}
