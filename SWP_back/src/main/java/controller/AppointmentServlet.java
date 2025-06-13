package controller;
import com.google.gson.JsonObject;
import dal.AppointmentDAO;
import model.Appointment;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.sql.SQLException;

@WebServlet("/api/appointments/*")
public class AppointmentServlet extends HttpServlet {
    private final AppointmentDAO dao = new AppointmentDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(jakarta.servlet.http.HttpServletRequest req, jakarta.servlet.http.HttpServletResponse resp) throws jakarta.servlet.ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(jakarta.servlet.http.HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Received POST request to /api/appointments");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        try {
            // Read JSON from request body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String jsonString = sb.toString();
            System.out.println("Received JSON: " + jsonString);

            // Parse JSON to Appointment object
            Appointment appointment = gson.fromJson(jsonString, Appointment.class);

            // Validate required fields
            if (appointment.getPatientId() <= 0 || appointment.getDoctorId() <= 0 || appointment.getAppointmentDatetime() == null ||
                    appointment.getShift() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Missing required fields: patientId, doctorId, appointmentDateTime, or shift\"}");
                return;
            }

            // Validate shift
            if (!isValidShift(appointment.getShift())) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Invalid shift. Must be Morning, Afternoon, or Evening\"}");
                return;
            }

            // Validate appointment date (must be in future)
            Date currentDate = new Date();
            if (appointment.getAppointmentDatetime().before(currentDate)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\": \"Appointment date must be in the future\"}");
                return;
            }

            // Handle receptionistId (set to -1 if null or 0 to match DAO logic)
            if (appointment.getReceptionistId() == 0 || appointment.getReceptionistId() == -1) {
                appointment.setReceptionistId(-1); // -1 indicates NULL in DAO
            }

            // Call DAO to create appointment
            Appointment createdAppointment = dao.createAppointment(appointment);
            if (createdAppointment == null) {
                throw new SQLException("Appointment creation failed, no result returned.");
            }

            // Prepare response
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.write(gson.toJson(createdAppointment));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\": \"Unexpected error: " + e.getMessage() + "\"}");
        }
        out.flush();
    }

    private boolean isValidShift(String shift) {
        return shift != null && (shift.equals("Morning") || shift.equals("Afternoon") || shift.equals("Evening"));
    }
}