package controller;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dal.PatientAppointmentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.AppointmentRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

@WebServlet("/api/patientCancel/*")
public class PatientCancelAppointmentServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PatientAppointmentServlet.class.getName());
    private final PatientAppointmentDAO appointmentDAO = new PatientAppointmentDAO();
    private final Gson gson = new Gson();


    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        try (PrintWriter out = response.getWriter()) {

            if (pathInfo == null || pathInfo.split("/").length != 2) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid URL format. Expected /api/patientCancel/{id}\"}");
                LOGGER.warning("Invalid pathInfo: " + pathInfo);
                return;
            }

            int id;
            try {
                id = Integer.parseInt(pathInfo.split("/")[1].trim());
                if (id <= 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"ID must be a positive integer\"}");
                    LOGGER.warning("Invalid ID provided: " + id);
                    return;
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid ID format\"}");
                LOGGER.warning("NumberFormatException for pathInfo: " + pathInfo);
                return;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String jsonData = sb.toString().trim();
            if (jsonData.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Request body is empty\"}");
                LOGGER.warning("Empty JSON body for appointment ID: " + id);
                return;
            }

            AppointmentRequest appointmentRequestUpdate;
            try {
                appointmentRequestUpdate = gson.fromJson(jsonData, AppointmentRequest.class);
                if (appointmentRequestUpdate == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid request body format\"}");
                    LOGGER.warning("Failed to parse JSON for appointment ID: " + id);
                    return;
                }
            } catch (JsonSyntaxException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid JSON format\"}");
                LOGGER.warning("JsonSyntaxException for appointment ID: " + id + ": " + e.getMessage());
                return;
            }

            String note = appointmentRequestUpdate.getNote();
            if (note == null) {
                note = "";
            } else if (note.length() > 255) { // Example max length
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Note exceeds maximum length of 255 characters\"}");
                LOGGER.warning("Note too long for appointment ID: " + id);
                return;
            }

            note = note.replaceAll("[<>\"&]", "");

            try {
                boolean updated = appointmentDAO.cancelAppointmentById(id, note);
                if (updated) {
                    response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    LOGGER.info("Appointment " + id + " cancelled successfully");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Appointment not found\"}");
                    LOGGER.warning("Appointment not found: " + id);
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"Failed to cancel appointment due to server error\"}");
                LOGGER.severe("Error cancelling appointment ID " + id + ": " + e.getMessage());
            }
        }
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


}