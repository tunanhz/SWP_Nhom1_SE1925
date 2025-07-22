package controller;

import com.google.gson.Gson;
import dal.PatientDAO;
import model.Patient;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/patients-of-account")
public class GetPatientServlet extends HttpServlet {
    private final PatientDAO patientDAO = new PatientDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        try {
            // Lấy account_patient_id từ session
//            HttpSession session = request.getSession(false);
//            if (session == null || session.getAttribute("account_patient_id") == null) {
//                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                out.println("{\"error\": \"No authenticated account found. Please log in.\"}");
//                return;
//            }

//            int accountPatientId = (Integer) session.getAttribute("account_patient_id");


            int accountPatientId = Integer.parseInt(request.getParameter("accountPatientId"));
            List<Patient> patients = patientDAO.getPatientsByAccountId(accountPatientId);

            if (patients != null && !patients.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_OK);
                out.println(gson.toJson(patients));
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                out.println("[]"); // Trả về mảng rỗng nếu không có bệnh nhân
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Unexpected error: " + e.getMessage().replace("\"", "\\\"") + "\"}");
        } finally {
            out.flush();
        }
    }

    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept");
    }
}