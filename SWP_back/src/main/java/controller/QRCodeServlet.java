package controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.imageio.ImageIO;

@WebServlet("/api/generateQR/*")
public class QRCodeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCORSHeaders(response);
        try {
            // Lấy tham số từ request
            String bankCode = request.getParameter("bankCode");
            String bankAccount = request.getParameter("bankAccount");
            String amount = request.getParameter("amount");
            String message = request.getParameter("message");

            // Kiểm tra tham số
            if (bankCode == null || bankAccount == null || amount == null || message == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu tham số bắt buộc");
                return;
            }

            // Tạo chuỗi mã QR
            String qrCodeContent = generateQRCodeContent(bankCode, bankAccount, amount, message);

            // Thiết lập phản hồi
            response.setContentType("image/png");

            // Tạo và gửi hình ảnh QR
            int width = 300;
            int height = 300;
            OutputStream outputStream = response.getOutputStream();
            generateQRCodeImage(qrCodeContent, width, height, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi tạo mã QR: " + e.getMessage());
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response); // Xử lý POST giống GET
    }

    private String generateQRCodeContent(String bankCode, String bankAccount, String amount, String message) {
        Map<String, String> bankIdByCode = new HashMap<>();
        bankIdByCode.put("vcb", "970436");
        bankIdByCode.put("vietinbank", "970415");
        bankIdByCode.put("MB", "970422");
        bankIdByCode.put("BIDV", "970418");
        bankIdByCode.put("Agribank", "970405");
        bankIdByCode.put("OCB", "970448");
        bankIdByCode.put("ACB", "970416");
        bankIdByCode.put("VPBank", "970432");
        bankIdByCode.put("TPBank", "970423");
        bankIdByCode.put("HDBank", "970437");
        bankIdByCode.put("VietCapitalBank", "970454");
        bankIdByCode.put("scb", "970429");
        bankIdByCode.put("vib", "970441");
        bankIdByCode.put("shb", "970443");
        bankIdByCode.put("Eximbank", "970431");
        bankIdByCode.put("msb", "970426");
        bankIdByCode.put("cake", "546034");

        String bankId = bankIdByCode.get(bankCode);
        if (bankId == null) {
            throw new IllegalArgumentException("Mã ngân hàng không hợp lệ: " + bankCode);
        }
        StringBuilder part12Builder = new StringBuilder()
                .append("00")
                .append(String.format("%02d", bankId.length()))
                .append(bankId)
                .append("01")
                .append(String.format("%02d", bankAccount.length()))
                .append(bankAccount);

        StringBuilder part11Builder = new StringBuilder()
                .append("0010A000000727")
                .append("01")
                .append(String.format("%02d", part12Builder.length()))
                .append(part12Builder)
                .append("0208QRIBFTTA");

        StringBuilder part1Builder = new StringBuilder()
                .append("38")
                .append(String.format("%02d", part11Builder.length()))
                .append(part11Builder);

        StringBuilder part21Builder = new StringBuilder()
                .append("08")
                .append(String.format("%02d", message.length()))
                .append(message);

        String part2 = new StringBuilder()
                .append("5303704")
                .append("54")
                .append(String.format("%02d", amount.length()))
                .append(amount)
                .append("5802VN")
                .append("62")
                .append(String.format("%02d", part21Builder.length()))
                .append(part21Builder)
                .toString();

        StringBuilder builder = new StringBuilder()
                .append("000201")
                .append("010212")
                .append(part1Builder)
                .append(part2)
                .append("6304");

        return builder.append(generateCheckSum(builder.toString()).toUpperCase()).toString();
    }

    private String generateCheckSum(String text) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;
        byte[] bytes = text.getBytes();
        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }
        return String.format("%04X", crc & 0xFFFF);
    }

    private void generateQRCodeImage(String qrCodeContent, int width, int height, OutputStream outputStream)
            throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeContent, BarcodeFormat.QR_CODE, width, height, hints);

        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCORSHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }
}