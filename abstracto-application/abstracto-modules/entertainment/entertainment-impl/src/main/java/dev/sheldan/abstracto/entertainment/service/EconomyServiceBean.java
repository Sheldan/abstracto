package dev.sheldan.abstracto.entertainment.service;

import dev.sheldan.abstracto.core.models.database.AServer;
import dev.sheldan.abstracto.core.models.database.AUserInAServer;
import dev.sheldan.abstracto.core.models.template.display.MemberDisplay;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.abstracto.entertainment.config.EconomyFeatureConfig;
import dev.sheldan.abstracto.entertainment.dto.CreditGambleResult;
import dev.sheldan.abstracto.entertainment.dto.PayDayResult;
import dev.sheldan.abstracto.entertainment.dto.SlotsResult;
import dev.sheldan.abstracto.entertainment.exception.NotEnoughCreditsException;
import dev.sheldan.abstracto.entertainment.model.command.CreditsLeaderboardEntry;
import dev.sheldan.abstracto.entertainment.model.database.EconomyLeaderboardResult;
import dev.sheldan.abstracto.entertainment.model.database.EconomyUser;
import dev.sheldan.abstracto.entertainment.service.management.EconomyUserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.*;

@Component
public class EconomyServiceBean implements EconomyService {

    @Autowired
    private ConfigService configService;

    @Autowired
    private EconomyUserManagementService economyUserManagementService;

    @Autowired
    private SecureRandom secureRandom;

    private static final String CREDIT_GAMBLE_STORAGE = "creditGambleJackpot";

    private static final String SNOWFLAKE = "❄";
    private static final String CHERRY = "🍒";
    private static final String COOKIE = "🍪";
    private static final String TWO = "2️⃣";
    private static final String CLOVER = "🍀";
    private static final String MUSHROOM = "🍄";
    private static final String SUNFLOWER = "🌻";
    private static final String HEART = "❤";
    private static final String SIX = "6️⃣";
    private static final String CYCLONE =  "🌀";
    private static final String OUTCOME_KEY_THREE_CHERRIES = "threecherries";
    private static final String OUTCOME_KEY_NOTHING = "nothing";
    private static final String OUTCOME_KEY_JACKPOT = "jackpot";
    private static final String OUTCOME_KEY_CLOVERS = "clovers";
    private static final String OUTCOME_KEY_TWO_CHERRIES = "twocherries";
    private static final String OUTCOME_KEY_TWOSIX = "twosix";
    private static final String OUTCOME_KEY_3SYMBOLS = "threesymbols";
    private static final String OUTCOME_KEY_2SYMBOLS = "twosymbols";
    private static final List<String> POSSIBLE_SLOTS = Arrays.asList(SNOWFLAKE, CHERRY, COOKIE, TWO, CLOVER, MUSHROOM, SUNFLOWER, HEART, SIX, CYCLONE);
    private static final List<SlotMapping> WINNING_PATTERNS = Arrays.asList(
            new SlotMapping(Arrays.asList(CHERRY, CHERRY, CHERRY), 20, OUTCOME_KEY_THREE_CHERRIES),
            new SlotMapping(Arrays.asList(CLOVER, CLOVER, CLOVER), 25, OUTCOME_KEY_CLOVERS),
            new SlotMapping(Arrays.asList(TWO, TWO, SIX), 50, OUTCOME_KEY_JACKPOT),
            new SlotMapping(Arrays.asList(TWO, SIX), 4, OUTCOME_KEY_TWOSIX),
            new SlotMapping(Arrays.asList(CHERRY, CHERRY), 3, OUTCOME_KEY_TWO_CHERRIES)
    );
    private static final Integer TRIPLE_FACTOR = 10;
    private static final Integer DOUBLE_FACTOR = 2;

    @Override
    public EconomyUser addCredits(AUserInAServer aUserInAServer, Long credits) {
        Optional<EconomyUser> existingUserOptional = economyUserManagementService.getUser(aUserInAServer);
        if (existingUserOptional.isPresent()) {
            EconomyUser existingUser = existingUserOptional.get();
            addCredits(existingUser, credits);
            return existingUser;
        } else {
            EconomyUser user = economyUserManagementService.createUser(aUserInAServer);
            user.setCredits(credits);
            return user;
        }
    }

