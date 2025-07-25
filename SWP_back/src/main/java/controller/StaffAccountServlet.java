package controller;

import com.google.gson.Gson;
import dal.PatientDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;

@WebServlet("/api/admin/staff")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 1,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 50
)
public class StaffAccountServlet extends HttpServlet {
    private final PatientDAO dao = new PatientDAO(); // dùng chung PatientDAO vì có method createStaffAccount
    private final Gson gson = new Gson();

    private void setCORS(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) {
        setCORS(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        setCORS(resp);
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        try {
            String username = req.getParameter("username");
            String password = req.getParameter("password");
            String email = req.getParameter("email");
            String role = req.getParameter("role");
            String fullName = req.getParameter("fullName");
            String phone = req.getParameter("phone");
            String department = req.getParameter("department");
            String eduLevel = req.getParameter("eduLevel");
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));

            Part imgPart = req.getPart("img");
            String imgPath = null;
            if (imgPart != null && imgPart.getSize() > 0) {
                String fileName = System.currentTimeMillis() + "_" + imgPart.getSubmittedFileName();
                String uploadDir = getServletContext().getRealPath("") + "images/accounts";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();
                imgPart.write(uploadDir + File.separator + fileName);
                imgPath = "images/accounts/" + fileName;
            }

            boolean success = dao.createStaffAccount(username, hashedPassword, email, imgPath, role, fullName, phone, department, eduLevel);
            out.print("{\"success\":" + success + "}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"success\":false, \"error\":\"" + e.getMessage().replace("\"", "'") + "\"}");
        }
    }
}
