package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.command.models.ACommand;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "channel_group_command")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AChannelGroupCommand {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "command_id", nullable = false)
    @Getter
    @Setter
    @Column
    private ACommand command;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @Getter
    @Setter
    @Column
    private AChannelGroup group;

    private Boolean enabled;

}
