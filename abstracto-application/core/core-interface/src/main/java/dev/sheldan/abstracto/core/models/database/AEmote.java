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
    @Setter
    private String emoteKey;

    @Column
    @Setter
    private Long emoteId;

    @Column
    @Setter
    private Boolean animated;

    @Column
    @Setter
    private Boolean custom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emote_server_id", nullable = false)
    private AServer serverRef;


}
