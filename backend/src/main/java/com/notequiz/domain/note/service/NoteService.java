package com.notequiz.domain.note.service;

import com.notequiz.common.exception.ApiException;
import com.notequiz.common.exception.ErrorCode;
import com.notequiz.domain.note.dto.NoteUploadResponse;
import com.notequiz.domain.note.entity.Note;
import com.notequiz.domain.note.repository.NoteRepository;
import com.notequiz.domain.user.entity.User;
import com.notequiz.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final Tesseract tesseract;

    public NoteUploadResponse uploadNote(String title, MultipartFile file) {
        validateFile(file);

        String extractedText = extractText(file);
        String cleanedText = cleanText(extractedText);

        User currentUser = getCurrentUser();

        Note note = Note.builder()
                .user(currentUser)
                .title(title)
                .extractedText(cleanedText)
                .build();

        noteRepository.save(note);

        return NoteUploadResponse.builder()
                .noteId(note.getNoteId())
                .title(note.getTitle())
                .extractedTextLength(cleanedText.length())
                .build();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ApiException(ErrorCode.INVALID_FILE_FORMAT);
        }

        if (file.getSize() > 20 * 1024 * 1024) {
            throw new ApiException(ErrorCode.FILE_TOO_LARGE);
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf") &&
                !contentType.startsWith("image/"))) {
            throw new ApiException(ErrorCode.INVALID_FILE_FORMAT);
        }
    }

    private String extractText(MultipartFile file) {
        String contentType = Objects.requireNonNull(file.getContentType());

        try {
            if (contentType.equals("application/pdf")) {
                return extractTextFromPdf(file);
            } else if (contentType.startsWith("image/")) {
                return extractTextFromImage(file);
            }
        } catch (Exception e) {
            log.error("Text extraction failed", e);
            throw new ApiException(ErrorCode.TEXT_EXTRACT_FAILED);
        }

        throw new ApiException(ErrorCode.INVALID_FILE_FORMAT);
    }

    protected String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    protected String extractTextFromImage(MultipartFile file) throws IOException, TesseractException {
        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        return tesseract.doOCR(bufferedImage);
    }

    private String cleanText(String text) {
        if (text == null) return "";
        // Remove consecutive spaces and normalize special characters if needed
        return text.replaceAll("\\s+", " ").trim();
    }
}
