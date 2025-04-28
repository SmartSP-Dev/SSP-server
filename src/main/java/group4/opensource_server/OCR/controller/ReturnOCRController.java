package group4.opensource_server.OCR.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Collections;

@RestController
public class ReturnOCRController {

    @GetMapping("/returnOCRResult")
    public List<String> returnOCRResult(HttpSession session) {
        Object ocrResult = session.getAttribute("ocrResult");

        if (ocrResult instanceof List<?>) {
            return (List<String>) ocrResult;
        } else {
            return Collections.emptyList(); // 만약 없으면 빈 리스트 반환
        }
    }
}