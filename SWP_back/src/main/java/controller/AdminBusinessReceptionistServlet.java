package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.AdminBusinessDAO;
import model.AccountStaff;
import model.Receptionist;
import dto.ReceptionistResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

@WebServlet("/api/admin/receptionist/*")
public class AdminBusinessReceptionistServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AdminBusinessReceptionistServlet.class.getName());
    private final AdminBusinessDAO adminBusinessDAO = new AdminBusinessDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCORSHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            LOGGER.info("GET request from " + request.getRemoteAddr() + " for " + request.getRequestURI() +
                    "?" + (request.getQueryString() != null ? request.getQueryString() : ""));

            String pathInfo = request.getPathInfo();
            if ("/list".equals(pathInfo)) {
                String searchQuery = request.getParameter("searchQuery");
                String statusFilter = request.getParameter("statusFilter");
                int page = parseIntParam(request, "page", 1);
                int pageSize = parseIntParam(request, "pageSize", 10);

                if (page < 1 || pageSize <= 0 || pageSize > 100) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_PAGE",
                            "Page must be >= 1 and pageSize must be between 1 and 100");
                    return;
                }

                long startTime = System.currentTimeMillis();
                ArrayList<ReceptionistResponseDTO> receptionists = adminBusinessDAO.getReceptionists(searchQuery, statusFilter, page, pageSize);
                int totalReceptionists = adminBusinessDAO.countReceptionists(searchQuery, statusFilter);
                LOGGER.info("Servlet processing time: " + (System.currentTimeMillis() - startTime) + "ms");

                JsonObject responseJson = new JsonObject();
                responseJson.add("receptionists", gson.toJsonTree(receptionists));
                responseJson.addProperty("totalPages", (int) Math.ceil((double) totalReceptionists / pageSize));
                responseJson.addProperty("currentPage", page);
                responseJson.addProperty("pageSize", pageSize);
                responseJson.addProperty("totalReceptionists", totalReceptionists);
                responseJson.addProperty("success", true);
                responseJson.addProperty("message", receptionists.isEmpty() ? "Không tìm thấy nhân viên lễ tân" : "Lễ tân đã được đón thành công");
                out.print(gson.toJson(responseJson));
            } else if (pathInfo != null && pathInfo.matches("/\\d+")) {
                int accountStaffId = Integer.parseInt(pathInfo.substring(1));
                long startTime = System.currentTimeMillis();
                ReceptionistResponseDTO receptionist = adminBusinessDAO.getReceptionistById(accountStaffId);
                LOGGER.info("Get receptionist by ID time: " + (System.currentTimeMillis() - startTime) + "ms");

                if (receptionist != null) {
                    JsonObject responseJson = new JsonObject();
                    responseJson.add("receptionist", gson.toJsonTree(receptionist));
                    responseJson.addProperty("success", true);
                    out.print(gson.toJson(responseJson));
                } else {
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Receptionist not found");
                }
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "INVALID_ENDPOINT", "Endpoint not found");
            }
        } catch (NumberFormatException e) {
            LOGGER.severe("Number format error: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_INPUT",
                    "Invalid number format for account staff ID");
        } catch (Exception e) {
            LOGGER.severe("Error processing GET request: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                    "Failed to fetch receptionists: " + e.getMessage());
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            LOGGER.info("POST request from " + request.getRemoteAddr() + " for " + request.getRequestURI());

            String pathInfo = request.getPathInfo();
            StringBuilder jsonBody = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBody.append(line);
                }
            }

            JsonObject jsonObject = gson.fromJson(jsonBody.toString(), JsonObject.class);

            if ("/create".equals(pathInfo)) {
                AccountStaff account = new AccountStaff();
                account.setUserName(jsonObject.get("username").getAsString());
                String plainPassword = jsonObject.get("password").getAsString();
                account.setPassWord(BCrypt.hashpw(plainPassword, BCrypt.gensalt(12))); // Hash password
                account.setEmail(jsonObject.get("email").getAsString());
                account.setStatus(jsonObject.has("status") ? jsonObject.get("status").getAsString().equals("Enable") : true);
                account.setRole("Receptionist");

                Receptionist receptionist = new Receptionist();
                receptionist.setFullName(jsonObject.get("fullName").getAsString());
                receptionist.setPhone(jsonObject.get("phone").getAsString());

                long startTime = System.currentTimeMillis();
                boolean success = adminBusinessDAO.createReceptionist(account, receptionist);
                LOGGER.info("Create receptionist time: " + (System.currentTimeMillis() - startTime) + "ms");

                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("success", success);
                responseJson.addProperty("message", "Lễ tân đã được tạo thành công");
                out.print(gson.toJson(responseJson));
            } else if ("/update".equals(pathInfo)) {
                AccountStaff account = new AccountStaff();
                account.setAccountStaffId(jsonObject.get("accountStaffId").getAsInt());
                account.setUserName(jsonObject.get("username").getAsString());
                String plainPassword = jsonObject.has("password") ? jsonObject.get("password").getAsString() : null;
                account.setPassWord(plainPassword != null && !plainPassword.isEmpty() ?
                        BCrypt.hashpw(plainPassword, BCrypt.gensalt(12)) : null); // Hash password if provided
                account.setEmail(jsonObject.get("email").getAsString());
                account.setStatus(jsonObject.has("status") ? jsonObject.get("status").getAsString().equals("Enable") : true);
                account.setRole("Receptionist");

                Receptionist receptionist = new Receptionist();
                receptionist.setReceptionistId(jsonObject.get("receptionistId").getAsInt());
                receptionist.setFullName(jsonObject.get("fullName").getAsString());
                receptionist.setPhone(jsonObject.get("phone").getAsString());
                receptionist.setAccountStaffId(jsonObject.get("accountStaffId").getAsInt());

                long startTime = System.currentTimeMillis();
                boolean success = adminBusinessDAO.updateReceptionist(account, receptionist);
                LOGGER.info("Update receptionist time: " + (System.currentTimeMillis() - startTime) + "ms");

                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("success", success);
                responseJson.addProperty("message", "Lễ tân đã cập nhật thành công");
                out.print(gson.toJson(responseJson));
            } else if ("/delete".equals(pathInfo)) {
                int accountStaffId = jsonObject.get("accountStaffId").getAsInt();
                long startTime = System.currentTimeMillis();
                boolean success = adminBusinessDAO.deleteReceptionist(accountStaffId);
                LOGGER.info("Delete receptionist time: " + (System.currentTimeMillis() - startTime) + "ms");

                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("success", success);
                responseJson.addProperty("message", success ? "Lễ tân đã vô hiệu hóa thành công" : "Không thể vô hiệu hóa nhân viên tiếp tân");
                out.print(gson.toJson(responseJson));
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ENDPOINT", "Invalid endpoint");
            }
        } catch (SQLException e) {
            String message = e.getMessage();
            String errorCode;
            if (message.equals("Username already exists")) {
                errorCode = "DUPLICATE_USERNAME";
            } else if (message.equals("Email already exists")) {
                errorCode = "DUPLICATE_EMAIL";
            } else if (message.equals("Phone already exists")) {
                errorCode = "DUPLICATE_PHONE";
            } else if (message.equals("Invalid email format")) {
                errorCode = "INVALID_EMAIL";
            } else if (message.equals("Phone must be 10 digits starting with 0")) {
                errorCode = "INVALID_PHONE";
            } else if (message.equals("Username must be 3-50 characters, alphanumeric or underscore only")) {
                errorCode = "INVALID_USERNAME";
            } else if (message.equals("Password must be 6-50 characters")) {
                errorCode = "INVALID_PASSWORD";
            } else if (message.equals("Full name must be 1-100 characters with single spaces")) {
                errorCode = "INVALID_FULLNAME";
            } else {
                errorCode = "DATABASE_ERROR";
                message = "Database error: " + message;
            }
            LOGGER.severe("SQL error processing POST request: " + message);
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, errorCode, message);
        } catch (NumberFormatException e) {
            LOGGER.severe("Number format error: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_INPUT",
                    "Invalid number format for account staff ID or receptionist ID");
        } catch (Exception e) {
            LOGGER.severe("Error processing POST request: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                    "Failed to process request: " + e.getMessage());
        } finally {
            out.flush();
        }
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private int parseIntParam(HttpServletRequest request, String paramName, int defaultValue) {
        String param = request.getParameter(paramName);
        if (param == null || param.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(param);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + paramName + ": " + param);
        }
    }

    private void sendError(HttpServletResponse response, int status, String errorCode, String message) throws IOException {
        response.setStatus(status);
        JsonObject errorJson = new JsonObject();
        errorJson.addProperty("success", false);
        errorJson.addProperty("errorCode", errorCode);
        errorJson.addProperty("message", message);
        response.getWriter().print(gson.toJson(errorJson));
        response.getWriter().flush();
    }
}