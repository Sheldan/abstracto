package dev.sheldan.abstracto.moderation.command;

import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.exception.EntityGuildMismatchException;
import dev.sheldan.abstracto.core.interaction.InteractionService;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandConfig;
import dev.sheldan.abstracto.core.interaction.slash.SlashCommandPrivilegeLevels;
import dev.sheldan.abstracto.core.interaction.slash.parameter.SlashCommandParameterService;
import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.PaginatorService;
import dev.sheldan.abstracto.core.service.management.ServerManagementService;
import dev.sheldan.abstracto.core.service.management.UserInServerManagementService;
import dev.sheldan.abstracto.core.templating.model.MessageToSend;
import dev.sheldan.abstracto.core.templating.service.TemplateService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.moderation.config.ModerationModuleDefinition;
import dev.sheldan.abstracto.moderation.config.ModerationSlashCommandNames;
import dev.sheldan.abstracto.moderation.config.feature.ModerationFeatureDefinition;
import dev.sheldan.abstracto.moderation.model.database.Infraction;
import dev.sheldan.abstracto.moderation.model.database.InfractionParameter;
import dev.sheldan.abstracto.moderation.model.template.command.InfractionEntry;
import dev.sheldan.abstracto.moderation.model.template.command.InfractionsModel;
import dev.sheldan.abstracto.moderation.service.management.InfractionManagementService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Component
public class Infractions extends AbstractConditionableCommand {

    private static final String INFRACTIONS_COMMAND = "infractions";
    private static final String USER_PARAMETER = "user";
    private static final String INFRACTIONS_RESPONSE_TEMPLATE = "infractions_display_response";
    private static final String NO_INFRACTIONS_TEMPLATE_KEY = "infractions_no_infractions_found";

    @Autowired
    private InfractionManagementService infractionManagementService;

    @Autowired
    private UserInServerManagementService userInServerManagementService;

    @Autowired
    private ServerManagementService serverManagementService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PaginatorService paginatorService;

    @Autowired
    private InteractionService interactionService;

    @Autowired
    private SlashCommandParameterService slashCommandParameterService;

    @Autowired
    private Infractions self;

    @Override
    public CompletableFuture<CommandResult> executeSlash(SlashCommandInteractionEvent event) {
        return event.deferReply().submit()
            .thenCompose(interactionHook -> self.showInfractions(interactionHook, event));
    }

    @Transactional
    public CompletableFuture<CommandResult> showInfractions(InteractionHook hook, SlashCommandInteractionEvent event) {
        List<Infraction> infractions;
        Guild guild = hook.getInteraction().getGuild();
        if(slashCommandParameterService.hasCommandOptionWithFullType(USER_PARAMETER, event, OptionType.USER)) {
            Member member = slashCommandParameterService.getCommandOption(USER_PARAMETER, event, User.class, Member.class);
            if(!member.getGuild().equals(guild)) {
                throw new EntityGuildMismatchException();
            }
            infractions = infractionManagementService.getInfractionsForUser(userInServerManagementService.loadOrCreateUser(member));
        } else if(slashCommandParameterService.hasCommandOptionWithFullType(USER_PARAMETER, event, OptionType.STRING)){
            String userIdStr = slashCommandParameterService.getCommandOption(USER_PARAMETER, event, User.class, String.class);
            Long userId = Long.parseLong(userIdStr);
            AUserInAServer userInServer = userInServerManagementService.loadOrCreateUser(guild.getIdLong(), userId);
            infractions = infractionManagementService.getInfractionsForUser(userInServer);

        } else {
            AServer server = serverManagementService.loadServer(guild);
            infractions = infractionManagementService.getInfractionsForServer(server);
        }
        if(infractions.isEmpty()) {
            MessageToSend messageToSend = templateService.renderEmbedTemplate(NO_INFRACTIONS_TEMPLATE_KEY, new Object(), guild.getIdLong());
            return FutureUtils.toSingleFutureGeneric(interactionService.sendMessageToInteraction(messageToSend, hook))
                .thenApply(interactionHook -> CommandResult.fromSuccess());

        } else {
            List<InfractionEntry> convertedInfractions = fromInfractions(infractions);
            InfractionsModel model = InfractionsModel
                    .builder()
                    .entries(convertedInfractions)
                    .build();
            return paginatorService.sendPaginatorToInteraction(INFRACTIONS_RESPONSE_TEMPLATE, model, hook)
                .thenApply(unused -> CommandResult.fromSuccess());
        }
    }

    public List<InfractionEntry> fromInfractions(List<Infraction> infractions) {
        return infractions
                .stream()
                .map(this::fromInfraction)
                .collect(Collectors.toList());
    }

    private InfractionEntry fromInfraction(Infraction infraction) {
        Map<String, String> parameters = infraction
                .getParameters()
                .stream()
                .collect(Collectors.toMap(infractionParameter -> infractionParameter.getInfractionParameterId().getName(), InfractionParameter::getValue));
        return InfractionEntry
                .builder()
                .infractionId(infraction.getId())
                .serverId(infraction.getServer().getId())
                .decayed(infraction.getDecayed())
                .decayDate(infraction.getDecayedDate())
                .parameters(parameters)
                .creationDate(infraction.getCreated())
                .infractionUser(MemberDisplay.fromAUserInAServer(infraction.getUser()))
                .infractionCreationUser(MemberDisplay.fromAUserInAServer(infraction.getInfractionCreator()))
                .reason(infraction.getDescription())
                .type(infraction.getType())
                .build();
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();
        Parameter userParameter = Parameter
                .builder()
                .name(USER_PARAMETER)
                .type(User.class)
                .templated(true)
                .optional(true)
                .build();
        parameters.add(userParameter);

        SlashCommandConfig slashCommandConfig = SlashCommandConfig
                .builder()
                .enabled(true)
                .rootCommandName(ModerationSlashCommandNames.INFRACTIONS)
                .defaultPrivilege(SlashCommandPrivilegeLevels.ADMIN)
                .commandName("list")
                .build();

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();
        return CommandConfiguration.builder()
                .name(INFRACTIONS_COMMAND)
                .module(ModerationModuleDefinition.MODERATION)
                .templated(true)
                .slashCommandOnly(true)
                .async(true)
                .causesReaction(false)
                .slashCommandConfig(slashCommandConfig)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }

    @Override
    public FeatureDefinition getFeature() {
        return ModerationFeatureDefinition.INFRACTIONS;
    }
}
