package com.notequiz.domain.quiz.repository;

import com.notequiz.domain.quiz.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
}
