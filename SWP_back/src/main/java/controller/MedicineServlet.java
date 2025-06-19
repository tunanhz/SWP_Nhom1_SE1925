package controller;

import com.google.gson.JsonObject;
import dal.MedicineDAO;
import model.MedicineDTO;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicLong;


@WebServlet("/api/medicine/*")
public class MedicineServlet extends HttpServlet {

    private final MedicineDAO dao = new MedicineDAO();
    private final AtomicLong counter = new AtomicLong();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }


    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        setCORSHeaders(resp);
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                // Get paginated prescriptions
                int page = getIntParameter(req, "page", 1);
                int size = getIntParameter(req, "size", 10);

                if (page < 1 || size < 1) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid page or size parameters");
                    return;
                }

                MedicineDTO medicines = dao.getMedicineById(9);
                out.println(gson.toJsonTree(medicines));
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                // Single prescription retrieval not supported by current DAO
                sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Individual prescription retrieval not supported");
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter format");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }


    private void setCORSHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    private void sendError(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        JsonObject error = new JsonObject();
        error.addProperty("error", message);
        resp.getWriter().println(gson.toJson(error));
    }

    private int getIntParameter(HttpServletRequest req, String paramName, int defaultValue) {
        String param = req.getParameter(paramName);
        if (param == null || param.trim().isEmpty()) {
            return defaultValue;
        }
        return Integer.parseInt(param);
    }

}
