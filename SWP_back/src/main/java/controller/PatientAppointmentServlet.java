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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@WebServlet("/api/patientAppointment/*")
public class PatientAppointmentServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PatientAppointmentServlet.class.getName());
    private final PatientAppointmentDAO appointmentDAO = new PatientAppointmentDAO();
    private final Gson gson = new Gson();

    // Supported date-time formats
    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy-MM"),
            DateTimeFormatter.ofPattern("yyyy"),
            DateTimeFormatter.ofPattern("M/d/yyyy, h:mm:ss a"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy, HH:mm:ss a"),
            DateTimeFormatter.ofPattern("M/d/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
    );

    // Output formatter for SQL compatibility (yyyy-MM-dd HH:mm:ss)
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Input validation pattern (allows digits, hyphens, slashes, colons, spaces, commas, AM/PM)
    private static final Pattern VALID_DATE_PATTERN = Pattern.compile("^[0-9\\-/:, aApPmM]{0,25}$");

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCORSHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();

        if (pathInfo == null || pathInfo.equals("/")) {
            try {
                // Parse query parameters
                String accountPatientIdParam = request.getParameter("accountPatientId");
                if (accountPatientIdParam == null || accountPatientIdParam.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"accountPatientId is required\"}");
                    return;
                }
                int accountPatientId = Integer.parseInt(accountPatientIdParam.trim());
                String fullName = request.getParameter("fullName");
                String appointmentDateTime = request.getParameter("appointmentDateTime");
                String status = request.getParameter("status");
                int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
                int pageSize = Integer.parseInt(request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "8");

                // Validate pagination parameters
                if (page < 0 || pageSize <= 0 || pageSize > 100) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid page or pageSize\"}");
                    return;
                }

                // Validate and sanitize appointmentDateTime
                String validatedDateTime = validateAndParseDateTime(appointmentDateTime);
                if (appointmentDateTime != null && validatedDateTime == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid appointmentDateTime format. Use formats like 'yyyy-MM-dd HH:mm:ss', 'MM/dd/yyyy, HH:mm:ss a', 'yyyy-MM-dd', or 'yyyy-MM'\"}");
                    return;
                }

                // Log request for debugging
                LOGGER.info("Fetching appointments for accountPatientId=" + accountPatientId +
                        ", fullName=" + fullName + ", appointmentDateTime=" + validatedDateTime +
                        ", status=" + status + ", page=" + page + ", pageSize=" + pageSize);

                // Get appointments and total count
                ArrayList<AppointmentDTO> appointments = appointmentDAO.getAppointmentsByAccountPatientId(
                        accountPatientId, fullName, validatedDateTime, status, page, pageSize);

                int totalAppointment = appointmentDAO.countAppointmentsByAccountPatientId(
                        accountPatientId, fullName, validatedDateTime, status);
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
        }
        out.flush();
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private String validateAndParseDateTime(String dateTime) {
        if (dateTime == null || dateTime.trim().isEmpty()) {
            return null;
        }

        // Sanitize input
        String cleanedDateTime = dateTime.trim();
        if (cleanedDateTime.length() > 25 || !VALID_DATE_PATTERN.matcher(cleanedDateTime).matches()) {
            LOGGER.warning("Invalid date-time input (length or characters): " + cleanedDateTime);
            return null;
        }

        // Try parsing with supported formats
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                String pattern = formatter.toString();
                LocalDateTime localDateTime;

                // Handle partial formats (yyyy, yyyy-MM, MM/dd/yyyy, etc.)
                if (pattern.equals("yyyy-MM") && !cleanedDateTime.contains(":")) {
                    localDateTime = LocalDateTime.parse(cleanedDateTime + "-01 00:00:00",
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    return cleanedDateTime; // Return partial for SQL LIKE
                } else if (pattern.equals("yyyy") && !cleanedDateTime.contains("-") && !cleanedDateTime.contains("/")) {
                    localDateTime = LocalDateTime.parse(cleanedDateTime + "-01-01 00:00:00",
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    return cleanedDateTime; // Return year for SQL LIKE
                } else if (pattern.contains("/yyyy") && !cleanedDateTime.contains(":")) {
                    // Handle MM/dd/yyyy without time
                    localDateTime = LocalDateTime.parse(cleanedDateTime + ", 00:00:00 AM",
                            DateTimeFormatter.ofPattern(pattern + ", HH:mm:ss a"));
                    return formatter.format(localDateTime).substring(0, 10).replace("/", "-"); // Return yyyy-MM-dd
                } else {
                    // Handle full date-time formats
                    localDateTime = LocalDateTime.parse(cleanedDateTime, formatter);
                }

                // Validate year range
                if (localDateTime.getYear() < 1900 || localDateTime.getYear() > 9999) {
                    LOGGER.warning("Date out of valid range: " + cleanedDateTime);
                    return null;
                }

                // Return formatted string for SQL LIKE
                return pattern.contains("h:mm:ss a") || pattern.contains("HH:mm")
                        ? OUTPUT_FORMATTER.format(localDateTime) // Full date-time
                        : formatter.format(localDateTime).replace("/", "-"); // Partial date

            } catch (DateTimeParseException e) {
                // Continue to try next formatter
            }
        }
        //test
        LOGGER.warning("Failed to parse date-time: " + cleanedDateTime);
        return null;
    }
}