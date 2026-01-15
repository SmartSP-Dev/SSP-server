package group4.opensource_server.quiz.service;

import group4.opensource_server.quiz.domain.QuestionType;
import org.springframework.stereotype.Component;

@Component
public class QuizPromptBuilder {

    /**
     * 퀴즈 생성을 위한 프롬프트를 생성합니다.
     *
     * @param textChunk 텍스트 청크
     * @param keyword 키워드
     * @param questionType 문제 유형
     * @param startNum 시작 번호
     * @param endNum 끝 번호
     * @return 생성된 프롬프트
     */
    public String buildPrompt(String textChunk, String keyword, QuestionType questionType, int startNum, int endNum) {
        StringBuilder promptBuilder = new StringBuilder();

        appendUserPrompt(promptBuilder, keyword, questionType, startNum, endNum);
        appendQuestionTypeRules(promptBuilder, questionType);
        appendReferenceText(promptBuilder, textChunk);
        appendOutputFormat(promptBuilder, questionType);
        appendFinalInstructions(promptBuilder);

        return promptBuilder.toString();
    }

    private void appendUserPrompt(StringBuilder builder, String keyword, QuestionType questionType, int startNum, int endNum) {
        int questionCount = endNum - startNum + 1;

        builder.append("[사용자 프롬프트]\n");
        builder.append("- 생성된 문제는 반드시 '").append(keyword).append("' 키워드와 연관된 내용을 중심으로 출제해 주세요.\n");
        builder.append("아래 텍스트를 참고하여, 해당 내용을 학습하는 학습자가 이해도를 점검할 수 있도록 퀴즈를 총 **")
                .append(questionCount).append("문제** 생성해 주세요.\n");
        builder.append("문제 유형: ").append(questionType).append("\n");
        builder.append("절대로 정답이 같은 단어로 반복되지 않도록 주의해 주세요.\n");
        builder.append("정답은 문맥에서 중요하게 언급된 용어, 개념을 사용해 주세요.\n");
        builder.append("유사하거나 반복적인 문장/형식 절대 없이 다양하게 구성해 주세요.\n");
        builder.append("각 문제에는 반드시 \"number\" 필드를 추가하여 번호를 ")
                .append(startNum).append("~").append(endNum).append(" 범위로 부여해 주세요.\n");
    }

    private void appendQuestionTypeRules(StringBuilder builder, QuestionType questionType) {
        if (questionType == QuestionType.FILL_BLANK) {
            builder.append("※ 규칙 (반드시 지켜야 함):\n");
            builder.append("- 정답과 오답은 모두 한글 또는 영어 단어여야 하며, 문장이나 구문은 절대 포함하지 마세요.\n");
            builder.append("- 정답은 반드시 용언(한다, 이다 등)을 제외한 명사형이어야 하며,\n");
            builder.append("  문제 문장의 빈칸에도 해당 단어만 들어가 자연스럽게 완성되어야 합니다.\n");
            builder.append("- 예: \"상향한다\" → 문제는 \"___한다\", 정답은 \"상향\"\n");
        }
    }

    private void appendReferenceText(StringBuilder builder, String textChunk) {
        builder.append("\n[참고 텍스트]\n").append(textChunk).append("\n\n");
    }

    private void appendOutputFormat(StringBuilder builder, QuestionType questionType) {
        switch (questionType) {
            case MULTIPLE_CHOICE:
                builder.append("출력 형식 예시: { \"type\": \"multiple_choice\", \"question\": \"질문\", ")
                        .append("\"correct_answer\": \"정답\", \"incorrect_answers\": [\"오답1\", \"오답2\", \"오답3\"] }\n");
                break;
            case OX:
                builder.append("출력 형식 예시: { \"type\": \"ox\", \"question\": \"OX 질문\", ")
                        .append("\"correct_answer\": \"O 또는 X\", \"incorrect_answers\": [] }\n");
                break;
            case FILL_BLANK:
                builder.append("출력 형식 예시: { \"type\": \"fill_blank\", \"question\": \"문장 ___\", ")
                        .append("\"correct_answer\": \"정답\" }\n");
                break;
            default:
                builder.append("반드시 위의 형식 중 하나를 사용하세요.\n");
        }
    }

    private void appendFinalInstructions(StringBuilder builder) {
        builder.append("\n[중요] 반드시 JSON 배열 형태로만 출력하세요. 다른 설명이나 문장 없이, JSON 배열만 출력해야 합니다.\n");
    }
}
