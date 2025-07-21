package controller;

import dal.AdminBusinessReportDAO;
import dto.AppointmentReportDTO;
import dto.AppointmentSummaryDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.io.OutputStream;

@WebServlet("/api/AdminBusinessAppointment/*")
public class AdminBusinessAppointmentServlet extends HttpServlet {
    private AdminBusinessReportDAO appointmentDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        appointmentDAO = new AdminBusinessReportDAO();
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
        String pathInfo = req.getPathInfo() != null ? req.getPathInfo() : "";
        if (pathInfo.equals("/api/AdminBusinessAppointment/export")) {
            handleExportRequest(req, resp);
        } else {
            setCORSHeaders(resp);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");

            JsonObject responseJson = new JsonObject();
            try {
                // Parse query parameters
                Timestamp startDate = parseTimestamp(req.getParameter("startDate"), true);
                Timestamp endDate = parseTimestamp(req.getParameter("endDate"), false);
                String status = req.getParameter("status");
                String searchTerm = req.getParameter("searchTerm");

                if (pathInfo.equals("/overview")) {
                    // Handle overview endpoint
                    AppointmentSummaryDTO summary = appointmentDAO.getAppointmentSummary(
                            startDate != null ? startDate.toString() : null,
                            endDate != null ? endDate.toString() : null
                    );

                    // Prepare response
                    responseJson.addProperty("status", "success");
                    responseJson.add("overview", gson.toJsonTree(summary));
                } else {
                    // Handle appointment list endpoint
                    int page = parseInt(req.getParameter("page"), 1);
                    int pageSize = parseInt(req.getParameter("pageSize"), 10);

                    // Validate parameters
                    if (page < 1 || pageSize < 1) {
                        throw new IllegalArgumentException("Page and pageSize must be positive");
                    }

                    // Call DAO methods
                    ArrayList<AppointmentReportDTO> appointmentList = appointmentDAO.getAppointmentDetails(
                            startDate != null ? endDate.toString() : null,
                            endDate != null ? endDate.toString() : null,
                            status, searchTerm, page, pageSize
                    );
                    int totalItems = appointmentDAO.countAppointmentDetails(
                            startDate != null ? startDate.toString() : null,
                            endDate != null ? endDate.toString() : null,
                            status, searchTerm
                    );
                    int totalPages = (int) Math.ceil((double) totalItems / pageSize);

                    AppointmentSummaryDTO summary = appointmentDAO.getAppointmentSummary(
                            startDate != null ? startDate.toString() : null,
                            endDate != null ? endDate.toString() : null
                    );

                    // Prepare response
                    responseJson.addProperty("status", "success");
                    responseJson.add("appointments", gson.toJsonTree(appointmentList));
                    responseJson.add("overview", gson.toJsonTree(summary));
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
    }

    private void handleExportRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCORSHeaders(resp);
        resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=appointments_export.xlsx");

        try {
            // Parse query parameters
            Timestamp startDate = parseTimestamp(req.getParameter("startDate"), true);
            Timestamp endDate = parseTimestamp(req.getParameter("endDate"), false);
            String status = req.getParameter("status");
            String searchTerm = req.getParameter("searchTerm");

            // Fetch all appointment details (no pagination)
            ArrayList<AppointmentReportDTO> appointmentList = appointmentDAO.getAppointmentDetails(
                    startDate != null ? startDate.toString() : null,
                    endDate != null ? endDate.toString() : null,
                    status, searchTerm, 1, Integer.MAX_VALUE
            );

            // Create XLSX workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Appointments");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Appointment ID", "Patient ID", "Patient Name", "Appointment DateTime",
                    "Shift", "Cancellation Reason", "Status", "Doctor ID", "Doctor Name", "No-Show"
            };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            int rowNum = 1;
            for (AppointmentReportDTO appointment : appointmentList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(appointment.getAppointmentId());
                row.createCell(1).setCellValue(appointment.getPatientId());
                row.createCell(2).setCellValue(appointment.getPatientName());
                row.createCell(3).setCellValue(appointment.getAppointmentDateTime());
                row.createCell(4).setCellValue(appointment.getShift());
                row.createCell(5).setCellValue(appointment.getCancellationReason());
                row.createCell(6).setCellValue(appointment.getAppointmentStatus());
                row.createCell(7).setCellValue(appointment.getDoctorId());
                row.createCell(8).setCellValue(appointment.getDoctorName());
                row.createCell(9).setCellValue(appointment.getIsNoShow());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to response output stream
            try (OutputStream out = resp.getOutputStream()) {
                workbook.write(out);
            }
            workbook.close();

        } catch (IllegalArgumentException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("status", "error");
            errorJson.addProperty("message", e.getMessage());
            try (PrintWriter out = resp.getWriter()) {
                resp.setContentType("application/json");
                out.print(gson.toJson(errorJson));
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("status", "error");
            errorJson.addProperty("message", "Internal server error: " + e.getMessage());
            try (PrintWriter out = resp.getWriter()) {
                resp.setContentType("application/json");
                out.print(gson.toJson(errorJson));
            }
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