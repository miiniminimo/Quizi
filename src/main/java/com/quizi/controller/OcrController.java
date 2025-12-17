package com.quizi.controller;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import com.quizi.dto.UserDTO;
import com.quizi.dto.WorkbookDTO;
import com.quizi.dao.WorkbookDAO;
import com.quizi.service.OCRService;

// [추가] PDF 변환을 위한 라이브러리 임포트
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

@WebServlet("/ocr")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 10,
        maxRequestSize = 1024 * 1024 * 50
)
public class OcrController extends HttpServlet {

    private static final String UPLOAD_DIR = "uploads";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 1. 로그인 체크 (작성자 ID 필요)
        HttpSession session = request.getSession();
        UserDTO user = (UserDTO) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect("views/login.jsp");
            return;
        }

        // 2. 파일 저장 경로 설정
        String applicationPath = request.getServletContext().getRealPath("");
        String uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;

        File uploadDir = new File(uploadFilePath);
        if (!uploadDir.exists()) uploadDir.mkdirs();

        // 3. 파일 업로드
        Part part = request.getPart("file");
        String fileName = getFileName(part);
        String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
        String fullPath = uploadFilePath + File.separator + uniqueFileName;

        part.write(fullPath);

        File savedFile = new File(fullPath);

        // [추가] PDF 파일일 경우 첫 페이지를 이미지로 변환
        if (fileName.toLowerCase().endsWith(".pdf")) {
            try {
                savedFile = convertPdfToImage(savedFile);
            } catch (Exception e) {
                e.printStackTrace();
                response.sendError(500, "PDF 파일을 이미지로 변환하는 중 오류가 발생했습니다.");
                return;
            }
        }

        // 4. AI 분석 서비스 호출 (이제 savedFile은 항상 이미지임)
        OCRService service = new OCRService();
        String tessDataPath = request.getServletContext().getRealPath("/WEB-INF/tessdata");

        // AI API를 통해 문제집 데이터 생성
        WorkbookDTO workbook = service.extractQuestions(savedFile, tessDataPath);

        // 5. [핵심] DB 자동 저장
        if (workbook != null && workbook.getQuestions() != null && !workbook.getQuestions().isEmpty()) {
            workbook.setCreatorId(user.getId()); // 작성자 설정

            WorkbookDAO dao = new WorkbookDAO();
            boolean success = dao.createWorkbook(workbook);

            if (success) {
                // 성공 시 메인 페이지로 이동
                response.sendRedirect(request.getContextPath() + "/main");
            } else {
                response.sendError(500, "문제집 저장에 실패했습니다.");
            }
        } else {
            response.sendError(500, "AI가 문제를 생성하지 못했습니다.");
        }
    }

    // [신규 메서드] PDF의 첫 페이지를 JPG 이미지로 변환하여 저장
    private File convertPdfToImage(File pdfFile) throws IOException {
        // PDFBox 3.x 버전용 로더 사용
        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            // 첫 번째 페이지(0)를 300 DPI로 렌더링 (화질 확보)
            BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);

            // 이미지 파일 경로 생성 (.pdf -> .jpg)
            String imagePath = pdfFile.getAbsolutePath() + ".jpg";
            File imageFile = new File(imagePath);

            // 이미지 저장
            ImageIO.write(bim, "jpg", imageFile);

            return imageFile;
        }
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
}