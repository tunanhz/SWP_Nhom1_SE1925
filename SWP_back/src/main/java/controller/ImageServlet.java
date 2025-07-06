package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@WebServlet("/images/*")
public class ImageServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String filename = req.getPathInfo().substring(1);
        String imagePath = getServletContext().getRealPath("") + File.separator + "images" + File.separator + "accounts" + File.separator + filename;
        File file = new File(imagePath);

        if (file.exists()) {
            String mimeType = Files.probeContentType(Paths.get(imagePath));
            resp.setContentType(mimeType != null ? mimeType : "application/octet-stream");
            Files.copy(Paths.get(imagePath), resp.getOutputStream());
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
