package controller;


import com.google.gson.Gson;
import dal.PrescriptionDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicLong;

@WebServlet("/api/prescription/*")
public class PrescriptionServlet extends HttpServlet {

    private final PrescriptionDAO dao = new PrescriptionDAO();
    private final AtomicLong counter = new AtomicLong();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Add CORS headers
        resp.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type"); // A

        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")){
            out.print(gson.toJson(dao.getAllPrescription()));

        } else {
            // Lấy user theo ID
            String[] splits = pathInfo.split("/");

            if (splits.length == 2) {
                try {
//                    int id = Integer.parseInt(splits[1]);
//                    Doctor doctor = dao.getDoctorById(id);
//                    if (doctor != null) {
//                        out.print(gson.toJson(doctor));
//                    } else {
//                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
//                        out.print("{\"error\":\"Doctor not found\"}");
//                    }
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
