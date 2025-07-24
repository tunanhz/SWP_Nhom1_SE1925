package controller;

import dal.ReceptionistDAO;
import dto.PatientPaymentDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

@WebServlet({"/api/receptionistInvoices", "/api/receptionistInvoices/export"})
public class ReceptionistInvoicesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private ReceptionistDAO invoiceDAO = new ReceptionistDAO();

    private void setCORSHeaders(HttpServletResponse response, HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        // Allow only the specific origin where your client is running
        if (origin != null && origin.equals("http://127.0.0.1:5500")) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500"); // Default to your client origin
        }
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS, PUT, POST, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Allow-Credentials", "true"); // Allow credentials
        response.setHeader("Access-Control-Max-Age", "3600"); // Cache preflight response for 1 hour
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setCORSHeaders(response, request);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCORSHeaders(response, request); // Add CORS headers to all GET responses
        String path = request.getServletPath();
        if ("/api/receptionistInvoices/export".equals(path)) {
            handleExportRequest(request, response);
        } else {
            handleListRequest(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCORSHeaders(response, request); // Add CORS headers to POST responses
        JsonObject responseJson = new JsonObject();
        try {
            // Get invoiceId from request
            String invoiceIdStr = request.getParameter("invoiceId");
            if (invoiceIdStr == null || invoiceIdStr.trim().isEmpty()) {
                throw new NumberFormatException("Invoice ID is required");
            }
            int invoiceId = Integer.parseInt(invoiceIdStr);

            // Update invoice status
            boolean success = invoiceDAO.updateInvoice(invoiceId);

            responseJson.addProperty("success", success);
            responseJson.addProperty("message", success ?
                    "Invoice status updated successfully" :
                    "Failed to update invoice status");

        } catch (NumberFormatException e) {
            responseJson.addProperty("success", false);
            responseJson.addProperty("message", "Invalid invoice ID format: " + e.getMessage());
        } catch (Exception e) {
            responseJson.addProperty("success", false);
            responseJson.addProperty("message", "Error updating invoice: " + e.getMessage());
        }

        // Set response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseJson.toString());
    }

    private void handleListRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        JsonObject responseJson = new JsonObject();
        try {
            // Get query parameters and set to null if empty or not provided
            String fromDate = request.getParameter("fromDate");
            fromDate = (fromDate == null || fromDate.trim().isEmpty()) ? null : fromDate;

            String toDate = request.getParameter("toDate");
            toDate = (toDate == null || toDate.trim().isEmpty()) ? null : toDate;

            String status = request.getParameter("status");
            status = (status == null || status.trim().isEmpty()) ? null : status;

            // Get pagination parameters
            int page = Integer.parseInt(request.getParameter("page") != null && !request.getParameter("page").trim().isEmpty() ? request.getParameter("page") : "1");
            int pageSize = Integer.parseInt(request.getParameter("pageSize") != null && !request.getParameter("pageSize").trim().isEmpty() ? request.getParameter("pageSize") : "10");

            // Get invoices and total count
            ArrayList<PatientPaymentDTO> invoices = invoiceDAO.getPatientInvoices(fromDate, toDate, status, page, pageSize);
            int totalInvoices = invoiceDAO.getTotalInvoices(fromDate, toDate, status);

            // Convert to JSON
            Gson gson = new Gson();
            responseJson.addProperty("success", true);
            responseJson.add("invoices", gson.toJsonTree(invoices));
            responseJson.addProperty("totalInvoices", totalInvoices);
            responseJson.addProperty("page", page);
            responseJson.addProperty("pageSize", pageSize);

        } catch (NumberFormatException e) {
            responseJson.addProperty("success", false);
            responseJson.addProperty("message", "Invalid page or pageSize format");
        } catch (Exception e) {
            responseJson.addProperty("success", false);
            responseJson.addProperty("message", "Error retrieving invoices: " + e.getMessage());
        }

        // Set response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(responseJson.toString());
    }

    private void handleExportRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Get query parameters
            String fromDate = request.getParameter("fromDate");
            fromDate = (fromDate == null || fromDate.trim().isEmpty()) ? null : fromDate;

            String toDate = request.getParameter("toDate");
            toDate = (toDate == null || toDate.trim().isEmpty()) ? null : toDate;

            String status = request.getParameter("status");
            status = (status == null || status.trim().isEmpty()) ? null : status;

            String exportType = request.getParameter("exportType");
            if (!"xlsx".equalsIgnoreCase(exportType)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject errorJson = new JsonObject();
                errorJson.addProperty("success", false);
                errorJson.addProperty("message", "Unsupported export type. Only 'xlsx' is supported.");
                response.setContentType("application/json");
                response.getWriter().write(errorJson.toString());
                return;
            }

            // Fetch all invoices (no pagination for export)
            ArrayList<PatientPaymentDTO> invoices = invoiceDAO.getPatientInvoices(fromDate, toDate, status, 1, Integer.MAX_VALUE);

            // Create Excel workbook
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Invoices");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"No.", "Patient Name", "Patient Phone", "Issue Date", "Total Cost", "Status", "Service Details", "Medicine Details"};
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy"));

            for (int i = 0; i < invoices.size(); i++) {
                PatientPaymentDTO invoice = invoices.get(i);
                Row row = sheet.createRow(i + 1);
                row.createCell(0).setCellValue(i + 1);
                row.createCell(1).setCellValue(invoice.getPatient() != null ? invoice.getPatient().getFullName() : "-");
                row.createCell(2).setCellValue(invoice.getPatient() != null ? invoice.getPatient().getPhone() : "-");
                Cell dateCell = row.createCell(3);
                dateCell.setCellValue(invoice.getIssueDate());
                dateCell.setCellStyle(dateStyle);
                row.createCell(4).setCellValue(Double.parseDouble(invoice.getInvoiceTotalAmount()));
                row.createCell(5).setCellValue(invoice.getInvoiceStatus());
                row.createCell(6).setCellValue(invoice.getServiceDetail() != null ? invoice.getServiceDetail() : "-");
                row.createCell(7).setCellValue(invoice.getMedicineDetail() != null ? invoice.getMedicineDetail() : "-");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Set response headers for Excel download
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=invoices_report.xlsx");

            // Write workbook to response output stream
            try (OutputStream out = response.getOutputStream()) {
                workbook.write(out);
                out.flush();
            }
            workbook.close();

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("success", false);
            errorJson.addProperty("message", "Error exporting invoices: " + e.getMessage());
            response.setContentType("application/json");
            response.getWriter().write(errorJson.toString());
        }
    }
}