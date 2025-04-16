package group4.opensource_server.OCR.controller;

import group4.opensource_server.OCR.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
    public String uploadFile(@RequestParam("file") MultipartFile[] files, Model model) {
        try {
            for (MultipartFile file : files) {
                File upload = getFileService.uploadFile(file); // upload : 서버에 저장한 파일

                int type = identifyFileService.getType(upload);

                System.out.println("파일 타입: " + type);

                if (type == 3) { // PDF 파일
                    List<String> contentPages = getTextFromPdf.extractTextByPage(upload); // 수정된 메서드명

                    boolean hasMeaningfulText = contentPages.stream().anyMatch(page -> page.trim().length() > 10);

                    if (hasMeaningfulText) {
                        System.out.println("PDF 텍스트 추출 결과:");
                        for (int i = 0; i < contentPages.size(); i++) {
                            System.out.printf("Page %d:\n%s\n", i + 1, contentPages.get(i));
                        }
                    } else {
                        // 텍스트가 거의 없는 경우, 이미지 추출 후 OCR 진행
                        StringBuilder content02 = new StringBuilder();
                        List<byte[]> imageList = getImageFromPdf.extractImageFromPdf(upload);

                        for (byte[] imageBytes : imageList) {
                            content02.append(ocrService.BinaryToText(imageBytes));
                        }

                        System.out.println("OCR로 추출된 내용:");
                        System.out.println(content02);
                    }
                }
                else {
                    // 이미지 파일 처리
                    String content = ocrService.ImageToText(upload);
                    System.out.println("이미지 OCR 결과:");
                    System.out.println(content);
                }
            }

            return "redirect:/uploadSuccess";
        }
        catch (IOException e) {
            model.addAttribute("errorMessage", "파일 업로드 중 오류가 발생했습니다. 다시 시도해 주세요.");
            e.printStackTrace();
            return "redirect:/uploadFailure";
        }
    }

    @GetMapping("/uploadSuccess")
    public String uploadSuccess() {
        return "uploadSuccess";
    }

    @GetMapping("/uploadFailure")
    public String uploadFailure(Model model) {
        if (model.containsAttribute("errorMessage")) {
            model.addAttribute("errorMessage", "파일 업로드 실패: 오류가 발생했습니다.");
        }
        return "uploadFailure";
    }
}
