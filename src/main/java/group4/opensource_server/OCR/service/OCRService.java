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

    public List<String> ImageToText(File file) {
        List<String> ocrList = new ArrayList<String>();

        try {
            // File을 BufferedImage로 변환
            BufferedImage image = ImageIO.read(file);

            if (image == null) {
                ocrList.add("Failed to read image from the file.");

                return ocrList;
            }

            // Tesseract로 OCR 수행
            String content = tesseract.doOCR(image);
            ocrList.add(content);

            return ocrList;
        } catch (IOException | TesseractException e) {
            e.printStackTrace();
            ocrList.add("Error processing the image: " + e.getMessage());
            return ocrList;
        }
    }

    public List<String> BinaryToText(byte[] bytes) {
        List<String> ocrList = new ArrayList<>();

        try {
            // byte[]를 BufferedImage로 변환
            BufferedImage image = ImageIO.read(new java.io.ByteArrayInputStream(bytes));

            if (image == null) {
                ocrList.add("Failed to read image from the byte array.");

                return ocrList;
            }

            // Tesseract로 OCR 수행
            String content = tesseract.doOCR(image);
            ocrList.add(content);

            return ocrList;
        } catch (IOException | TesseractException e) {
            e.printStackTrace();
            ocrList.add("Error processing the image: " + e.getMessage());

            return ocrList;
        }
    }
}
