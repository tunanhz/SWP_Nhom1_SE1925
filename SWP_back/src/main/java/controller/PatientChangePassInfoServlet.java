package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.PatientDAO;
import dto.PatientDTO;
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

@WebServlet("/api/patientProfile/*")
public class PatientChangePassInfoServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PatientChangePassInfoServlet.class.getName());
    private final PatientDAO patientDAO = new PatientDAO();
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

            String accountPatientIdStr = request.getParameter("accountPatientId");
            if (accountPatientIdStr == null || accountPatientIdStr.trim().isEmpty()) {
                LOGGER.warning("Missing accountPatientId parameter");
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "MISSING_ACCOUNT_STAFF_ID",
                        "accountStaffId is required");
                return;
            }

            int accountPatientId;
            try {
                accountPatientId = Integer.parseInt(accountPatientIdStr);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid accountStaffId: " + accountPatientIdStr);
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ACCOUNT_STAFF_ID",
                        "accountStaffId must be a valid integer");
                return;
            }

            PatientDTO patient = patientDAO.getPatientInfoByAccountPatientId(accountPatientId);
            JsonObject responseJson = new JsonObject();
            if (patient == null) {
                LOGGER.warning("Patient not found for accountPatientId: " + accountPatientId);
                responseJson.addProperty("success", false);
                responseJson.addProperty("error", "Patient not found");
            } else {
                responseJson.addProperty("success", true);
                responseJson.add("data", gson.toJsonTree(patient));
                responseJson.addProperty("message", "Patient profile fetched successfully");
            }
            out.print(gson.toJson(responseJson));
            LOGGER.info("Fetched Patient profile for accountPatientId: " + accountPatientId);
        } catch (Exception e) {
            LOGGER.severe("Error fetching Patient profile: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                    "Failed to fetch Patient profile: " + e.getMessage());
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
        if ("/api/patientProfile/changePassword".equals(servletPath)) {
            //handleChangePassword(request, response, out);
        } else {
            handleUpdateProfile(request, response, out);
        }
    }

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response, PrintWriter out) throws IOException {
        try {
            String accountPatientIdStr = request.getParameter("accountPatientId");
            String imgUrl = request.getParameter("uploadedImgUrl");

            if (accountPatientIdStr == null || accountPatientIdStr.trim().isEmpty()) {
                LOGGER.warning("Missing receptionistId parameter");
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "MISSING_RECEPTIONIST_ID",
                        "receptionistId is required");
                return;
            }

            int accountPatientId;
            try {
                accountPatientId = Integer.parseInt(accountPatientIdStr);
            } catch (NumberFormatException e) {
                LOGGER.warning("Invalid accountPatientId: " + accountPatientIdStr);
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_RECEPTIONIST_ID",
                        "accountPatientId must be a valid integer");
                return;
            }

            if (imgUrl != null && !imgUrl.trim().isEmpty()) {
                if (!imgUrl.matches("^(https?://.*\\.(?:png|jpg|jpeg|gif))$")) {
                    LOGGER.warning("Invalid imgUrl format: " + imgUrl);
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_IMAGE_URL",
                            "Image URL must be a valid URL ending with .png, .jpg, .jpeg, or .gif");
                    return;
                }
            }

            boolean success = patientDAO.updatePatientIMG(accountPatientId, imgUrl);
            if (success) {
                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("success", true);
                responseJson.addProperty("message", "Profile updated successfully");
                responseJson.addProperty("img", imgUrl);
                out.print(gson.toJson(responseJson));
                LOGGER.info("Patient profile updated successfully: accountPatientId=" + accountPatientId);
            }
        } catch (Exception e) {
            LOGGER.severe("Exception during profile update: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                    "Failed to update profile: " + e.getMessage());
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
