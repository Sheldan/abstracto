package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name="channelGroup")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AChannelGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @Setter
    private String groupName;

    @ManyToMany
    @JoinTable(
            name = "channel_in_group",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "channel_id"))
    private Set<AChannel> channels;


}
