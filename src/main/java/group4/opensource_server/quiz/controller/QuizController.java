package group4.opensource_server.quiz.controller;

import group4.opensource_server.quiz.domain.QuestionType;
import group4.opensource_server.quiz.domain.Quiz;
import group4.opensource_server.quiz.domain.QuizQuestion;
import group4.opensource_server.quiz.service.QuizService;
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

    // 1. OCR 결과를 기반으로 퀴즈 생성하고 저장
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

            // String을 QuestionType으로 변환 (대소문자 무시하고 변환)
            QuestionType type = QuestionType.valueOf(questionType.toUpperCase()); // String -> QuestionType으로 변환

            // 퀴즈 생성
            JSONObject result = quizService.generateQuizzesFromText(combinedText.toString(), keyword, type);

            // 생성된 퀴즈를 디비에 저장
            quizService.createQuizWithQuestions(result, "OCR 퀴즈", keyword, type, 1L); // 1L은 예시로 사용자 ID

            return ResponseEntity.ok(result.toString());
        } else {
            // 세션에 ocrResult가 없거나 비정상적인 경우
            JSONObject errorResult = new JSONObject().put("error", "No OCR result found in session.");
            return ResponseEntity.ok(errorResult.toString());
        }
    }

    // 2. 퀴즈 목록 조회
    @GetMapping
    public List<Quiz> getAllQuizzes() {
        return quizService.getAllQuizzes();
    }

    // 3. 특정 퀴즈 조회
    @GetMapping("/{id}")
    public Quiz getQuizById(@PathVariable Long id) {
        return quizService.getQuizById(id);
    }

    // 4. 특정 퀴즈에 해당하는 문제 목록 조회
    @GetMapping("/{quizId}/questions")
    public List<QuizQuestion> getQuizQuestions(@PathVariable Long quizId) {
        return quizService.getQuizQuestionsByQuizId(quizId);
    }
}