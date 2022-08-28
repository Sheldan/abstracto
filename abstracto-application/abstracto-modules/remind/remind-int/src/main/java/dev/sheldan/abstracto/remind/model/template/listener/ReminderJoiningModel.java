package dev.sheldan.abstracto.remind.model.template.listener;

import lombok.*;

import java.time.Instant;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReminderJoiningModel {
    private Instant reminderDate;
    private boolean failedToJoin;
    private boolean selfJoin;
    private boolean joined;
}
