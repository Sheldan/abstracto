package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "component_payload")
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ComponentPayload {
    @Id
    @Column(name = "id", nullable = false, length = 100)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Getter
    @Setter
    @JoinColumn(name = "server_id")
    private AServer server;

    @Lob
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "payload")
    private String payload;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ComponentType type;

    @Column(name = "payload_type", length = 255)
    private String payloadType;

    @Column(name = "origin", length = 128)
    private String origin;

    @Column(name = "created", nullable = false, insertable = false, updatable = false)
    private Instant created;
}
