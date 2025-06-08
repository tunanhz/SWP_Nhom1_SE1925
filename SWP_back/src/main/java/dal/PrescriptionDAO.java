package dal;

import model.Prescription;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PrescriptionDAO {
    DBContext ad = new DBContext();

    public ArrayList<Prescription> getAllPrescription() {
        ArrayList<Prescription> l = new ArrayList<>();
        String xSql = "SELECT pr.prescription_id, pr.prescription_date, p.full_name AS patient_name, p.phone, d.full_name AS doctor_name\n" +
                "FROM Prescription pr\n" +
                "JOIN MedicineRecords mr ON pr.medicineRecord_id = mr.medicineRecord_id\n" +
                "JOIN Patient p ON mr.patient_id = p.patient_id\n" +
                "JOIN Doctor d ON pr.doctor_id = d.doctor_id\n" +
                "WHERE pr.status = 'Pending';";

        try {
            PreparedStatement ps = ad.getConnection().prepareStatement(xSql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                l.add(new  Prescription(rs.getInt(1), rs.getDate(2), rs.getNString(3),
                        rs.getNString(4), rs.getNString(5)));
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return (l);
    }


    public static void main(String[] args) {
        PrescriptionDAO dao = new PrescriptionDAO();
        ArrayList<Prescription> l = dao.getAllPrescription();
    }
}
