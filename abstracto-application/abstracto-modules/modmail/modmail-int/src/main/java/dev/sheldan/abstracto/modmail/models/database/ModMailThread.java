package dev.sheldan.abstracto.modmail.models.database;

import dev.sheldan.abstracto.core.models.database.*;
import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "modmail_threads")
@Cacheable
@Getter
@Setter
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class ModMailThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modmail_user", nullable = false)
    private AUserInAServer user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modmail_thread_channel", nullable = false)
    private AChannel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modmail_thread_server", nullable = false)
    private AServer server;

    @Column(name = "created")
    private Instant created;

    @PrePersist
    private void onInsert() {
        this.created = Instant.now();
    }

    @Column(name = "updated")
    private Instant updated;

    @PreUpdate
    private void onUpdate() {
        this.updated = Instant.now();
    }

    @Column
    private Instant closed;

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            mappedBy = "threadReference")
    @Builder.Default
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<ModMailMessage> messages = new ArrayList<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true,
            mappedBy = "threadReference")
    @Builder.Default
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<ModMailThreadSubscriber> subscribers = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column
    private ModMailThreadState state;

}
