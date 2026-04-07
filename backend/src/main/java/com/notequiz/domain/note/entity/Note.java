package com.notequiz.domain.note.entity;

import com.notequiz.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "notes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Note extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "note_id", nullable = false, unique = true)
    private String noteId;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Builder
    public Note(String title, String extractedText) {
        this.noteId = UUID.randomUUID().toString();
        this.title = title;
        this.extractedText = extractedText;
    }
}
