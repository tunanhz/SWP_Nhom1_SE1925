package controller;

import com.google.gson.JsonObject;
import dal.DoctorDAO;
import model.Doctor;
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


@WebServlet("/api/doctors/*")
public class DoctorServlet extends HttpServlet{
    private final DoctorDAO dao = new DoctorDAO();
    private final AtomicLong counter = new AtomicLong();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Add CORS headers
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();

        if ("/departments".equals(pathInfo)) {
            try {
                ArrayList<String> departments = dao.getDepartments();
                out.print(gson.toJson(departments));
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"Failed to fetch departments\"}");
            }
            out.flush();
            return;
        }

        if (pathInfo == null || pathInfo.equals("/")){
            int page = 1; // Mặc định trang 1
            int size = 8; // Mặc định 8 bác sĩ mỗi trang
            String name = null;
            String department = null;

            try {
                String pageParam = req.getParameter("page");
                String sizeParam = req.getParameter("size");
                name = req.getParameter("name");
                department = req.getParameter("department");

                if (pageParam != null) page = Integer.parseInt(pageParam);
                if (sizeParam != null) size = Integer.parseInt(sizeParam);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid page or size parameter\"}");
                out.flush();
                return;
            }

            // Get paginated data
            ArrayList<Doctor> doctors;
            int totalDoctors;

            boolean hasName = name != null && !name.trim().isEmpty();
            boolean hasDepartment = department != null && !department.trim().isEmpty();

            if (hasName || hasDepartment) {
                // Search by name and/or department with pagination
                String nameQuery = hasName ? name.trim().replaceAll("\\s+", " ") : null;
                String deptQuery = hasDepartment ? department.trim().replaceAll("\\s+", " ") : null;
                doctors = dao.searchDoctorsByNameAndDepartment(nameQuery, deptQuery, page, size);
                totalDoctors = dao.getTotalDoctorsByNameAndDepartment(nameQuery, deptQuery);
            } else {
                // Get all doctors with pagination
                doctors = dao.getAllDoctors(page, size);
                totalDoctors = dao.getTotalDoctors();
            }

            int totalPages = (int) Math.ceil((double) totalDoctors / size);

            // Tạo đối tượng JSON trả về
            JsonObject responseJson = new JsonObject();
            responseJson.add("doctors", gson.toJsonTree(doctors));
            responseJson.addProperty("totalPages", totalPages);
            responseJson.addProperty("currentPage", page);
            if (hasName || hasDepartment) {
                JsonObject searchKeywords = new JsonObject();
                if (hasName) searchKeywords.addProperty("name", name);
                if (hasDepartment) searchKeywords.addProperty("department", department);
                responseJson.add("searchKeywords", searchKeywords);
            }
            out.print(gson.toJson(responseJson));

        } else {
            // Lấy user theo ID
            String[] splits = pathInfo.split("/");

            if (splits.length == 2) {
                try {
                    int id = Integer.parseInt(splits[1]);
                    Doctor doctor = dao.getDoctorById(id);
                    if (doctor != null) {
                        out.print(gson.toJson(doctor));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Doctor not found\"}");
                    }
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid path\"}");
                }
            }
        }
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        // Đọc JSON từ request body
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
//        User user = gson.fromJson(sb.toString(), User.class);
//        user.setId(counter.incrementAndGet());
//        users.add(user);

//        resp.setStatus(HttpServletResponse.SC_CREATED);
//        out.print(gson.toJson(user));
        out.flush();
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.split("/").length == 2) {
            try {
                Long id = Long.parseLong(pathInfo.split("/")[1]);
                // Đọc JSON từ request body
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = req.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
//                User updatedUser = gson.fromJson(sb.toString(), User.class);
//                User existingUser = users.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
//
//                if (existingUser != null) {
//                    existingUser.setName(updatedUser.getName());
//                    existingUser.setEmail(updatedUser.getEmail());
//                    out.print(gson.toJson(existingUser));
//                } else {
//                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
//                    out.print("{\"error\":\"User not found\"}");
//                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid ID\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid request\"}");
        }
        out.flush();
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.split("/").length == 2) {
            try {
//                Long id = Long.parseLong(pathInfo.split("/")[1]);
//                boolean removed = users.removeIf(u -> u.getId().equals(id));
//                if (removed) {
//                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
//                } else {
//                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
//                    out.print("{\"error\":\"User not found\"}");
//                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid ID\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid request\"}");
        }
        out.flush();
    }

}

