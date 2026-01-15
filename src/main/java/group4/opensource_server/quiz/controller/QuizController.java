package group4.opensource_server.quiz.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import group4.opensource_server.quiz.dto.*;
import group4.opensource_server.quiz.domain.Quiz;
import group4.opensource_server.quiz.service.QuizService;
import group4.opensource_server.user.domain.User;
import group4.opensource_server.user.domain.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quiz", description = "퀴즈 생성, 조회, 제출 관련 API")
public class QuizController {

    private final QuizService quizService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * OCR 기반 퀴즈 생성 (RESTful 개선)
     * POST /quizzes + @RequestBody + 201 Created
     */
    @Operation(summary = "OCR 기반 퀴즈 생성", description = "세션에 저장된 OCR 결과를 바탕으로 키워드와 문제 유형에 따라 퀴즈를 생성합니다.")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuizGenerateResponseDto> generateQuizFromOCR(
            @Valid @RequestBody QuizGenerateRequestDto request,
            HttpSession session,
            @AuthenticationPrincipal UserDetails userDetails) {

        Object ocrResult = session.getAttribute("ocrResult");
        if (!(ocrResult instanceof List<?>)) {
            log.warn("세션에 OCR 결과가 없습니다. user={}", userDetails.getUsername());
            return ResponseEntity.badRequest().build();
        }

        // OCR 결과를 텍스트로 변환
        String combinedText = buildCombinedText((List<?>) ocrResult);

        // 사용자 조회
        User currentUser = findUserByEmail(userDetails.getUsername());

        // 퀴즈 생성
        JSONObject result = quizService.generateQuizzesFromText(
                combinedText,
                request.getKeyword(),
                request.getQuestionType()
        );

        Quiz quiz = quizService.createQuizWithQuestions(
                result,
                request.getTitle(),
                request.getKeyword(),
                request.getQuestionType(),
                currentUser
        );

        log.info("퀴즈 생성 완료: quizId={}, user={}", quiz.getId(), currentUser.getEmail());

        QuizGenerateResponseDto response = QuizGenerateResponseDto.builder()
                .quizId(quiz.getId())
                .title(quiz.getTitle())
                .questionCount(result.getJSONArray("quizzes").length())
                .message("퀴즈가 성공적으로 생성되었습니다.")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 내 퀴즈 목록 조회 (RESTful 개선)
     * GET /quizzes
     */
    @Operation(summary = "내 퀴즈 목록 조회", description = "로그인한 사용자가 생성한 퀴즈 목록을 반환합니다.")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuizListDto>> getMyQuizzes(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = findUserByEmail(userDetails.getUsername());

        List<QuizListDto> quizzes = quizService.getQuizzesByUserId(currentUser.getId()).stream()
                .map(QuizListDto::fromEntity)
                .collect(Collectors.toList());

        logQuizList(userDetails.getUsername(), quizzes);

        return ResponseEntity.ok(quizzes);
    }

    /**
     * 퀴즈 상세 조회 (RESTful 개선)
     * GET /quizzes/{quizId}
     */
    @Operation(summary = "퀴즈 상세 조회", description = "지정된 퀴즈 ID에 해당하는 문제 목록(10문제)을 반환합니다.")
    @GetMapping("/{quizId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<QuizQuestionDto>> getQuizQuestions(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User currentUser = findUserByEmail(userDetails.getUsername());

        List<QuizQuestionDto> questions = quizService.getQuizzesByUserId(currentUser.getId()).stream()
                .filter(quiz -> quiz.getId().equals(quizId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 퀴즈 없음: " + quizId))
                .getQuestions().stream()
                .map(QuizQuestionDto::fromEntity)
                .collect(Collectors.toList());

        logQuestions(quizId, questions);

        return ResponseEntity.ok(questions);
    }

    /**
     * 퀴즈 제출 (RESTful 개선)
     * POST /quizzes/{quizId}/attempts
     */
    @PostMapping("/{quizId}/attempts")
    @Operation(summary = "퀴즈 제출", description = "프론트엔드에서 제출한 퀴즈 응답을 채점하고 결과를 반환합니다.")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuizSubmitResultDto> submitQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizSubmitRequestDto request,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = findUserByEmail(userDetails.getUsername());

        // RequestDto에 quizId 설정 (PathVariable에서 받은 값 사용)
        request.setQuizId(quizId);

        QuizSubmitResultDto result = quizService.submitQuiz(request, user);

        log.info("퀴즈 채점 완료: quizId={}, user={}, score={}/{}",
                quizId, user.getEmail(), result.getCorrectAnswers(), result.getTotalQuestions());

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * 주간 퀴즈 요약 조회 (RESTful 개선)
     * GET /quizzes/summary?period=week
     */
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "퀴즈 요약 조회", description = "기간별 퀴즈 요약 정보를 반환합니다.")
    public ResponseEntity<WeeklyQuizSummaryDto> getSummary(
            @RequestParam(defaultValue = "week") String period,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!"week".equals(period)) {
            return ResponseEntity.badRequest().build();
        }

        User user = findUserByEmail(userDetails.getUsername());
        WeeklyQuizSummaryDto summary = quizService.getWeeklyQuizSummary(user);

        return ResponseEntity.ok(summary);
    }

    /**
     * 퀴즈 삭제 (RESTful 개선)
     * DELETE /quizzes/{quizId} + 204 No Content
     */
    @DeleteMapping("/{quizId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "퀴즈 삭제", description = "본인이 생성한 퀴즈를 삭제합니다.")
    public ResponseEntity<Void> deleteQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = findUserByEmail(userDetails.getUsername());
        quizService.deleteQuiz(user, quizId);

        log.info("퀴즈 삭제 성공: quizId={}, user={}", quizId, user.getEmail());

        return ResponseEntity.noContent().build();
    }

    /**
     * 퀴즈 최신 채점 결과 조회 (RESTful 개선)
     * GET /quizzes/{quizId}/attempts/latest
     */
    @Operation(summary = "퀴즈 채점 결과 조회", description = "해당 퀴즈의 가장 최근 채점 결과를 반환합니다.")
    @GetMapping("/{quizId}/attempts/latest")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuizSubmitResultDto> getLatestQuizResult(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = findUserByEmail(userDetails.getUsername());
        QuizSubmitResultDto result = quizService.getLatestResultByQuizId(quizId, user);

        return ResponseEntity.ok(result);
    }

    // === Private Helper Methods ===

    private String buildCombinedText(List<?> ocrList) {
        StringBuilder combinedText = new StringBuilder();
        for (Object item : ocrList) {
            combinedText.append(item.toString()).append("\n");
        }
        return combinedText.toString();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일 유저 없음: " + email));
    }

    private void logQuizList(String email, List<QuizListDto> quizzes) {
        try {
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            log.debug("Quiz list for user {}: {}", email, objectMapper.writeValueAsString(quizzes));
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 오류", e);
        }
    }

    private void logQuestions(Long quizId, List<QuizQuestionDto> questions) {
        try {
            log.debug("Questions for quiz {}: {}", quizId, objectMapper.writeValueAsString(questions));
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 오류", e);
        }
    }
}
