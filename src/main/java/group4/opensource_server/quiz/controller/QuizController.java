package group4.opensource_server.quiz.controller;

import group4.opensource_server.quiz.domain.QuizService;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quiz")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    // (1) 사용자가 직접 텍스트/키워드를 입력해서 생성하는 기존 방식
    @PostMapping("/generate")
    public ResponseEntity<String> generateQuizFromUserInput(
            @RequestParam String text,
            @RequestParam String keyword,
            @RequestParam String questionType) {
        JSONObject result = quizService.generateQuizzesFromText(text, keyword, questionType);
        return ResponseEntity.ok(result.toString());
    }

    // (2) 세션 OCR 결과로 바로 퀴즈 생성하는 방식
    @PostMapping("/generateFromOCR")
    public ResponseEntity<String> generateQuizFromOCR(
            @RequestParam String keyword,
            @RequestParam String questionType,
            HttpSession session) {
        Object ocrResult = session.getAttribute("ocrResult");
        if (ocrResult instanceof List<?>) {
            List<?> ocrList = (List<?>) ocrResult;
            // 리스트 요소를 모두 합쳐서 하나의 긴 텍스트로 만듦
            StringBuilder combinedText = new StringBuilder();
            for (Object item : ocrList) {
                combinedText.append(item.toString()).append("\n");
            }
            JSONObject result = quizService.generateQuizzesFromText(combinedText.toString(), keyword, questionType);
            return ResponseEntity.ok(result.toString());
        } else {
            // 세션에 ocrResult가 없거나 비정상적인 경우
            JSONObject errorResult = new JSONObject().put("error", "No OCR result found in session.");
            return ResponseEntity.ok(errorResult.toString());
        }
    }
}