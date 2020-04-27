package dev.sheldan.abstracto.utility.models.database;

import dev.sheldan.abstracto.core.models.database.AUser;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name="starboard_post_reaction")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StarboardPostReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reactorId", nullable = false)
    private AUser reactor;

    @OneToOne
    @JoinColumn(name = "postId", nullable = false)
    private StarboardPost starboardPost;

}
