package com.notequiz.domain.note.dto;

import com.notequiz.domain.note.entity.Note;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NoteResponse {
    private String noteId;
    private String title;
    private int extractedTextLength;
    private LocalDateTime createdAt;

    public static NoteResponse from(Note note) {
        return NoteResponse.builder()
                .noteId(note.getNoteId())
                .title(note.getTitle())
                .extractedTextLength(note.getExtractedText() != null ? note.getExtractedText().length() : 0)
                .createdAt(note.getCreatedAt())
                .build();
    }
}
