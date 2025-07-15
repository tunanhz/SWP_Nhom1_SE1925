package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dal.MedicalRecordsDAO;
import dto.MedicalRecordResponseDTO;
import dto.MedicalRecordRequestDTO;
import model.MedicineRecords;

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

@WebServlet("/api/doctor/medical-records/*")
public class MedicalRecordsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(MedicalRecordsServlet.class.getName());
    private MedicalRecordsDAO medicalRecordsDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        medicalRecordsDAO = new MedicalRecordsDAO();
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
            // Check if medical record ID is provided in URL path
            String pathInfo = request.getPathInfo();

            if (pathInfo != null && pathInfo.length() > 1) {
                // Handle GET by medical record ID: /api/doctor/medical-records/{id}
                handleGetMedicalRecordById(request, response, out, pathInfo);
            } else {
                // Handle GET by doctor ID with pagination: /api/doctor/medical-records?doctorId=X
                handleGetMedicalRecordsByDoctorId(request, response, out);
            }

        } catch (Exception e) {
            LOGGER.severe("Error processing medical records request: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to fetch medical records data\"}");
        } finally {
            out.flush();
        }
    }

    /**
     * Handle GET request for a single medical record by ID
     */
    private void handleGetMedicalRecordById(HttpServletRequest request, HttpServletResponse response,
                                            PrintWriter out, String pathInfo) throws Exception {
        String recordIdStr = pathInfo.substring(1); // Remove leading "/"
        int recordId;

        try {
            recordId = Integer.parseInt(recordIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid medical record ID format\"}");
            return;
        }

        LOGGER.info("Fetching medical record by ID=" + recordId);

        // Get detailed medical record by ID
        MedicalRecordResponseDTO record = medicalRecordsDAO.getDetailedMedicalRecordById(recordId);

        if (record == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\": \"Medical record not found\"}");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        out.print(gson.toJson(record));

        LOGGER.info("Successfully returned medical record with ID: " + recordId);
    }

    /**
     * Handle GET request for medical records by doctor ID with pagination
     */
    private void handleGetMedicalRecordsByDoctorId(HttpServletRequest request, HttpServletResponse response,
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

        LOGGER.info("Fetching medical records for doctorId=" + doctorId +
                ", page=" + page + ", pageSize=" + pageSize);

        // Get medical records data
        ArrayList<MedicalRecordResponseDTO> recordsList = medicalRecordsDAO.getMedicalRecordsByDoctorId(doctorId, page, pageSize);
        int totalRecords = medicalRecordsDAO.countMedicalRecordsByDoctorId(doctorId);
        int totalPages = (int) Math.ceil((double) totalRecords / pageSize);

        // Build response JSON
        JsonObject responseJson = new JsonObject();
        responseJson.add("records", gson.toJsonTree(recordsList));
        responseJson.addProperty("totalPages", totalPages);
        responseJson.addProperty("currentPage", page);
        responseJson.addProperty("pageSize", pageSize);
        responseJson.addProperty("totalRecords", totalRecords);

        response.setStatus(HttpServletResponse.SC_OK);
        out.print(gson.toJson(responseJson));

        LOGGER.info("Successfully returned " + recordsList.size() + " medical records");
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

            // Parse JSON to MedicalRecordRequestDTO
            MedicalRecordRequestDTO medicalRecordRequest = gson.fromJson(sb.toString(), MedicalRecordRequestDTO.class);

            // Validate required fields
            if (medicalRecordRequest.getPatientId() <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"patientId is required and must be positive\"}");
                return;
            }

            LOGGER.info("Creating medical record for patientId=" + medicalRecordRequest.getPatientId());

            // Create medical record
            MedicineRecords createdRecord = medicalRecordsDAO.createMedicalRecord(medicalRecordRequest);

            response.setStatus(HttpServletResponse.SC_CREATED);
            out.print(gson.toJson(createdRecord));

            LOGGER.info("Successfully created medical record with ID: " + createdRecord.getMedicineRecordId());

        } catch (SQLException e) {
            LOGGER.severe("Database error creating medical record: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Database error: " + e.getMessage().replace("\"", "\\\"") + "\"}");
        } catch (Exception e) {
            LOGGER.severe("Error creating medical record: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to create medical record\"}");
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
            // Extract record ID from URL path
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Medical record ID is required in URL path\"}");
                return;
            }

            String recordIdStr = pathInfo.substring(1); // Remove leading "/"
            int recordId;
            try {
                recordId = Integer.parseInt(recordIdStr);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid medical record ID format\"}");
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

            // Parse JSON to MedicalRecordRequestDTO
            MedicalRecordRequestDTO medicalRecordRequest = gson.fromJson(sb.toString(), MedicalRecordRequestDTO.class);

            // Validate required fields
            if (medicalRecordRequest.getPatientId() <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"patientId is required and must be positive\"}");
                return;
            }

            LOGGER.info("Updating medical record ID=" + recordId);

            // Check if medical record exists
            MedicineRecords existingRecord = medicalRecordsDAO.getMedicalRecordById(recordId);
            if (existingRecord == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Medical record not found\"}");
                return;
            }

            // Update medical record
            MedicineRecords updatedRecord = medicalRecordsDAO.updateMedicalRecord(recordId, medicalRecordRequest);

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(updatedRecord));

            LOGGER.info("Successfully updated medical record with ID: " + recordId);

        } catch (SQLException e) {
            LOGGER.severe("Database error updating medical record: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Database error: " + e.getMessage().replace("\"", "\\\"") + "\"}");
        } catch (Exception e) {
            LOGGER.severe("Error updating medical record: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to update medical record\"}");
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
            // Extract record ID from URL path
            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.length() <= 1) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Medical record ID is required in URL path\"}");
                return;
            }

            String recordIdStr = pathInfo.substring(1); // Remove leading "/"
            int recordId;
            try {
                recordId = Integer.parseInt(recordIdStr);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid medical record ID format\"}");
                return;
            }

            LOGGER.info("Deleting medical record ID=" + recordId);

            // Check if medical record exists
            MedicineRecords existingRecord = medicalRecordsDAO.getMedicalRecordById(recordId);
            if (existingRecord == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Medical record not found\"}");
                return;
            }

            // Delete medical record
            boolean deleted = medicalRecordsDAO.deleteMedicalRecord(recordId);

            if (deleted) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.print("{\"message\": \"Medical record deleted successfully\"}");
                LOGGER.info("Successfully deleted medical record with ID: " + recordId);
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\": \"Failed to delete medical record\"}");
            }

        } catch (SQLException e) {
            LOGGER.severe("Database error deleting medical record: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Database error: " + e.getMessage().replace("\"", "\\\"") + "\"}");
        } catch (Exception e) {
            LOGGER.severe("Error deleting medical record: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to delete medical record\"}");
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
