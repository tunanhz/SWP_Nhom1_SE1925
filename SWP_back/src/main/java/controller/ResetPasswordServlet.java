package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.ResetPasswordRequestDTO;
import dto.ResponseDTO;
import service.AccountService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

@WebServlet("/api/resetPasswordServlet")
public class ResetPasswordServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ResetPasswordServlet.class.getName());
    private final AccountService accountService = new AccountService();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            StringBuilder jsonBody = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBody.append(line);
                }
            }
            JsonObject jsonObject = JsonParser.parseString(jsonBody.toString()).getAsJsonObject();
            ResetPasswordRequestDTO requestDTO = gson.fromJson(jsonObject, ResetPasswordRequestDTO.class);

            LOGGER.info("Received POST request for reset password with email: " + requestDTO.getEmail());

            ResponseDTO responseDTO = accountService.requestPasswordReset(requestDTO);

            response.setStatus(responseDTO.isSuccess() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
            out.print(gson.toJson(responseDTO));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(new ResponseDTO(false, null, "Failed to process reset request")));
            LOGGER.severe("Error processing reset request: " + e.getMessage());
        } finally {
            out.flush();
        }
    }
}