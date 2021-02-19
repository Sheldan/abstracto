package dev.sheldan.abstracto.core.command.execution;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class UnParsedCommandParameter {

    private static Pattern SPLIT_REGEX  = Pattern.compile("\"([^\"]*)\"|(\\S+)");

    public UnParsedCommandParameter(String parameters, Message message) {
        this.parameters = new ArrayList<>();
        Matcher m = SPLIT_REGEX.matcher(parameters);
        boolean skippedCommand = false;
        while (m.find()) {
            if(!skippedCommand) {
                skippedCommand = true;
                continue;
            }
            if (m.group(1) != null) {
                String group = m.group(1);
                if(!group.equals("")) {
                    this.parameters.add(UnparsedCommandParameterPiece.builder().value(group).build());
                }
            } else {
                String group = m.group(2);
                if(!group.equals("")) {
                    this.parameters.add(UnparsedCommandParameterPiece.builder().value(group).build());
                }
            }
        }
        message.getAttachments().forEach(attachment ->
                this.parameters.add(UnparsedCommandParameterPiece.builder().value(attachment).type(ParameterPieceType.ATTACHMENT).build()));
    }
    private List<UnparsedCommandParameterPiece> parameters;
}
