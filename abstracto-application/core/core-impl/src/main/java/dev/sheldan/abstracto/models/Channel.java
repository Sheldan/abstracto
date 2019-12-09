package dev.sheldan.abstracto.models;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table
@Builder
public class Channel {

    @Id
    @Getter
    public Long id;

    @Getter
    @ManyToMany(mappedBy = "channels")
    private Set<ChannelGroup> groups;
}
