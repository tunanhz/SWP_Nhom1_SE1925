package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.ReceptionistDAO;
import dto.WaitlistDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.logging.Logger;

@WebServlet("/api/receptionist/waitlist/*")
public class ReceptionistWaitlistServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ReceptionistWaitlistServlet.class.getName());
    private final ReceptionistDAO receptionistDAO = new ReceptionistDAO();
    private final Gson gson = new Gson();
    private static final String[] VALID_STATUSES = {"Waiting", "InProgress", "Skipped", "Completed"};
    private static final String[] VALID_VISIT_TYPES = {"Initial", "Result"};
    private static final String[] VALID_SORT_FIELDS = {"waitlist_id", "registered_at", "estimated_time", "status"};

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

            String searchQuery = request.getParameter("searchQuery");
            String startDate = request.getParameter("startDate");
            String endDate = request.getParameter("endDate");
            String status = request.getParameter("status");
            String visitType = request.getParameter("visitType");
            int page = parseIntParam(request, "page", 1);
            int pageSize = parseIntParam(request, "pageSize", 10);
            String sortBy = request.getParameter("sortBy") != null ? request.getParameter("sortBy") : "waitlist_id";
            String sortOrder = request.getParameter("sortOrder") != null ? request.getParameter("sortOrder") : "ASC";

            LOGGER.info("Request params: page=" + page + ", pageSize=" + pageSize + ", startDate=" + startDate +
                    ", endDate=" + endDate + ", status=" + status + ", visitType=" + visitType +
                    ", sortBy=" + sortBy + ", sortOrder=" + sortOrder);

            if (page < 1 || pageSize <= 0 || pageSize > 100) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_PAGE",
                        "Page must be >= 1 and pageSize must be between 1 and 100");
                return;
            }

            if (!isValidSortField(sortBy)) {
                sortBy = "waitlist_id";
            }

            if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
                sortOrder = "ASC";
            }

            if (status != null && !status.isEmpty() && !isValidStatus(status)) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_STATUS",
                        "Status must be one of: " + String.join(", ", VALID_STATUSES));
                return;
            }

            if (visitType != null && !visitType.isEmpty() && !isValidVisitType(visitType)) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_VISIT_TYPE",
                        "VisitType must be one of: " + String.join(", ", VALID_VISIT_TYPES));
                return;
            }

            if (startDate != null && startDate.trim().isEmpty()) startDate = null;
            if (endDate != null && endDate.trim().isEmpty()) endDate = null;

            String formattedStartDate = parseDateTime(startDate, false);
            String formattedEndDate = parseDateTime(endDate, true);
            LOGGER.info("Formatted dates: startDate=" + formattedStartDate + ", endDate=" + formattedEndDate);
            if ((startDate != null && formattedStartDate == null) || (endDate != null && formattedEndDate == null)) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_DATE",
                        "Invalid date format. Use yyyy-MM-dd or yyyy-MM-dd'T'HH:mm[:ss]");
                return;
            }

            ArrayList<WaitlistDTO> waitlistEntries = receptionistDAO.getWaitlistEntries(
                    searchQuery, formattedStartDate, formattedEndDate, status, visitType, page, pageSize, sortBy, sortOrder);
            int totalEntries = receptionistDAO.countWaitlistEntries(searchQuery, formattedStartDate, formattedEndDate, status, visitType);
            LOGGER.info("DAO returned " + waitlistEntries.size() + " waitlist entries, total count: " + totalEntries +
                    ", Connection URL: " + receptionistDAO.getConnection().getMetaData().getURL());

            JsonObject responseJson = new JsonObject();
            responseJson.add("waitlistEntries", gson.toJsonTree(waitlistEntries));
            responseJson.addProperty("totalPages", (int) Math.ceil((double) totalEntries / pageSize));
            responseJson.addProperty("currentPage", page);
            responseJson.addProperty("pageSize", pageSize);
            responseJson.addProperty("totalEntries", totalEntries);
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", waitlistEntries.isEmpty() ? "No waitlist entries found" : "Waitlist entries fetched successfully");
            out.print(gson.toJson(responseJson));
        } catch (Exception e) {
            LOGGER.severe("Error processing GET request: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                    "Failed to fetch waitlist entries: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            LOGGER.info("POST request from " + request.getRemoteAddr() + " for " + request.getRequestURI());

            String pathInfo = request.getPathInfo();
            if (pathInfo == null || !pathInfo.equals("/update-estimated-time")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "INVALID_ENDPOINT", "Endpoint must be /update-estimated-time");
                return;
            }

            StringBuilder jsonBody = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBody.append(line);
                }
            }

            JsonObject jsonObject = gson.fromJson(jsonBody.toString(), JsonObject.class);
            if (!jsonObject.has("waitlistId") || !jsonObject.has("estimatedTime")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "MISSING_FIELDS", "waitlistId and estimatedTime are required");
                return;
            }

            int waitlistId = jsonObject.get("waitlistId").getAsInt();
            String estimatedTimeStr = jsonObject.get("estimatedTime").getAsString();

            if (waitlistId <= 0) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "INVALID_ID", "waitlistId must be a positive integer");
                return;
            }

            String formattedEstimatedTime = parseDateTime(estimatedTimeStr, false);
            if (formattedEstimatedTime == null) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_DATE",
                        "Invalid estimatedTime format. Use yyyy-MM-dd'T'HH:mm[:ss]");
                return;
            }

            LOGGER.info("Processing estimated_time update for waitlistId=" + waitlistId + ", estimatedTime=" + formattedEstimatedTime);

            boolean success = receptionistDAO.updateEstimatedTime(waitlistId, formattedEstimatedTime);

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", success);
            responseJson.addProperty("message", success ? "Estimated time updated successfully" :
                    "Update failed: waitlist entry may not exist, or visittype is not Initial, or status is not Waiting");
            out.print(gson.toJson(responseJson));
        } catch (com.google.gson.JsonSyntaxException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_JSON", "Invalid JSON format: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Error processing POST request: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "UPDATE_ERROR", "Failed to update estimated time: " + e.getMessage());
        } finally {
            out.flush();
        }
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private String parseDateTime(String input, boolean isEndDate) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DateTimeFormatter datetimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd['T'HH:mm[:ss]]");
            LocalDateTime dateTime;
            try {
                LocalDate date = LocalDate.parse(input, dateFormatter);
                dateTime = date.atStartOfDay();
                if (isEndDate) {
                    dateTime = date.atTime(23, 59, 59);
                }
            } catch (DateTimeParseException e) {
                dateTime = LocalDateTime.parse(input, datetimeFormatter);
            }
            return dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            LOGGER.warning("Invalid date format: " + input + ", error: " + e.getMessage());
            return null;
        }
    }

    private int parseIntParam(HttpServletRequest request, String paramName, int defaultValue) {
        String param = request.getParameter(paramName);
        if (param == null || param.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(param);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + paramName + ": " + param);
        }
    }

    private boolean isValidStatus(String status) {
        for (String validStatus : VALID_STATUSES) {
            if (validStatus.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidVisitType(String visitType) {
        for (String validVisitType : VALID_VISIT_TYPES) {
            if (validVisitType.equalsIgnoreCase(visitType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidSortField(String sortBy) {
        for (String field : VALID_SORT_FIELDS) {
            if (field.equalsIgnoreCase(sortBy)) {
                return true;
            }
        }
        return false;
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