package com.notequiz.domain.quiz.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notequiz.common.exception.ApiException;
import com.notequiz.common.exception.ErrorCode;
import com.notequiz.domain.note.entity.Note;
import com.notequiz.domain.note.repository.NoteRepository;
import com.notequiz.domain.quiz.dto.*;
import com.notequiz.domain.quiz.entity.Question;
import com.notequiz.domain.quiz.entity.Quiz;
import com.notequiz.domain.quiz.entity.QuizResult;
import com.notequiz.domain.quiz.entity.WrongAnswer;
import com.notequiz.domain.quiz.repository.QuizRepository;
import com.notequiz.domain.quiz.repository.QuizResultRepository;
import com.notequiz.domain.quiz.repository.WrongAnswerRepository;
import com.notequiz.domain.user.entity.User;
import com.notequiz.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

    private final QuizRepository quizRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final QuizResultRepository quizResultRepository;
    private final WrongAnswerRepository wrongAnswerRepository;
    private final OllamaClient ollamaClient;
    private final ObjectMapper objectMapper;

    public QuizResponse generateQuiz(QuizGenerateRequest request) {
        Note note = noteRepository.findByNoteId(request.getNoteId())
                .orElseThrow(() -> new ApiException(ErrorCode.UPLOAD_NOT_FOUND));

        String prompt = createPrompt(note.getExtractedText(), request.getQuestionCount());
        String llmResponse = ollamaClient.generate(prompt);

        List<QuestionData> questionDataList = parseLlmResponse(llmResponse);

        User currentUser = getCurrentUser();

        Quiz quiz = Quiz.builder()
                .note(note)
                .user(currentUser)
                .build();

        for (int i = 0; i < questionDataList.size(); i++) {
            QuestionData data = questionDataList.get(i);
            Question question = Question.builder()
                    .body(data.getBody())
                    .options(data.getOptions())
                    .answer(data.getAnswer())
                    .explanation(data.getExplanation())
                    .orderNum(i + 1)
                    .build();
            quiz.addQuestion(question);
        }

        quizRepository.save(quiz);

        return QuizResponse.from(quiz);
    }

    public QuizResponse getQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ApiException(ErrorCode.QUIZ_NOT_FOUND));
        return QuizResponse.from(quiz);
    }

    public QuizResponse getSharedQuiz(String shareToken) {
        Quiz quiz = quizRepository.findByShareToken(shareToken)
                .orElseThrow(() -> new ApiException(ErrorCode.QUIZ_NOT_FOUND));
        return QuizResponse.from(quiz);
    }

    public QuizResultResponse submitResult(Long quizId, QuizResultRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ApiException(ErrorCode.QUIZ_NOT_FOUND));

        List<Question> questions = quiz.getQuestions();
        List<Integer> userAnswers = request.getAnswers();

        if (questions.size() != userAnswers.size()) {
            throw new ApiException(ErrorCode.INVALID_FILE_FORMAT);
        }

        int score = 0;
        User currentUser = getCurrentUser();

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            Integer userAnswer = userAnswers.get(i);

            if (question.getAnswer().equals(userAnswer)) {
                score++;
            } else if (currentUser != null) {
                // Save wrong answer for logged-in user
                WrongAnswer wrongAnswer = WrongAnswer.builder()
                        .user(currentUser)
                        .question(question)
                        .resolved(false)
                        .build();
                wrongAnswerRepository.save(wrongAnswer);
            }
        }

        QuizResult result = QuizResult.builder()
                .quiz(quiz)
                .user(currentUser)
                .score(score)
                .total(questions.size())
                .build();

        quizResultRepository.save(result);

        return QuizResultResponse.builder()
                .resultId(result.getId())
                .score(score)
                .total(questions.size())
                .build();
    }

    private String createPrompt(String text, int count) {
        return String.format("""
                너는 학습 보조 도우미야. 다음 제공된 텍스트를 바탕으로 객관식 퀴즈 %d문제를 만들어줘.
                각 문제는 반드시 4개의 보기를 가져야 하며, 정답은 하나여야 해.
                정답에 대한 자세한 해설도 포함해줘.
                응답은 반드시 아래 JSON 배열 형식을 지켜야 하며, 다른 텍스트는 포함하지 마.
                
                형식:
                [
                  {
                    "body": "문제 내용",
                    "options": ["보기1", "보기2", "보기3", "보기4"],
                    "answer": 0, (0~3 사이의 정수, options 배열의 인덱스)
                    "explanation": "해설 내용"
                  }
                ]
                
                텍스트:
                %s
                """, count, text);
    }

    private List<QuestionData> parseLlmResponse(String response) {
        try {
            int start = response.indexOf('[');
            int end = response.lastIndexOf(']') + 1;
            if (start == -1 || end == 0) {
                throw new RuntimeException("Invalid JSON format from LLM");
            }
            String json = response.substring(start, end);
            return objectMapper.readValue(json, new TypeReference<List<QuestionData>>() {});
        } catch (Exception e) {
            log.error("Failed to parse LLM response: {}", response, e);
            throw new ApiException(ErrorCode.TEXT_EXTRACT_FAILED);
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    @lombok.Getter
    @lombok.Setter
    @lombok.NoArgsConstructor
    public static class QuestionData {
        private String body;
        private List<String> options;
        private Integer answer;
        private String explanation;
    }
}
