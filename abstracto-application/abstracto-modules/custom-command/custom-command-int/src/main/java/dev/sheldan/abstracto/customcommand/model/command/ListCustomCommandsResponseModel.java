package dev.sheldan.abstracto.customcommand.model.command;

import dev.sheldan.abstracto.customcommand.model.database.CustomCommand;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@Getter
public class ListCustomCommandsResponseModel {
    private List<ListCustomCommandModel> customCommands;

    public static ListCustomCommandsResponseModel fromCommands(List<CustomCommand> customCommands) {
        return ListCustomCommandsResponseModel
                .builder()
                .customCommands(customCommands
                        .stream()
                        .map(ListCustomCommandModel::fromCustomCommand)
                        .collect(Collectors.toList()))
                .build();
    }
}
