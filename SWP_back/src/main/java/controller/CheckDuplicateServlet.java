package controller;

import dal.AccountPatientDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

@WebServlet({"/api/checkUsername", "/api/checkEmail"})
public class CheckDuplicateServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CheckDuplicateServlet.class.getName());
    private final AccountPatientDAO patientDAO = new AccountPatientDAO();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String path = request.getServletPath();
            String value = request.getParameter("value");

            if (value == null || value.trim().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"exists\": false, \"error\": \"Value is required\"}");
                return;
            }

            boolean exists;
            if ("/api/checkUsername".equals(path)) {
                exists = patientDAO.isUsernameExists(value);
                LOGGER.info("Checked username: " + value + ", exists: " + exists);
            } else if ("/api/checkEmail".equals(path)) {
                exists = patientDAO.isEmailExists(value);
                LOGGER.info("Checked email: " + value + ", exists: " + exists);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"exists\": false, \"error\": \"Invalid endpoint\"}");
                return;
            }

            out.print("{\"exists\": " + exists + "}");
        } catch (Exception e) {
            LOGGER.severe("Error checking duplicate: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"exists\": false, \"error\": \"Server error: " + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }
}