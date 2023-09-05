package dev.sheldan.abstracto.entertainment.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.entertainment.dto.CreditGambleResult;
import dev.sheldan.abstracto.entertainment.dto.PayDayResult;
import dev.sheldan.abstracto.entertainment.dto.SlotsResult;
import dev.sheldan.abstracto.entertainment.model.command.CreditsLeaderboardEntry;
import dev.sheldan.abstracto.entertainment.model.database.EconomyUser;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public interface EconomyService {
    EconomyUser addCredits(AUserInAServer aUserInAServer, Long credits);
    void addCredits(EconomyUser economyUser, Long credits);
    void addPayDayCredits(AUserInAServer aUserInAServer);
    PayDayResult triggerPayDay(AUserInAServer aUserInAServer);
    SlotsResult triggerSlots(AUserInAServer aUserInAServer, Long bid);
    SlotGame playSlots();
    List<CreditsLeaderboardEntry> getCreditLeaderboard(AServer server, Integer page);
    CreditsLeaderboardEntry getRankOfUser(AUserInAServer aUserInAServer);
    void transferCredits(AUserInAServer source, AUserInAServer target, Long amount);
    CreditGambleResult triggerCreditGamble(AUserInAServer aUserInAServer);

    @Builder
    @Getter
    class SlotGame {
        private List<List<String>> rows;
        private String outcome;
        private Integer resultFactor;
    }
}
