package group4.opensource_server.quiz.controller;

import group4.opensource_server.quiz.dto.QuizListDto;
import group4.opensource_server.quiz.dto.QuizQuestionDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import group4.opensource_server.quiz.domain.QuestionType;
import group4.opensource_server.quiz.service.QuizService;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import group4.opensource_server.quiz.dto.QuizResponseDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final UserRepository userRepository; // 추가

    // 1. OCR 결과를 기반으로 퀴즈 생성하고 저장
    @PostMapping("/generate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> generateQuizFromOCR(
            @RequestParam String keyword,
            @RequestParam String questionType,
            HttpSession session,
            @AuthenticationPrincipal UserDetails userDetails) {

        Object ocrResult = session.getAttribute("ocrResult");
        if (ocrResult instanceof List<?>) {
            List<?> ocrList = (List<?>) ocrResult;
            StringBuilder combinedText = new StringBuilder();
            for (Object item : ocrList) {
                combinedText.append(item.toString()).append("\n");
            }

            QuestionType type = QuestionType.valueOf(questionType.toUpperCase());

            // 🔥 JwtUserDetailsService에서 email 기준으로 불러오니까, username = email
            String email = userDetails.getUsername();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음: " + email));

            JSONObject result = quizService.generateQuizzesFromText(combinedText.toString(), keyword, type);

            quizService.createQuizWithQuestions(result, "OCR 퀴즈", keyword, type, currentUser);

            return ResponseEntity.ok(result.toString());
        } else {
            JSONObject errorResult = new JSONObject().put("error", "No OCR result found in session.");
            return ResponseEntity.ok(errorResult.toString());
        }
    }

    // 로그인한 사용자의 퀴즈 목록 조회
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public List<QuizListDto> getMyQuizzes(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails userDetails) {
        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음: " + email));
        return quizService.getQuizzesByUserId(currentUser.getId()).stream()
                .map(QuizListDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 3. 퀴즈에 해당하는 문제들 조회
    @GetMapping("/{quizId}/questions")
    @PreAuthorize("isAuthenticated()")
    public List<QuizQuestionDto> getQuizQuestions(@PathVariable Long quizId, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음: " + email));

        return quizService.getQuizzesByUserId(currentUser.getId()).stream()
                .filter(quiz -> quiz.getId().equals(quizId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 퀴즈 없음: " + quizId))
                .getQuestions().stream()
                .map(QuizQuestionDto::fromEntity)
                .collect(Collectors.toList());
    }
}