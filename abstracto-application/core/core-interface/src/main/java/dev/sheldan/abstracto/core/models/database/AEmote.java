package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "emote")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class AEmote {

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer Id;

    @Column
    private String name;

    @Column
    private String emoteKey;

    @Column
    private Long emoteId;

    @Column
    private Boolean animated;

    @Column
    private Boolean custom;

    @ManyToOne(fetch = FetchType.LAZY)
    private AServer serverRef;


}
