package dev.sheldan.abstracto.core.command.handler;

import dev.sheldan.abstracto.core.command.Command;
import dev.sheldan.abstracto.core.command.CommandConstants;
import dev.sheldan.abstracto.core.command.config.Parameter;
import dev.sheldan.abstracto.core.command.exception.NoAttachmentFoundException;
import dev.sheldan.abstracto.core.command.execution.ParameterPieceType;
import dev.sheldan.abstracto.core.command.execution.UnparsedCommandParameterPiece;
import dev.sheldan.abstracto.core.command.handler.provided.FileParameterHandler;
import dev.sheldan.abstracto.core.service.HttpService;
import net.dv8tion.jda.api.entities.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

@Component
public class FileParameterHandlerImpl implements FileParameterHandler {

    @Autowired
    private HttpService httpService;

    @Override
    public boolean async() {
        return true;
    }

    @Override
    public CompletableFuture<Object> handleAsync(UnparsedCommandParameterPiece input, CommandParameterIterators iterators, Parameter param, Message context, Command command) {
        if(context.getAttachments().isEmpty()) {
            throw new NoAttachmentFoundException();
        }
        Message.Attachment attachment = (Message.Attachment) input.getValue();
        CompletableFuture<Object> result = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try {
                result.complete(httpService.downloadFileToTempFile(attachment.getUrl()));
            } catch (IOException e) {
                result.completeExceptionally(e);
            }
        });
        return result;
    }

    @Override
    public boolean handles(Class clazz, UnparsedCommandParameterPiece value) {
        return clazz.equals(File.class) && value.getType().equals(ParameterPieceType.ATTACHMENT);
    }

    @Override
    public Integer getPriority() {
        return CommandConstants.CORE_HANDLER_PRIORITY;
    }
}
