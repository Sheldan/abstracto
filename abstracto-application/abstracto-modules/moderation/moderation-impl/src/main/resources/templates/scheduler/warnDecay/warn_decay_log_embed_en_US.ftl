{
  "title": {
    "title": "<#include "warnDecay_title">"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "<#list warnings as warning>
        <#if warning.warnedMember??><#assign warnedUser>${warning.warnedMember.asMention} (${warning.warnedMember.id})</#assign><#else><#assign warnedUser> ${warning.warning.warnedUser.userReference.id?c}</#assign></#if> <#if warning.warningMember??><#assign warningUser> ${warning.warningMember.asMention} (${warning.warningMember.id})</#assign><#else><#assign warningUser>${warning.warning.warningUser.userReference.id?c}</#assign></#if>   <#include "warnDecay_log_warn_entry">
  <#else>
  <#include "warnDecay_log_no_warnings">
  </#list>"
}