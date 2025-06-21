package controller;

import com.google.gson.Gson;
import dal.AccountPatientDAO;
import model.AccountPatient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;


@WebServlet("/api/signupPatient")
public class SignupPatientServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(SignupPatientServlet.class.getName());
    private final AccountPatientDAO patientDAO = new AccountPatientDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
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
            // Retrieve data from the form
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String email = request.getParameter("email");
            String imgUrl = request.getParameter("img");

            LOGGER.info("Received request - username: " + (username != null ? username : "null") +
                    ", email: " + (email != null ? email : "null") +
                    ", imgUrl: " + (imgUrl != null ? imgUrl : "null"));

            if (username == null || username.trim().isEmpty() ||
                    password == null || password.trim().isEmpty() ||
                    email == null || email.trim().isEmpty() ||
                    imgUrl == null || imgUrl.trim().isEmpty()) {
                LOGGER.warning("Validation failed - Missing or empty required fields");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"success\": false, \"error\": \"All fields are required\"}");
                return;
            }

            if (patientDAO.isUsernameExists(username)) {
                LOGGER.warning("Validation failed - Username already exists: " + username);
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print("{\"success\": false, \"error\": \"Username already exists\"}");
                return;
            }

            if (patientDAO.isEmailExists(email)) {
                LOGGER.warning("Validation failed - Email already exists: " + email);
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                out.print("{\"success\": false, \"error\": \"Email already exists\"}");
                return;
            }



            AccountPatient patient = new AccountPatient();
            patient.setUsername(username);
            patient.setPassword(password);
            patient.setEmail(email);
            patient.setImg(imgUrl);

            LOGGER.info("Patient object created - imgUrl: " + patient.getImg());

            patientDAO.addPatientWithStatus(patient, "Enable");

            LOGGER.info("Patient registered successfully: " + username);
            out.print("{\"success\": true, \"message\": \"Patient registered successfully\"}");
        } catch (Exception e) {
            LOGGER.severe("Exception during signup: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\": false, \"error\": \"Failed to register patient: " + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }
}