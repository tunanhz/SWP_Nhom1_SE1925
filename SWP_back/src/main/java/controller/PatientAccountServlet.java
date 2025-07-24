package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.PatientDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Patient;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.util.ArrayList;

@WebServlet("/api/admin/patient")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 1,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 50
)
public class PatientAccountServlet extends HttpServlet {
    private final PatientDAO patientDAO = new PatientDAO();
    private final Gson gson = new Gson();

    // CORS setup for all responses
    private void setCORS(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    // Preflight request handler
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCORS(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    // GET: Lấy danh sách hoặc 1 bệnh nhân theo id
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCORS(resp);
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String id = req.getParameter("id");
        if (id == null) {
            ArrayList<Patient> patients = patientDAO.getAllPatients(null, null, null, null, 1, 100);
            out.print(gson.toJson(patients));
        } else {
            Patient p = patientDAO.getPatientByPatientId(Integer.parseInt(id));
            if (p != null) {
                out.print(gson.toJson(p));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\":\"Patient not found\"}");
            }
        }
    }

    // POST: Tạo tài khoản bệnh nhân mới
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        setCORS(resp);
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {
            String username = req.getParameter("username");
            String password = req.getParameter("password");
            String email = req.getParameter("email");
            String fullName = req.getParameter("fullName");
            String dob = req.getParameter("dob");
            String gender = req.getParameter("gender");
            String phone = req.getParameter("phone");
            String address = req.getParameter("address");
            Part imgPart = req.getPart("img");
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
            // Upload image
            String imgPath = null;
            if (imgPart != null && imgPart.getSize() > 0) {
                String fileName = System.currentTimeMillis() + "_" + imgPart.getSubmittedFileName();
                String uploadDir = getServletContext().getRealPath("") + "images/accounts";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();
                imgPart.write(uploadDir + File.separator + fileName);
                imgPath = "images/accounts/" + fileName;
            }

            // Create account
            boolean success = patientDAO.createPatientAccount(username, hashedPassword, email, imgPath, fullName, dob, gender, phone, address);
            out.print("{\"success\":" + success + "}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false, \"error\":\"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }

    // PUT: Cập nhật thông tin bệnh nhân
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCORS(resp);
        resp.setContentType("application/json");
        BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
        JsonObject data = gson.fromJson(reader, JsonObject.class);
        int patientId = data.get("patientId").getAsInt();
        String fullName = data.get("fullName").getAsString();
        String dob = data.get("dob").getAsString();
        String gender = data.get("gender").getAsString();
        String phone = data.get("phone").getAsString();
        String address = data.get("address").getAsString();
        boolean success = patientDAO.updatePatient(patientId, fullName, dob, gender, phone, address);
        resp.getWriter().print("{\"success\":" + success + "}");
    }

    // DELETE: Vô hiệu hóa bệnh nhân
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCORS(resp);
        resp.setContentType("application/json");
        int id = Integer.parseInt(req.getParameter("id"));
        boolean success = patientDAO.deletePatient(id);
        resp.getWriter().print("{\"success\":" + success + "}");
    }
}
