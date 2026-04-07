package com.notequiz.domain.quiz.repository;

import com.notequiz.domain.quiz.entity.WrongAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WrongAnswerRepository extends JpaRepository<WrongAnswer, Long> {
}
