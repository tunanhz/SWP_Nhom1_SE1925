package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.AdminBusinessReportDAO;
import dto.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;

@WebServlet("/api/adminbusiness-revenue-reports/*")
public class AdminBusinessRevenueReportServlet extends HttpServlet {
    private final AdminBusinessReportDAO dao = new AdminBusinessReportDAO();
    private final Gson gson = new Gson();
    private final Logger logger = Logger.getLogger(AdminBusinessRevenueReportServlet.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FILE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type");
        req.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if ("/export".equals(pathInfo)) {
            handleExportRequest(req, resp);
        } else {
            handleReportRequest(req, resp);
        }
    }

    private void handleReportRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");

        String startDate = req.getParameter("startDate");
        String endDate = req.getParameter("endDate");
        String searchTerm = req.getParameter("searchTerm");
        int page;
        int pageSize;

        try {
            String pageStr = req.getParameter("page");
            String pageSizeStr = req.getParameter("pageSize");
            page = (pageStr != null && !pageStr.isEmpty()) ? Integer.parseInt(pageStr) : 1;
            pageSize = (pageSizeStr != null && !pageSizeStr.isEmpty()) ? Integer.parseInt(pageSizeStr) : 10;

            if (page < 1) {
                throw new NumberFormatException("Page must be at least 1");
            }
            if (pageSize < 1 || pageSize > 100) {
                throw new NumberFormatException("PageSize must be between 1 and 100");
            }
        } catch (NumberFormatException e) {
            try (PrintWriter out = resp.getWriter()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", "Invalid page or pageSize parameter: " + e.getMessage());
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
            return;
        }

        try {
            if (startDate != null && !startDate.trim().isEmpty()) {
                LocalDate.parse(startDate, DATE_FORMATTER);
            } else {
                startDate = null;
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                LocalDate.parse(endDate, DATE_FORMATTER);
            } else {
                endDate = null;
            }
        } catch (DateTimeParseException e) {
            try (PrintWriter out = resp.getWriter()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", "Invalid date format. Use YYYY-MM-DD.");
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
            return;
        }

        JsonObject responseJson = new JsonObject();
        long startTime = System.currentTimeMillis();

        try (PrintWriter out = resp.getWriter()) {
            JsonObject data = new JsonObject();
            String pathInfo = req.getPathInfo() != null ? req.getPathInfo() : "/";

            if (pathInfo.equals("/")) {
                data.add("totalRevenue", gson.toJsonTree(dao.getTotalRevenue(startDate, endDate)));

                JsonObject topServicesData = new JsonObject();
                topServicesData.add("items", gson.toJsonTree(dao.getTopServices(startDate, endDate, searchTerm, page, pageSize)));
                topServicesData.addProperty("totalRecords", dao.countTopServices(startDate, endDate, searchTerm));
                data.add("topServices", topServicesData);

                data.add("topRevenueMonths", gson.toJsonTree(dao.getTopRevenueMonths(startDate, endDate)));
                data.add("revenueByType", gson.toJsonTree(dao.getRevenueByType(startDate, endDate)));

                JsonObject topDoctorsData = new JsonObject();
                topDoctorsData.add("items", gson.toJsonTree(dao.getTopDoctors(startDate, endDate, searchTerm, page, pageSize)));
                topDoctorsData.addProperty("totalRecords", dao.countTopDoctors(startDate, endDate, searchTerm));
                data.add("topDoctors", topDoctorsData);

                JsonObject revenueByDepartmentData = new JsonObject();
                revenueByDepartmentData.add("items", gson.toJsonTree(dao.getRevenueByDepartment(startDate, endDate, searchTerm, page, pageSize)));
                revenueByDepartmentData.addProperty("totalRecords", dao.countRevenueByDepartment(startDate, endDate, searchTerm));
                data.add("revenueByDepartment", revenueByDepartmentData);

                data.add("invoiceStatus", gson.toJsonTree(dao.getInvoiceStatus(startDate, endDate)));

                JsonObject params = new JsonObject();
                if (startDate != null) params.addProperty("startDate", startDate);
                if (endDate != null) params.addProperty("endDate", endDate);
                if (searchTerm != null) params.addProperty("searchTerm", searchTerm);
                params.addProperty("currentPage", page);
                params.addProperty("pageSize", pageSize);
                responseJson.add("parameters", params);
                responseJson.add("data", data);
            } else {
                switch (pathInfo) {
                    case "/total":
                        TotalRevenueDTO totalRevenue = dao.getTotalRevenue(startDate, endDate);
                        data.add("totalRevenue", gson.toJsonTree(totalRevenue != null ? totalRevenue : new TotalRevenueDTO(0)));
                        break;
                    case "/top-services":
                        data.add("items", gson.toJsonTree(dao.getTopServices(startDate, endDate, searchTerm, page, pageSize)));
                        data.addProperty("totalRecords", dao.countTopServices(startDate, endDate, searchTerm));
                        break;
                    case "/top-months":
                        data.add("topRevenueMonths", gson.toJsonTree(dao.getTopRevenueMonths(startDate, endDate)));
                        break;
                    case "/revenue-by-type":
                        data.add("revenueByType", gson.toJsonTree(dao.getRevenueByType(startDate, endDate)));
                        break;
                    case "/top-doctors":
                        data.add("items", gson.toJsonTree(dao.getTopDoctors(startDate, endDate, searchTerm, page, pageSize)));
                        data.addProperty("totalRecords", dao.countTopDoctors(startDate, endDate, searchTerm));
                        break;
                    case "/revenue-by-department":
                        data.add("items", gson.toJsonTree(dao.getRevenueByDepartment(startDate, endDate, searchTerm, page, pageSize)));
                        data.addProperty("totalRecords", dao.countRevenueByDepartment(startDate, endDate, searchTerm));
                        break;
                    case "/invoice-status":
                        data.add("invoiceStatus", gson.toJsonTree(dao.getInvoiceStatus(startDate, endDate)));
                        break;
                    default:
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        JsonObject errorResponse = new JsonObject();
                        errorResponse.addProperty("error", "Invalid endpoint");
                        out.print(gson.toJson(errorResponse));
                        out.flush();
                        return;
                }
                responseJson.add("data", data);
            }
            out.print(gson.toJson(responseJson));
            out.flush();
        } catch (Exception e) {
            logger.severe("Error processing request: " + e.getMessage());
            try (PrintWriter out = resp.getWriter()) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonObject errorResponse = new JsonObject();
                errorResponse.addProperty("error", "Failed to fetch revenue reports: " + e.getMessage());
                out.print(gson.toJson(errorResponse));
                out.flush();
            }
        }
    }

