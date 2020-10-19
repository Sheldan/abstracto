package dev.sheldan.abstracto.core.models.database;

import dev.sheldan.abstracto.core.command.models.database.ACommand;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "channel_group_command")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AChannelGroupCommand implements Serializable {

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
