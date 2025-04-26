package group4.opensource_server.OCR.controller;

import group4.opensource_server.OCR.service.*;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession; // ✅ 추가

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

@Controller
public class GetFileController {

    @Autowired
    private GetFileService getFileService;

    @Autowired
    private IdentifyFileService identifyFileService;

    @Autowired
    private GetTextFromPdf getTextFromPdf;

    @Autowired
    private GetImageFromPdf getImageFromPdf;

    @Autowired
    private OCRService ocrService;

    @PostMapping("/files/upload")
    public String uploadFile(@RequestParam("file") MultipartFile[] files, Model model, HttpSession session) {
        try {
            List<String> finalResult = new ArrayList<>(); // OCR 결과를 모을 리스트

            for (MultipartFile file : files) {
                File upload = getFileService.uploadFile(file); // 서버에 파일 저장
                int type = identifyFileService.getType(upload);

                System.out.println("파일 타입: " + type);

                if (type == 3) { // PDF 파일
                    List<String> contentPages = getTextFromPdf.extractTextByPage(upload);

                    boolean hasMeaningfulText = contentPages.stream().anyMatch(page -> page.trim().length() > 10);

                    if (hasMeaningfulText) {
                        System.out.println("PDF 텍스트 추출 결과:");
                        for (int i = 0; i < contentPages.size(); i++) {
                            System.out.printf("Page %d:\n%s\n", i + 1, contentPages.get(i));
                            finalResult.add(contentPages.get(i)); // ✅ OCR 결과 저장
                        }
                    }
                    else {
                        StringBuilder content02 = new StringBuilder();
                        List<byte[]> imageList = getImageFromPdf.extractImageFromPdf(upload);

                        for (byte[] imageBytes : imageList) {
                            List<String> ocrResult = ocrService.BinaryToText(imageBytes);

                            if (!ocrResult.isEmpty()) {
                                content02.append(ocrResult.get(0));
                            }
                        }

                        System.out.println("OCR로 추출된 내용:");
                        System.out.println(content02);
                        finalResult.add(content02.toString()); // OCR 결과 저장
                    }
                }
                else { // 이미지 파일 처리
                    String content = ocrService.ImageToText(upload).get(0);
                    System.out.println("이미지 OCR 결과:");
                    System.out.println(content);
                    finalResult.add(content); // OCR 결과 저장
                }
            }

            // 세션에 OCR 결과 저장
            session.setAttribute("ocrResult", finalResult);

            return "redirect:/uploadSuccess"; // 기존 화면 리다이렉트 유지
        }
        catch (IOException e) {
            model.addAttribute("errorMessage", "파일 업로드 중 오류가 발생했습니다. 다시 시도해 주세요.");
            e.printStackTrace();
            return "redirect:/uploadFailure";
        }
        catch (Exception e) {
            model.addAttribute("errorMessage", "예상치 못한 오류가 발생했습니다. 다시 시도해 주세요.");
            e.printStackTrace();
            return "redirect:/uploadFailure";
        }
    }


    @GetMapping("/uploadSuccess")
    public String uploadSuccess(HttpSession session, Model model) {
        // 세션에서 OCR 결과 가져오기
        List<String> ocrResult = (List<String>) session.getAttribute("ocrResult");

        if (ocrResult != null) {
            model.addAttribute("ocrResult", ocrResult); // 모델에 OCR 결과 추가
        }

        return "uploadSuccess"; // 결과를 보여줄 uploadSuccess.html 페이지로 이동
    }

    


    @GetMapping("/uploadFailure")
    public String uploadFailure(Model model) {
        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", "파일 업로드 실패: 오류가 발생했습니다.");
        }
        return "uploadFailure";
    }
}
