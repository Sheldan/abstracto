{
  "author": {
    "name": "${member.effectiveName}",
    "avatar":  "${member.user.effectiveAvatarUrl}"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  }
  <#assign userMention>${member.effectiveName}</#assign>
  <#if serverUsesDecays>
  ,"description" :"<#include "myWarnings_with_decay_embed_description">"
  <#else>
  ,"description" :"<#include "myWarnings_embed_description">"
  </#if>
}