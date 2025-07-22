package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dal.AccountStaffDAO;
import dal.AccountPatientDAO;
import dal.AccountPharmacistDAO;
import model.Account;
import model.AccountStaff;
import model.AccountPatient;
import model.AccountPharmacist;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

@WebServlet("/api/loginServlet")
public class LoginServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());
    private final AccountStaffDAO staffDAO = new AccountStaffDAO();
    private final AccountPatientDAO patientDAO = new AccountPatientDAO();
    private final AccountPharmacistDAO pharmacistDAO = new AccountPharmacistDAO();
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
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Đọc body JSON
            StringBuilder jsonBody = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBody.append(line);
                }
            }
            JsonObject jsonObject = JsonParser.parseString(jsonBody.toString()).getAsJsonObject();
            String identifier = jsonObject.get("identifier").getAsString();
            String password = jsonObject.get("password").getAsString();

            LOGGER.info("Received POST request at: " + request.getRequestURI());
            LOGGER.info("Parameters - identifier: " + identifier + ", password: " + password);
            System.out.println("Debug: identifier = " + identifier + ", Password = " + password);

            if (identifier == null || password == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"identifier and password are required\"}");
                return;
            }

            LOGGER.info("Attempting login for username: " + identifier);

            Account account = authenticate(identifier, password);

            if (account != null) {
                request.getSession().setAttribute("account", account);
                JsonObject responseJson = new JsonObject();
                responseJson.add("account", gson.toJsonTree(account));
                responseJson.addProperty("success", true);
                responseJson.addProperty("role", account.getRole());
                responseJson.addProperty("message", "Login successful");
                out.print(gson.toJson(responseJson));
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\": \"Invalid username or password\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to process login\"}");
            LOGGER.severe("Error processing login: " + e.getMessage());
        } finally {
            out.flush();
        }
    }

    private Account authenticate(String identifier, String password) {
        Account account = null;

        try {
//            AccountStaff staff = staffDAO.getAccountByUsernameAndPassword(identifier, password);
            AccountStaff staff = staffDAO.getAccountByUsernameOrEmail(identifier);
            if (staff != null && BCrypt.checkpw(password, staff.getPassword())) {
                account = staff;
            }

            if (account == null) {
//              AccountPatient patient = patientDAO.getAccountByUsernameOrEmailAndPassword(identifier, password);
                AccountPatient patient = patientDAO.getAccountByUsernameOrEmail(identifier);
                if (patient != null && BCrypt.checkpw(password, patient.getPassword())) {
                    account = patient;
                }
            }

            if (account == null) {
 //               AccountPharmacist pharmacist = pharmacistDAO.getAccountByUsernameAndPassword(identifier, password);
                AccountPharmacist pharmacist = pharmacistDAO.getAccountByUsername(identifier);
                if (pharmacist != null && BCrypt.checkpw(password, pharmacist.getPassword())) {
                    account = pharmacist;
                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Error during authentication: " + e.getMessage());
        }

        return account;
    }
}