    @Override
    public void addCredits(EconomyUser economyUser, Long credits) {
        economyUser.setCredits(economyUser.getCredits() + credits);
    }

    @Override
    public void addPayDayCredits(AUserInAServer aUserInAServer) {
        Long creditsToAdd = configService.getLongValueOrConfigDefault(EconomyFeatureConfig.PAYDAY_CREDITS_CONFIG_KEY,
                aUserInAServer.getServerReference().getId());
        addCredits(aUserInAServer, creditsToAdd);
    }

    @Override
    public PayDayResult triggerPayDay(AUserInAServer aUserInAServer) {
        Long creditsToAdd = configService.getLongValueOrConfigDefault(EconomyFeatureConfig.PAYDAY_CREDITS_CONFIG_KEY,
                aUserInAServer.getServerReference().getId());
        EconomyUser economyUser = addCredits(aUserInAServer, creditsToAdd);
        return PayDayResult
                .builder()
                .currentCredits(economyUser.getCredits())
                .gainedCredits(creditsToAdd)
                .build();
    }

    @Override
    public SlotsResult triggerSlots(AUserInAServer aUserInAServer, Long bid) {
        Optional<EconomyUser> userOptional = economyUserManagementService.getUser(aUserInAServer);
        if(!userOptional.isPresent()) {
            throw new NotEnoughCreditsException();
        }
        EconomyUser user = userOptional.get();
        Long oldCredits = user.getCredits();
        if(user.getCredits() < bid) {
            throw new NotEnoughCreditsException();
        }
        SlotGame slotGame = playSlots();
        Integer factor = slotGame.getResultFactor();
        Long creditChange = bid * factor;
        addCredits(user, -bid);
        addCredits(user, creditChange);

        return SlotsResult
                .builder()
                .bid(bid)
                .factor(factor.longValue())
                .newCredits(user.getCredits())
                .outComeKey(slotGame.getOutcome())
                .oldCredits(oldCredits)
                .winnings(creditChange)
                .rows(slotGame.getRows())
                .build();
    }

