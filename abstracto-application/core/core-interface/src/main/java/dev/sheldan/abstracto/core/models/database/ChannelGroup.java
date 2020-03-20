package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name="channelGroup")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChannelGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Column
    @Getter @Setter
    private String groupName;

    @ManyToMany
    @JoinTable(
            name = "channel_in_group",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "channel_id"))
    @Getter
    private Set<AChannel> channels;


}
