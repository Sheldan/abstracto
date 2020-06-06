{
  "author": {
    <#if suggester?has_content>
    "name": "${suggester.effectiveName}",
    "avatar":  "${suggester.user.effectiveAvatarUrl}"
    <#else>
    "name": "${suggesterUser.userReference.id}"
    </#if>
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  <#assign user>${member.effectiveName}</#assign>
  <#assign id>${suggestion.id}</#assign>
  <#if suggestion.state = "ACCEPTED">
    "description": "~~${text}~~ \n✅ ${reason} - <#include "suggest_accepted_by">",
  <#elseif suggestion.state = "REJECTED">
    "description": "~~${text}~~ \n❌ ${reason} - <#include "suggest_rejected_by">",
  <#else>
    "description": "${text}",
  </#if>
  <#if suggestion.state = "ACCEPTED" || suggestion.state = "REJECTED">
  "fields": [
    {
        "name": "<#include "suggest_link_field_title">",
        "value": "[<#include "suggest_link_display_value">](${originalMessageUrl})"
    }
  ],
  </#if>
  "footer": {
    "text": "<#include "suggest_suggestion_id_footer">"
  }
}