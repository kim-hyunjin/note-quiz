package com.notequiz.domain.quiz.repository;

import com.notequiz.domain.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByShareToken(String shareToken);
}
