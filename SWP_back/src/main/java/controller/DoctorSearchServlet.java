package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import dal.DoctorSearchDAO;
import dto.DiagnosisResponseDTO;
import dto.MedicalRecordResponseDTO;
import dto.PatientSearchDTO;
import dto.AppointmentSearchDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Logger;

@WebServlet("/api/doctor/search/*")
public class DoctorSearchServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(DoctorSearchServlet.class.getName());
    private DoctorSearchDAO searchDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        searchDAO = new DoctorSearchDAO();
        // Configure Gson with custom date format
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, (com.google.gson.JsonSerializer<LocalDate>)
                        (src, typeOfSrc, context) -> context.serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .registerTypeAdapter(LocalDate.class, (com.google.gson.JsonDeserializer<LocalDate>)
                        (json, typeOfT, context) -> LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
                .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonSerializer<LocalDateTime>)
                        (src, typeOfSrc, context) -> context.serialize(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .registerTypeAdapter(LocalDateTime.class, (com.google.gson.JsonDeserializer<LocalDateTime>)
                        (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
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
            // Get parameters
            String query = request.getParameter("query");
            String accountStaffIdParam = request.getParameter("accountStaffId");

            // Validate required parameters
            if (query == null || query.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"query parameter is required\"}");
                return;
            }

            if (accountStaffIdParam == null || accountStaffIdParam.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"accountStaffId parameter is required\"}");
                return;
            }

            int accountStaffId;
            try {
                accountStaffId = Integer.parseInt(accountStaffIdParam);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid accountStaffId format\"}");
                return;
            }

            // Get doctor ID from account staff ID
            int doctorId = searchDAO.getDoctorIdByAccountStaffId(accountStaffId);
            if (doctorId == -1) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Doctor not found for the given account\"}");
                return;
            }

            LOGGER.info("Performing search for doctorId=" + doctorId + ", query=" + query);

            // Perform searches
            ArrayList<PatientSearchDTO> patients = searchDAO.searchPatients(query, doctorId);
            ArrayList<MedicalRecordResponseDTO> medicalRecords = searchDAO.searchMedicalRecords(query, doctorId);
            ArrayList<AppointmentSearchDTO> appointments = searchDAO.searchAppointments(query, doctorId);
            ArrayList<DiagnosisResponseDTO> diagnosis = searchDAO.searchDiagnosis(query, doctorId);

            // Build response JSON
            JsonObject responseJson = new JsonObject();
            responseJson.add("patients", gson.toJsonTree(patients));
            responseJson.add("medicalRecords", gson.toJsonTree(medicalRecords));
            responseJson.add("appointments", gson.toJsonTree(appointments));
            responseJson.add("diagnosis", gson.toJsonTree(diagnosis));
            responseJson.addProperty("totalResults",
                    patients.size() + medicalRecords.size() + appointments.size() + diagnosis.size());

            out.print(gson.toJson(responseJson));

            LOGGER.info("Search completed successfully. Results: " +
                    patients.size() + " patients, " +
                    medicalRecords.size() + " records, " +
                    appointments.size() + " appointments, " +
                    diagnosis.size() + " diagnosis");

        } catch (Exception e) {
            LOGGER.severe("Error processing search request: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to perform search\"}");
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
