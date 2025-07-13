package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.DoctorDAO;
import dto.WaitlistDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

@WebServlet("/api/doctor/waitlist/*")
public class DoctorWaitlistServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DoctorWaitlistServlet.class.getName());
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final Gson gson = new Gson();
    private static final String[] VALID_STATUSES = {"Waiting", "InProgress", "Skipped", "Completed"};
    private static final String[] VALID_SORT_FIELDS = {"waitlist_id", "registered_at", "estimated_time", "status"};

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String accountStaffIdParam = request.getParameter("account_staff_id");
            if (accountStaffIdParam == null || accountStaffIdParam.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Missing account_staff_id in request\"}");
                return;
            }

            int accountStaffId;
            try {
                accountStaffId = Integer.parseInt(accountStaffIdParam.trim());
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid account_staff_id format\"}");
                return;
            }

            int doctorId = doctorDAO.getDoctorByAccountStaffId(accountStaffId);
            if (doctorId == -1) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\": \"No doctor found for this account\"}");
                return;
            }

            int page = parseIntParam(request.getParameter("page"), 1);
            int pageSize = parseIntParam(request.getParameter("pageSize"), 10);
            String sortBy = request.getParameter("sortBy");
            String sortOrder = request.getParameter("sortOrder");

            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 10;
            if (sortBy == null || !isValidSortField(sortBy)) sortBy = "waitlist_id";
            if (sortOrder == null || (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC"))) {
                sortOrder = "ASC";
            }

            LOGGER.info("Fetching waitlist for doctor_id=" + doctorId + ", page=" + page + ", pageSize=" + pageSize);

            ArrayList<WaitlistDTO> waitlistEntries = doctorDAO.getWaitlistEntriesForDoctor(doctorId, page, pageSize, sortBy, sortOrder);
            int totalEntries = doctorDAO.countWaitlistEntriesForDoctor(doctorId);
            int totalPages = (totalEntries + pageSize - 1) / pageSize;

            JsonObject responseData = new JsonObject();
            responseData.add("waitlistEntries", gson.toJsonTree(waitlistEntries));
            responseData.addProperty("totalEntries", totalEntries);
            responseData.addProperty("totalPages", totalPages);
            responseData.addProperty("currentPage", page);
            responseData.addProperty("pageSize", pageSize);
            responseData.addProperty("doctorId", doctorId);

            response.getWriter().write(gson.toJson(responseData));
            LOGGER.info("Successfully returned " + waitlistEntries.size() + " waitlist entries for doctor " + doctorId);

        } catch (Exception e) {
            LOGGER.severe("Error in DoctorWaitlistServlet GET: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            String accountStaffIdParam = request.getParameter("account_staff_id");
            if (accountStaffIdParam == null || accountStaffIdParam.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Missing account_staff_id in request\"}");
                return;
            }

            int accountStaffId;
            try {
                accountStaffId = Integer.parseInt(accountStaffIdParam.trim());
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid account_staff_id format\"}");
                return;
            }

            int doctorId = doctorDAO.getDoctorByAccountStaffId(accountStaffId);
            if (doctorId == -1) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"error\": \"No doctor found for this account\"}");
                return;
            }

            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            JsonObject requestData = gson.fromJson(sb.toString(), JsonObject.class);
            int waitlistId = requestData.get("waitlistId").getAsInt();
            String newStatus = requestData.get("status").getAsString();

            if (!isValidStatus(newStatus)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\": \"Invalid status: " + newStatus + "\"}");
                return;
            }

            boolean success = doctorDAO.updateWaitlistStatus(waitlistId, newStatus);

            if (success) {
                JsonObject responseData = new JsonObject();
                responseData.addProperty("success", true);
                responseData.addProperty("message", "Waitlist status updated successfully");
                responseData.addProperty("waitlistId", waitlistId);
                responseData.addProperty("newStatus", newStatus);
                response.getWriter().write(gson.toJson(responseData));
                LOGGER.info("Doctor " + doctorId + " updated waitlist " + waitlistId + " status to " + newStatus);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\": \"Waitlist entry not found or update failed\"}");
            }

        } catch (Exception e) {
            LOGGER.severe("Error in DoctorWaitlistServlet PUT: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Internal server error: " + e.getMessage() + "\"}");
        }
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    private int parseIntParam(String param, int defaultValue) {
        if (param == null || param.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(param.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean isValidSortField(String sortBy) {
        for (String field : VALID_SORT_FIELDS) {
            if (field.equals(sortBy)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidStatus(String status) {
        for (String validStatus : VALID_STATUSES) {
            if (validStatus.equals(status)) {
                return true;
            }
        }
        return false;
    }
}
