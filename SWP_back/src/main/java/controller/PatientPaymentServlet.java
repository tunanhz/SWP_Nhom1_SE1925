package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.PatientPaymentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.PatientPaymentDTO;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet("/api/patientPayment/*")
public class PatientPaymentServlet extends HttpServlet {
    private final PatientPaymentDAO patientPaymentDAO = new PatientPaymentDAO();
    private final Gson gson = new Gson();

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
        String pathInfo = request.getPathInfo();

        try (PrintWriter out = response.getWriter()) {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Validate required parameters
                if (request.getParameter("accountPatientId") == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Missing accountPatientId parameter\"}");
                    return;
                }

                // Parse parameters
                int accountPatientId = Integer.parseInt(request.getParameter("accountPatientId"));
                String issueDate = request.getParameter("issueDate");
                String status = request.getParameter("status");
                int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
                int pageSize = Integer.parseInt(request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "6");

                // Validate page and pageSize
                if (page < 1 || pageSize < 1) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Page and pageSize must be positive\"}");
                    return;
                }

                // Fetch invoices
                ArrayList<PatientPaymentDTO> invoices = patientPaymentDAO.getPatientInvoicesByAccountId(
                        accountPatientId, issueDate, status, page, pageSize
                );

                // Calculate total pages
                int totalInvoice = patientPaymentDAO.getTotalInvoices(accountPatientId, issueDate, status);
                int totalPages = (int) Math.ceil((double) totalInvoice / pageSize);

                // Build response
                JsonObject responseJson = new JsonObject();
                responseJson.add("invoices", gson.toJsonTree(invoices));
                responseJson.addProperty("totalPages", totalPages);

                // Write response
                out.print(gson.toJson(responseJson));
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Resource not found\"}");
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\": \"Invalid number format in parameters\"}");
            }
        } catch (Exception e) {
            log("Unexpected error: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\": \"Unexpected error occurred\"}");
            }
        }
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
}