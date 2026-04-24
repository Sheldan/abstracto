package dev.sheldan.abstracto.modmail.listener;

import dev.sheldan.abstracto.core.config.ListenerPriority;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.core.service.FeatureModeService;
import dev.sheldan.abstracto.core.service.GuildService;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.ParseUtils;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureConfig;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.config.ModMailMode;
import dev.sheldan.abstracto.modmail.model.ClosingContext;
import dev.sheldan.abstracto.modmail.model.database.ModMailThread;
import dev.sheldan.abstracto.modmail.model.database.ModMailThreadState;
import dev.sheldan.abstracto.modmail.model.listener.ModmailThreadActionListenerModel;
import dev.sheldan.abstracto.modmail.service.ModMailThreadService;
import dev.sheldan.abstracto.modmail.service.management.ModMailThreadManagementService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ModmailAutoCloseListener implements ModmailThreadActionListener {

    @Autowired
    private ModMailThreadManagementService modMailThreadManagementService;

    @Autowired
    private ConfigService configService;

    @Autowired
    private FeatureModeService featureModeService;

    @Autowired
    private ModMailThreadService modMailThreadService;

    @Autowired
    private ModmailReminderListener self;

    @Autowired
    private GuildService guildService;

    @Autowired
    private TemplateService templateService;

    private static final String AUTO_CLOSE_NOTE_TEMPLATE_KEY = "modmail_auto_closing_note_text";

    @Override
    public Integer getPriority() {
        return ListenerPriority.HIGH;
    }

    @Override
    public ModmailThreadActionListenerResult execute(ModmailThreadActionListenerModel model) {
        ModmailThreadActionListenerResult result;
        if(!featureModeService.featureModeActive(ModMailFeatureDefinition.MOD_MAIL, model.getServerId(), ModMailMode.THREAD_AUTO_CLOSE)) {
            result = ModmailThreadActionListenerResult.IGNORED;
        } else {
            String closeDuration = configService.getStringValueOrConfigDefault(ModMailFeatureConfig.MOD_MAIL_AUTO_CLOSE_DURATION, model.getServerId());
            Duration duration = ParseUtils.parseDuration(closeDuration);
            Instant timeInPastDuration = Instant.now().minus(duration);
            ModMailThread thread = modMailThreadManagementService.getById(model.getThreadId());
            if(thread.getState() == ModMailThreadState.PAUSED) {
                log.info("Thread {} is paused - not closing.", thread.getId());
                return ModmailThreadActionListenerResult.IGNORED;
            }
            Instant timeStampToConsider = getTimeStampToConsider(thread);
            boolean mustBeClosed = timeInPastDuration.isAfter(timeStampToConsider);
            if (mustBeClosed) {
                closeThread(thread)
                    .thenAccept(unused -> {
                        self.updateSnoozeTimer(model.getThreadId(), duration);
                        log.info("Automatically closed thread {}", model.getThreadId());
                    })
                    .exceptionally(throwable -> {
                        log.warn("Failed to automatically close thread {}.", model.getThreadId(), throwable);
                        return null;
                    });
                result = ModmailThreadActionListenerResult.FINAL;
            } else {
                result = ModmailThreadActionListenerResult.IGNORED;
            }
        }

        return result;
    }

    private static Instant getTimeStampToConsider(ModMailThread thread) {
        if(thread.getUpdated() != null) {
            return thread.getUpdated();
        }
        return thread.getCreated();
    }

    private CompletableFuture<Void> closeThread(ModMailThread modMailThread) {
        Guild guild = guildService.getGuildById(modMailThread.getServer().getId());
        if(guild != null) {
            String closingNote = templateService.renderTemplate(AUTO_CLOSE_NOTE_TEMPLATE_KEY, new Object(), modMailThread.getServer().getId());
            ClosingContext closingContext = ClosingContext
                .builder()
                .notifyUser(true)
                .log(true)
                .closingMember(guild.getSelfMember())
                .note(closingNote)
                .build();
            return modMailThreadService.closeModMailThreadEvaluateLogging(modMailThread, closingContext, new ArrayList<>());
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }
}
