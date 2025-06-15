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
            String username = jsonObject.get("username").getAsString();
            String password = jsonObject.get("password").getAsString();

            LOGGER.info("Received POST request at: " + request.getRequestURI());
            LOGGER.info("Parameters - username: " + username + ", password: " + password);
            System.out.println("Debug: Username = " + username + ", Password = " + password);

            if (username == null || password == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"username and password are required\"}");
                return;
            }

            LOGGER.info("Attempting login for username: " + username);

            Account account = authenticate(username, password);

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

    private Account authenticate(String username, String password) {
        Account account = null;

        try {
            AccountStaff staff = staffDAO.getAccountByUsernameAndPassword(username, password);
            if (staff != null) {
                account = staff;
            }

            if (account == null) {
                AccountPatient patient = patientDAO.getAccountByUsernameAndPassword(username, password);
                if (patient != null) {
                    account = patient;
                }
            }

            if (account == null) {
                AccountPharmacist pharmacist = pharmacistDAO.getAccountByUsernameAndPassword(username, password);
                if (pharmacist != null) {
                    account = pharmacist;
                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException("Error during authentication: " + e.getMessage());
        }

        return account;
    }
}