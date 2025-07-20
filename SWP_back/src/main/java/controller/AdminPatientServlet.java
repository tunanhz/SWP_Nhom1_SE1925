package controller;

import com.google.gson.Gson;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/api/AdminBusinessPatient/*")
public class AdminPatientServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PatientAppointmentServlet.class.getName());
    private static final PatientDAO patientDAO = new PatientDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCORSHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
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

                try {
                    LocalDate dob = LocalDate.parse(addPatient.getDob(), DateTimeFormatter.ISO_LOCAL_DATE);
                    LocalDate today = LocalDate.now();
                    if (dob.isAfter(today)) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                        out.print("{\"error\":\"Date of birth cannot be in the future\"}");
                        return;
                    }
                } catch (DateTimeParseException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
                    out.print("{\"error\":\"Invalid date of birth format\"}");
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
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 400
                        out.print("{\"message\":\"Patient already\"}");
                        LOGGER.info("Patient already : " + existingPatient.getFullName());
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

                if (inserted) {
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
                int id = Integer.parseInt(pathInfo.split("/")[1]);
                Patient patient = patientDAO.getPatientByPatientId(id);
                if (patient != null) {
                    if (patient.getStatus().equalsIgnoreCase("Enable")) {
                        boolean disable = patientDAO.deletePatient(id);
                        if (disable) {
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
                            LOGGER.info("Patient " + id + " disable successfully");
                        } else {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
                            out.print("{\"error\":\"Patient not found\"}");
                            LOGGER.info("Patient " + id + " not found");
                        }
                    } else if (patient.getStatus().equalsIgnoreCase("Disable")) {
                        boolean enabled = patientDAO.enablePatient(id);
                        if (enabled) {
                            response.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
                            LOGGER.info("Patient " + id + " enabled successfully");
                        } else {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
                            out.print("{\"error\":\"Patient not found\"}");
                            LOGGER.info("Patient " + id + " not found");
                        }
                    }
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
}
