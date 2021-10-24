package dev.sheldan.abstracto.core.utils;

import dev.sheldan.abstracto.core.models.database.AUserInAServer;

public class MemberUtils {
    public static String getAUserInAServerAsMention(AUserInAServer aUserInAServer) {
        return "<@" + aUserInAServer.getUserReference().getId() + ">";
    }
}
