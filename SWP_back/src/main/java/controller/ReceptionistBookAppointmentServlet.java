package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.ReceptionistDAO;
import model.Appointment;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

@WebServlet("/api/receptionist/book-appointment")
public class ReceptionistBookAppointmentServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ReceptionistBookAppointmentServlet.class.getName());
    private final ReceptionistDAO receptionistDAO = new ReceptionistDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Đọc JSON payload
            StringBuilder jsonBuffer = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuffer.append(line);
                }
            }
            String json = jsonBuffer.toString();
            LOGGER.info("Received JSON: " + json);

            // Parse JSON
            JsonObject appointmentJson = gson.fromJson(json, JsonObject.class);
            String patientIdStr = appointmentJson.has("patientId") ? appointmentJson.get("patientId").getAsString() : null;
            String doctorIdStr = appointmentJson.has("doctorId") ? appointmentJson.get("doctorId").getAsString() : null;
            String date = appointmentJson.has("date") ? appointmentJson.get("date").getAsString() : null;
            String time = appointmentJson.has("time") ? appointmentJson.get("time").getAsString() : null;
            String note = appointmentJson.has("note") ? appointmentJson.get("note").getAsString() : null;
            String accountStaffIdStr = appointmentJson.has("accountStaffId") ? appointmentJson.get("accountStaffId").getAsString() : null;

            // Validate input
            if (patientIdStr == null || doctorIdStr == null || date == null || time == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "MISSING_FIELDS",
                        "Missing required fields: patientId, doctorId, date, or time");
                return;
            }

            if (!patientIdStr.matches("\\d+") || !doctorIdStr.matches("\\d+")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_FORMAT",
                        "Invalid patientId or doctorId format");
                return;
            }

            int patientId = Integer.parseInt(patientIdStr);
            int doctorId = Integer.parseInt(doctorIdStr);
            int accountStaffId = Integer.parseInt(accountStaffIdStr);

            // Lấy receptionistId từ accountStaffId
            int receptionistId = receptionistDAO.getReceptionistByAccountStaffId(accountStaffId);
            if (receptionistId == -1) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_RECEPTIONIST", "Invalid receptionist ID");
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date appointmentDatetime;
            try {
                appointmentDatetime = sdf.parse(date + " " + time);
            } catch (ParseException e) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_DATETIME",
                        "Invalid date or time format");
                return;
            }

            Date currentDate = new Date();
            if (appointmentDatetime.before(currentDate)) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "PAST_DATETIME",
                        "Appointment date must be in the future");
                return;
            }

            // Tạo đối tượng Appointment
            Appointment appointment = new Appointment();
            appointment.setPatientId(patientId);
            appointment.setDoctorId(doctorId);
            appointment.setAppointmentDatetime(appointmentDatetime);
            appointment.setNote(note != null && !note.trim().isEmpty() ? note.trim() : null);

            // Gọi createAppointment từ ReceptionistDAO
            Appointment createdAppointment = receptionistDAO.createAppointment(appointment, receptionistId);
            if (createdAppointment != null) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "Appointment booked successfully");
                responseJson.add("appointment", gson.toJsonTree(createdAppointment));
                out.print(gson.toJson(responseJson));
                LOGGER.info("Appointment booked successfully for patientId: " + patientId);
            } else {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "BOOKING_FAILED",
                        "Failed to create appointment");
            }
        } catch (SQLException e) {
            LOGGER.severe("SQL Error: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SQL_ERROR",
                    "Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Unexpected error: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "UNEXPECTED_ERROR",
                    "Unexpected error: " + e.getMessage());
        } finally {
            out.flush();
        }
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private void sendError(HttpServletResponse response, int status, String errorCode, String message) throws IOException {
        response.setStatus(status);
        JsonObject errorJson = new JsonObject();
        errorJson.addProperty("success", false);
        errorJson.addProperty("errorCode", errorCode);
        errorJson.addProperty("message", message);
        response.getWriter().print(gson.toJson(errorJson));
        response.getWriter().flush();
    }
}