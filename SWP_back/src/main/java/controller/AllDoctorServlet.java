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

@WebServlet("/api/all-doctors/*")
public class AllDoctorServlet extends HttpServlet {
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

        if (pathInfo == null || pathInfo.equals("/")) {
            String name = req.getParameter("name");
            String department = req.getParameter("department");

            // Get data
            ArrayList<Doctor> doctors;
            boolean hasName = name != null && !name.trim().isEmpty();
            boolean hasDepartment = department != null && !department.trim().isEmpty();

            try {
                if (hasName || hasDepartment) {
                    // Search by name and/or department
                    String nameQuery = hasName ? name.trim().replaceAll("\\s+", " ") : null;
                    String deptQuery = hasDepartment ? department.trim().replaceAll("\\s+", " ") : null;
                    doctors = dao.searchDoctorsByNameAndDepartment(nameQuery, deptQuery);
                } else {
                    // Get all doctors without pagination
                    doctors = dao.getAllDoctors();
                }

                // Create JSON response
                JsonObject responseJson = new JsonObject();
                responseJson.add("doctors", gson.toJsonTree(doctors));
                if (hasName || hasDepartment) {
                    JsonObject searchKeywords = new JsonObject();
                    if (hasName) searchKeywords.addProperty("name", name);
                    if (hasDepartment) searchKeywords.addProperty("department", department);
                    responseJson.add("searchKeywords", searchKeywords);
                }
                out.print(gson.toJson(responseJson));

            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"Failed to fetch doctors\"}");
            }
        } else {
            // Fetch doctor by ID
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

        // Read JSON from request body
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        // Uncomment and implement if needed
        /*
        Doctor doctor = gson.fromJson(sb.toString(), Doctor.class);
        doctor.setId(counter.incrementAndGet());
        dao.addDoctor(doctor); // Assuming DAO has an addDoctor method
        resp.setStatus(HttpServletResponse.SC_CREATED);
        out.print(gson.toJson(doctor));
        */
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
                // Read JSON from request body
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = req.getReader()) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                }
                // Uncomment and implement if needed
                /*
                Doctor updatedDoctor = gson.fromJson(sb.toString(), Doctor.class);
                boolean updated = dao.updateDoctor(id, updatedDoctor); // Assuming DAO has an updateDoctor method
                if (updated) {
                    out.print(gson.toJson(updatedDoctor));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Doctor not found\"}");
                }
                */
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
                Long id = Long.parseLong(pathInfo.split("/")[1]);
                // Uncomment and implement if needed
                /*
                boolean deleted = dao.deleteDoctor(id); // Assuming DAO has a deleteDoctor method
                if (deleted) {
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Doctor not found\"}");
                }
                */
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