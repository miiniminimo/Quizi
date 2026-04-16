package com.quizi.controller;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig; // 파일 업로드 지원
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import com.quizi.dto.UserDTO;
import com.quizi.dto.WorkbookDTO;
import com.quizi.dao.WorkbookDAO;
import com.quizi.service.AiService;

@WebServlet("/ai-generate")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 50
)
public class AiGenController extends HttpServlet {

    private static final String UPLOAD_DIR = "uploads";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/views/login.jsp");
            return;
        }

        // 파라미터 수신
        String topic = request.getParameter("topic");
        String countStr = request.getParameter("count");
        String difficulty = request.getParameter("difficulty");
        Part filePart = null;
        try {
            filePart = request.getPart("file"); // 파일 파트 가져오기
        } catch (Exception e) {
            // 파일이 없는 요청일 수 있음 (무시)
        }

        int count = 5;
        try { count = Integer.parseInt(countStr); } catch (Exception e) {}

        AiService service = new AiService();
        WorkbookDTO generatedData = null;

        try {
            // A. 파일 업로드 방식
            if (filePart != null && filePart.getSize() > 0) {
                File savedFile = saveFile(request, filePart);
                // PDF라면 이미지로 변환
                if (savedFile.getName().toLowerCase().endsWith(".pdf")) {
                    savedFile = convertPdfToImage(savedFile);
                }
                // 이미지 기반 생성 호출
                generatedData = service.generateQuestionsFromImage(savedFile, count, difficulty);
            }
            // B. 텍스트 입력 방식
            else if (topic != null && !topic.trim().isEmpty()) {
                generatedData = service.generateQuestions(topic, count, difficulty);
            }
            // C. 입력 없음
            else {
                response.sendRedirect(request.getContextPath() + "/views/ai_setup.jsp?error=empty");
                return;
            }

            // DB 저장 및 이동
            if (generatedData != null && generatedData.getQuestions() != null && !generatedData.getQuestions().isEmpty()) {
                generatedData.setCreatorId(user.getId());
                WorkbookDAO dao = new WorkbookDAO();
                boolean success = dao.createWorkbook(generatedData);

                if (success) {
                    response.sendRedirect(request.getContextPath() + "/main?msg=ai_created");
                } else {
                    response.sendRedirect(request.getContextPath() + "/views/ai_setup.jsp?error=db_fail");
                }
            } else {
                response.sendRedirect(request.getContextPath() + "/views/ai_setup.jsp?error=ai_fail");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/views/ai_setup.jsp?error=server_error");
        }
    }

    // 파일 저장 유틸리티
    private File saveFile(HttpServletRequest request, Part part) throws IOException {
        String applicationPath = request.getServletContext().getRealPath("");
        String uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;

        File uploadDir = new File(uploadFilePath);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        String fileName = UUID.randomUUID().toString() + "_" + getFileName(part);
        String fullPath = uploadFilePath + File.separator + fileName;

        part.write(fullPath);
        return new File(fullPath);
    }

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        for (String content : contentDisp.split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf("=") + 2, content.length() - 1).replace("\"", "");
            }
        }
        return "unknown.jpg";
    }

    // PDF -> JPG 변환 유틸리티
    private File convertPdfToImage(File pdfFile) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
            String imagePath = pdfFile.getAbsolutePath() + ".jpg";
            File imageFile = new File(imagePath);
            ImageIO.write(bim, "jpg", imageFile);
            return imageFile;
        }
    }
}