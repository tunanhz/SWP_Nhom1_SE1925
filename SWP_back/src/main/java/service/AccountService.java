package service;

import dal.AccountDAO;
import dto.ResetPasswordRequestDTO;
import dto.ResponseDTO;
import dto.VerifyOTPRequestDTO;
import org.mindrot.jbcrypt.BCrypt;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

public class AccountService {
    private final AccountDAO accountDAO = new AccountDAO();
    private static final Map<String, Map<String, Object>> otpStore = new HashMap<>();
    private static final long OTP_EXPIRY_TIME = 5 * 60 * 1000; // set time 5 minutes

    public ResponseDTO requestPasswordReset(ResetPasswordRequestDTO request) {
        String email = request.getEmail();
        if (email == null || email.trim().isEmpty()) {
            return new ResponseDTO(false, null, "Email is required");
        }

        String accountType = accountDAO.isEmailExists(email);
        if (accountType == null) {
            return new ResponseDTO(false, null, "Email not found");
        }

        String otp = generateOTP();
        Map<String, Object> otpData = new HashMap<>();
        otpData.put("otp", otp);
        otpData.put("expiry", System.currentTimeMillis() + OTP_EXPIRY_TIME);
        otpData.put("accountType", accountType);
        otpStore.put(email, otpData);

        sendEmail(email, "Your OTP Code", "Your OTP code is: " + otp);
        return new ResponseDTO(true, "Một mã OTP đã được gửi tới email của bạn.", null);
    }

    public ResponseDTO verifyOTPAndSetPassword(VerifyOTPRequestDTO request) {
        String email = request.getEmail();
        String otp = request.getOtp();
        String password = request.getPassword();
        String confirmPassword = request.getConfirmPassword();

        if (email == null || otp == null || password == null || confirmPassword == null ||
                email.trim().isEmpty() || otp.trim().isEmpty() || password.trim().isEmpty() || confirmPassword.trim().isEmpty()) {
            return new ResponseDTO(false, null, "All fields are required");
        }

        if (!password.equals(confirmPassword)) {
            return new ResponseDTO(false, null, "Mật khẩu không khớp");
        }

        Map<String, Object> otpData = otpStore.get(email);
        if (otpData == null || !otpData.get("otp").equals(otp) || (Long) otpData.get("expiry") < System.currentTimeMillis()) {
            return new ResponseDTO(false, null, "OTP không hợp lệ hoặc hết hạn");
        }

        String accountType = (String) otpData.get("accountType");
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));

        accountDAO.updatePassword(email, hashedPassword, accountType);
        otpStore.remove(email);
        return new ResponseDTO(true, "Mật khẩu của bạn đã được cập nhật.", null);
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }

    private void sendEmail(String to, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("hospital.swp@gmail.com", "gmjxoepgcnyevxva");
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("hospital.swp@gmail.com"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending email: " + e.getMessage());
        }
    }
}