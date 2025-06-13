package controller;

import com.google.gson.JsonObject;
import dal.PrescriptionDAO;
import model.Prescription;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

                ArrayList<Prescription> prescriptions = dao.getPrescriptionDetailById(prescriptionId);
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