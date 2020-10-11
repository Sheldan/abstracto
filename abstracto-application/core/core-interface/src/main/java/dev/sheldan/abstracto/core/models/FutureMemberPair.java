package dev.sheldan.abstracto.core.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;

import java.util.concurrent.CompletableFuture;


@Getter
@Setter
@Builder
public class FutureMemberPair {
    private CompletableFuture<Member> firstMember;
    private CompletableFuture<Member> secondMember;
}
