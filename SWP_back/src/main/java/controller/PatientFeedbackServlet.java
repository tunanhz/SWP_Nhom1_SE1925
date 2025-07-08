package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dal.FeedbackDAO;
import model.Feedback;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet("/api/*")
public class PatientFeedbackServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private FeedbackDAO feedbackDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        feedbackDAO = new FeedbackDAO();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCORSHeaders(response);
        String pathInfo = request.getPathInfo();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (pathInfo != null && pathInfo.startsWith("/check-eligibility/")) {
            try {
                String patientIdStr = pathInfo.substring("/check-eligibility/".length());
                int patientId = Integer.parseInt(patientIdStr);

                // Check eligibility (has completed appointments AND has not submitted feedback)
                boolean eligible = feedbackDAO.checkEligibility(patientId);
                JsonObject json = new JsonObject();
                json.addProperty("eligible", eligible);
//                if (!eligible) {
//                    // Check if feedback already exists
//                    boolean hasFeedback = feedbackDAO.hasFeedback(patientId);
//                    if (hasFeedback) {
//                        json.addProperty("error", "Feedback already submitted");
//                    } else {
//                        json.addProperty("error", "No completed appointments");
//                    }
//                }
                out.print(gson.toJson(json));
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject json = new JsonObject();
                json.addProperty("eligible", false);
                json.addProperty("error", "Invalid patient ID");
                out.print(gson.toJson(json));
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            JsonObject json = new JsonObject();
            json.addProperty("error", "Invalid endpoint");
            out.print(gson.toJson(json));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCORSHeaders(response);
        String pathInfo = request.getPathInfo();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if ("/feedback".equals(pathInfo)) {
            try {
                // Read JSON body
                StringBuilder jb = new StringBuilder();
                String line;
                BufferedReader reader = request.getReader();
                while ((line = reader.readLine()) != null) {
                    jb.append(line);
                }

                // Parse JSON
                JsonObject jsonBody = gson.fromJson(jb.toString(), JsonObject.class);

                // Validate required fields
                if (!jsonBody.has("patientId") || !jsonBody.has("content") ||
                        !jsonBody.has("serviceRating") || !jsonBody.has("doctorRating") ||
                        !jsonBody.has("receptionistRating") || !jsonBody.has("pharmacistRating")) {
                    throw new IllegalArgumentException("Missing required fields");
                }

                // Create Feedback object
                Feedback feedback = new Feedback(
                        jsonBody.get("patientId").getAsInt(),
                        jsonBody.get("content").getAsString(),
                        jsonBody.get("serviceRating").getAsInt(),
                        jsonBody.get("doctorRating").getAsInt(),
                        jsonBody.get("receptionistRating").getAsInt(),
                        jsonBody.get("pharmacistRating").getAsInt()
                );

                // Validate ratings (1-5)
                if (feedback.getServiceRating() < 1 || feedback.getServiceRating() > 5 ||
                        feedback.getDoctorRating() < 1 || feedback.getDoctorRating() > 5 ||
                        feedback.getReceptionistRating() < 1 || feedback.getReceptionistRating() > 5 ||
                        feedback.getPharmacistRating() < 1 || feedback.getPharmacistRating() > 5) {
                    throw new IllegalArgumentException("Ratings must be between 1 and 5");
                }

                // Check if feedback already exists
                if (feedbackDAO.checkEligibility(feedback.getPatientId())) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonObject json = new JsonObject();
                    json.addProperty("success", false);
                    json.addProperty("error", "Feedback already submitted");
                    out.print(gson.toJson(json));
                    return;
                }

                // Save feedback
                boolean success = feedbackDAO.saveFeedback(feedback);
                JsonObject json = new JsonObject();
                json.addProperty("success", success);
                if (!success) {
                    json.addProperty("error", "Failed to save feedback");
                }
                out.print(gson.toJson(json));
            } catch (JsonSyntaxException | IllegalArgumentException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject json = new JsonObject();
                json.addProperty("success", false);
                json.addProperty("error", e.getMessage() != null ? e.getMessage() : "Invalid input");
                out.print(gson.toJson(json));
            } catch (SQLException e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonObject json = new JsonObject();
                json.addProperty("success", false);
                json.addProperty("error", "Database error");
                out.print(gson.toJson(json));
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            JsonObject json = new JsonObject();
            json.addProperty("error", "Invalid endpoint");
            out.print(gson.toJson(json));
        }
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept");
        response.setHeader("Access-Control-Max-Age", "86400");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCORSHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}