package controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dal.AdminBusinessReportDAO;
import dto.PatientRecordDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/adminBusinessReport/*")
public class AdminBusinessPatientRecordServlet extends HttpServlet {
    private AdminBusinessReportDAO patientRecordDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        patientRecordDAO = new AdminBusinessReportDAO();
        // Configure Gson with custom TypeAdapters for LocalDate and LocalDateTime
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Get filter parameters
            String patientName = request.getParameter("patientName");
            String startDateParam = request.getParameter("startDate");
            String endDateParam = request.getParameter("endDate");
            String gender = request.getParameter("gender");

            // Get pagination parameters
            int page = 1;
            int size = 10; // Default size
            String pageParam = request.getParameter("page");
            String sizeParam = request.getParameter("size");
            if (pageParam != null && !pageParam.isEmpty()) {
                page = Integer.parseInt(pageParam);
                if (page < 1) page = 1;
            }
            if (sizeParam != null && !sizeParam.isEmpty()) {
                size = Integer.parseInt(sizeParam);
                if (size < 1) size = 10;
            }

            // Parse date parameters
            LocalDate startDate = null;
            LocalDate endDate = null;
            DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
            if (startDateParam != null && !startDateParam.isEmpty()) {
                startDate = LocalDate.parse(startDateParam, dateFormatter);
            }
            if (endDateParam != null && !endDateParam.isEmpty()) {
                endDate = LocalDate.parse(endDateParam, dateFormatter);
            }

            // Handle empty parameters by passing null/empty values to DAO
            patientName = (patientName != null && !patientName.trim().isEmpty()) ? patientName.trim() : null;
            gender = (gender != null && !gender.trim().isEmpty()) ? gender.trim() : null;

            // Get records and count
            List<PatientRecordDTO> records = patientRecordDAO.getPatientRecordsWithFilters(
                    patientName, startDate, endDate, gender, page, size);
            int totalRecords = patientRecordDAO.countPatientRecordsWithFilters(
                    patientName, startDate, endDate, gender);
            int totalPages = (totalRecords + size - 1) / size;

            // Build response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("records", records);
            responseData.put("totalRecords", totalRecords);
            responseData.put("totalPages", totalPages);

            String json = gson.toJson(responseData);
            response.getWriter().write(json);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\": \"Invalid pagination parameter format\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Server error: " + e.getMessage() + "\"}");
        }
    }

    // TypeAdapter for LocalDate
    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(formatter.format(value));
            }
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            String date = in.nextString();
            return LocalDate.parse(date, formatter);
        }
    }

    // TypeAdapter for LocalDateTime
    private static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(formatter.format(value));
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            String dateTime = in.nextString();
            return LocalDateTime.parse(dateTime, formatter);
        }
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept");
        response.setHeader("Access-Control-Max-Age", "86400");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCORSHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}