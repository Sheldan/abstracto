package dev.sheldan.abstracto.moderation.commands;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.command.execution.ContextConverter;
import dev.sheldan.abstracto.core.config.FeatureEnum;
import dev.sheldan.abstracto.core.models.FullUser;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.moderation.config.ModerationModule;
import dev.sheldan.abstracto.moderation.config.features.ModerationFeatures;
import dev.sheldan.abstracto.moderation.converter.UserNotesConverter;
import dev.sheldan.abstracto.moderation.models.database.UserNote;
import dev.sheldan.abstracto.moderation.models.template.commands.ListNotesModel;
import dev.sheldan.abstracto.moderation.service.management.UserNoteManagementService;
import dev.sheldan.abstracto.templating.service.TemplateService;
import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserNotes extends AbstractConditionableCommand {
    @Autowired
    private UserNoteManagementService userNoteManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private UserNotesConverter userNotesConverter;

    @Override
    public CommandResult execute(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        List<UserNote> userNotes;

        ListNotesModel model = (ListNotesModel) ContextConverter.fromCommandContext(commandContext, ListNotesModel.class);
        if(parameters.size() == 1) {
            Member member = (Member) parameters.get(0);
            AUserInAServer userInAServer = userInServerManagementService.loadUser(member);
            userNotes = userNoteManagementService.loadNotesForUser(userInAServer);
            FullUser specifiedUser = FullUser
                    .builder()
                    .aUserInAServer(userInAServer)
                    .member(member)
                    .build();
            model.setSpecifiedUser(specifiedUser);
        } else {
            userNotes = userNoteManagementService.loadNotesForServer(commandContext.getUserInitiatedContext().getServer());
        }
        model.setUserNotes(userNotesConverter.fromNotes(userNotes));
        channelService.sendEmbedTemplateInChannel("user_notes_response", model, commandContext.getChannel());
        return CommandResult.fromSuccess();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter user = Parameter.builder().name("user").type(Member.class).optional(true).templated(true).build();
        parameters.add(user);
        HelpInfo helpInfo = HelpInfo.builder().templated(true).build();
        return CommandConfiguration.builder()
                .name("userNotes")
                .module(ModerationModule.MODERATION)
                .templated(true)
                .causesReaction(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureEnum getFeature() {
        return ModerationFeatures.USER_NOTES;
    }
}