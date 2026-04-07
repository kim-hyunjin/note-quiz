package com.notequiz.domain.note.repository;

import com.notequiz.domain.note.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NoteRepository extends JpaRepository<Note, Long> {
    Optional<Note> findByNoteId(String noteId);
}
