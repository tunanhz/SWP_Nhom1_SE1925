package dal;

public class AccountDAO {
    private final AccountPatientDAO patientDAO = new AccountPatientDAO();
    private final AccountStaffDAO staffDAO = new AccountStaffDAO();
    private final AccountPharmacistDAO pharmacistDAO = new AccountPharmacistDAO();
    private static final String PATIENT = "patient";
    private static final String STAFF = "staff";
    private static final String PHARMACIST = "pharmacist";

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
}
