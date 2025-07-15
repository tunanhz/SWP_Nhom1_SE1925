package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dal.DiagnosisDAO;
import dto.DiagnosisResponseDTO;
import dto.DiagnosisRequestDTO;
import model.Diagnosis;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Logger;

@WebServlet("/api/doctor/diagnosis/*")
public class DiagnosisServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DiagnosisServlet.class.getName());
    private DiagnosisDAO diagnosisDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        diagnosisDAO = new DiagnosisDAO();
        // Configure Gson with custom date format
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, (com.google.gson.JsonSerializer<LocalDate>)
                        (src, typeOfSrc, context) -> context.serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .registerTypeAdapter(LocalDate.class, (com.google.gson.JsonDeserializer<LocalDate>)
                        (json, typeOfT, context) -> LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
                .create();
    }

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

        PrintWriter out = response.getWriter();

        try {
            // Check if diagnosis ID is provided in URL path
            String pathInfo = request.getPathInfo();

            if (pathInfo != null && pathInfo.length() > 1) {
                // Handle GET by diagnosis ID: /api/doctor/diagnosis/{id}
                handleGetDiagnosisById(request, response, out, pathInfo);
            } else {
                // Handle GET by doctor ID with pagination: /api/doctor/diagnosis?doctorId=X
                handleGetDiagnosisByDoctorId(request, response, out);
            }

        } catch (Exception e) {
            LOGGER.severe("Error processing diagnosis request: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to fetch diagnosis data\"}");
        } finally {
            out.flush();
        }
    }

    /**
     * Handle GET request for a single diagnosis by ID
     */
    private void handleGetDiagnosisById(HttpServletRequest request, HttpServletResponse response,
                                        PrintWriter out, String pathInfo) throws Exception {
        String diagnosisIdStr = pathInfo.substring(1); // Remove leading "/"
        int diagnosisId;

        try {
            diagnosisId = Integer.parseInt(diagnosisIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid diagnosis ID format\"}");
            return;
        }

        LOGGER.info("Fetching diagnosis by ID=" + diagnosisId);

        // Get diagnosis by ID
        Diagnosis diagnosis = diagnosisDAO.getDiagnosisById(diagnosisId);

        if (diagnosis == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\": \"Diagnosis not found\"}");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        out.print(gson.toJson(diagnosis));

        LOGGER.info("Successfully returned diagnosis with ID: " + diagnosisId);
    }

    /**
     * Handle GET request for diagnoses by doctor ID with pagination
     */
    private void handleGetDiagnosisByDoctorId(HttpServletRequest request, HttpServletResponse response,
                                              PrintWriter out) throws Exception {
        // Get parameters
        String doctorIdParam = request.getParameter("doctorId");
        String pageParam = request.getParameter("page");
        String pageSizeParam = request.getParameter("pageSize");

        // Validate required parameters
        if (doctorIdParam == null || doctorIdParam.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"doctorId parameter is required\"}");
            return;
        }

        // Parse parameters with defaults
        int doctorId;
        int page = 1;
        int pageSize = 10;

        try {
            doctorId = Integer.parseInt(doctorIdParam);
            if (pageParam != null && !pageParam.trim().isEmpty()) {
                page = Integer.parseInt(pageParam);
            }
            if (pageSizeParam != null && !pageSizeParam.trim().isEmpty()) {
                pageSize = Integer.parseInt(pageSizeParam);
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid number format in parameters\"}");
            return;
        }

        // Validate page and pageSize
        if (page < 1) page = 1;
        if (pageSize < 1) pageSize = 10;
        if (pageSize > 100) pageSize = 100; // Limit max page size

        LOGGER.info("Fetching diagnosis for doctorId=" + doctorId +
                ", page=" + page + ", pageSize=" + pageSize);

        // Get diagnosis data
        ArrayList<DiagnosisResponseDTO> diagnosisList = diagnosisDAO.getDiagnosisByDoctorId(doctorId, page, pageSize);
        int totalRecords = diagnosisDAO.countDiagnosisByDoctorId(doctorId);
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

        // Build response JSON
        JsonObject responseJson = new JsonObject();
        responseJson.add("diagnosis", gson.toJsonTree(diagnosisList));
        responseJson.addProperty("totalPages", totalPages);
        responseJson.addProperty("currentPage", page);
        responseJson.addProperty("pageSize", pageSize);
        responseJson.addProperty("totalRecords", totalRecords);

        response.setStatus(HttpServletResponse.SC_OK);
        out.print(gson.toJson(responseJson));

        LOGGER.info("Successfully returned " + diagnosisList.size() + " diagnosis records");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        try {
            // Read JSON from request body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            if (sb.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Request body is empty\"}");
                return;
            }

            // Parse JSON to DiagnosisRequestDTO
            DiagnosisRequestDTO diagnosisRequest = gson.fromJson(sb.toString(), DiagnosisRequestDTO.class);

            // Validate required fields
            if (diagnosisRequest.getDoctorId() <= 0 || diagnosisRequest.getMedicineRecordId() <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"doctorId and medicineRecordId are required and must be positive\"}");
                return;
            }

            if (diagnosisRequest.getDisease() == null || diagnosisRequest.getDisease().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"disease field is required\"}");
                return;
            }

            LOGGER.info("Creating diagnosis for doctorId=" + diagnosisRequest.getDoctorId() +
                    ", medicineRecordId=" + diagnosisRequest.getMedicineRecordId());

            // Create diagnosis
            Diagnosis createdDiagnosis = diagnosisDAO.createDiagnosis(diagnosisRequest);

            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(gson.toJson(createdDiagnosis));

            LOGGER.info("Successfully created diagnosis with ID: " + createdDiagnosis.getDiagnosisId());

        } catch (SQLException e) {
            LOGGER.severe("Database error creating diagnosis: " + e.getMessage());

            // Check for specific business rule violations
            if (e.getMessage().contains("does not have access")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            } else if (e.getMessage().contains("not found")) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Database error: " + e.getMessage().replace("\"", "\\\"") + "\"}");
            }
        } catch (Exception e) {
            LOGGER.severe("Error creating diagnosis: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to create diagnosis\"}");
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        try {
            // Extract diagnosis ID from URL path
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Diagnosis ID is required in URL path\"}");
                return;
            }

            String diagnosisIdStr = pathInfo.substring(1); // Remove leading "/"
            int diagnosisId;
            try {
                diagnosisId = Integer.parseInt(diagnosisIdStr);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid diagnosis ID format\"}");
                return;
            }

            // Read JSON from request body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            if (sb.length() == 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Request body is empty\"}");
                return;
            }

            // Parse JSON to DiagnosisRequestDTO
            DiagnosisRequestDTO diagnosisRequest = gson.fromJson(sb.toString(), DiagnosisRequestDTO.class);

            // Validate required fields
            if (diagnosisRequest.getDoctorId() <= 0 || diagnosisRequest.getMedicineRecordId() <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"doctorId and medicineRecordId are required and must be positive\"}");
                return;
            }

            if (diagnosisRequest.getDisease() == null || diagnosisRequest.getDisease().trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"disease field is required\"}");
                return;
            }

            LOGGER.info("Updating diagnosis ID=" + diagnosisId);

            // Check if diagnosis exists
            Diagnosis existingDiagnosis = diagnosisDAO.getDiagnosisById(diagnosisId);
            if (existingDiagnosis == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Diagnosis not found\"}");
                return;
            }

            // Update diagnosis
            Diagnosis updatedDiagnosis = diagnosisDAO.updateDiagnosis(diagnosisId, diagnosisRequest);

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(updatedDiagnosis));

            LOGGER.info("Successfully updated diagnosis with ID: " + diagnosisId);

        } catch (SQLException e) {
            LOGGER.severe("Database error updating diagnosis: " + e.getMessage());

            // Check for specific business rule violations
            if (e.getMessage().contains("does not have access") || e.getMessage().contains("does not have permission")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            } else if (e.getMessage().contains("not found")) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Database error: " + e.getMessage().replace("\"", "\\\"") + "\"}");
            }
        } catch (Exception e) {
            LOGGER.severe("Error updating diagnosis: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to update diagnosis\"}");
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        try {
            // Extract diagnosis ID from URL path
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Diagnosis ID is required in URL path\"}");
                return;
            }

            String diagnosisIdStr = pathInfo.substring(1); // Remove leading "/"
            int diagnosisId;
            try {
                diagnosisId = Integer.parseInt(diagnosisIdStr);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid diagnosis ID format\"}");
                return;
            }

            LOGGER.info("Deleting diagnosis ID=" + diagnosisId);

            // Check if diagnosis exists
            Diagnosis existingDiagnosis = diagnosisDAO.getDiagnosisById(diagnosisId);
            if (existingDiagnosis == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Diagnosis not found\"}");
                return;
            }

            // Delete diagnosis
            boolean deleted = diagnosisDAO.deleteDiagnosis(diagnosisId);

            if (deleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"message\": \"Diagnosis deleted successfully\"}");
                LOGGER.info("Successfully deleted diagnosis with ID: " + diagnosisId);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Failed to delete diagnosis\"}");
            }

        } catch (SQLException e) {
            LOGGER.severe("Database error deleting diagnosis: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Database error: " + e.getMessage().replace("\"", "\\\"") + "\"}");
        } catch (Exception e) {
            LOGGER.severe("Error deleting diagnosis: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to delete diagnosis\"}");
        } finally {
            out.flush();
        }
    }

    /**
     * Set CORS headers for cross-origin requests
     */
    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");
    }
}
