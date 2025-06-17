package controller;

import com.google.gson.Gson;
import dal.AppointmentDAO;
import model.Appointment;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@WebServlet("/api/Add_appointments/*")
public class AppointmentServlet extends HttpServlet {
    private final AppointmentDAO dao = new AppointmentDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
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
            // Get form parameters
            String department = request.getParameter("department");
            String doctorIdStr = request.getParameter("doctorId");
            String date = request.getParameter("date");
            String time = request.getParameter("time");
            String note = request.getParameter("note");

            // Validate required fields
            if (doctorIdStr == null || doctorIdStr.isEmpty() || date == null || date.isEmpty() || time == null || time.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Missing required fields: doctorId, date, or time\"}");
                return;
            }

            // Parse doctorId
            int doctorId;
            try {
                doctorId = Integer.parseInt(doctorIdStr);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Invalid doctorId format\"}");
                return;
            }

            // Parse date and time into appointmentDatetime
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date appointmentDatetime;
            try {
                appointmentDatetime = sdf.parse(date + " " + time);
            } catch (ParseException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Invalid date or time format\"}");
                return;
            }

            // Determine shift based on time
            String shift = determineShift(time);
            if (shift == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Invalid time for shift determination\"}");
                return;
            }

            // Validate appointment date (must be in future)
            Date currentDate = new Date();
            if (appointmentDatetime.before(currentDate)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Appointment date must be in the future\"}");
                return;
            }

            // Create Appointment object
            Appointment appointment = new Appointment();
            appointment.setDoctorId(doctorId);
            appointment.setAppointmentDatetime(appointmentDatetime);
            appointment.setShift(shift);
            appointment.setNote(note != null && !note.trim().isEmpty() ? note.trim() : null); // Set note, allow null if empty
            // Set patientId (assuming a default or session-based value)
            appointment.setPatientId(getPatientId(request)); // Implement this based on your authentication
            // Set receptionistId to -1 (indicating NULL in DAO)
            appointment.setReceptionistId(-1);

            // Call DAO to create appointment
            Appointment createdAppointment = dao.createAppointment(appointment);
            if (createdAppointment == null) {
                throw new SQLException("Appointment creation failed, no result returned.");
            }

            // Prepare response
            response.setStatus(HttpServletResponse.SC_CREATED);
            out.println(gson.toJson(createdAppointment));
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Unexpected error: " + e.getMessage() + "\"}");
        }
        out.flush();
    }

    private String determineShift(String time) {
        try {
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            if (hour >= 6 && hour < 12) {
                return "Morning";
            } else if (hour >= 12 && hour < 16) {
                return "Afternoon";
            } else if (hour >= 16 && hour < 24) {
                return "Evening";
            }
        } catch (Exception e) {
            // Handle parsing errors
        }
        return null;
    }

    private int getPatientId(HttpServletRequest request) {
        // Placeholder: Implement logic to get patientId (e.g., from session or authentication)
        // For now, return a default value or throw an error if not authenticated
        return 1; // Replace with actual logic (e.g., session.getAttribute("patientId"))
    }
}