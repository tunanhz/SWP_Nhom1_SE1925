package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.ReceptionistDAO;
import dto.ReceptionistCheckInDTO;
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

@WebServlet("/api/receptionist/appointments/*")
public class ReceptionistAppointmentServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ReceptionistAppointmentServlet.class.getName());
    private final ReceptionistDAO receptionistDAO = new ReceptionistDAO();
    private final Gson gson = new Gson();
    private static final String[] VALID_STATUSES = {"Pending", "Confirmed", "Completed", "Cancelled"};
    private static final String[] VALID_SORT_FIELDS = {"appointment_id", "appointment_datetime", "status"};

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
            int page = parseIntParam(request, "page", 1);
            int pageSize = parseIntParam(request, "pageSize", 10);
            String sortBy = request.getParameter("sortBy") != null ? request.getParameter("sortBy") : "appointment_id";
            String sortOrder = request.getParameter("sortOrder") != null ? request.getParameter("sortOrder") : "ASC";

            LOGGER.info("Request params: page=" + page + ", pageSize=" + pageSize + ", startDate=" + startDate + ", endDate=" + endDate +
                    ", status=" + status + ", sortBy=" + sortBy + ", sortOrder=" + sortOrder);

            if (page < 1 || pageSize <= 0 || pageSize > 100) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_PAGE",
                        "Page must be >= 1 and pageSize must be between 1 and 100");
                return;
            }

            if (!isValidSortField(sortBy)) {
                sortBy = "appointment_id";
            }

            if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
                sortOrder = "ASC";
            }

            if (status != null && !status.isEmpty() && !isValidStatus(status)) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_STATUS",
                        "Status must be one of: " + String.join(", ", VALID_STATUSES));
                return;
            }

            if (startDate != null && startDate.trim().isEmpty()) startDate = null;
            if (endDate != null && endDate.trim().isEmpty()) endDate = null;

            String formattedStartDate = parseDateTime(startDate, false); // Start of day
            String formattedEndDate = parseDateTime(endDate, true);     // End of day
            LOGGER.info("Formatted dates: startDate=" + formattedStartDate + ", endDate=" + formattedEndDate);
            if ((startDate != null && formattedStartDate == null) || (endDate != null && formattedEndDate == null)) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_DATE",
                        "Invalid date format. Use yyyy-MM-dd or yyyy-MM-dd'T'HH:mm[:ss]");
                return;
            }

            ArrayList<ReceptionistCheckInDTO> appointments = receptionistDAO.getAppointmentsByStatus(
                    searchQuery, formattedStartDate, formattedEndDate, status, page, pageSize, sortBy, sortOrder);
            int totalAppointments = receptionistDAO.countAppointmentsByStatus(searchQuery, formattedStartDate, formattedEndDate, status);
            LOGGER.info("DAO returned " + appointments.size() + " appointments, total count: " + totalAppointments +
                    ", Connection URL: " + receptionistDAO.getConnection().getMetaData().getURL());
            LOGGER.info("Full API response: " + gson.toJson(appointments)); // Log full appointment list for this page

            JsonObject responseJson = new JsonObject();
            responseJson.add("appointments", gson.toJsonTree(appointments));
            responseJson.addProperty("totalPages", (int) Math.ceil((double) totalAppointments / pageSize));
            responseJson.addProperty("currentPage", page);
            responseJson.addProperty("pageSize", pageSize);
            responseJson.addProperty("totalAppointments", totalAppointments);
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", appointments.isEmpty() ? "No appointments found" : "Appointments fetched successfully");
            out.print(gson.toJson(responseJson));
        } catch (Exception e) {
            LOGGER.severe("Error processing GET request: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                    "Failed to fetch appointments: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
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
            if (pathInfo == null || !pathInfo.equals("/checkin")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "INVALID_ENDPOINT", "Endpoint must be /checkin");
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
            if (!jsonObject.has("appointmentId") || !jsonObject.has("accountStaffId")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "MISSING_FIELDS", "appointmentId and accountStaffId are required");
                return;
            }

            int appointmentId = jsonObject.get("appointmentId").getAsInt();
            int accountStaffId = jsonObject.get("accountStaffId").getAsInt();
            int receptionistId = receptionistDAO.getReceptionistByAccountStaffId(accountStaffId);
            if (appointmentId <= 0 || receptionistId <= 0) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "INVALID_IDS", "appointmentId and receptionistId must be positive integers");
                return;
            }

            LOGGER.info("Processing check-in for appointmentId=" + appointmentId + ", receptionistId=" + receptionistId);

            boolean success = receptionistDAO.checkInAppointment(appointmentId, receptionistId);

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", success);
            responseJson.addProperty("message", success ? "Xác nhận thành công" :
                    "Không thể xác nhận phòng: cuộc hẹn có thể không tồn tại, không ở trạng thái Đang chờ, không có phòng được chỉ định hoặc định dạng dấu thời gian không hợp lệ");
            out.print(gson.toJson(responseJson));
        } catch (com.google.gson.JsonSyntaxException e) {
            sendError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "INVALID_JSON", "Invalid JSON format: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Error processing POST request: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "CHECKIN_ERROR", "Failed to process check-in: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
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
                dateTime = date.atStartOfDay(); // 00:00:00 for start date
                if (isEndDate) {
                    dateTime = date.atTime(23, 59, 59); // 23:59:59 for end date
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
            if (validStatus.equals(status)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidSortField(String sortBy) {
        for (String field : VALID_SORT_FIELDS) {
            if (field.equals(sortBy)) {
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