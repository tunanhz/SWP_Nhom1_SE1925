
package controller;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import dal.FeedbackDAO;
import model.Feedback;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@WebServlet("/api/AdminBusinessFeedback/export")
public class ExportFeedbackServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ExportFeedbackServlet.class);
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Get query parameters
        setCORSHeaders(response);
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String minAvgFeedback = request.getParameter("minAvgFeedback");
        String maxAvgFeedback = request.getParameter("maxAvgFeedback");
        String exportType = request.getParameter("exportType");

        // Validate exportType
        if (exportType == null || (!exportType.equalsIgnoreCase("pdf") && !exportType.equalsIgnoreCase("excel"))) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid export type. Use 'pdf' or 'excel'.");
            return;
        }

        // Validate dates
        if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setLenient(false);
                Date start = dateFormat.parse(startDate);
                Date end = dateFormat.parse(endDate);
                if (start.after(end)) {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Start date must be before end date.");
                    return;
                }
            } catch (Exception e) {
                logger.error("Invalid date format: startDate={}, endDate={}", startDate, endDate, e);
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid date format. Use yyyy-MM-dd.");
                return;
            }
        }

        // Validate feedback ratings
        try {
            Double min = minAvgFeedback != null && !minAvgFeedback.isEmpty() ? Double.parseDouble(minAvgFeedback) : null;
            Double max = maxAvgFeedback != null && !maxAvgFeedback.isEmpty() ? Double.parseDouble(maxAvgFeedback) : null;
            if (min != null && (min < 1 || min > 5)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Min average feedback must be between 1 and 5.");
                return;
            }
            if (max != null && (max < 1 || max > 5)) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Max average feedback must be between 1 and 5.");
                return;
            }
            if (min != null && max != null && min > max) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Min average feedback must be less than max.");
                return;
            }
        } catch (NumberFormatException e) {
            logger.error("Invalid feedback rating format: minAvgFeedback={}, maxAvgFeedback={}", minAvgFeedback, maxAvgFeedback, e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Feedback ratings must be valid numbers.");
            return;
        }

        // Fetch feedback data
        List<Feedback> feedbacks;
        try {
            feedbacks = fetchFeedbackData(startDate, endDate, minAvgFeedback, maxAvgFeedback);
        } catch (Exception e) {
            logger.error("Error fetching feedback data", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error fetching feedback data: " + e.getMessage());
            return;
        }

        try (OutputStream out = response.getOutputStream()) {
            if (exportType.equalsIgnoreCase("pdf")) {
                exportToPDF(response, feedbacks, out);
            } else {
                exportToExcel(response, feedbacks, out);
            }
        } catch (Exception e) {
            logger.error("Error generating report: exportType={}", exportType, e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating report: " + e.getMessage());
        }
    }

    private void exportToPDF(HttpServletResponse response, List<Feedback> feedbacks, OutputStream out) throws DocumentException, IOException {
        // Set response headers
        setCORSHeaders(response);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=feedback_report.pdf");

        // Create PDF document
        Document document = new Document();
        PdfWriter.getInstance(document, out);
        document.open();

        // Add title with Unicode support
        BaseFont bf;
        try {
            bf = BaseFont.createFont("C:/Windows/Fonts/times.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        } catch (Exception e) {
            logger.warn("Times New Roman font not found, falling back to Helvetica", e);
            bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        }
        Font titleFont = new Font(bf, 16, Font.BOLD);
        Paragraph title = new Paragraph("Patient Feedback Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        // Add generation date
        Font normalFont = new Font(bf, 12, Font.NORMAL);
        Paragraph date = new Paragraph("Generated on: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()), normalFont);
        date.setAlignment(Element.ALIGN_CENTER);
        document.add(date);
        document.add(Chunk.NEWLINE);

        // Create table
        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 2, 3, 1, 1, 1, 1, 1, 2});

        // Add table headers
        String[] headers = {"ID", "Patient", "Content", "Service", "Doctor", "Receptionist", "Pharmacist", "AVG", "Date Sent"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, new Font(bf, 10, Font.BOLD)));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Add table data
        for (Feedback fb : feedbacks) {
            fb.includePatient(); // Ensure patient data is loaded
            table.addCell(new PdfPCell(new Phrase(String.valueOf(fb.getFeedbackId()), normalFont)));
            table.addCell(new PdfPCell(new Phrase(fb.getPatient() != null ? sanitize(fb.getPatient().getFullName()) : "-", normalFont)));
            table.addCell(new PdfPCell(new Phrase(fb.getContent() != null ? sanitize(fb.getContent()) : "-", normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(fb.getServiceRating()), normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(fb.getDoctorRating()), normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(fb.getReceptionistRating()), normalFont)));
            table.addCell(new PdfPCell(new Phrase(String.valueOf(fb.getPharmacistRating()), normalFont)));
            table.addCell(new PdfPCell(new Phrase(fb.getAvgFeedback() != null ? String.format("%.1f", fb.getAvgFeedback()) : "-", normalFont)));
            table.addCell(new PdfPCell(new Phrase(formatTimestamp(fb.getCreatedAt()), normalFont)));
        }

        document.add(table);
        document.close();
    }

    private void exportToExcel(HttpServletResponse response, List<Feedback> feedbacks, OutputStream out) throws IOException {
        // Create workbook
        setCORSHeaders(response);
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Feedback Report");

        // Create font for Vietnamese support
        Font excelFont;
        try {
            excelFont = (Font) workbook.createFont();
        } catch (Exception e) {
            logger.warn("Error creating Times New Roman font, falling back to Arial", e);
        }

        // Create cell styles
        CellStyle headerStyle = workbook.createCellStyle();
        CellStyle cellStyle = workbook.createCellStyle();

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Patient", "Content", "Service", "Doctor", "Receptionist", "Pharmacist", "AVG", "Date Sent"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Add data rows
        try {
            int rowNum = 1;
            for (Feedback fb : feedbacks) {
                fb.includePatient(); // Ensure patient data is loaded
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(fb.getFeedbackId());
                row.createCell(1).setCellValue(fb.getPatient() != null ? sanitize(fb.getPatient().getFullName()) : "-");
                row.createCell(2).setCellValue(fb.getContent() != null ? sanitize(fb.getContent()) : "-");
                row.createCell(3).setCellValue(fb.getServiceRating());
                row.createCell(4).setCellValue(fb.getDoctorRating());
                row.createCell(5).setCellValue(fb.getReceptionistRating());
                row.createCell(6).setCellValue(fb.getPharmacistRating());
                row.createCell(7).setCellValue(fb.getAvgFeedback() != null ? fb.getAvgFeedback() : 0);
                row.createCell(8).setCellValue(formatTimestamp(fb.getCreatedAt()));
                // Apply cell style to each cell
                for (int i = 0; i < 9; i++) {
                    Cell cell = row.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    cell.setCellStyle(cellStyle);
                }
            }
        } catch (Exception e) {
            logger.error("Error populating Excel data", e);
            throw new IOException("Failed to populate Excel data: " + e.getMessage());
        }

        // Auto-size columns
        try {
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
        } catch (Exception e) {
            logger.warn("Error auto-sizing columns, using fixed widths", e);
            int[] columnWidths = {3000, 8000, 12000, 3000, 3000, 3000, 3000, 3000, 6000};
            for (int i = 0; i < headers.length; i++) {
                sheet.setColumnWidth(i, columnWidths[i]);
            }
        }

        // Set response headers
        response.setContentType("application/vnd.ms-excel; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=feedback_report.xls");

        // Write to output stream
        try {
            workbook.write(out);
        } finally {
            workbook.close();
        }
    }

    private ArrayList<Feedback> fetchFeedbackData(String startDate, String endDate, String minAvgFeedback, String maxAvgFeedback) {
        FeedbackDAO feedbackDAO = new FeedbackDAO();
        Timestamp start = null;
        Timestamp end = null;

        // Validate and parse dates
        if (startDate != null && !startDate.isEmpty()) {
            if (!DATE_PATTERN.matcher(startDate).matches()) {
                throw new IllegalArgumentException("Invalid start date format. Use yyyy-MM-dd.");
            }
            try {
                start = Timestamp.valueOf(startDate + " 00:00:00");
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Error parsing start date: " + startDate, e);
            }
        }
        if (endDate != null && !endDate.isEmpty()) {
            if (!DATE_PATTERN.matcher(endDate).matches()) {
                throw new IllegalArgumentException("Invalid end date format. Use yyyy-MM-dd.");
            }
            try {
                end = Timestamp.valueOf(endDate + " 23:59:59");
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Error parsing end date: " + endDate, e);
            }
        }

        Double min = minAvgFeedback != null && !minAvgFeedback.isEmpty() ? Double.parseDouble(minAvgFeedback) : null;
        Double max = maxAvgFeedback != null && !maxAvgFeedback.isEmpty() ? Double.parseDouble(maxAvgFeedback) : null;

        return feedbackDAO.getFeedbackByFilters(start, end, min, max, 1, Integer.MAX_VALUE);
    }

    private String formatTimestamp(Timestamp timestamp) {
        if (timestamp == null) return "-";
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
        return outputFormat.format(timestamp);
    }

    private String sanitize(String input) {
        return input != null ? input.replaceAll("[<>\"&]", "") : "-";
    }

    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setStatus(statusCode);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":\"error\",\"message\":\"" + message + "\"}");
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCORSHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }
}