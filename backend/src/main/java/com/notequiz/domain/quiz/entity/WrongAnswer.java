package com.notequiz.domain.quiz.entity;

import com.notequiz.common.entity.BaseTimeEntity;
import com.notequiz.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wrong_answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WrongAnswer extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false)
    private Boolean resolved;

    @Builder
    public WrongAnswer(User user, Question question, Boolean resolved) {
        this.user = user;
        this.question = question;
        this.resolved = resolved != null ? resolved : false;
    }

    public void resolve() {
        this.resolved = true;
    }
}
