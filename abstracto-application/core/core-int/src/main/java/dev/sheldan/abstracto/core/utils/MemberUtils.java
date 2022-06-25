package dev.sheldan.abstracto.core.utils;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;

public class MemberUtils {
    public static String getAUserInAServerAsMention(AUserInAServer aUserInAServer) {
        return getUserAsMention(aUserInAServer.getUserReference().getId());
    }

    public static String getUserAsMention(Long userId) {
        return "<@" + userId + ">";
    }
}
