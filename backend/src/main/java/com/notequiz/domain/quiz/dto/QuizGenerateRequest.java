package com.notequiz.domain.quiz.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class QuizGenerateRequest {
    private String noteId;
    private int questionCount;
}
