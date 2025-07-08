package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Servlet to generate a PDF medical record based on JSON input.
 */
@WebServlet("/api/generate-pdf")
public class GeneratePDFServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneratePDFServlet.class);
    private static final String DEFAULT_FONT_PATH = "fonts/arial.ttf";
    private static final String FALLBACK_SYSTEM_FONT = "C:/Windows/Fonts/Arial.ttf";
    private static final String PDF_FILENAME = "patient_record.pdf";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CONTENT_TYPE_PDF = "application/pdf";

    // Define custom colors
    private static final Color PRIMARY_COLOR = new DeviceRgb(41, 128, 185); // Nice blue
    private static final Color SECONDARY_COLOR = new DeviceRgb(44, 62, 80); // Dark blue-gray
    private static final Color TABLE_HEADER_COLOR = new DeviceRgb(236, 240, 241); // Light gray
    private static final Color TABLE_BORDER_COLOR = new DeviceRgb(189, 195, 199); // Medium gray

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCORSHeaders(response);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        try (InputStream inputStream = request.getInputStream()) {
            String jsonString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            LOGGER.debug("Received JSON: {}", jsonString);

            JsonObject patientData = parseJson(jsonString);
            generatePDF(response, patientData);
        } catch (JsonSyntaxException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid JSON format: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Error generating PDF", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error generating PDF: " + e.getMessage());
        }
    }

    /**
     * Parses JSON string into a JsonObject.
     *
     * @param jsonString The JSON string to parse.
     * @return Parsed JsonObject.
     * @throws JsonSyntaxException If JSON is invalid.
     */
    private JsonObject parseJson(String jsonString) throws JsonSyntaxException {
        return new Gson().fromJson(jsonString, JsonObject.class);
    }

    /**
     * Generates a PDF document based on patient data.
     *
     * @param response    HttpServletResponse to write the PDF.
     * @param patientData JsonObject containing patient data.
     * @throws IOException If PDF generation fails.
     */
    private void generatePDF(HttpServletResponse response, JsonObject patientData) throws IOException {
        response.setContentType(CONTENT_TYPE_PDF);
        response.setHeader("Content-Disposition", "attachment; filename=" + PDF_FILENAME);

        try (PdfWriter writer = new PdfWriter(response.getOutputStream());
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            // Set document margins
            document.setMargins(40, 40, 40, 40);

            PdfFont font = loadFont();
            addDocumentContent(document, patientData, font);
        }
    }

    /**
     * Loads a font supporting Vietnamese characters.
     *
     * @return PdfFont instance.
     * @throws IOException If font loading fails.
     */
    private PdfFont loadFont() throws IOException {
        try {
            return PdfFontFactory.createFont(FALLBACK_SYSTEM_FONT);
        } catch (IOException e) {
            LOGGER.warn("System font not found, attempting to load embedded font");
            String fontPath = getClass().getClassLoader().getResource(DEFAULT_FONT_PATH).getPath();
            if (fontPath == null) {
                throw new IOException("Font file not found: " + DEFAULT_FONT_PATH);
            }
            return PdfFontFactory.createFont(fontPath, "Identity-H");
        }
    }

    /**
     * Adds content to the PDF document.
     *
     * @param document    The PDF document.
     * @param patientData JsonObject containing patient data.
     * @param font        The font to use.
     */
    private void addDocumentContent(Document document, JsonObject patientData, PdfFont font) {

        // Hospital name and date
        document.add(new Paragraph("\nBệnh viện: SỞ Y TẾ THÀNH PHỐ HÀ NỘI - PHÒNG KHÁM ĐA KHOA KIVICARE")
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));
        document.add(new Paragraph("Ngày: 26/07/2025")
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER));

        // Title
        document.add(new Paragraph("HỒ SƠ Y TẾ BỆNH NHÂN")
                .setFont(font)
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setFontColor(PRIMARY_COLOR)
                .setMarginBottom(30));

        // Patient Info
        addSectionTitle(document, font, "THÔNG TIN BỆNH NHÂN");
        addPatientInfo(document, font, patientData);
        addSeparator(document);

        // Diagnoses Table
        addSectionTitle(document, font, "CHẨN ĐOÁN");
        addTable(document, font, new float[]{20, 20, 20, 20, 20},
                new String[]{"Ngày", "Bác Sĩ", "Bệnh", "Kết Luận", "Kế Hoạch Điều Trị"},
                new String[]{"diagnosisDate", "diagnosisDoctorName", "disease", "conclusion", "treatmentPlan"},
                patientData);
        addSeparator(document);

        // Exam Results Table
        addSectionTitle(document, font, "KẾT QUẢ KHÁM");
        addTable(document, font, new float[]{25, 25, 25, 25},
                new String[]{"Ngày", "Bác Sĩ", "Triệu Chứng", "Chẩn Đoán Sơ Bộ"},
                new String[]{"examDate", "examDoctorName", "symptoms", "preliminaryDiagnosis"},
                patientData);
        addSeparator(document);

        // Appointments Table
        addSectionTitle(document, font, "LỊCH HẸN");
        addTable(document, font, new float[]{25, 25, 25, 25},
                new String[]{"Ngày", "Bác Sĩ", "Ca", "Trạng Thái"},
                new String[]{"appointmentDatetime", "appointmentDoctorName", "shift", "appointmentStatus"},
                patientData);
        addSeparator(document);

        // Prescriptions Table
        addSectionTitle(document, font, "ĐƠN THUỐC");
        document.add(new Paragraph("Ngày: " + safeGetString(patientData, "prescriptionDate", "N/A") +
                " | Trạng Thái: " + safeGetString(patientData, "prescriptionStatus", "N/A"))
                .setFont(font)
                .setFontSize(11)
                .setFontColor(SECONDARY_COLOR)
                .setMarginBottom(10));

        addTable(document, font, new float[]{33, 33, 33},
                new String[]{"Thuốc", "Số Lượng", "Liều Lượng"},
                new String[]{"medicineName", "medicineQuantity", "medicineDosage"},
                patientData);

        // Hospital name and date
        document.add(new Paragraph("\nNgày 26 tháng 07 năm 2025")
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(20));
        // Hospital name and date
        document.add(new Paragraph("\nNgười Thực Hiện")
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(20));
    }

    /**
     * Adds a section title to the document.
     *
     * @param document The PDF document.
     * @param font     The font to use.
     * @param title    The title text.
     */
    private void addSectionTitle(Document document, PdfFont font, String title) {
        document.add(new Paragraph(title)
                .setFont(font)
                .setFontSize(16)
                .setBold()
                .setFontColor(SECONDARY_COLOR)
                .setMarginTop(20)
                .setMarginBottom(10));
    }

    /**
     * Adds a separator to the document.
     *
     * @param document The PDF document.
     */
    private void addSeparator(Document document) {
        Table separator = new Table(1)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(10)
                .setMarginBottom(10);

        Cell cell = new Cell()
                .setBorder(new SolidBorder(TABLE_BORDER_COLOR, 1))
                .setBorderLeft(null)
                .setBorderRight(null)
                .setHeight(1)
                .setPadding(0);

        separator.addCell(cell);
        document.add(separator);
    }

    /**
     * Adds patient information to the document.
     *
     * @param document    The PDF document.
     * @param font        The font to use.
     * @param patientData JsonObject containing patient data.
     */
    private void addPatientInfo(Document document, PdfFont font, JsonObject patientData) {
        Table infoTable = new Table(2)
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        String[][] fields = {
                {"Họ và Tên", safeGetString(patientData, "patientName", "N/A")},
                {"Ngày Sinh", safeGetString(patientData, "dob", "N/A")},
                {"Giới Tính", safeGetString(patientData, "gender", "N/A")},
                {"Số Điện Thoại", safeGetString(patientData, "phone", "N/A")},
                {"Địa Chỉ", safeGetString(patientData, "address", "N/A")}
        };

        for (String[] field : fields) {
            infoTable.addCell(new Cell()
                    .add(new Paragraph(field[0])
                            .setFont(font)
                            .setFontSize(11)
                            .setFontColor(SECONDARY_COLOR)
                            .setBold())
                    .setBorder(null)
                    .setPadding(5));

            infoTable.addCell(new Cell()
                    .add(new Paragraph(field[1])
                            .setFont(font)
                            .setFontSize(11))
                    .setBorder(null)
                    .setPadding(5));
        }

        document.add(infoTable);
    }

    /**
     * Adds a table to the document.
     *
     * @param document     The PDF document.
     * @param font         The font to use.
     * @param columnWidths Column width percentages.
     * @param headers      Table headers.
     * @param keys         JSON keys for data.
     * @param patientData  JsonObject containing patient data.
     */
    private void addTable(Document document, PdfFont font, float[] columnWidths, String[] headers, String[] keys, JsonObject patientData) {
        Table table = new Table(UnitValue.createPercentArray(columnWidths))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(20);

        // Add headers
        for (String header : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(header)
                            .setFont(font)
                            .setFontSize(11)
                            .setBold())
                    .setBackgroundColor(TABLE_HEADER_COLOR)
                    .setBorder(new SolidBorder(TABLE_BORDER_COLOR, 1))
                    .setPadding(8)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        // Add data
        for (String key : keys) {
            table.addCell(new Cell()
                    .add(new Paragraph(safeGetString(patientData, key, "N/A"))
                            .setFont(font)
                            .setFontSize(11))
                    .setBorder(new SolidBorder(TABLE_BORDER_COLOR, 1))
                    .setPadding(8));
        }

        document.add(table);
    }

    /**
     * Safely retrieves a string value from a JsonObject.
     *
     * @param json         The JsonObject to query.
     * @param key          The key to retrieve.
     * @param defaultValue The default value if key is missing or null.
     * @return The string value or default.
     */
    private String safeGetString(JsonObject json, String key, String defaultValue) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : defaultValue;
    }

    /**
     * Sends an error response in JSON format.
     *
     * @param response The HttpServletResponse.
     * @param status   The HTTP status code.
     * @param message  The error message.
     * @throws IOException If response writing fails.
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType(CONTENT_TYPE_JSON);
        response.setStatus(status);
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }

    /**
     * Sets CORS headers for the response.
     *
     * @param response The HttpServletResponse.
     */
    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept");
        response.setHeader("Access-Control-Max-Age", "86400");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCORSHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}