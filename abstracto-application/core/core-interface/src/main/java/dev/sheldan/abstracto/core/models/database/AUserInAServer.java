package dev.sheldan.abstracto.core.models.database;

import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;

@Entity @Getter @Builder
public class AUserInAServer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userInServerId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "userReference")
    private AUser userReference;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "serverReference")
    private AServer serverReference;
}
