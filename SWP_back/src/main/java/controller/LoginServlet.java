package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // Parse query parameters
            String username = request.getParameter("username");
            String password = request.getParameter("pwd");

            if (username == null || password == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"username and password are required\"}");
                return;
            }

            // Log request for debugging
            LOGGER.info("Attempting login for username: " + username);

            // Authenticate user
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