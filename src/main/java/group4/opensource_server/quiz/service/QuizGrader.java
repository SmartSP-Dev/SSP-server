package group4.opensource_server.quiz.service;

import group4.opensource_server.quiz.domain.QuestionType;
import org.springframework.stereotype.Component;

@Component
public class QuizGrader {

    /**
     * 사용자 답안을 채점합니다.
     *
     * @param type 문제 유형
     * @param userAnswer 사용자 답안
     * @param correctAnswer 정답
     * @return 정답 여부
     */
    public boolean evaluateAnswer(QuestionType type, String userAnswer, String correctAnswer) {
        if (userAnswer == null || correctAnswer == null) {
            return false;
        }

        return switch (type) {
            case FILL_BLANK -> normalize(userAnswer).equals(normalize(correctAnswer));
            case MULTIPLE_CHOICE, OX -> userAnswer.equalsIgnoreCase(correctAnswer);
            default -> false;
        };
    }

    /**
     * 문자열을 정규화합니다 (공백 제거, 소문자 변환).
     *
     * @param input 입력 문자열
     * @return 정규화된 문자열
     */
    private String normalize(String input) {
        return input.trim().toLowerCase();
    }
}
