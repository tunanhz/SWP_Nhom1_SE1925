package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dal.AccountDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import model.AccountPatient;
import model.AccountStaff;
import model.AccountPharmacist;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

@WebServlet("/api/account-management/*")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 1, // 1 MB
        maxFileSize = 1024 * 1024 * 10,     // 10 MB
        maxRequestSize = 1024 * 1024 * 50    // 50 MB
)
public class AccountManagementServlet extends HttpServlet {
    private final AccountDAO dao = new AccountDAO();
    private final AtomicLong counter = new AtomicLong();
    private final Gson gson = new Gson();

    // Helper method to get value from Part
    private String getPartValue(HttpServletRequest req, String partName) throws IOException, ServletException {
        Part part = req.getPart(partName);
        if (part != null && part.getSize() > 0) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream(), "UTF-8"));
            StringBuilder value = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                value.append(line);
            }
            String result = value.toString().trim();
            System.out.println("Part " + partName + ": " + result);
            return result.isEmpty() ? null : result;
        }
        System.out.println("Part " + partName + ": null");
        return null;
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            try {
                ArrayList<AccountPatient> patientAccounts = dao.getAllPatientAccounts();
                ArrayList<AccountStaff> staffAccounts = dao.getAllStaffAccounts();
                ArrayList<AccountPharmacist> pharmacistAccounts = dao.getAllPharmacistAccounts();

                JsonObject responseJson = new JsonObject();
                responseJson.add("patients", gson.toJsonTree(patientAccounts));
                responseJson.add("staff", gson.toJsonTree(staffAccounts));
                responseJson.add("pharmacists", gson.toJsonTree(pharmacistAccounts));
                out.print(gson.toJson(responseJson));
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"Failed to fetch accounts: " + e.getMessage() + "\"}");
            }
        } else {
            String[] splits = pathInfo.split("/");
            if (splits.length == 3) {
                String accountType = splits[1];
                try {
                    int id = Integer.parseInt(splits[2]);
                    Object account = null;
                    switch (accountType) {
                        case "patient":
                            account = dao.getPatientAccountById(id);
                            break;
                        case "staff":
                            account = dao.getStaffAccountById(id);
                            break;
                        case "pharmacist":
                            account = dao.getPharmacistAccountById(id);
                            break;
                        default:
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            out.print("{\"error\":\"Invalid account type\"}");
                            out.flush();
                            return;
                    }
                    if (account != null) {
                        out.print(gson.toJson(account));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\":\"Account not found\"}");
                    }
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid ID\"}");
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid path\"}");
            }
        }
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        try {
            req.setCharacterEncoding("UTF-8");

            // Log all parts for debugging
            System.out.println("Received parts asdasdasd:");
            for (Part part : req.getParts()) {
                System.out.println("Part Name: " + part.getName() + ", Size: " + part.getSize());
            }

            // Get form fields using getPartValue
            String accountType = getPartValue(req, "accountType");
            String username = getPartValue(req, "username");
            String password = getPartValue(req, "password");
            String email = getPartValue(req, "email");
            String role = getPartValue(req, "role");
            String status = getPartValue(req, "status");

            System.err.println("asdasdsadas"+ "Account Type: " + accountType);
            // Validate required fields
            if (accountType == null || accountType.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Account type is required\"}");
                out.flush();
                return;
            }
            if (username == null || password == null || email == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Username, password, and email are required\"}");
                out.flush();
                return;
            }

            // Handle image upload
            String imgPath = null;
            Part filePart = req.getPart("img");
            if (filePart != null && filePart.getSize() > 0) {
                String fileName = filePart.getSubmittedFileName();
                String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
                if (!extension.matches("\\.(png|jpg|jpeg)")) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Only PNG, JPG, and JPEG files are allowed\"}");
                    out.flush();
                    return;
                }
                String uniqueFileName = accountType + "_" + counter.incrementAndGet() + "_" + System.currentTimeMillis() + extension;
                String uploadPath = getServletContext().getRealPath("") + File.separator + "images" + File.separator + "accounts";

                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                    throw new ServletException("Cannot create upload directory: " + uploadPath);
                }
                if (!uploadDir.canWrite()) {
                    throw new ServletException("No write permission for directory: " + uploadPath);
                }

                String fullPath = uploadPath + File.separator + uniqueFileName;
                filePart.write(fullPath);
                System.out.println("File saved to: " + fullPath);
                imgPath = "images/accounts/" + uniqueFileName;
            } else if (!accountType.equals("patient")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Image is required for staff and pharmacist accounts\"}");
                out.flush();
                return;
            }

            switch (accountType) {
                case "patient":
                    AccountPatient patient = new AccountPatient();
                    patient.setUsername(username);
                    patient.setPassword(password);
                    patient.setEmail(email);
                    patient.setImg(imgPath);
//                    patient.setStatus("true".equals(status));
                    String fullName = getPartValue(req, "fullName");
                    String dob = getPartValue(req, "dob");
                    String gender = getPartValue(req, "gender");
                    String phone = getPartValue(req, "phone");
                    String address = getPartValue(req, "address");

                    if (fullName == null || dob == null || gender == null || phone == null || address == null) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"All patient fields are required\"}");
                        out.flush();
                        return;
                    }

                    // Validate and format dob
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        sdf.setLenient(false);
                        sdf.parse(dob);
                    } catch (ParseException e) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"Invalid date format. Use YYYY-MM-DD.\"}");
                        out.flush();
                        return;
                    }

                    dao.addPatientAccount(patient, fullName, dob, gender, phone, address);
                    break;

                case "staff":
                    AccountStaff staff = new AccountStaff();
                    staff.setUserName(username);
                    staff.setPassWord(password);
                    staff.setEmail(email);
                    staff.setRole(role);
                    staff.setImg(imgPath);
                    staff.setStatus("true".equals(status));

                    if (role == null || role.isEmpty()) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"Role is required for staff accounts\"}");
                        out.flush();
                        return;
                    }

                    String staffFullName, staffPhone, department, eduLevel;
                    if (role.equals("Doctor") || role.equals("Nurse")) {
                        staffFullName = getPartValue(req, "staffFullNameDn");
                        staffPhone = getPartValue(req, "staffPhoneDn");
                        department = getPartValue(req, "departmentDn");
                        eduLevel = getPartValue(req, "eduLevelDn");
                        if (staffFullName == null || staffPhone == null || department == null || eduLevel == null) {
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            out.print("{\"error\":\"All Doctor/Nurse fields are required\"}");
                            out.flush();
                            return;
                        }
                    } else if (role.equals("Receptionist")) {
                        staffFullName = getPartValue(req, "staffFullNameR");
                        staffPhone = getPartValue(req, "staffPhoneR");
                        department = null;
                        eduLevel = null;
                        if (staffFullName == null || staffPhone == null) {
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            out.print("{\"error\":\"All Receptionist fields are required\"}");
                            out.flush();
                            return;
                        }
                    } else {
                        staffFullName = getPartValue(req, "staffFullNameA");
                        staffPhone = getPartValue(req, "staffPhoneA");
                        department = getPartValue(req, "departmentA");
                        eduLevel = null;
                        if (staffFullName == null || staffPhone == null || department == null) {
                            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            out.print("{\"error\":\"All Admin fields are required\"}");
                            out.flush();
                            return;
                        }
                    }

                    dao.addStaffAccount(staff, staffFullName, staffPhone, department, eduLevel);
                    break;

                case "pharmacist":
                    AccountPharmacist pharmacist = new AccountPharmacist();
                    pharmacist.setUsername(username);
                    pharmacist.setPassword(password);
                    pharmacist.setEmail(email);
                    pharmacist.setImg(imgPath);
                    pharmacist.setStatus("true".equals(status));
                    String pharmacistFullName = getPartValue(req, "pharmacistFullName");
                    String pharmacistPhone = getPartValue(req, "pharmacistPhone");
                    String pharmacistEduLevel = getPartValue(req, "pharmacistEduLevel");

                    if (pharmacistFullName == null || pharmacistPhone == null || pharmacistEduLevel == null) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"All pharmacist fields are required\"}");
                        out.flush();
                        return;
                    }

                    dao.addPharmacistAccount(pharmacist, pharmacistFullName, pharmacistPhone, pharmacistEduLevel);
                    break;

                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\":\"Invalid account type\"}");
                    out.flush();
                    return;
            }

            out.print("{\"message\":\"Account created successfully\"}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\":\"Failed to create account: " + (e.getMessage() != null ? e.getMessage() : "Unknown error") + "\"}");
        }
        out.flush();
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.split("/").length == 3) {
            String accountType = pathInfo.split("/")[1];
            try {
                int id = Integer.parseInt(pathInfo.split("/")[2]);
                String username = getPartValue(req, "username");
                String email = getPartValue(req, "email");
                String role = getPartValue(req, "role");
                String status = getPartValue(req, "status");

                // Log all parts for debugging
                System.out.println("Received parts for PUT:");
                for (Part part : req.getParts()) {
                    System.out.println("Part Name: " + part.getName() + ", Size: " + part.getSize());
                }

                // Handle image upload
                String imgPath = null;
                String oldImgPath = null;
                Part filePart = req.getPart("img");
                if (filePart != null && filePart.getSize() > 0) {
                    String fileName = filePart.getSubmittedFileName();
                    String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
                    if (!extension.matches("\\.(png|jpg|jpeg)")) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"Only PNG, JPG, and JPEG files are allowed\"}");
                        out.flush();
                        return;
                    }
                    String uniqueFileName = accountType + "_" + id + "_" + System.currentTimeMillis() + extension;
                    String uploadPath = getServletContext().getRealPath("") + File.separator + "images" + File.separator + "accounts";

                    File uploadDir = new File(uploadPath);
                    if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                        throw new ServletException("Cannot create upload directory: " + uploadPath);
                    }

                    String fullPath = uploadPath + File.separator + uniqueFileName;
                    filePart.write(fullPath);
                    System.out.println("File saved to: " + fullPath);
                    imgPath = "images/accounts/" + uniqueFileName;

                    // Get old image path to delete later
                    switch (accountType) {
                        case "patient":
                            AccountPatient oldPatient = dao.getPatientAccountById(id);
                            if (oldPatient != null) oldImgPath = oldPatient.getImg();
                            break;
                        case "staff":
                            AccountStaff oldStaff = dao.getStaffAccountById(id);
                            if (oldStaff != null) oldImgPath = oldStaff.getImg();
                            break;
                        case "pharmacist":
                            AccountPharmacist oldPharmacist = dao.getPharmacistAccountById(id);
                            if (oldPharmacist != null) oldImgPath = oldPharmacist.getImg();
                            break;
                    }
                }

                boolean updated = false;
                Object updatedAccount = null;
                switch (accountType) {
                    case "patient":
                        AccountPatient patient = new AccountPatient();
                        patient.setUsername(username);
                        patient.setEmail(email);
                        if (imgPath != null) patient.setImg(imgPath);
                        patient.setStatus("true".equals(status));
                        updated = dao.updatePatientAccount(id, patient);
                        updatedAccount = patient;
                        break;
                    case "staff":
                        AccountStaff staff = new AccountStaff();
                        staff.setUserName(username);
                        staff.setEmail(email);
                        staff.setRole(role);
                        if (imgPath != null) staff.setImg(imgPath);
                        staff.setStatus("true".equals(status));
                        updated = dao.updateStaffAccount(id, staff);
                        updatedAccount = staff;
                        break;
                    case "pharmacist":
                        AccountPharmacist pharmacist = new AccountPharmacist();
                        pharmacist.setUsername(username);
                        pharmacist.setEmail(email);
                        if (imgPath != null) pharmacist.setImg(imgPath);
                        pharmacist.setStatus("true".equals(status));
                        updated = dao.updatePharmacistAccount(id, pharmacist);
                        updatedAccount = pharmacist;
                        break;
                    default:
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"Invalid account type\"}");
                        out.flush();
                        return;
                }
                if (updated) {
                    // Delete old image if a new one was uploaded
                    if (oldImgPath != null && imgPath != null) {
                        File oldFile = new File(getServletContext().getRealPath("") + File.separator + oldImgPath);
                        if (oldFile.exists()) {
                            oldFile.delete();
                            System.out.println("Deleted old image: " + oldImgPath);
                        }
                    }
                    out.print(gson.toJson(updatedAccount));
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Account not found\"}");
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid ID\"}");
            } catch (Exception e) {
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"error\":\"Failed to update account: " + e.getMessage() + "\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid request\"}");
        }
        out.flush();
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.split("/").length == 3) {
            String accountType = pathInfo.split("/")[1];
            try {
                int id = Integer.parseInt(pathInfo.split("/")[2]);
                // Get old image path to delete
                String oldImgPath = null;
                switch (accountType) {
                    case "patient":
                        AccountPatient patient = dao.getPatientAccountById(id);
                        if (patient != null) oldImgPath = patient.getImg();
                        break;
                    case "staff":
                        AccountStaff staff = dao.getStaffAccountById(id);
                        if (staff != null) oldImgPath = staff.getImg();
                        break;
                    case "pharmacist":
                        AccountPharmacist pharmacist = dao.getPharmacistAccountById(id);
                        if (pharmacist != null) oldImgPath = pharmacist.getImg();
                        break;
                }

                boolean deleted = false;
                switch (accountType) {
                    case "patient":
                        deleted = dao.disablePatientAccount(id);
                        break;
                    case "staff":
                        deleted = dao.disableStaffAccount(id);
                        break;
                    case "pharmacist":
                        deleted = dao.disablePharmacistAccount(id);
                        break;
                    default:
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.print("{\"error\":\"Invalid account type\"}");
                        out.flush();
                        return;
                }
                if (deleted) {
                    // Delete old image if exists
                    if (oldImgPath != null) {
                        File oldFile = new File(getServletContext().getRealPath("") + File.separator + oldImgPath);
                        if (oldFile.exists()) {
                            oldFile.delete();
                            System.out.println("Deleted image: " + oldImgPath);
                        }
                    }
                    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.print("{\"error\":\"Account not found\"}");
                }
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\":\"Invalid ID\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\":\"Invalid request\"}");
        }
        out.flush();
    }
}