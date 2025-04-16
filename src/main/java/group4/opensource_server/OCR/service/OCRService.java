package group4.opensource_server.OCR.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.*;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

@Service
public class OCRService {
    private Tesseract tesseract;

    public OCRService() {
        tesseract = new Tesseract();
        tesseract.setDatapath("/opt/homebrew/share/tessdata");
        tesseract.setLanguage("eng+kor");
        tesseract.setPageSegMode(6);  // 페이지 세그먼트 모드
        tesseract.setOcrEngineMode(1);  // OCR 엔진 모드 설정
    }

    public String ImageToText(File file) {
        try {
            // File을 BufferedImage로 변환
            BufferedImage image = ImageIO.read(file);

            if (image == null) {
                return "Failed to read image from the file.";
            }

            // Tesseract로 OCR 수행
            String content = tesseract.doOCR(image);

            return content;
        } catch (IOException | TesseractException e) {
            e.printStackTrace();
            return "Error processing the image: " + e.getMessage();
        }
    }

    public String BinaryToText(byte[] bytes) {
        try {
            // byte[]를 BufferedImage로 변환
            BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(bytes));

            if (image == null) {
                return "Failed to read image from the byte array.";
            }

            // Tesseract로 OCR 수행
            String content = tesseract.doOCR(image);

            return content;
        } catch (IOException | TesseractException e) {
            e.printStackTrace();
            return "Error processing the image: " + e.getMessage();
        }
    }
}
