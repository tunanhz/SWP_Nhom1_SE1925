package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.PatientPaymentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.PatientPaymentDTO;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/api/patientPayment/*")
public class PatientPaymentServlet extends HttpServlet {
    private final PatientPaymentDAO patientPaymentDAO = new PatientPaymentDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCORSHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();

        try (PrintWriter out = response.getWriter()) {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Validate required parameters
                if (request.getParameter("accountPatientId") == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Missing accountPatientId parameter\"}");
                    return;
                }

                // Parse parameters
                int accountPatientId = Integer.parseInt(request.getParameter("accountPatientId"));
                String issueDate = request.getParameter("issueDate");
                String status = request.getParameter("status");
                int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
                int pageSize = Integer.parseInt(request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "6");

                // Validate page and pageSize
                if (page < 1 || pageSize < 1) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Page and pageSize must be positive\"}");
                    return;
                }

                String dateValid = parseDateTime(issueDate);

                // Fetch invoices
                ArrayList<PatientPaymentDTO> invoices = patientPaymentDAO.getPatientInvoicesByAccountId(
                        accountPatientId, dateValid, status, page, pageSize
                );

                // Calculate total pages
                int totalInvoice = patientPaymentDAO.getTotalInvoices(accountPatientId, dateValid, status);
                int totalPages = (int) Math.ceil((double) totalInvoice / pageSize);

                // Build response
                JsonObject responseJson = new JsonObject();
                responseJson.add("invoices", gson.toJsonTree(invoices));
                responseJson.addProperty("totalPages", totalPages);

                // Write response
                out.print(gson.toJson(responseJson));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Resource not found\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\": \"Invalid number format in parameters\"}");
            }
        } catch (Exception e) {
            log("Unexpected error: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\": \"Unexpected error occurred\"}");
            }
        }
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    public static String parseDateTime(String input) {
        // Return null if input is null
        if (input == null) {
            return null;
        }

        // Check for empty input
        if (input.trim().isEmpty()) {
            return "Error: Năm là bắt buộc. Vui lòng nhập năm.";
        }

        // Normalize input: replace / with - and trim spaces
        input = input.trim().replace("/", "-");

        // Regex patterns for different input formats
        Pattern fullDateTimePattern = Pattern.compile("^(\\d{4})-(\\d{1,2})-(\\d{1,2})\\s+(\\d{1,2}):(\\d{1,2})$");
        Pattern dateOnlyPattern = Pattern.compile("^(\\d{4})-(\\d{1,2})-(\\d{1,2})$");
        Pattern yearMonthPattern = Pattern.compile("^(\\d{4})-(\\d{1,2})$");
        Pattern yearOnlyPattern = Pattern.compile("^(\\d{4})$");
        Pattern ddMMyyyyDateTimePattern = Pattern.compile("^(\\d{1,2})-(\\d{1,2})-(\\d{4})\\s+(\\d{1,2}):(\\d{1,2})$");
        Pattern ddMMyyyyDatePattern = Pattern.compile("^(\\d{1,2})-(\\d{1,2})-(\\d{4})$");
        Pattern mmDDyyyyDateTimePattern = Pattern.compile("^(\\d{1,2})-(\\d{1,2})-(\\d{4})\\s+(\\d{1,2}):(\\d{1,2})$");
        Pattern mmDDyyyyDatePattern = Pattern.compile("^(\\d{1,2})-(\\d{1,2})-(\\d{4})$");
        Pattern mmYYYYPattern = Pattern.compile("^(\\d{1,2})-(\\d{4})$");

        Matcher matcher;

        try {
            // Full date and time (yyyy-mm-dd hh:mm, e.g., 2025-06-15 14:30 or 2025-06-15 4:5)
            matcher = fullDateTimePattern.matcher(input);
            if (matcher.matches()) {
                int year = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));
                int hour = Integer.parseInt(matcher.group(4));
                int minute = Integer.parseInt(matcher.group(5));

                return validateAndFormatDateTime(year, month, day, hour, minute);
            }

            // Full date and time (dd-mm-yyyy hh:mm, e.g., 15-06-2025 14:30)
            matcher = ddMMyyyyDateTimePattern.matcher(input);
            if (matcher.matches()) {
                int day = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int year = Integer.parseInt(matcher.group(3));
                int hour = Integer.parseInt(matcher.group(4));
                int minute = Integer.parseInt(matcher.group(5));

                return validateAndFormatDateTime(year, month, day, hour, minute);
            }

            // Full date and time (mm-dd-yyyy hh:mm, e.g., 06-15-2025 14:30)
            matcher = mmDDyyyyDateTimePattern.matcher(input);
            if (matcher.matches()) {
                int month = Integer.parseInt(matcher.group(1));
                int day = Integer.parseInt(matcher.group(2));
                int year = Integer.parseInt(matcher.group(3));
                int hour = Integer.parseInt(matcher.group(4));
                int minute = Integer.parseInt(matcher.group(5));

                return validateAndFormatDateTime(year, month, day, hour, minute);
            }

            // Date only (yyyy-mm-dd, e.g., 2025-6-5)
            matcher = dateOnlyPattern.matcher(input);
            if (matcher.matches()) {
                int year = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int day = Integer.parseInt(matcher.group(3));

                return validateAndFormatDate(year, month, day);
            }

            // Date only (dd-mm-yyyy, e.g., 5-6-2025)
            matcher = ddMMyyyyDatePattern.matcher(input);
            if (matcher.matches()) {
                int day = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));
                int year = Integer.parseInt(matcher.group(3));

                return validateAndFormatDate(year, month, day);
            }

            // Date only (mm-dd-yyyy, e.g., 6-5-2025)
            matcher = mmDDyyyyDatePattern.matcher(input);
            if (matcher.matches()) {
                int month = Integer.parseInt(matcher.group(1));
                int day = Integer.parseInt(matcher.group(2));
                int year = Integer.parseInt(matcher.group(3));

                return validateAndFormatDate(year, month, day);
            }

            // Year and month only (yyyy-mm, e.g., 2025-06)
            matcher = yearMonthPattern.matcher(input);
            if (matcher.matches()) {
                int year = Integer.parseInt(matcher.group(1));
                int month = Integer.parseInt(matcher.group(2));

                if (month < 1 || month > 12) {
                    return "Error: Tháng không hợp lệ. Vui lòng nhập tháng từ 1 đến 12.";
                }

                return String.format("%04d-%02d", year, month);
            }

            // Month and year only (mm-yyyy, e.g., 06-2025)
            matcher = mmYYYYPattern.matcher(input);
            if (matcher.matches()) {
                int month = Integer.parseInt(matcher.group(1));
                int year = Integer.parseInt(matcher.group(2));

                if (month < 1 || month > 12) {
                    return "Error: Tháng không hợp lệ. Vui lòng nhập tháng từ 1 đến 12.";
                }

                return String.format("%04d-%02d", year, month);
            }

            // Year only (e.g., 2025)
            matcher = yearOnlyPattern.matcher(input);
            if (matcher.matches()) {
                int year = Integer.parseInt(matcher.group(1));
                return String.valueOf(year);
            }

        } catch (NumberFormatException e) {
            return "Error: Định dạng số không hợp lệ.";
        }

        return "Error: Định dạng không hợp lệ. Vui lòng kiểm tra lại đầu vào.";
    }

    private static String validateAndFormatDateTime(int year, int month, int day, int hour, int minute) {
        // Validate month
        if (month < 1 || month > 12) {
            return "Error: Tháng không hợp lệ. Vui lòng nhập tháng từ 1 đến 12.";
        }

        // Validate day
        if (!isValidDay(year, month, day)) {
            return "Error: Ngày không hợp lệ cho tháng và năm đã nhập.";
        }

        // Validate time
        if (hour > 23) {
            return "Error: Giờ không hợp lệ. Vui lòng nhập giờ từ 0 đến 23.";
        }
        if (minute > 59) {
            return "Error: Phút không hợp lệ. Vui lòng nhập phút từ 0 đến 59.";
        }

        // Format output
        return String.format("%04d-%02d-%02d %02d:%02d:00", year, month, day, hour, minute);
    }

    private static String validateAndFormatDate(int year, int month, int day) {
        // Validate month
        if (month < 1 || month > 12) {
            return "Error: Tháng không hợp lệ. Vui lòng nhập tháng từ 1 đến 12.";
        }

        // Validate day
        if (!isValidDay(year, month, day)) {
            return "Error: Ngày không hợp lệ cho tháng và năm đã nhập.";
        }

        // Format output
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    private static boolean isValidDay(int year, int month, int day) {
        try {
            LocalDate date = LocalDate.of(year, month, day);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}