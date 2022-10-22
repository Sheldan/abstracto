package dev.sheldan.abstracto.webservices.threadreader.command;

import dev.sheldan.abstracto.core.command.UtilityModuleDefinition;
import dev.sheldan.abstracto.core.command.condition.AbstractConditionableCommand;
import dev.sheldan.abstracto.core.command.config.CommandConfiguration;
import dev.sheldan.abstracto.core.command.config.HelpInfo;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.execution.CommandContext;
import dev.sheldan.abstracto.core.command.execution.CommandResult;
import dev.sheldan.abstracto.core.config.FeatureDefinition;
import dev.sheldan.abstracto.core.service.ChannelService;
import dev.sheldan.abstracto.core.utils.FutureUtils;
import dev.sheldan.abstracto.webservices.config.WebserviceFeatureDefinition;
import dev.sheldan.abstracto.webservices.threadreader.exception.NoTwitterLinkFoundException;
import dev.sheldan.abstracto.webservices.threadreader.model.ThreadReaderCommandResponseModel;
import dev.sheldan.abstracto.webservices.threadreader.service.ThreadReaderService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class ThreadReaderCommand extends AbstractConditionableCommand {

    public static final String THREAD_READER_RESPONSE_TEMPLATE_KEY = "threadReader_response";
    public static final String THREAD_READER_COMMAND = "threadReader";
    public static final String MESSAGE_PARAMETER = "message";

    @Autowired
    private ThreadReaderService threadReaderService;

    @Autowired
    private ChannelService channelService;

    @Override
    public CompletableFuture<CommandResult> executeAsync(CommandContext commandContext) {
        List<Object> parameters = commandContext.getParameters().getParameters();
        Message message = (Message) parameters.get(0);
        String messageText = message.getContentRaw();
        Optional<Long> tweetIdOptional = threadReaderService.extractTweetId(messageText);
        if(!tweetIdOptional.isPresent()) {
            throw new NoTwitterLinkFoundException();
        }
        Long tweetId = tweetIdOptional.get();
        ThreadReaderCommandResponseModel model = ThreadReaderCommandResponseModel
                .builder()
                .tweetId(tweetId)
                .build();
        return FutureUtils.toSingleFutureGeneric(channelService.sendEmbedTemplateInTextChannelList(THREAD_READER_RESPONSE_TEMPLATE_KEY, model, commandContext.getChannel()))
               .thenApply(unused -> CommandResult.fromSuccess());
    }

    @Override
    public FeatureDefinition getFeature() {
        return WebserviceFeatureDefinition.THREAD_READER;
    }

    @Override
    public CommandConfiguration getConfiguration() {
        List<Parameter> parameters = new ArrayList<>();

        Parameter messageParameter = Parameter
                .builder()
                .name(MESSAGE_PARAMETER)
                .type(Message.class)
                .remainder(true)
                .templated(true)
                .build();

        parameters.add(messageParameter);

        HelpInfo helpInfo = HelpInfo
                .builder()
                .templated(true)
                .build();

        return CommandConfiguration.builder()
                .name(THREAD_READER_COMMAND)
                .module(UtilityModuleDefinition.UTILITY)
                .templated(true)
                .async(true)
                .supportsEmbedException(true)
                .parameters(parameters)
                .help(helpInfo)
                .build();
    }
}
