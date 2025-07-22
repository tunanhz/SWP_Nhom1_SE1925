package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dal.PatientAppointmentDAO;
import dto.AppointmentDTO;
import dto.AppointmentPatientDTO;
import dto.PatientPaymentDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Appointment;
import model.AppointmentRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/api/patientAppointment/*")
public class PatientAppointmentServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PatientAppointmentServlet.class.getName());
    private final PatientAppointmentDAO appointmentDAO = new PatientAppointmentDAO();
    private final Gson gson = new Gson();


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();

        if (pathInfo == null || pathInfo.equals("/")) {
            try {

                String accountPatientIdParam = request.getParameter("accountPatientId");

                if (accountPatientIdParam == null || accountPatientIdParam.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"accountPatientId is required\"}");
                    return;
                }
                int accountPatientId = Integer.parseInt(accountPatientIdParam.trim());
                String fullName = request.getParameter("name");
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

                String appointmentDateValid = parseDateTime(appointmentDateTime);

                // Log request for debugging
                LOGGER.info("Fetching appointments for accountPatientId=" + accountPatientId +
                        ", fullName=" + fullName + ", appointmentDateTime=" + appointmentDateValid +
                        ", status=" + status + ", page=" + page + ", pageSize=" + pageSize);

                // Get appointments and total count
                ArrayList<AppointmentDTO> appointments = appointmentDAO.getAppointmentsByAccountPatientId(
                        accountPatientId, fullName, appointmentDateValid, status, page, pageSize);

                int totalAppointment = appointmentDAO.countAppointmentsByAccountPatientId(
                        accountPatientId, fullName, appointmentDateValid, status);
                int totalPages = (int) Math.ceil((double) totalAppointment / pageSize);

                ArrayList<AppointmentPatientDTO> appointmentPatientDTOS = appointmentDAO.getThreeAppointmentsUpcoming(accountPatientId);
                ArrayList<AppointmentDTO> threeAppointmentComplete = appointmentDAO.getTop3CompletedAppointments(accountPatientId);
                ArrayList<PatientPaymentDTO> threePaymentPending = appointmentDAO.getTop3Payment(accountPatientId);
                ArrayList<AppointmentPatientDTO> allAppointment = appointmentDAO.getAllAppointments(accountPatientId);
                // Build response JSON
                JsonObject responseJson = new JsonObject();
                responseJson.add("appointments", gson.toJsonTree(appointments));
                responseJson.add("threeAppointmentsUpcoming", gson.toJsonTree(appointmentPatientDTOS));
                responseJson.add("threeAppointmentComplete", gson.toJsonTree(threeAppointmentComplete));
                responseJson.add("threePaymentPending", gson.toJsonTree(threePaymentPending));
                responseJson.add("allAppointment", gson.toJsonTree(allAppointment));
                responseJson.addProperty("totalPages", totalPages);
                responseJson.addProperty("currentPage", page);
                responseJson.addProperty("pageSize", pageSize);
                responseJson.addProperty("totalAppointment", totalAppointment);
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

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();

        if (pathInfo != null && pathInfo.split("/").length == 2) {
            try {
                int id = Integer.parseInt(pathInfo.split("/")[1]);
                // Đọc JSON từ request body
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = request.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                String jsonData = sb.toString();
                AppointmentRequest appointmentRequestUpdate = gson.fromJson(jsonData, AppointmentRequest.class);

                if (appointmentRequestUpdate.getDoctorId() == null || appointmentRequestUpdate.getDate() == null || appointmentRequestUpdate.getTime() == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"error\": \"Missing required fields: doctorId, date, or time\"}");
                    return;
                }

                if (!appointmentRequestUpdate.getDoctorId().matches("\\d+")) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"error\": \"Invalid doctorId format\"}");
                    return;
                }

                int doctorId = Integer.parseInt(appointmentRequestUpdate.getDoctorId());
                String date = appointmentRequestUpdate.getDate();
                String time = appointmentRequestUpdate.getTime();
                String note = appointmentRequestUpdate.getNote();
                String shift = appointmentRequestUpdate.getShift();
                Integer receptionistId = appointmentRequestUpdate.getReceptionistId();
                Integer patientId = appointmentRequestUpdate.getPatientId();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date appointmentDateTime;

                try {
                    appointmentDateTime = sdf.parse(date + " " + time);
                } catch (ParseException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"error\": \"Invalid date or time format\"}");
                    return;
                }

                Date currentDate = new Date();
                if (appointmentDateTime.before(currentDate)) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"error\": \"Appointment date must be in the future\"}");
                    return;
                }

                // Gọi hàm updateAppointment từ AppointmentDAO
                Appointment updatedAppointment;
                try {
                    updatedAppointment = appointmentDAO.updateAppointment(
                            id, doctorId, patientId, appointmentDateTime, shift, receptionistId, note
                    );
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.println(gson.toJson(updatedAppointment));
                } catch (SQLException e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    out.println("{\"error\": \"" + e.getMessage() + "\"}");
                }

            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Invalid ID\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\": \"Invalid request\"}");
        }
        out.flush();
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();

        if (pathInfo != null && pathInfo.split("/").length == 2) {
            try {
                int id = Integer.parseInt(pathInfo.split("/")[1]);
                boolean removed = appointmentDAO.deleteAppointmentById(id);
                if (removed) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT); // Use 204 instead of 200
                    LOGGER.info("Appointment " + id + " deleted successfully");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Appointment not found\"}");
                    LOGGER.info("Appointment " + id + " not found");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid ID\"}");
                LOGGER.severe("Error processing Appointment request: " + e.getMessage());
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid request\"}");
        }
        out.flush();
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCORSHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept");
        response.setHeader("Access-Control-Max-Age", "86400");
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