package dev.sheldan.abstracto.modmail.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.service.PaginatorService;
import dev.sheldan.abstracto.modmail.config.ModMailFeatureDefinition;
import dev.sheldan.abstracto.modmail.config.ModMailSlashCommandNames;
import dev.sheldan.abstracto.modmail.model.database.QuickReply;
import dev.sheldan.abstracto.modmail.model.template.QuickRepliesListResponseModel;
import dev.sheldan.abstracto.modmail.service.QuickReplyService;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class QuickReplyList extends AbstractConditionableCommand {


    private static final String LIST_QUICK_REPLIES_COMMAND = "listQuickReplies";
    private static final String LIST_QUICK_REPLIES_TEMPLATE_KEY = "listQuickReplies_response";
    private static final String NO_QUICK_REPLIES_TEMPLATE_KEY = "listQuickReplies_no_quick_replies_response";

    @Autowired
    private QuickReplyService quickReplyService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private PaginatorService paginatorService;


    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        List<QuickReply> quickReplies = quickReplyService.getQuickReplies(event.getGuild());
        if(quickReplies.isEmpty()) {
            return interactionService.replyEmbed(NO_QUICK_REPLIES_TEMPLATE_KEY, event)
                .thenApply(interactionHook -> CommandResult.fromSuccess());
        }
        QuickRepliesListResponseModel model = QuickRepliesListResponseModel.fromQuickReplies(quickReplies);
        return paginatorService.createPaginatorFromTemplate(LIST_QUICK_REPLIES_TEMPLATE_KEY, model, event)
            .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModMailFeatureDefinition.MOD_MAIL;
    }

    @Override
    public CommandConfiguration getConfiguration() {
        HelpInfo helpInfo = HelpInfo
            .builder()
            .templated(true)
            .build();

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
            .builder()
            .enabled(true)
            .rootCommandName(ModMailSlashCommandNames.MODMAIL)
            .commandName("listQuickReply")
            .build();

        return CommandConfiguration.builder()
            .name(LIST_QUICK_REPLIES_COMMAND)
            .module(UtilityModuleDefinition.UTILITY)
            .templated(true)
            .async(true)
            .slashCommandOnly(true)
            .slashCommandConfig(slashCommandConfig)
            .causesReaction(true)
            .supportsEmbedException(true)
            .help(helpInfo)
            .build();
    }
}
