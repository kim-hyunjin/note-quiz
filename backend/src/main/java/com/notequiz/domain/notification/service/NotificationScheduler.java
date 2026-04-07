package com.notequiz.domain.notification.service;

import com.notequiz.domain.notification.entity.NotificationSetting;
import com.notequiz.domain.notification.entity.NotificationTargetNote;
import com.notequiz.domain.notification.repository.NotificationSettingRepository;
import com.notequiz.domain.quiz.dto.QuizGenerateRequest;
import com.notequiz.domain.quiz.dto.QuizResponse;
import com.notequiz.domain.quiz.service.QuizService;
import com.notequiz.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private final NotificationSettingRepository notificationSettingRepository;
    private final QuizService quizService;
    private final JavaMailSender mailSender;

    @Scheduled(cron = "0 * * * * *") // Every minute
    public void sendDailyQuizzes() {
        LocalTime now = LocalTime.now().truncatedTo(ChronoUnit.MINUTES);
        log.info("Checking daily quizzes for time: {}", now);

        List<NotificationSetting> settings = notificationSettingRepository.findAll().stream()
                .filter(s -> s.getDailyQuizEnabled() && s.getDailyQuizTime() != null &&
                        s.getDailyQuizTime().truncatedTo(ChronoUnit.MINUTES).equals(now))
                .toList();

        for (NotificationSetting setting : settings) {
            processDailyQuiz(setting);
        }
    }

    private void processDailyQuiz(NotificationSetting setting) {
        User user = setting.getUser();
        log.info("Processing daily quiz for user: {}", user.getEmail());

        // Set security context for QuizService (since it calls getCurrentUser)
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getEmail(), null, List.of())
        );

        try {
            StringBuilder emailBody = new StringBuilder();
            emailBody.append("오늘의 퀴즈가 도착했습니다!\n\n");

            for (NotificationTargetNote target : setting.getTargetNotes()) {
                QuizGenerateRequest request = new QuizGenerateRequest();
                // Set fields using reflection or similar if no public constructor/setter
                setField(request, "noteId", target.getNote().getNoteId());
                setField(request, "questionCount", target.getQuestionCount());

                QuizResponse quiz = quizService.generateQuiz(request);
                emailBody.append(String.format("- %s: http://localhost:5173/quiz/%d\n",
                        target.getNote().getTitle(), quiz.getId()));
            }

            sendEmail(user.getEmail(), "[NoteQuiz] 오늘의 퀴즈", emailBody.toString());
        } catch (Exception e) {
            log.error("Failed to generate daily quiz for user: {}", user.getEmail(), e);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
        }
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
