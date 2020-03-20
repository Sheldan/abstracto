package dev.sheldan.abstracto.core.models.database;

import lombok.*;

import javax.persistence.*;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name="posttarget")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Getter
    private Long id;

    @Column(unique = true)
    @Getter
    private String name;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    @Getter @Setter
    private AChannel channelReference;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="server_id", nullable = false)
    @Getter @Setter
    private AServer serverReference;

    public static String JOIN_LOG = "joinLog";
    public static String LEAVE_LOG = "leaveLog";
    public static String WARN_LOG = "warnLog";
    public static String KICK_LOG = "kickLog";
    public static String BAN_LOG = "banLog";
    public static String EDIT_LOG = "editLog";
    public static String DELETE_LOG = "deleteLog";

    public static List<String> AVAILABLE_POST_TARGETS = Arrays.asList(JOIN_LOG, LEAVE_LOG, WARN_LOG, KICK_LOG, BAN_LOG, EDIT_LOG, DELETE_LOG);
}
