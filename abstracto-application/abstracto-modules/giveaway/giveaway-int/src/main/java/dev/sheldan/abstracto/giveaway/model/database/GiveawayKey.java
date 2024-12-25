package dev.sheldan.abstracto.giveaway.model.database;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.giveaway.model.database.embed.GiveawayKeyId;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "giveaway_key")
@Getter
@Setter
@EqualsAndHashCode
public class GiveawayKey {
  @Id
  @EmbeddedId
  private GiveawayKeyId id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumns(
      {
          @JoinColumn(name = "giveaway_id", referencedColumnName = "id"),
          @JoinColumn(name = "giveaway_server_id", referencedColumnName = "server_id")
      })
  private Giveaway giveaway;

  @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @MapsId("serverId")
  @JoinColumn(name = "server_id", referencedColumnName = "id", nullable = false)
  private AServer server;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "creator_user_id", nullable = false)
  private AUserInAServer creator;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "winner_user_id")
  private AUserInAServer winner;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "benefactor_user_id")
  private AUserInAServer benefactor;

  @Column(name = "key", nullable = false)
  private String key;

  @Column(name = "name")
  private String name;

  @Column(name = "used")
  private Boolean used;

  @Column(name = "description")
  private String description;

}
