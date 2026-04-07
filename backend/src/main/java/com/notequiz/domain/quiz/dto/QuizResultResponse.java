package com.notequiz.domain.quiz.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizResultResponse {
    private Long resultId;
    private int score;
    private int total;
}