    @Override
    public SlotGame playSlots() {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            List<String> tempSlots = new ArrayList<>(POSSIBLE_SLOTS);
            Collections.rotate(tempSlots, secureRandom.nextInt(2000) - 1000);
            result.add(tempSlots.get(0));
            result.add(tempSlots.get(1));
            result.add(tempSlots.get(2));
        }
        List<List<String>> rows = new ArrayList<>();
        rows.add(Arrays.asList(result.get(0), result.get(3), result.get(6)));
        List<String> decidingRow = Arrays.asList(result.get(1), result.get(4), result.get(7));
        rows.add(decidingRow);
        rows.add(Arrays.asList(result.get(2), result.get(5), result.get(8)));
        String decidingRowAsString = String.join("", decidingRow);
        SlotMapping specialPattern = getSpecialPattern(decidingRowAsString);
        Integer factor = 0;
        String outcomeKey = OUTCOME_KEY_NOTHING;
        if(specialPattern != null){
            factor = specialPattern.factor;
            outcomeKey = specialPattern.outcome;
        } else {
            Set<String> uniqueChars = new HashSet<>(decidingRow);
            if(uniqueChars.size() == 1) {
                factor = TRIPLE_FACTOR;
                outcomeKey = OUTCOME_KEY_3SYMBOLS;
            } else if(decidingRow.get(0).equals(decidingRow.get(1)) || decidingRow.get(1).equals(decidingRow.get(2)) ) {
                factor =  DOUBLE_FACTOR;
                outcomeKey = OUTCOME_KEY_2SYMBOLS;
            }
        }
        return SlotGame
                .builder()
                .rows(rows)
                .outcome(outcomeKey)
                .resultFactor(factor)
                .build();
    }

    @Override
    public List<CreditsLeaderboardEntry> getCreditLeaderboard(AServer server, Integer page) {
        if(page <= 0) {
            throw new IllegalArgumentException("Page needs to be >= 1");
        }
        page--;
        int pageSize = 10;
        List<CreditsLeaderboardEntry> entries = new ArrayList<>();
        List<EconomyUser> ranks = economyUserManagementService.getRanksInServer(server, page, pageSize);
        int pageOffset = page * pageSize;
        for (int i = 0; i < ranks.size(); i++) {
            EconomyUser rank = ranks.get(i);
            CreditsLeaderboardEntry entry = CreditsLeaderboardEntry
                    .builder()
                    .credits(rank.getCredits())
                    .memberDisplay(MemberDisplay.fromAUserInAServer(rank.getUser()))
                    .rank(pageOffset + i + 1)
                    .build();
            entries.add(entry);
        }
        return entries;
    }

    @Override
    public CreditsLeaderboardEntry getRankOfUser(AUserInAServer aUserInAServer) {
        EconomyLeaderboardResult rank = economyUserManagementService.getRankOfUserInServer(aUserInAServer);
        if(rank != null) {
            return CreditsLeaderboardEntry
                    .builder()
                    .credits(rank.getCredits())
                    .memberDisplay(MemberDisplay.fromAUserInAServer(aUserInAServer))
                    .rank(rank.getRank())
                    .build();
        } else {
            return CreditsLeaderboardEntry
                    .builder()
                    .credits(0L)
                    .memberDisplay(MemberDisplay.fromAUserInAServer(aUserInAServer))
                    .rank(-1)
                    .build();
        }
    }

    @Override
    public void transferCredits(AUserInAServer source, AUserInAServer target, Long amount) {
        Optional<EconomyUser> userOptional = economyUserManagementService.getUser(source);
        if(!userOptional.isPresent()) {
            throw new NotEnoughCreditsException();
        }
        EconomyUser user = userOptional.get();
        if(user.getCredits() < amount) {
            throw new NotEnoughCreditsException();
        }
        addCredits(target, amount);
        addCredits(user, -amount);
    }

    @Override
    public CreditGambleResult triggerCreditGamble(AUserInAServer aUserInAServer) {
        // TODO move these constants to system configs
        Optional<EconomyUser> userOptional = economyUserManagementService.getUser(aUserInAServer);
        if(!userOptional.isPresent()) {
            throw new NotEnoughCreditsException();
        }
        EconomyUser user = userOptional.get();
        Long bid = 25L;
        if(user.getCredits() < bid) {
            throw new NotEnoughCreditsException();
        }
        Long serverId = aUserInAServer.getServerReference().getId();
        Long currentJackpot = configService.getLongValueOrConfigDefault(CREDIT_GAMBLE_STORAGE, serverId);
        List<Integer> diceRoles = new ArrayList<>();
        diceRoles.add(creditGambleDiceResult());
        diceRoles.add(creditGambleDiceResult());
        diceRoles.add(creditGambleDiceResult());
        diceRoles.add(creditGambleDiceResult());

        Long toJackpot = 20L;
        Integer uniqueNumbers = new HashSet<>(diceRoles).size();
        Boolean won = uniqueNumbers == 1;
        CreditGambleResult result = CreditGambleResult
                .builder()
                .uniqueNumbers(uniqueNumbers)
                .won(won)
                .bid(bid)
                .toBank(bid - toJackpot)
                .toJackpot(toJackpot)
                .rolls(diceRoles)
                .currentJackpot(currentJackpot + toJackpot)
                .build();
        if(won) {
            addCredits(user, currentJackpot);
            currentJackpot = 1000L;
        } else {
            currentJackpot += toJackpot;
            addCredits(user, -bid);
        }
        configService.setOrCreateConfigValue(CREDIT_GAMBLE_STORAGE, serverId, currentJackpot.toString());
        return result;
    }

    private Integer creditGambleDiceResult() {
        return secureRandom.nextInt(7) + 1;
    }

    private SlotMapping getSpecialPattern(String row){
        return WINNING_PATTERNS
                .stream()
                .filter(slotMapping -> row.contains(slotMapping.processedMapping))
                .findFirst()
                .orElse(null);
    }

    private static class SlotMapping {
        private Integer factor;
        private String processedMapping;
        private String outcome;

        public SlotMapping(List<String> slots, Integer factor, String outcome) {
            this.factor = factor;
            this.processedMapping = String.join("", slots);
            this.outcome = outcome;
        }
    }



}
