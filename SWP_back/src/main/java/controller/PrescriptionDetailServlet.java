package controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dal.PrescriptionDAO;
import dto.PrescriptionDTO;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

@WebServlet("/api/prescriptionDetail/*")
public class PrescriptionDetailServlet extends HttpServlet {

    private final PrescriptionDAO dao = new PrescriptionDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCORSHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCORSHeaders(resp);
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.length() > 1) {
                // Extract prescriptionId from path (e.g., /123)
                String idStr = pathInfo.substring(1); // Remove leading "/"
                int prescriptionId = Integer.parseInt(idStr);

                if (prescriptionId < 1) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid prescription ID");
                    return;
                }

                ArrayList<PrescriptionDTO> prescriptions = dao.getPrescriptionDetailById(prescriptionId);
                if (prescriptions == null || prescriptions.isEmpty()) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Prescription not found");
                    return;
                }
                out.println(gson.toJson(prescriptions));
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Prescription ID is required");
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid prescription ID format");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCORSHeaders(resp);
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.length() > 1) {
                // Extract prescriptionId from path (e.g., /123)
                String idStr = pathInfo.substring(1); // Remove leading "/"
                int prescriptionId = Integer.parseInt(idStr);

                if (prescriptionId < 1) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid prescription ID");
                    return;
                }

                // Read request body
                BufferedReader reader = req.getReader();
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                String newStatus = json.has("status") ? json.get("status").getAsString() : null;

                // Validate status
                if (newStatus == null || !isValidStatus(newStatus)) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid or missing status. Must be Pending, Dispensed, or Cancelled.");
                    return;
                }

                // Update status in database
                boolean updated = dao.updatePrescriptionStatus(prescriptionId, newStatus, 6);
                if (!updated) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Prescription not found or update failed");
                    return;
                }

                JsonObject response = new JsonObject();
                response.addProperty("message", "Prescription status updated successfully");
                out.println(gson.toJson(response));
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Prescription ID is required");
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid prescription ID format");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }

    private boolean isValidStatus(String status) {
        return status != null && (status.equals("Pending") || status.equals("Dispensed") || status.equals("Cancelled"));
    }


    private void setCORSHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        resp.getWriter().println(gson.toJson(error));
    }
}