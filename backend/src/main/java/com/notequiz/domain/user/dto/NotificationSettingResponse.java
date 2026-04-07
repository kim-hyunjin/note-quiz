package com.notequiz.domain.user.dto;

import com.notequiz.domain.notification.entity.NotificationSetting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSettingResponse {
    private Boolean dailyQuizEnabled;
    private LocalTime dailyQuizTime;
    private List<TargetNoteDto> targetNotes;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetNoteDto {
        private String noteId;
        private Integer questionCount;
    }

    public static NotificationSettingResponse from(NotificationSetting setting) {
        return NotificationSettingResponse.builder()
                .dailyQuizEnabled(setting.getDailyQuizEnabled())
                .dailyQuizTime(setting.getDailyQuizTime())
                .targetNotes(setting.getTargetNotes().stream()
                        .map(tn -> TargetNoteDto.builder()
                                .noteId(tn.getNote().getNoteId())
                                .questionCount(tn.getQuestionCount())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
