package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.ReceptionistDAO;
import dto.ReceptionistCheckInDTO;
import dto.WaitlistDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/api/receptionist/dashboard/*")
public class ReceptionistDashboardServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ReceptionistDashboardServlet.class.getName());
    private final ReceptionistDAO receptionistDAO = new ReceptionistDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCORSHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            LOGGER.info("GET request from " + request.getRemoteAddr() + " for " + request.getRequestURI() +
                    "?" + (request.getQueryString() != null ? request.getQueryString() : ""));

            String pathInfo = request.getPathInfo();
            if (pathInfo == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ENDPOINT",
                        "Invalid endpoint");
                return;
            }

            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");

            LOGGER.info("Request params: startDate=" + startDate + ", endDate=" + endDate);

            if (startDate == null || startDate.trim().isEmpty() || endDate == null || endDate.trim().isEmpty()) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "MISSING_DATE",
                        "startDate and endDate are required");
                return;
            }

            String formattedStartDate = parseDate(startDate);
            String formattedEndDate = parseDate(endDate);
            LOGGER.info("Formatted dates: startDate=" + formattedStartDate + ", endDate=" + formattedEndDate);

            if (formattedStartDate == null || formattedEndDate == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_DATE",
                        "Invalid date format. Use yyyy-MM-dd");
                return;
            }

            JsonObject responseJson = new JsonObject();
            if (pathInfo.equals("/top3appointments")) {
                List<ReceptionistCheckInDTO> appointments = receptionistDAO.getTop3AppointmentsPerDay(formattedStartDate, formattedEndDate);
                LOGGER.info("DAO returned " + appointments.size() + " top 3 appointments per day");
                responseJson.add("appointments", gson.toJsonTree(appointments));
                responseJson.addProperty("totalAppointments", appointments.size());
                responseJson.addProperty("success", true);
                responseJson.addProperty("message", appointments.isEmpty() ? "No appointments found" : "Top 3 appointments per day fetched successfully");
            } else if (pathInfo.equals("/top3waitlist")) {
                List<WaitlistDTO> waitlistEntries = receptionistDAO.getTop3WaitlistEntriesPerDay(formattedStartDate, formattedEndDate);
                LOGGER.info("DAO returned " + waitlistEntries.size() + " top 3 waitlist entries per day");
                responseJson.add("waitlistEntries", gson.toJsonTree(waitlistEntries));
                responseJson.addProperty("totalWaitlistEntries", waitlistEntries.size());
                responseJson.addProperty("success", true);
                responseJson.addProperty("message", waitlistEntries.isEmpty() ? "No waitlist entries found" : "Top 3 waitlist entries per day fetched successfully");
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ENDPOINT",
                        "Endpoint must be /top3appointments or /top3waitlist");
                return;
            }

            out.print(gson.toJson(responseJson));
        } catch (Exception e) {
            LOGGER.severe("Error processing GET request: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                    "Failed to fetch data: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        } finally {
            out.flush();
        }
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private String parseDate(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDateTime.parse(input + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return input;
        } catch (DateTimeParseException e) {
            LOGGER.warning("Invalid date format: " + input + ", error: " + e.getMessage());
            return null;
        }
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