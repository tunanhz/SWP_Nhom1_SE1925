package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dal.PatientDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Patient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/api/patient/*")
public class PatientServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PatientAppointmentServlet.class.getName());
    private static final PatientDAO patientDAO = new PatientDAO();
    private final Gson gson = new Gson();

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
                String accountPatientIdParam = request.getParameter("accountPatientId");

                if (accountPatientIdParam == null || accountPatientIdParam.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"accountPatientId is required\"}");
                    return;
                }
                int accountPatientId = Integer.parseInt(accountPatientIdParam.trim());
                String fullName = request.getParameter("name");
                String dob = request.getParameter("dob");
                String gender = request.getParameter("gender");
                int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
                int pageSize = Integer.parseInt(request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "8");

                // Validate pagination parameters
                if (page < 0 || pageSize <= 0 || pageSize > 100) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Invalid page or pageSize\"}");
                    return;
                }

                String dobValid = parseDateTime(dob);

                // Log request for debugging
                LOGGER.info("Fetching appointments for accountPatientId=" + accountPatientId +
                        ", fullName=" + fullName + ", dob=" + dobValid +
                        ", gender=" + gender + ", page=" + page + ", pageSize=" + pageSize);

                // Get appointments and total count
                ArrayList<Patient> patients = patientDAO.getAllPatientsByAccountPatientId(
                        accountPatientId, fullName, dobValid, gender, page, pageSize);

                int totalPatient = patientDAO.countPatientByAccountPatientId(
                        accountPatientId, fullName, dobValid, gender);
                int totalPages = (int) Math.ceil((double) totalPatient / pageSize);

                // Build response JSON
                JsonObject responseJson = new JsonObject();
                responseJson.add("patients", gson.toJsonTree(patients));
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
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            try {
                String accountPatientIdParam = request.getParameter("accountPatientId");

                if (accountPatientIdParam == null || accountPatientIdParam.trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"accountPatientId is required\"}");
                    return;
                }
                int accountPatientId = Integer.parseInt(accountPatientIdParam.trim());
                // Read request body
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = request.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }

                // Parse JSON data
                Patient addPatient;
                try {
                    addPatient = gson.fromJson(sb.toString(), Patient.class);
                } catch (JsonSyntaxException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                    out.print("{\"error\":\"Invalid JSON format\"}");
                    LOGGER.severe("Error parsing JSON: " + e.getMessage());
                    return;
                }

                // Validate input
                if (addPatient.getFullName() == null || addPatient.getFullName().isEmpty() ||
                        addPatient.getFullName().length() > 100 || addPatient.getFullName().matches(".*\\s{2,}.*")) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                    out.print("{\"error\":\"Invalid full name: must not be empty, max 100 characters, no multiple spaces\"}");
                    return;
                }

                if (addPatient.getDob() == null || addPatient.getDob().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                    out.print("{\"error\":\"Date of birth is required\"}");
                    return;
                }

                if (addPatient.getGender() == null || addPatient.getGender().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                    out.print("{\"error\":\"Gender is required\"}");
                    return;
                }

                if (addPatient.getPhone() == null || !addPatient.getPhone().matches("^[0][1-9]{9}$")) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                    out.print("{\"error\":\"Invalid phone number: must be 10 digits starting with 0\"}");
                    return;
                }

                if (addPatient.getAddress() == null || addPatient.getAddress().isEmpty() ||
                        addPatient.getAddress().length() > 225) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                    out.print("{\"error\":\"Invalid address: must not be empty, max 225 characters\"}");
                    return;
                }

                // Check for existing patient
                Patient existingPatient = patientDAO.getPatientByDetails(
                        addPatient.getFullName(),
                        addPatient.getDob(),
                        addPatient.getGender(),
                        addPatient.getPhone(),
                        addPatient.getAddress()
                );

                if (existingPatient != null) {
                    if ("Disable".equalsIgnoreCase(existingPatient.getStatus())) {
                        // Update status to Enable
                        boolean updated = patientDAO.updatePatientStatus(existingPatient.getId(), "Enable");
                        if (updated) {
                            response.setStatus(HttpServletResponse.SC_OK); // 200
                            out.print("{\"message\":\"Patient status updated to Enable\"}");
                            LOGGER.info("Patient status updated to Enable: " + existingPatient.getFullName());
                        } else {
                            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
                            out.print("{\"error\":\"Failed to update patient status\"}");
                            LOGGER.severe("Failed to update patient status: " + existingPatient.getFullName());
                        }
                    } else {
                        // Patient is already Enable
                        response.setStatus(HttpServletResponse.SC_OK); // 200
                        out.print("{\"message\":\"Patient already enabled\"}");
                        LOGGER.info("Patient already enabled: " + existingPatient.getFullName());
                    }
                    return;
                }

                // Insert new patient
                boolean inserted = patientDAO.insertPatient(
                        addPatient.getFullName(),
                        addPatient.getDob(),
                        addPatient.getGender(),
                        addPatient.getPhone(),
                        addPatient.getAddress(),
                        "Enable"
                );
                boolean inserted2 = patientDAO.insertAccountPatient(
                        addPatient.getFullName(),
                        addPatient.getDob(),
                        addPatient.getGender(),
                        addPatient.getPhone(),
                        addPatient.getAddress(),
                        accountPatientId
                );

                if (inserted && inserted2) {
                    response.setStatus(HttpServletResponse.SC_CREATED); // 201
                    out.print("{\"message\":\"Patient created successfully\"}");
                    LOGGER.info("Patient created successfully: " + addPatient.getFullName());
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
                    out.print("{\"error\":\"Failed to create patient\"}");
                    LOGGER.severe("Failed to create patient: " + addPatient.getFullName());
                }

            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
                out.print("{\"error\":\"Unexpected server error\"}");
                LOGGER.severe("Unexpected error: " + e.getMessage());
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            out.print("{\"error\":\"Invalid request path\"}");
            LOGGER.info("Invalid request path: " + pathInfo);
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
                int patientId = Integer.parseInt(pathInfo.split("/")[1]);
                // Read JSON from request body
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = request.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                Patient updatedPatient = gson.fromJson(sb.toString(), Patient.class);

                // Validate required fields
                if (updatedPatient.getFullName() == null || updatedPatient.getFullName().trim().isEmpty() ||
                        updatedPatient.getDob() == null || updatedPatient.getDob().trim().isEmpty() ||
                        updatedPatient.getPhone() == null || updatedPatient.getPhone().trim().isEmpty() ||
                        updatedPatient.getAddress() == null || updatedPatient.getAddress().trim().isEmpty()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Missing required fields\"}");
                    out.flush();
                    return;
                }

                // Set default gender if not provided
                String gender = updatedPatient.getGender() != null ? updatedPatient.getGender() : "Unknown";

                // Create PatientDAO instance
                boolean success = patientDAO.updatePatient(
                        patientId,
                        updatedPatient.getFullName(),
                        updatedPatient.getDob(),
                        gender,
                        updatedPatient.getPhone(),
                        updatedPatient.getAddress()
                );

                if (success) {
                    // Create response object with updated patient data
                    Patient responsePatient = new Patient(
                            patientId,
                            updatedPatient.getFullName(),
                            updatedPatient.getDob(),
                            gender,
                            updatedPatient.getPhone(),
                            updatedPatient.getAddress(),
                            updatedPatient.getStatus()
                    );
                    out.print(gson.toJson(responsePatient));
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Patient not found or update failed\"}");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid ID\"}");
            } catch (JsonSyntaxException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid JSON format\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid request\"}");
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
                boolean removed = patientDAO.deletePatient(id);
                if (removed) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
                    LOGGER.info("Patient " + id + " deleted successfully");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
                    out.print("{\"error\":\"Patient not found\"}");
                    LOGGER.info("Patient " + id + " not found");
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                out.print("{\"error\":\"Invalid ID\"}");
                LOGGER.severe("Error processing Patient request: " + e.getMessage());
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
                out.print("{\"error\":\"Server error\"}");
                LOGGER.severe("Unexpected error: " + e.getMessage());
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            out.print("{\"error\":\"Invalid request\"}");
        }
        out.flush();
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
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