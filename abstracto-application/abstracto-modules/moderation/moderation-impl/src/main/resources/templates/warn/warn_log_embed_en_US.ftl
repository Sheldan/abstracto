{
  "author": {
    "name": "${warnedUser.effectiveName}",
    "avatar":  "${warnedUser.user.effectiveAvatarUrl}"
  },
  "title": {
    "title": "User has been warned"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "fields": [
    {
      "name": "Warned User",
      "value": "${warnedUser.effectiveName} ${warnedUser.asMention} (${warnedUser.idLong?c})"
    },
    <#if warningUser?has_content>
    {
        "name": "Warned by",
        "value": "${warningUser.effectiveName} ${warningUser.asMention} (${warningUser.idLong?c})"
    },
    </#if>
     <#if warning?has_content>
    {
        "name": "Location of the incident",
        "value": "[${textChannel.name}](${message.jumpUrl})"
    },
    </#if>
    {
        "name": "Reason",
        "value": "${reason}"
    }
  ],
  "footer": {
    <#if warningUser?has_content>
    "text": "Warning #${warning.id}"
    </#if>
  }
}