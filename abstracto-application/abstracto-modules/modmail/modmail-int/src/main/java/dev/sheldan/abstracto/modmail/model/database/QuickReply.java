package dev.sheldan.abstracto.modmail.model.database;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "quick_reply")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class QuickReply implements Serializable {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "additional_message")
    private String additionalMessage;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    private AServer server;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_in_server_id")
    private AUserInAServer creator;

    @Column(name = "anonymous")
    private Boolean anonymous;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;

    @Column(name = "updated", insertable = false, updatable = false)
    private Instant updated;

}
