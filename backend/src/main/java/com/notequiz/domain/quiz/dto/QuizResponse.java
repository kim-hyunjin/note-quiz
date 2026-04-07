package com.notequiz.domain.quiz.dto;

import com.notequiz.domain.quiz.entity.Quiz;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class QuizResponse {
    private Long id;
    private List<QuestionResponse> questions;

    public static QuizResponse from(Quiz quiz) {
        return QuizResponse.builder()
                .id(quiz.getId())
                .questions(quiz.getQuestions().stream()
                        .map(QuestionResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
