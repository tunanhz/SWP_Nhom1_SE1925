package controller;

import com.google.gson.Gson;
import dal.DoctorScheduleDAO;
import dto.DoctorScheduleDTO;
import model.DoctorSchedule;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@WebServlet("/api/Add_doctor_schedule/*")
public class DoctorScheduleServlet extends HttpServlet {
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
            String doctorIdParam = request.getParameter("doctorId");
            String startDateParam = request.getParameter("startDate");
            String endDateParam = request.getParameter("endDate");
            String shiftParam = request.getParameter("shift");
            String departmentParam = request.getParameter("department");

            List<DoctorScheduleDTO> schedules = dao.getDoctorSchedules(doctorIdParam, startDateParam, endDateParam, shiftParam, departmentParam);
            response.setStatus(HttpServletResponse.SC_OK);
            out.println(gson.toJson(schedules));
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
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Received POST request to /api/Add_doctor_schedule");
        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String jsonData = sb.toString();

            DoctorSchedule schedule = gson.fromJson(jsonData, DoctorSchedule.class);

            if (schedule.getDoctorId() <= 0 || schedule.getWorkingDate() == null || schedule.getShift() == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Missing required fields: doctorId, workingDate, or shift\"}");
                return;
            }

            if (!schedule.getShift().matches("Morning|Afternoon|Evening")) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Invalid shift value. Must be Morning, Afternoon, or Evening\"}");
                return;
            }

            // Validate date format (yyyy-MM-dd)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date workingDate;
            try {
                workingDate = sdf.parse(schedule.getWorkingDate());
            } catch (ParseException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Invalid working date format. Use yyyy-MM-dd\"}");
                return;
            }

            // Ensure date is in the future
            Date currentDate = new Date();
            if (workingDate.before(currentDate)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\": \"Working date must be in the future\"}");
                return;
            }

            DoctorSchedule createdSchedule = dao.createDoctorSchedule(schedule);
            if (createdSchedule != null) {
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.println(gson.toJson(createdSchedule));
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"error\": \"Failed to create doctor schedule\"}");
            }

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
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept");
    }
}