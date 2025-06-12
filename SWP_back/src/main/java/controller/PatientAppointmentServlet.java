
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.PatientAppointmentDAO;
import model.AppointmentDTO;
import model.AppointmentPatientDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.logging.Logger;

@WebServlet("/api/patientAppointment/*")
public class PatientAppointmentServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PatientAppointmentServlet.class.getName());
    private final PatientAppointmentDAO appointmentDAO = new PatientAppointmentDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();

        if (pathInfo == null || pathInfo.equals("/")) {
            try {
                // Parse query parameters
                String accountPatientIdParam = request.getParameter("accountPatientId");
                if (accountPatientIdParam == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"accountPatientId is required\"}");
                    return;
                }
                int accountPatientId = Integer.parseInt(accountPatientIdParam);
                String fullName = request.getParameter("fullName");
                Timestamp appointmentDateTime = parseTimestamp(request.getParameter("appointmentDateTime"));
                String status = request.getParameter("status");
                int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
                int pageSize = Integer.parseInt(request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "8");

                // Validate pagination parameters
                if (page < 0 || pageSize <= 0 || pageSize > 100) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid offset or pageSize\"}");
                    return;
                }

                // Log request for debugging
                LOGGER.info("Fetching appointments for accountPatientId=" + accountPatientId +
                        ", fullName=" + fullName + ", status=" + status +
                        ", offset=" + page + ", pageSize=" + pageSize);

                // Get appointments and total count
                ArrayList<AppointmentDTO> appointments = appointmentDAO.getAppointmentsByAccountPatientId(
                        accountPatientId, fullName, appointmentDateTime, status, page, pageSize);

                int totalAppointment = appointmentDAO.countAppointmentsByAccountPatientId(
                        accountPatientId, fullName, appointmentDateTime, status);
                int totalPages = (int) Math.ceil((double) totalAppointment / pageSize);

                ArrayList<AppointmentPatientDTO> appointmentPatientDTOS = appointmentDAO.getThreeAppointmentsUpcoming(accountPatientId);

                // Build response JSON
                JsonObject responseJson = new JsonObject();
                responseJson.add("appointments", gson.toJsonTree(appointments));
                responseJson.add("threeAppointmentsUpcoming", gson.toJsonTree(appointmentPatientDTOS));
                responseJson.addProperty("totalPages", totalPages);
                responseJson.addProperty("currentPage", page);
                responseJson.addProperty("pageSize", pageSize);
                out.print(gson.toJson(responseJson));
                out.flush();

            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid number format in parameters\"}");
                LOGGER.severe("Invalid number format: " + e.getMessage());
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Failed to fetch appointments\"}");
                LOGGER.severe("Error processing request: " + e.getMessage());
            }
        } else {
            String[] splits = pathInfo.split("/");
            if (splits.length == 2) {
                try {
                    int id = Integer.parseInt(splits[1]);
                    AppointmentDTO appointmentDTO = appointmentDAO.getAppointmentsByAppointmentId(id);
                    if (appointmentDTO != null) {
                        JsonObject responseJson = new JsonObject();
                        responseJson.add("appointment", gson.toJsonTree(appointmentDTO));
                        out.print(gson.toJson(responseJson));
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\": \"Appointment not found\"}");
                    }
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid Appointment ID format\"}");
                    LOGGER.severe("Invalid Appointment ID: " + e.getMessage());
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.print("{\"error\": \"Failed to fetch Appointment\"}");
                    LOGGER.severe("Error processing Appointment request: " + e.getMessage());
                }
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid path\"}");
            }
            out.flush();
        }
    }

    private Timestamp parseTimestamp(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(dateTime, formatter);
            return Timestamp.valueOf(localDateTime);
        } catch (DateTimeParseException e) {
            LOGGER.severe("Invalid date format: " + e.getMessage());
            return null;
        }
    }
}