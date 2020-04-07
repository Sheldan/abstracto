package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "channel_group_command")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AChannelGroupCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commandInGroupId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "command_id", nullable = false)
    @Setter
    private ACommand command;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @Setter
    private AChannelGroup group;

    @Setter
    private Boolean enabled;

}
