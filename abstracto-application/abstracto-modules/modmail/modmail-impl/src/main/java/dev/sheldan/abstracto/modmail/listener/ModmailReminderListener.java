package dev.sheldan.abstracto.modmail.listener;

import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.models.template.display.RoleDisplay;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureConfig;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.config.ModMailMode;
import dev.sheldan.abstracto.modmail.model.database.ModMailRole;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.model.database.ModMailThreadState;
import dev.sheldan.abstracto.modmail.model.listener.ModmailThreadActionListenerModel;
import dev.sheldan.abstracto.modmail.model.template.ModmailThreadReminderModel;
import dev.sheldan.abstracto.modmail.service.management.ModMailRoleManagementService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class ModmailReminderListener implements ModmailThreadActionListener {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private ModMailRoleManagementService modMailRoleManagementService;

    @Autowired
    private ModmailReminderListener self;

    private static final String MODMAIL_THREAD_REMINDER_TEMPLATE_KEY = "modmail_thread_reminder_notification";

    @Override
    public Integer getPriority() {
        return ListenerPriority.MEDIUM;
    }

    @Override
    public ModmailThreadActionListenerResult execute(ModmailThreadActionListenerModel model) {
        ModmailThreadActionListenerResult result;
        if(!featureModeService.featureModeActive(ModMailFeatureDefinition.MOD_MAIL, model.getServerId(), ModMailMode.THREAD_REMINDER)) {
            result = ModmailThreadActionListenerResult.IGNORED;
        } else {
            String reminderDurationString = configService.getStringValueOrConfigDefault(ModMailFeatureConfig.MOD_MAIL_REMINDER_DURATION, model.getServerId());
            Duration duration = ParseUtils.parseDuration(reminderDurationString);
            Instant timeInPastDuration = Instant.now().minus(duration);
            ModMailThread thread = modMailThreadManagementService.getById(model.getThreadId());
            if(List.of(ModMailThreadState.CLOSED, ModMailThreadState.CLOSING).contains(thread.getState())) {
                log.debug("Thread {} is closed - ignoring.", model.getThreadId());
                return ModmailThreadActionListenerResult.IGNORED;
            }
            Instant timeStampToConsider = getTimestampToUse(thread, duration);
            boolean mustBeReminded = timeInPastDuration.isAfter(timeStampToConsider);
            if (mustBeReminded) {
                    sendReminder(thread)
                        .thenAccept(unused -> {
                            self.updateSnoozeTimer(model.getThreadId(), duration);
                            log.info("Sent reminder about thread {}", model.getThreadId());
                        })
                    .exceptionally(throwable -> {
                        log.warn("Failed to send reminder about thread {}.", model.getThreadId(), throwable);
                        return null;
                    });
                    result = ModmailThreadActionListenerResult.PROCESSED;
                } else {
                    result = ModmailThreadActionListenerResult.IGNORED;
                }
        }

        return result;
    }


    private static Instant getTimestampToUse(ModMailThread thread, Duration configuredDuration) {
        if (thread.getRemindersSnoozedUntil() != null) {
            return thread.getRemindersSnoozedUntil().minus(configuredDuration);
        }
        return getUpdatedOrCrated(thread);
    }

    private static Instant getUpdatedOrCrated(ModMailThread thread) {
        if(thread.getUpdated() != null) {
            return thread.getUpdated();
        }
        return thread.getCreated();
    }

    private CompletableFuture<Void> sendReminder(ModMailThread modMailThread) {
        List<ModMailRole> modmailRolesToPing = modMailRoleManagementService.getRolesForServer(modMailThread.getServer());
        List<RoleDisplay> rolesToDisplay = modmailRolesToPing.stream().map(role -> RoleDisplay.fromARole(role.getRole())).toList();
        Instant autoCloseInstant;
        if(featureModeService.featureModeActive(ModMailFeatureDefinition.MOD_MAIL, modMailThread.getServer().getId(), ModMailMode.THREAD_AUTO_CLOSE)
            && !modMailThread.getState().equals(ModMailThreadState.PAUSED)) {
            String closeDurationString = configService.getStringValueOrConfigDefault(ModMailFeatureConfig.MOD_MAIL_AUTO_CLOSE_DURATION, modMailThread.getServer().getId());
            Duration autoCloseDuration = ParseUtils.parseDuration(closeDurationString);
            autoCloseInstant = getUpdatedOrCrated(modMailThread).plus(autoCloseDuration);
        } else {
            autoCloseInstant = null;
        }
        ModmailThreadReminderModel model = ModmailThreadReminderModel
            .builder()
            .updated(getUpdatedOrCrated(modMailThread))
            .created(modMailThread.getCreated())
            .paused(modMailThread.getState().equals(ModMailThreadState.PAUSED))
            .autoCloseInstant(autoCloseInstant)
            .pingRoles(rolesToDisplay)
            .memberDisplay(MemberDisplay.fromAUserInAServer(modMailThread.getUser()))
            .build();
        MessageToSend messageToSend = templateService.renderEmbedTemplate(MODMAIL_THREAD_REMINDER_TEMPLATE_KEY, model, modMailThread.getServer().getId());
        return FutureUtils.toSingleFutureGeneric(channelService.sendMessageEmbedToSendToAChannel(messageToSend, modMailThread.getChannel()));
    }

    @Transactional
    public void updateSnoozeTimer(long modmailThreadId, Duration duration) {
        ModMailThread thread = modMailThreadManagementService.getById(modmailThreadId);
        thread.setRemindersSnoozedUntil(Instant.now().plus(duration));
    }
}
