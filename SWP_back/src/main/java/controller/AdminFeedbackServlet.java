package controller;

import dal.FeedbackDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import model.Feedback;
import model.FeedbackOverview;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

@WebServlet("/api/AdminBusinessFeedback/*")
public class AdminFeedbackServlet extends HttpServlet {
    private FeedbackDAO feedbackDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        feedbackDAO = new FeedbackDAO();
        gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCORSHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCORSHeaders(resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject responseJson = new JsonObject();
        try {
            // Parse query parameters
            Timestamp startDate = parseTimestamp(req.getParameter("startDate"), true);
            Timestamp endDate = parseTimestamp(req.getParameter("endDate"), false);
            Double minAvgFeedback = parseDouble(req.getParameter("minAvgFeedback"));
            Double maxAvgFeedback = parseDouble(req.getParameter("maxAvgFeedback"));

            String pathInfo = req.getPathInfo() != null ? req.getPathInfo() : "";
            if (pathInfo.equals("/overview")) {
                // Handle overview endpoint
                FeedbackOverview overview = feedbackDAO.getFeedbackOverview(
                        startDate, endDate, minAvgFeedback, maxAvgFeedback
                );

                // Prepare response
                responseJson.addProperty("status", "success");
                responseJson.add("overview", gson.toJsonTree(overview));
            } else {
                // Handle feedback list endpoint
                int page = parseInt(req.getParameter("page"), 1);
                int pageSize = parseInt(req.getParameter("pageSize"), 10);

                // Validate parameters
                if (page < 1 || pageSize < 1) {
                    throw new IllegalArgumentException("Page and pageSize must be positive");
                }

                // Call DAO methods
                ArrayList<Feedback> feedbackList = feedbackDAO.getFeedbackByFilters(
                        startDate, endDate, minAvgFeedback, maxAvgFeedback, page, pageSize
                );
                int totalItems = feedbackDAO.countFeedbackByFilters(
                        startDate, endDate, minAvgFeedback, maxAvgFeedback
                );
                int totalPages = (int) Math.ceil((double) totalItems / pageSize);

                FeedbackOverview overview = feedbackDAO.getFeedbackOverview(startDate, endDate, minAvgFeedback, maxAvgFeedback);

                // Prepare response
                responseJson.addProperty("status", "success");
                responseJson.add("feedbacks", gson.toJsonTree(feedbackList));
                responseJson.add("overview", gson.toJsonTree(overview));
                responseJson.addProperty("page", page);
                responseJson.addProperty("pageSize", pageSize);
                responseJson.addProperty("totalItems", totalItems);
                responseJson.addProperty("totalPages", totalPages);
            }

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", e.getMessage());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseJson.addProperty("status", "error");
            responseJson.addProperty("message", "Internal server error: " + e.getMessage());
        }

        // Write JSON response
        try (PrintWriter out = resp.getWriter()) {
            out.print(gson.toJson(responseJson));
        }
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private Timestamp parseTimestamp(String value, boolean isStartDate) throws IllegalArgumentException {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(value));
            if (isStartDate) {
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            } else {
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
            }
            return new Timestamp(cal.getTimeInMillis());
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format, expected yyyy-MM-dd: " + value);
        }
    }

    private Double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format: " + value);
        }
    }

    private int parseInt(String value, int defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer format: " + value);
        }
    }
}