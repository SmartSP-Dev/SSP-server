package group4.opensource_server.quiz.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import group4.opensource_server.quiz.dto.QuizListDto;
import group4.opensource_server.quiz.dto.QuizQuestionDto;
import group4.opensource_server.quiz.domain.QuestionType;
import group4.opensource_server.quiz.dto.QuizSubmitRequestDto;
import group4.opensource_server.quiz.dto.QuizSubmitResultDto;
import group4.opensource_server.quiz.service.QuizService;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
@Tag(name = "Quiz", description = "퀴즈 생성, 조회, 제출 관련 API")
public class QuizController {

    private final QuizService quizService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Operation(
            summary = "OCR 기반 퀴즈 생성",
            description = "세션에 저장된 OCR 결과를 바탕으로 키워드와 문제 유형에 따라 퀴즈를 생성합니다."
    )
    @PostMapping("/generate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> generateQuizFromOCR(
            @RequestParam String title,
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
            String email = userDetails.getUsername();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음: " + email));

            JSONObject result = quizService.generateQuizzesFromText(combinedText.toString(), keyword, type);
            quizService.createQuizWithQuestions(result, title, keyword, type, currentUser);

            System.out.println("Generated Quiz JSON:\n" + result.toString(2));
            return ResponseEntity.ok(result.toString());
        } else {
            JSONObject errorResult = new JSONObject().put("error", "No OCR result found in session.");
            System.out.println("No OCR result found in session.");
            return ResponseEntity.ok(errorResult.toString());
        }
    }

    @Operation(
            summary = "내 퀴즈 목록 조회",
            description = "로그인한 사용자가 생성한 퀴즈 목록을 반환합니다."
    )
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public List<QuizListDto> getMyQuizzes(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음: " + email));

        List<QuizListDto> quizzes = quizService.getQuizzesByUserId(currentUser.getId()).stream()
                .map(QuizListDto::fromEntity)
                .collect(Collectors.toList());

        try {
            System.out.println("Quiz list for user " + email + ":\n" + objectMapper.writeValueAsString(quizzes));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return quizzes;
    }

    @Operation(
            summary = "퀴즈 상세 조회",
            description = "지정된 퀴즈 ID에 해당하는 문제 목록(10문제)을 반환합니다."
    )
    @GetMapping("/{quizId}")
    @PreAuthorize("isAuthenticated()")
    public List<QuizQuestionDto> getQuizQuestions(@PathVariable Long quizId, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일 유저 없음: " + email));

        List<QuizQuestionDto> questions = quizService.getQuizzesByUserId(currentUser.getId()).stream()
                .filter(quiz -> quiz.getId().equals(quizId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("해당 퀴즈 없음: " + quizId))
                .getQuestions().stream()
                .map(QuizQuestionDto::fromEntity)
                .collect(Collectors.toList());

        try {
            System.out.println("Questions for quiz " + quizId + ":\n" + objectMapper.writeValueAsString(questions));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return questions;
    }

    @Operation(
            summary = "퀴즈 제출",
            description = "프론트엔드에서 제출한 퀴즈 응답을 채점하고 결과를 반환합니다."
    )
    @PostMapping("/submit")
    public ResponseEntity<QuizSubmitResultDto> submitQuiz(@RequestBody QuizSubmitRequestDto request) {
        QuizSubmitResultDto result = quizService.submitQuiz(request);
        return ResponseEntity.ok(result);
    }
}