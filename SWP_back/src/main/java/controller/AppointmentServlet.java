package controller;

import com.google.gson.Gson;
import dal.AppointmentDAO;
import model.Appointment;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.AppointmentRequest;

import java.io.BufferedReader;
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
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Received POST request to /api/Add_appointments");
        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String jsonData = sb.toString();

            AppointmentRequest appointmentRequest = gson.fromJson(jsonData, AppointmentRequest.class);

            if (appointmentRequest.getDoctorId() == null || appointmentRequest.getDate() == null || appointmentRequest.getTime() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Missing required fields: doctorId, date, or time\"}");
                return;
            }

            if (!appointmentRequest.getDoctorId().matches("\\d+")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Invalid doctorId format\"}");
                return;
            }

            int doctorId = Integer.parseInt(appointmentRequest.getDoctorId());
            int patientId =  appointmentRequest.getPatientId();
            String date = appointmentRequest.getDate();
            String time = appointmentRequest.getTime();
            String note = appointmentRequest.getNote();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date appointmentDatetime;
            try {
                appointmentDatetime = sdf.parse(date + " " + time);
            } catch (ParseException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Invalid date or time format\"}");
                return;
            }

            Date currentDate = new Date();
            if (appointmentDatetime.before(currentDate)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Appointment date must be in the future\"}");
                return;
            }

            Appointment appointment = new Appointment();
            appointment.setDoctorId(doctorId);
            appointment.setPatientId(patientId);
            appointment.setAppointmentDatetime(appointmentDatetime);
            appointment.setNote(note != null && !note.trim().isEmpty() ? note.trim() : null);

            Appointment createdAppointment = dao.createAppointment(appointment);
            if (createdAppointment != null) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.println(gson.toJson(createdAppointment));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"error\": \"Failed to create appointment\"}");
            }

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Database error: " + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
//        catch (Exception e) {
//            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
//            out.println("{\"error\": \"Unexpected error: " + e.getMessage().replace("\"", "\\\"") + "\"}");
//        }
        finally {
            out.flush();
        }
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept");
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

//    private int getPatientId(HttpServletRequest request) {
//        HttpSession session = request.getSession(false);
//        if (session != null && session.getAttribute("patientId") != null) {
//            return (Integer) session.getAttribute("patientId");
//        }
//        throw new IllegalStateException("No authenticated patient found in session");
//    }
}
