package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.PatientDAO;
import dto.PatientDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/api/patientProfile/*")
public class PatientChangePassInfoServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PatientChangePassInfoServlet.class.getName());
    private static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";
    private final PatientDAO patientDAO;
    private final Gson gson;

    public PatientChangePassInfoServlet() {
        this.patientDAO = new PatientDAO();
        this.gson = new Gson();
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCORSHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType(JSON_CONTENT_TYPE);

        try (PrintWriter out = response.getWriter()) {
            String accountPatientIdStr = request.getParameter("accountPatientId");
            Integer accountPatientId = validateAccountId(accountPatientIdStr, response);
            if (accountPatientId == null) return;

            PatientDTO patient = patientDAO.getPatientInfoByAccountPatientId(accountPatientId);
            JsonObject responseJson = new JsonObject();

            if (patient == null) {
                LOGGER.warning("Patient not found for accountPatientId: " + accountPatientId);
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "PATIENT_NOT_FOUND",
                        "Patient not found for ID: " + accountPatientId);
                return;
            }

            responseJson.addProperty("success", true);
            responseJson.add("data", gson.toJsonTree(patient));
            responseJson.addProperty("message", "Hồ sơ bệnh nhân đã được tải thành công");
            out.print(gson.toJson(responseJson));
            LOGGER.info("Fetched Patient profile for accountPatientId: " + accountPatientId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching patient profile: {0}", e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                    "Failed to fetch patient profile: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCORSHeaders(response);
        response.setContentType(JSON_CONTENT_TYPE);

        try (PrintWriter out = response.getWriter()) {
            String pathInfo = request.getPathInfo();
            if ("/changePassword".equals(pathInfo)) {
                handleChangePassword(request, response, out);
            } else {
                handleUpdateProfile(request, response, out);
            }
        }
    }

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        try {
            String accountPatientIdStr = request.getParameter("accountPatientId");
            Integer accountPatientId = validateAccountId(accountPatientIdStr, response);
            if (accountPatientId == null) return;

            String imgUrl = request.getParameter("uploadedImgUrl");
            if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                if (!isValidImageUrl(imgUrl)) {
                    LOGGER.warning("Invalid imgUrl format: " + imgUrl);
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_IMAGE_URL",
                            "Image URL must be a valid URL ending with .png, .jpg, .jpeg, or .gif");
                    return;
                }
            }

            boolean success = patientDAO.updatePatientIMG(accountPatientId, imgUrl);
            if (!success) {
                sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "UPDATE_FAILED",
                        "Failed to update patient profile image");
                return;
            }

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "Phồ sơ đã được cập nhật thành công");
            responseJson.addProperty("img", imgUrl != null ? imgUrl : "");
            out.print(gson.toJson(responseJson));
            LOGGER.info("Patient profile updated successfully: accountPatientId=" + accountPatientId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating profile: {0}", e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                    "Failed to update profile: " + e.getMessage());
        }
    }

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        try {
            String accountPatientIdStr = request.getParameter("accountPatientId");
            Integer accountPatientId = validateAccountId(accountPatientIdStr, response);
            if (accountPatientId == null) return;

            String currentPassword = request.getParameter("currentPassword");

            String newPassword = request.getParameter("newPassword");
            String confirmPassword = request.getParameter("confirmPassword");

            if (!validatePasswordInputs(currentPassword, newPassword, confirmPassword, response)) {
                return;
            }

            patientDAO.updatePassword(accountPatientId, currentPassword, newPassword);

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("success", true);
            responseJson.addProperty("message", "Mật khẩu đã được thay đổi thành công");
            out.print(gson.toJson(responseJson));
            LOGGER.info("Password changed successfully for accountPatientId=" + accountPatientId);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during password change: {0}", e.getMessage());
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "DATABASE_ERROR",
                    "Error: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during password change: {0}", e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                    "Failed to change password: " + e.getMessage());
        }
    }

    private Integer validateAccountId(String accountPatientIdStr, HttpServletResponse response) throws IOException {
        if (accountPatientIdStr == null || accountPatientIdStr.trim().isEmpty()) {
            LOGGER.warning("Missing accountPatientId parameter");
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "MISSING_ACCOUNT_ID",
                    "accountPatientId is required");
            return null;
        }

        try {
            return Integer.parseInt(accountPatientIdStr);
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid accountPatientId: " + accountPatientIdStr);
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ACCOUNT_ID",
                    "accountPatientId must be a valid integer");
            return null;
        }
    }

    private boolean validatePasswordInputs(String currentPassword, String newPassword,
                                           String confirmPassword, HttpServletResponse response) throws IOException {
        if (currentPassword == null || currentPassword.trim().isEmpty() ||
                newPassword == null || newPassword.trim().isEmpty() ||
                confirmPassword == null || confirmPassword.trim().isEmpty()) {
            LOGGER.warning("Missing password parameters");
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "MISSING_PASSWORD",
                    "All password fields are required");
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            LOGGER.warning("New password and confirm password do not match");
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "PASSWORD_MISMATCH",
                    "New password and confirm password must match");
            return false;
        }

        if (newPassword.length() < 8) {
            LOGGER.warning("New password too short: " + newPassword.length());
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_PASSWORD_LENGTH",
                    "Password must be at least 8 characters");
            return false;
        }

        return true;
    }

    private boolean isValidImageUrl(String imgUrl) {
        return imgUrl.matches("^(https?://.*\\.(?:png|jpg|jpeg|gif))$");
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private void sendError(HttpServletResponse response, int status, String errorCode, String message) throws IOException {
        response.setContentType(JSON_CONTENT_TYPE);
        response.setStatus(status);
        JsonObject errorJson = new JsonObject();
        errorJson.addProperty("success", false);
        errorJson.addProperty("errorCode", errorCode);
        errorJson.addProperty("message", message);
        try (PrintWriter writer = response.getWriter()) {
            writer.print(gson.toJson(errorJson));
        }
    }
}