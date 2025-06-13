package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.AccountPatientDAO;
import dal.AccountStaffDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.AccountPatient;
import model.AccountStaff;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author Datnq
 */
@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {
    private final AccountPatientDAO patientDAO = new AccountPatientDAO();
    private final AccountStaffDAO staffDAO = new AccountStaffDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");
        request.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        try {
            // Đọc dữ liệu JSON từ request
            BufferedReader reader = request.getReader();
            JsonObject loginData = gson.fromJson(reader, JsonObject.class);
            String username = loginData.get("username").getAsString();
            String password = loginData.get("password").getAsString();

            // Kiểm tra đăng nhập cho AccountPatient
            AccountPatient patient = patientDAO.checkLogin(username, password);
            if (patient != null) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("role", "patient");
                jsonResponse.addProperty("redirectUrl", "../dashboard/patient-dashboard.html");
                jsonResponse.add("user", gson.toJsonTree(patient));
                out.println(jsonResponse.toString());
                return;
            }

            // Kiểm tra đăng nhập cho AccountStaff
            AccountStaff staff = staffDAO.checkLogin(username, password);
            if (staff != null) {
                JsonObject jsonResponse = new JsonObject();
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("role", staff.getRole());
                jsonResponse.addProperty("redirectUrl", "../dashboard/index.html");
                jsonResponse.add("user", gson.toJsonTree(staff));
                out.println(jsonResponse.toString());
                return;
            }

            // Nếu không tìm thấy tài khoản
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("status", "error");
            errorResponse.addProperty("message", "Invalid username or password");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.println(errorResponse.toString());

        } catch (Exception e) {
            JsonObject errorResponse = new JsonObject();
            errorResponse.addProperty("status", "error");
            errorResponse.addProperty("message", "Internal server error");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println(errorResponse.toString());
            e.printStackTrace();
        }
    }
}