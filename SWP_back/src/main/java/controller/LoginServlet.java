//package controller;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//import dal.AccountPatientDAO;
//import dal.AccountStaffDAO;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.HttpServlet;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import model.AccountPatient;
//import model.AccountStaff;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.PrintWriter;
//
///**
// *
// * @author Datnq
// */
//@WebServlet("/api/login/*")
//public class LoginServlet extends HttpServlet {
//    private final AccountPatientDAO patientDAO = new AccountPatientDAO();
//    private final AccountStaffDAO staffDAO = new AccountStaffDAO();
//    private final Gson gson = new Gson();
//
//    @Override
//    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        resp.setHeader("Access-Control-Allow-Origin", "*");
//        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
//        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
//        resp.setStatus(HttpServletResponse.SC_OK);
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//        response.setHeader("Access-Control-Allow-Origin", "*");
//        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
//        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
//        response.setContentType("application/json");
//        request.setCharacterEncoding("UTF-8");
//
//        String username = request.getParameter("user-name");
//        String password = request.getParameter("pwd");
//
//        // Tạo session
//        HttpSession session = request.getSession();
//
//        try {
//            // Kiểm tra đăng nhập cho AccountPatient
//            AccountPatient patient = patientDAO.checkLogin(username, password);
//            if (patient != null) {
//                session.setAttribute("user", patient);
//                session.setAttribute("role", "patient");
//                response.sendRedirect("dashboard/patient-dashboard.html");
//                return;
//            }
//
//            // Kiểm tra đăng nhập cho AccountStaff
//            AccountStaff staff = staffDAO.checkLogin(username, password);
//            if (staff != null) {
//                session.setAttribute("user", staff);
//                session.setAttribute("role", "staff");
//                response.sendRedirect("dashboard/doctors.html");
//                return;
//            }
//
//            // Đăng nhập thất bại
//            response.sendRedirect("dashboard/errors/error404.html");
//            print("đăng nhập thất bại");
//
//        } catch (Exception e) {
//            response.sendRedirect("dashboard/errors/error500.html");
//        }
//    }
//}