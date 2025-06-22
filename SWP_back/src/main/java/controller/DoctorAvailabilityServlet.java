package controller;

import com.google.gson.Gson;
import dal.DoctorScheduleDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@WebServlet("/api/doctor-availability")
public class DoctorAvailabilityServlet extends HttpServlet {
    private final DoctorScheduleDAO dao = new DoctorScheduleDAO();
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
            String doctorIdStr = request.getParameter("doctorId");
            String dateStr = request.getParameter("date");
            String action = request.getParameter("action");

            if (doctorIdStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Missing required parameter: doctorId\"}");
                return;
            }

            int doctorId;
            try {
                doctorId = Integer.parseInt(doctorIdStr);
                if (doctorId <= 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"error\": \"Invalid doctorId\"}");
                    return;
                }
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Invalid doctorId format\"}");
                return;
            }

            if ("getWorkingDates".equals(action)) {
                // Fetch working dates for the doctor
                List<String> workingDates = dao.getWorkingDates(doctorId);
                response.setStatus(HttpServletResponse.SC_OK);
                out.println(gson.toJson(workingDates));
                return;
            }

            if (dateStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Missing required parameter: date\"}");
                return;
            }

            // Validate date format (yyyy-MM-dd)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date date;
            try {
                date = sdf.parse(dateStr);
            } catch (ParseException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Invalid date format. Use yyyy-MM-dd\"}");
                return;
            }

            // Ensure date is not in the past
            Date currentDate = new Date();
            currentDate.setHours(0);
            currentDate.setMinutes(0);
            currentDate.setSeconds(0);
            if (date.before(currentDate)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Date must not be in the past\"}");
                return;
            }

            List<String> bookedTimes = dao.getBookedAppointmentTimes(doctorId, dateStr);
            response.setStatus(HttpServletResponse.SC_OK);
            out.println(gson.toJson(bookedTimes));

        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\": \"Database error: " + e.getMessage().replace("\"", "\\\"") + "\"}");
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