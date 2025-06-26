package controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dal.MedicineDAO;
import dto.MedicineDTO;
import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;


@WebServlet("/api/medicines/*")
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
        setCORSHeaders(resp);
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();
        String searchName = req.getParameter("name");
        try {
            if (pathInfo == null || pathInfo.equals("/")) {

                if (searchName != null && !searchName.trim().isEmpty()) {
                    // Search by name
                    ArrayList<MedicineDTO> medicines = dao.searchMedicinesByName(searchName);
                    out.println(gson.toJson(medicines));
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    // Paginated list
                    int page = getIntParameter(req, "page", 1);
                    int size = getIntParameter(req, "size", 10);
                    if (page < 1 || size < 1) {
                        sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid page or size parameters");
                        return;
                    }
                    ArrayList<MedicineDTO> medicines = dao.getMedicinesByPage(page, size);
                    out.println(gson.toJson(medicines));
                    resp.setStatus(HttpServletResponse.SC_OK);
                }

//                int page = getIntParameter(req, "page", 1);
//                int size = getIntParameter(req, "size", 10);
//                if (page < 1 || size < 1) {
//                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid page or size parameters");
//                    return;
//                }
//                // Giả định MedicineDAO có getMedicinesByPage
//                // Cần phát triển phương thức này trong MedicineDAO
//                // Ví dụ: ArrayList<MedicineDTO> getMedicinesByPage(int page, int size)
//                ArrayList<MedicineDTO> medicines = dao.getMedicinesByPage(page, size); // Placeholder
//                out.println(gson.toJson(medicines));
//                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                String idStr = pathInfo.substring(1);
                int medicineId = Integer.parseInt(idStr);
                if (medicineId < 1) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid medicine ID");
                    return;
                }
                MedicineDTO medicine = dao.getMedicineById(medicineId);
                if (medicine == null) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Medicine not found");
                    return;
                }
                out.println(gson.toJson(medicine));
                resp.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter format");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCORSHeaders(resp);
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo != null && pathInfo.length() > 1) {
                String idStr = pathInfo.substring(1);
                int medicineId = Integer.parseInt(idStr);

                if (medicineId < 1) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid medicine ID");
                    return;
                }

                BufferedReader reader = req.getReader();
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                int newQuantity = json.get("quantity").getAsInt();

                if (newQuantity < 0) {
                    sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Quantity cannot be negative");
                    return;
                }

                boolean updated = dao.updateMedicineQuantity(medicineId, newQuantity);
                if (!updated) {
                    sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Medicine not found or update failed");
                    return;
                }

                JsonObject response = new JsonObject();
                response.addProperty("message", "Medicine quantity updated successfully");
                out.println(gson.toJson(response));
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Medicine ID is required");
            }
        } catch (NumberFormatException e) {
            sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid parameter format");
        } catch (Exception e) {
            sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error: " + e.getMessage());
        }
    }


    private void setCORSHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:*");
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
