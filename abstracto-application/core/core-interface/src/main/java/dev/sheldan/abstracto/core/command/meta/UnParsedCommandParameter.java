package dev.sheldan.abstracto.core.command.meta;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class UnParsedCommandParameter {

    private static Pattern SPLIT_REGEX  = Pattern.compile("\"([^\"]*)\"|(\\S+)");

    public UnParsedCommandParameter(String parameters) {
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
                    this.parameters.add(group);
                }
            } else {
                String group = m.group(2);
                if(!group.equals("")) {
                    this.parameters.add(group);
                }
            }
        }
    }
    private List<String> parameters;
}