    private void handleExportRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // Lấy các tham số từ request
            String startDate = req.getParameter("startDate");
            String endDate = req.getParameter("endDate");
            String searchTerm = req.getParameter("searchTerm");
            String exportType = req.getParameter("exportType");

            // Validate exportType
            if (!"xlsx".equalsIgnoreCase(exportType)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject errorJson = new JsonObject();
                errorJson.addProperty("success", false);
                errorJson.addProperty("message", "Unsupported export type. Only 'xlsx' is supported.");
                resp.setContentType("application/json");
                try (PrintWriter out = resp.getWriter()) {
                    out.print(gson.toJson(errorJson));
                    out.flush();
                }
                return;
            }

            // Validate startDate và endDate
            try {
                if (startDate != null && !startDate.trim().isEmpty()) {
                    LocalDate.parse(startDate, DATE_FORMATTER);
                } else {
                    startDate = null;
                }
                if (endDate != null && !endDate.trim().isEmpty()) {
                    LocalDate.parse(endDate, DATE_FORMATTER);
                } else {
                    endDate = null;
                }
                if (startDate != null && endDate != null && LocalDate.parse(startDate, DATE_FORMATTER).isAfter(LocalDate.parse(endDate, DATE_FORMATTER))) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    JsonObject errorJson = new JsonObject();
                    errorJson.addProperty("success", false);
                    errorJson.addProperty("message", "Start date must be before or equal to end date.");
                    resp.setContentType("application/json");
                    try (PrintWriter out = resp.getWriter()) {
                        out.print(gson.toJson(errorJson));
                        out.flush();
                    }
                    return;
                }
            } catch (DateTimeParseException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject errorJson = new JsonObject();
                errorJson.addProperty("success", false);
                errorJson.addProperty("message", "Invalid date format. Use YYYY-MM-DD.");
                resp.setContentType("application/json");
                try (PrintWriter out = resp.getWriter()) {
                    out.print(gson.toJson(errorJson));
                    out.flush();
                }
                return;
            }

            // Lấy dữ liệu từ DAO (lấy tất cả dữ liệu, không phân trang)
            TotalRevenueDTO totalRevenue = dao.getTotalRevenue(startDate, endDate);
            List<TopServiceDTO> topServices = dao.getTopServices(startDate, endDate, searchTerm, 1, Integer.MAX_VALUE);
            List<MonthlyRevenueDTO> topRevenueMonths = dao.getTopRevenueMonths(startDate, endDate);
            List<RevenueByTypeDTO> revenueByType = dao.getRevenueByType(startDate, endDate);
            List<TopDoctorDTO> topDoctors = dao.getTopDoctors(startDate, endDate, searchTerm, 1, Integer.MAX_VALUE);
            List<RevenueByDepartmentDTO> revenueByDepartment = dao.getRevenueByDepartment(startDate, endDate, searchTerm, 1, Integer.MAX_VALUE);
            List<InvoiceStatusDTO> invoiceStatus = dao.getInvoiceStatus(startDate, endDate);

            // Tạo workbook Excel
            Workbook workbook = new XSSFWorkbook();

            // Tạo style cho tiêu đề
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Tạo style cho tiền tệ
            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("#,##0.00 [$VND]"));

            // Sheet Tổng Doanh Thu
            Sheet totalRevenueSheet = workbook.createSheet("Tổng Doanh Thu");
            Row filterRow = totalRevenueSheet.createRow(0);
            filterRow.createCell(0).setCellValue("Thời gian từ: " + (startDate != null ? startDate : "N/A"));
            filterRow.createCell(1).setCellValue("Đến: " + (endDate != null ? endDate : "N/A"));
            Row totalRevenueHeader = totalRevenueSheet.createRow(2);
            String[] totalRevenueHeaders = {"Tổng Doanh Thu"};
            for (int i = 0; i < totalRevenueHeaders.length; i++) {
                Cell cell = totalRevenueHeader.createCell(i);
                cell.setCellValue(totalRevenueHeaders[i]);
                cell.setCellStyle(headerStyle);
            }
            Row totalRevenueRow = totalRevenueSheet.createRow(3);
            Cell totalRevenueCell = totalRevenueRow.createCell(0);
            totalRevenueCell.setCellValue(totalRevenue != null ? totalRevenue.getTotalRevenue() : 0);
            totalRevenueCell.setCellStyle(currencyStyle);
            totalRevenueSheet.autoSizeColumn(0);

            // Sheet Tháng Doanh Thu Cao Nhất
            Sheet topMonthsSheet = workbook.createSheet("Tháng Doanh Thu");
            Row topMonthsHeader = topMonthsSheet.createRow(0);
            String[] topMonthsHeaders = {"Tháng/Năm", "Doanh Thu"};
            for (int i = 0; i < topMonthsHeaders.length; i++) {
                Cell cell = topMonthsHeader.createCell(i);
                cell.setCellValue(topMonthsHeaders[i]);
                cell.setCellStyle(headerStyle);
            }
            for (int i = 0; i < topRevenueMonths.size(); i++) {
                MonthlyRevenueDTO item = topRevenueMonths.get(i);
                Row row = topMonthsSheet.createRow(i + 1);
                row.createCell(0).setCellValue(item.getMonth() + "/" + item.getYear());
                Cell revenueCell = row.createCell(1);
                revenueCell.setCellValue(item.getMonthlyRevenue());
                revenueCell.setCellStyle(currencyStyle);
            }
            for (int i = 0; i < topMonthsHeaders.length; i++) {
                topMonthsSheet.autoSizeColumn(i);
            }

            // Sheet Top Dịch Vụ
            Sheet topServicesSheet = workbook.createSheet("Top Dịch Vụ");
            Row topServicesHeader = topServicesSheet.createRow(0);
            String[] topServicesHeaders = {"Tên Dịch Vụ", "Số Lượng", "Doanh Thu"};
            for (int i = 0; i < topServicesHeaders.length; i++) {
                Cell cell = topServicesHeader.createCell(i);
                cell.setCellValue(topServicesHeaders[i]);
                cell.setCellStyle(headerStyle);
            }
            for (int i = 0; i < topServices.size(); i++) {
                TopServiceDTO item = topServices.get(i);
                Row row = topServicesSheet.createRow(i + 1);
                row.createCell(0).setCellValue(item.getServiceName() != null ? item.getServiceName() : "N/A");
                row.createCell(1).setCellValue(item.getTotalQuantity());
                Cell revenueCell = row.createCell(2);
                revenueCell.setCellValue(item.getTotalServiceRevenue());
                revenueCell.setCellStyle(currencyStyle);
            }
            for (int i = 0; i < topServicesHeaders.length; i++) {
                topServicesSheet.autoSizeColumn(i);
            }

            // Sheet Top Bác Sĩ
            Sheet topDoctorsSheet = workbook.createSheet("Top Bác Sĩ");
            Row topDoctorsHeader = topDoctorsSheet.createRow(0);
            String[] topDoctorsHeaders = {"Tên Bác Sĩ", "Khoa", "Doanh Thu"};
            for (int i = 0; i < topDoctorsHeaders.length; i++) {
                Cell cell = topDoctorsHeader.createCell(i);
                cell.setCellValue(topDoctorsHeaders[i]);
                cell.setCellStyle(headerStyle);
            }
            for (int i = 0; i < topDoctors.size(); i++) {
                TopDoctorDTO item = topDoctors.get(i);
                Row row = topDoctorsSheet.createRow(i + 1);
                row.createCell(0).setCellValue(item.getDoctorName() != null ? item.getDoctorName() : "N/A");
                row.createCell(1).setCellValue(item.getDepartment() != null ? item.getDepartment() : "N/A");
                Cell revenueCell = row.createCell(2);
                revenueCell.setCellValue(item.getTotalRevenue());
                revenueCell.setCellStyle(currencyStyle);
            }
            for (int i = 0; i < topDoctorsHeaders.length; i++) {
                topDoctorsSheet.autoSizeColumn(i);
            }

            // Sheet Doanh Thu Theo Loại Thanh Toán
            Sheet revenueByTypeSheet = workbook.createSheet("Doanh Thu Theo Loại");
            Row revenueByTypeHeader = revenueByTypeSheet.createRow(0);
            String[] revenueByTypeHeaders = {"Loại Thanh Toán", "Doanh Thu"};
            for (int i = 0; i < revenueByTypeHeaders.length; i++) {
                Cell cell = revenueByTypeHeader.createCell(i);
                cell.setCellValue(revenueByTypeHeaders[i]);
                cell.setCellStyle(headerStyle);
            }
            for (int i = 0; i < revenueByType.size(); i++) {
                RevenueByTypeDTO item = revenueByType.get(i);
                Row row = revenueByTypeSheet.createRow(i + 1);
                row.createCell(0).setCellValue(item.getPaymentType() != null ? item.getPaymentType() : "N/A");
                Cell revenueCell = row.createCell(1);
                revenueCell.setCellValue(item.getTotalRevenueByType());
                revenueCell.setCellStyle(currencyStyle);
            }
            for (int i = 0; i < revenueByTypeHeaders.length; i++) {
                revenueByTypeSheet.autoSizeColumn(i);
            }

            // Sheet Tỷ Lệ Hoàn Thành Hóa Đơn
            Sheet invoiceStatusSheet = workbook.createSheet("Tỷ Lệ Hóa Đơn");
            Row invoiceStatusHeader = invoiceStatusSheet.createRow(0);
            String[] invoiceStatusHeaders = {"Trạng Thái", "Số Hóa Đơn", "Tổng Số Tiền", "Tỷ Lệ"};
            for (int i = 0; i < invoiceStatusHeaders.length; i++) {
                Cell cell = invoiceStatusHeader.createCell(i);
                cell.setCellValue(invoiceStatusHeaders[i]);
                cell.setCellStyle(headerStyle);
            }
            for (int i = 0; i < invoiceStatus.size(); i++) {
                InvoiceStatusDTO item = invoiceStatus.get(i);
                Row row = invoiceStatusSheet.createRow(i + 1);
                row.createCell(0).setCellValue(item.getStatus() != null ? item.getStatus() : "N/A");
                row.createCell(1).setCellValue(item.getInvoiceCount());
                Cell amountCell = row.createCell(2);
                amountCell.setCellValue(item.getTotalAmount());
                amountCell.setCellStyle(currencyStyle);
                row.createCell(3).setCellValue(item.getPercentage() + "%");
            }
            for (int i = 0; i < invoiceStatusHeaders.length; i++) {
                invoiceStatusSheet.autoSizeColumn(i);
            }

            // Sheet Doanh Thu Theo Khoa
            Sheet revenueByDepartmentSheet = workbook.createSheet("Doanh Thu Theo Khoa");
            Row revenueByDepartmentHeader = revenueByDepartmentSheet.createRow(0);
            String[] revenueByDepartmentHeaders = {"Khoa", "Doanh Thu"};
            for (int i = 0; i < revenueByDepartmentHeaders.length; i++) {
                Cell cell = revenueByDepartmentHeader.createCell(i);
                cell.setCellValue(revenueByDepartmentHeaders[i]);
                cell.setCellStyle(headerStyle);
            }
            for (int i = 0; i < revenueByDepartment.size(); i++) {
                RevenueByDepartmentDTO item = revenueByDepartment.get(i);
                Row row = revenueByDepartmentSheet.createRow(i + 1);
                row.createCell(0).setCellValue(item.getDepartment() != null ? item.getDepartment() : "N/A");
                Cell revenueCell = row.createCell(1);
                revenueCell.setCellValue(item.getTotalRevenueByDepartment());
                revenueCell.setCellStyle(currencyStyle);
            }
            for (int i = 0; i < revenueByDepartmentHeaders.length; i++) {
                revenueByDepartmentSheet.autoSizeColumn(i);
            }

            // Thiết lập response headers cho tải file Excel
            String fileName = "BaoCaoDoanhThu_" + LocalDate.now().format(FILE_DATE_FORMATTER) + ".xlsx";
            resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            resp.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            // Ghi workbook vào response
            try (OutputStream out = resp.getOutputStream()) {
                workbook.write(out);
                out.flush();
            }
            workbook.close();

        } catch (Exception e) {
            logger.severe("Error exporting revenue reports: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("success", false);
            errorJson.addProperty("message", "Error exporting revenue reports: " + e.getMessage());
            resp.setContentType("application/json");
            try (PrintWriter out = resp.getWriter()) {
                out.print(gson.toJson(errorJson));
                out.flush();
            }
        }
    }
}