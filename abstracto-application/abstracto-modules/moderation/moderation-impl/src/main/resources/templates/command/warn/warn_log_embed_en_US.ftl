{
  "author": {
    "name": "${warnedUser.effectiveName}",
    "avatar":  "${warnedUser.user.effectiveAvatarUrl}"
  },
  "title": {
    "title": "<#include "warn_log_title">"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "fields": [
    {
      "name": "<#include "warn_log_warned_user_field_title">",
      "value": "${warnedUser.effectiveName} ${warnedUser.asMention} (${warnedUser.idLong?c})"
    },
    <#if warningUser?has_content>
    {
        "name": "<#include "warn_log_warning_user_field_title">",
        "value": "${warningUser.effectiveName} ${warningUser.asMention} (${warningUser.idLong?c})"
    },
    </#if>
     <#if warning?has_content>
    {
        "name": "<#include "warn_log_warn_location_field_title">",
        "value": "[${messageChannel.name}](${message.jumpUrl})"
    },
    </#if>
    {
        "name": "<#include "warn_log_warn_reason_field_title">",
        "value": "${reason}"
    }
  ],
  "footer": {
    <#if warningUser?has_content>
    "text": "<#include "warn_log_warn_id_footer"> #${warning.id}"
    </#if>
  }
}