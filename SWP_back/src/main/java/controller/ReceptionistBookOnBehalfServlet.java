package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.ReceptionistDAO;
import model.Patient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@WebServlet("/api/receptionist/patients")
public class ReceptionistBookOnBehalfServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ReceptionistBookOnBehalfServlet.class.getName());
    private final ReceptionistDAO receptionistDAO = new ReceptionistDAO();
    private final Gson gson = new Gson();
    private static final String[] VALID_GENDERS_UI = {"Male", "Female", "Other", "All Gender"};
    private static final Map<String, String> GENDER_MAPPING = new HashMap<>() {{
        put("Male", "Nam");
        put("Female", "Nữ");
        put("Other", "Khác");
        put("All Gender", "All Gender");
    }};
    private static final String[] VALID_SORT_FIELDS = {"patient_id", "full_name", "dob", "phone"};

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
            String dob = request.getParameter("dob");
            String genderUI = request.getParameter("gender");
            int page = parseIntParam(request, "page", 1);
            int pageSize = parseIntParam(request, "pageSize", 10);
            String sortBy = request.getParameter("sortBy") != null ? request.getParameter("sortBy") : "patient_id";
            String sortOrder = request.getParameter("sortOrder") != null ? request.getParameter("sortOrder") : "ASC";

            LOGGER.info("Request params: page=" + page + ", pageSize=" + pageSize + ", searchQuery=" + searchQuery +
                    ", dob=" + dob + ", genderUI=" + genderUI + ", sortBy=" + sortBy + ", sortOrder=" + sortOrder);

            if (page < 1 || pageSize <= 0 || pageSize > 100) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_PAGE",
                        "Page must be >= 1 and pageSize must be between 1 and 100");
                return;
            }

            if (!isValidSortField(sortBy)) {
                sortBy = "patient_id";
            }

            if (!sortOrder.equalsIgnoreCase("ASC") && !sortOrder.equalsIgnoreCase("DESC")) {
                sortOrder = "ASC";
            }

            if (genderUI != null && !genderUI.isEmpty() && !isValidGender(genderUI)) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_GENDER",
                        "Gender must be one of: " + String.join(", ", VALID_GENDERS_UI));
                return;
            }

            String genderDB = (genderUI != null && !genderUI.isEmpty()) ? GENDER_MAPPING.get(genderUI) : null;

            ArrayList<Patient> patients = receptionistDAO.getPatients(
                    searchQuery, dob, genderDB, page, pageSize, sortBy, sortOrder);
            int totalPatients = receptionistDAO.countPatients(searchQuery, dob, genderDB);
            LOGGER.info("DAO returned " + patients.size() + " patients, total count: " + totalPatients +
                    ", Connection URL: " + receptionistDAO.getConnection().getMetaData().getURL());
            LOGGER.info("Full API response: " + gson.toJson(patients));

            JsonObject responseJson = new JsonObject();
            responseJson.add("patients", gson.toJsonTree(patients));
            responseJson.addProperty("totalPages", (int) Math.ceil((double) totalPatients / pageSize));
            responseJson.addProperty("currentPage", page);
            responseJson.addProperty("pageSize", pageSize);
            responseJson.addProperty("totalPatients", totalPatients);
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", patients.isEmpty() ? "No patients found" : "Patients fetched successfully");
            out.print(gson.toJson(responseJson));
        } catch (Exception e) {
            LOGGER.severe("Error processing GET request: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                    "Failed to fetch patients: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
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

            // Read JSON payload
            StringBuilder jsonBuffer = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuffer.append(line);
                }
            }
            String json = jsonBuffer.toString();
            LOGGER.info("Received JSON: " + json);

            // Parse JSON to Patient object
            Patient patient = gson.fromJson(json, Patient.class);

            // Validate input
            if (patient.getFullName() == null || patient.getFullName().trim().isEmpty()) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_NAME", "Patient name is required");
                return;
            }
            if (patient.getDob() == null || !patient.getDob().matches("\\d{4}-\\d{2}-\\d{2}")) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_DOB", "Invalid date of birth format (yyyy-MM-dd)");
                return;
            }
            if (patient.getGender() == null || !isValidGender(patient.getGender())) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_GENDER", "Gender must be one of: " + String.join(", ", VALID_GENDERS_UI));
                return;
            }
            if (patient.getPhone() == null || patient.getPhone().trim().isEmpty()) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_PHONE", "Phone number is required");
                return;
            }
            if (patient.getAddress() == null || patient.getAddress().trim().isEmpty()) {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ADDRESS", "Address is required");
                return;
            }

            // Map gender to database values
            patient.setGender(GENDER_MAPPING.get(patient.getGender()));
            LOGGER.info("Mapped gender: " + patient.getGender());

            // Add patient to database
            boolean success = receptionistDAO.addPatient(patient);

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", success);
            responseJson.addProperty("message", success ? "Patient added successfully" : "Failed to add patient");
            response.setStatus(success ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(responseJson));
            LOGGER.info("Patient addition result: " + (success ? "Success" : "Failed"));
        } catch (Exception e) {
            LOGGER.severe("Error processing POST request: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_JSON", "Invalid JSON format: " + e.getMessage());
        } finally {
            out.flush();
        }
    }


    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
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

    private boolean isValidGender(String gender) {
        for (String validGender : VALID_GENDERS_UI) {
            if (validGender.equalsIgnoreCase(gender)) {
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