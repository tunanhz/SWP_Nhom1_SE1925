package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.AdminBusinessDAO;
import model.ListOfMedicalService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Logger;

@WebServlet("/api/admin/business/*")
public class AdminBusinessServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AdminBusinessServlet.class.getName());
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
            if ("/services".equals(pathInfo)) {
                String searchQuery = request.getParameter("searchQuery");
                String minPriceStr = request.getParameter("minPrice");
                String maxPriceStr = request.getParameter("maxPrice");
                int page = parseIntParam(request, "page", 1);
                int pageSize = parseIntParam(request, "pageSize", 10);
                String sortBy = request.getParameter("sortBy");
                String sortOrder = request.getParameter("sortOrder");
                String statusFilter = request.getParameter("statusFilter");

                if (page < 1 || pageSize <= 0 || pageSize > 100) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_PAGE",
                            "Page must be >= 1 and pageSize must be between 1 and 100");
                    return;
                }

                Double minPrice = minPriceStr != null && !minPriceStr.isEmpty() ? Double.parseDouble(minPriceStr) : null;
                Double maxPrice = maxPriceStr != null && !maxPriceStr.isEmpty() ? Double.parseDouble(maxPriceStr) : null;

                if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
                    sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_PRICE_RANGE",
                            "Min price cannot be greater than max price");
                    return;
                }

                long startTime = System.currentTimeMillis();
                ArrayList<ListOfMedicalService> services = adminBusinessDAO.getServices(searchQuery, minPrice, maxPrice, page, pageSize, sortBy, sortOrder, statusFilter);
                int totalServices = adminBusinessDAO.countServices(searchQuery, minPrice, maxPrice, statusFilter);
                LOGGER.info("Servlet processing time: " + (System.currentTimeMillis() - startTime) + "ms");

                JsonObject responseJson = new JsonObject();
                responseJson.add("services", gson.toJsonTree(services));
                responseJson.addProperty("totalPages", (int) Math.ceil((double) totalServices / pageSize));
                responseJson.addProperty("currentPage", page);
                responseJson.addProperty("pageSize", pageSize);
                responseJson.addProperty("totalServices", totalServices);
                responseJson.addProperty("success", true);
                responseJson.addProperty("message", services.isEmpty() ? "Không tìm thấy dịch vụ nào" : "Dịch vụ đã được tải thành công");
                out.print(gson.toJson(responseJson));
            } else if (pathInfo != null && pathInfo.matches("/services/\\d+")) {
                int serviceId = Integer.parseInt(pathInfo.substring(1).split("/")[1]);
                long startTime = System.currentTimeMillis();
                ListOfMedicalService service = adminBusinessDAO.getServiceById(serviceId);
                LOGGER.info("Get service by ID time: " + (System.currentTimeMillis() - startTime) + "ms");

                if (service != null) {
                    JsonObject responseJson = new JsonObject();
                    responseJson.add("service", gson.toJsonTree(service));
                    responseJson.addProperty("success", true);
                    out.print(gson.toJson(responseJson));
                } else {
                    sendError(response, HttpServletResponse.SC_NOT_FOUND, "NOT_FOUND", "Service not found");
                }
            } else {
                sendError(response, HttpServletResponse.SC_NOT_FOUND, "INVALID_ENDPOINT", "Endpoint not found");
            }
        } catch (NumberFormatException e) {
            LOGGER.severe("Number format error: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_INPUT",
                    "Invalid number format for price or service ID parameters");
        } catch (Exception e) {
            LOGGER.severe("Error processing GET request: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "SERVER_ERROR",
                    "Failed to fetch services: " + e.getMessage());
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

            if ("/services/create".equals(pathInfo)) {
                ListOfMedicalService service = new ListOfMedicalService();
                service.setName(jsonObject.get("name").getAsString());
                service.setDescription(jsonObject.get("description").getAsString());
                service.setPrice(jsonObject.get("price").getAsDouble());
                service.setStatus("Enable");
                long startTime = System.currentTimeMillis();
                boolean success = adminBusinessDAO.createService(service);
                LOGGER.info("Create service time: " + (System.currentTimeMillis() - startTime) + "ms");

                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("success", success);
                responseJson.addProperty("message", success ? "Dịch vụ đã được tạo thành công" : "Đã phát hiện tên trùng lặp");
                out.print(gson.toJson(responseJson));
            } else if ("/services/update".equals(pathInfo)) {
                ListOfMedicalService service = new ListOfMedicalService();
                service.setServiceId(jsonObject.get("serviceId").getAsInt());
                service.setName(jsonObject.get("name").getAsString());
                service.setDescription(jsonObject.get("description").getAsString());
                service.setPrice(jsonObject.get("price").getAsDouble());
                service.setStatus(jsonObject.has("status") ? jsonObject.get("status").getAsString() : "Enable");
                long startTime = System.currentTimeMillis();
                boolean success = adminBusinessDAO.updateService(service);
                LOGGER.info("Update service time: " + (System.currentTimeMillis() - startTime) + "ms");

                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("success", success);
                responseJson.addProperty("message", success ? "Dịch vụ đã được cập nhật thành công" : "Đã phát hiện tên trùng lặp");
                out.print(gson.toJson(responseJson));
            } else if ("/services/delete".equals(pathInfo)) {
                int serviceId = jsonObject.get("serviceId").getAsInt();
                long startTime = System.currentTimeMillis();
                boolean success = adminBusinessDAO.deleteService(serviceId);
                LOGGER.info("Delete service time: " + (System.currentTimeMillis() - startTime) + "ms");

                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("success", success);
                responseJson.addProperty("message", success ? "Đã phát hiện ra vòng lặp tên" : "Đã phát hiện tên vòng lặp");
                out.print(gson.toJson(responseJson));
            } else {
                sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_ENDPOINT", "Invalid endpoint");
            }
        } catch (NumberFormatException e) {
            LOGGER.severe("Number format error: " + e.getMessage());
            sendError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_INPUT",
                    "Invalid number format for service ID or price");
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