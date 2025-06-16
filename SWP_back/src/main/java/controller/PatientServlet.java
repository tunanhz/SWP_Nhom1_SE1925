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

@WebServlet("/api/patient/*")
public class PatientServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCORSHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
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
                PatientDAO patientDAO = new PatientDAO();
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
                            updatedPatient.getAddress()
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

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
}