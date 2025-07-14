package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.ReceptionistDAO;
import dto.ReceptionistDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Logger;

@WebServlet({"/api/receptionistProfile/*", "/api/receptionist/changePassword"})
public class ReceptionistUpdateInformationServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ReceptionistUpdateInformationServlet.class.getName());
    private final ReceptionistDAO receptionistDAO = new ReceptionistDAO();
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
            LOGGER.info("GET request from " + request.getRemoteAddr() + " for /api/receptionistProfile");

            String accountStaffIdStr = request.getParameter("accountStaffId");
            if (accountStaffIdStr == null || accountStaffIdStr.trim().isEmpty()) {
                LOGGER.warning("Missing accountStaffId parameter");
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "MISSING_ACCOUNT_STAFF_ID",
                        "accountStaffId is required");
                return;
            }

            int accountStaffId;
            try {
                accountStaffId = Integer.parseInt(accountStaffIdStr);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid accountStaffId: " + accountStaffIdStr);
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ACCOUNT_STAFF_ID",
                        "accountStaffId must be a valid integer");
                return;
            }

            ReceptionistDTO receptionist = receptionistDAO.getReceptionistInfoByAccountStaffId(accountStaffId);
            JsonObject responseJson = new JsonObject();
            if (receptionist == null) {
                LOGGER.warning("Receptionist not found for accountStaffId: " + accountStaffId);
                responseJson.addProperty("success", false);
                responseJson.addProperty("error", "Receptionist not found");
            } else {
                responseJson.addProperty("success", true);
                responseJson.add("data", gson.toJsonTree(receptionist));
                responseJson.addProperty("message", "Receptionist profile fetched successfully");
            }
            out.print(gson.toJson(responseJson));
            LOGGER.info("Fetched receptionist profile for accountStaffId: " + accountStaffId);
        } catch (SQLException e) {
            LOGGER.severe("Error fetching receptionist profile: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                    "Failed to fetch receptionist profile: " + e.getMessage());
        } finally {
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType("application/json; charset=UTF-8");
        PrintWriter out = response.getWriter();

        String servletPath = request.getServletPath();
        if ("/api/receptionist/changePassword".equals(servletPath)) {
            handleChangePassword(request, response, out);
        } else {
            handleUpdateProfile(request, response, out);
        }
    }

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        try {
            String receptionistIdStr = request.getParameter("receptionistId");
            String fullName = request.getParameter("fullName");
            String phone = request.getParameter("phone");
            String imgUrl = request.getParameter("img");

            LOGGER.info("POST request - receptionistId: " + (receptionistIdStr != null ? receptionistIdStr : "null") +
                    ", fullName: " + (fullName != null ? fullName : "null") +
                    ", phone: " + (phone != null ? phone : "null") +
                    ", imgUrl: " + (imgUrl != null ? imgUrl : "null"));

            if (receptionistIdStr == null || receptionistIdStr.trim().isEmpty()) {
                LOGGER.warning("Missing receptionistId parameter");
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "MISSING_RECEPTIONIST_ID",
                        "receptionistId is required");
                return;
            }

            int receptionistId;
            try {
                receptionistId = Integer.parseInt(receptionistIdStr);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid receptionistId: " + receptionistIdStr);
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_RECEPTIONIST_ID",
                        "receptionistId must be a valid integer");
                return;
            }

            if (fullName == null && phone == null && imgUrl == null) {
                LOGGER.warning("No data provided for update");
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "NO_DATA_PROVIDED",
                        "At least one field (fullName, phone, or img) must be provided");
                return;
            }

            if (fullName != null && !fullName.trim().isEmpty()) {
                if (!fullName.matches("^[a-zA-Z\\s\\u00C0-\\u1EF9]{2,100}$")) {
                    LOGGER.warning("Invalid full name format: " + fullName);
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_FULL_NAME",
                            "Full name must contain only letters and spaces, 2-100 characters");
                    return;
                }
            }

            if (phone != null && !phone.trim().isEmpty()) {
                if (!phone.matches("^0[0-9]{9}$")) {
                    LOGGER.warning("Invalid phone number format: " + phone);
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_PHONE",
                            "Phone number must be 10 digits starting with 0");
                    return;
                }
            }

            if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                if (!imgUrl.matches("^(https?://.*\\.(?:png|jpg|jpeg|gif))$")) {
                    LOGGER.warning("Invalid imgUrl format: " + imgUrl);
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_IMAGE_URL",
                            "Image URL must be a valid URL ending with .png, .jpg, .jpeg, or .gif");
                    return;
                }
            }

            receptionistDAO.updateReceptionistProfile(receptionistId, fullName, phone, imgUrl);

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "Profile updated successfully");
            out.print(gson.toJson(responseJson));
            LOGGER.info("Receptionist profile updated successfully: receptionistId=" + receptionistId);
        } catch (SQLException e) {
            LOGGER.severe("SQLException during profile update: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DATABASE_ERROR",
                    "Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Exception during profile update: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                    "Failed to update profile: " + e.getMessage());
        } finally {
            out.flush();
        }
    }

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        try {
            String accountStaffIdStr = request.getParameter("accountStaffId");
            if (accountStaffIdStr == null || accountStaffIdStr.trim().isEmpty()) {
                LOGGER.warning("Missing accountStaffId parameter");
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "MISSING_ACCOUNT_STAFF_ID",
                        "accountStaffId is required");
                return;
            }

            int accountStaffId;
            try {
                accountStaffId = Integer.parseInt(accountStaffIdStr);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid accountStaffId: " + accountStaffIdStr);
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ACCOUNT_STAFF_ID",
                        "accountStaffId must be a valid integer");
                return;
            }

            String currentPassword = request.getParameter("currentPassword");
            String newPassword = request.getParameter("newPassword");
            String confirmPassword = request.getParameter("confirmPassword");

            if (currentPassword == null || currentPassword.trim().isEmpty() ||
                    newPassword == null || newPassword.trim().isEmpty() ||
                    confirmPassword == null || confirmPassword.trim().isEmpty()) {
                LOGGER.warning("Missing password parameters");
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "MISSING_PASSWORD",
                        "All password fields are required");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                LOGGER.warning("New password and confirm password do not match");
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "PASSWORD_MISMATCH",
                        "New password and confirm password must match");
                return;
            }

            if (newPassword.length() < 8) {
                LOGGER.warning("New password too short: " + newPassword.length());
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_PASSWORD_LENGTH",
                        "Password must be at least 8 characters");
                return;
            }

            receptionistDAO.updatePassword(accountStaffId, currentPassword, newPassword);

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "Password changed successfully");
            out.print(gson.toJson(responseJson));
            LOGGER.info("Password changed successfully for account_staff_id=" + accountStaffId);
        } catch (SQLException e) {
            LOGGER.severe("SQLException during password change: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "DATABASE_ERROR",
                    "Database error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("Exception during password change: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                    "Failed to change password: " + e.getMessage());
        } finally {
            out.flush();
        }
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
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