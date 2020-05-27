{
  "author": {
    "name": "${memberInfo.user.name}#${memberInfo.user.discriminator}",
    "avatar":  "${memberInfo.user.effectiveAvatarUrl}"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "thumbnail":  "${memberInfo.user.effectiveAvatarUrl}",
  "fields": [
  {
        "name": "<#include "userInfo_response_embed_id_field_title">",
        "value": "${memberInfo.user.id}",
        "inline": "true"
  },
  <#if memberInfo.nickname?has_content>
  {
        "name": "<#include "userInfo_response_embed_nickname_field_title">",
        "value": "${memberInfo.nickname}",
        "inline": "true"
  },
  </#if>
  {
        "name": "<#include "userInfo_response_embed_status_field_title">",
        "value": "${memberInfo.onlineStatus.key}",
        "inline": "true"
  },
  {
        "name": "<#include "userInfo_response_embed_joined_field_title">",
        "value": "${formatDate(memberInfo.timeJoined, "yyyy-MM-dd HH:mm:ss")}",
        "inline": "true"
  },
  {
        "name": "<#include "userInfo_response_embed_registered_field_title">",
        "value": "${formatDate(memberInfo.timeCreated, "yyyy-MM-dd HH:mm:ss")}",
        "inline": "true"
  }
  <#if memberInfo.activities?size gt 0>
  ,
  {
        "name": "<#include "userInfo_response_embed_activity_field_title">",
        "value": "<#list memberInfo.activities as activity>${activity.type}<#sep>, </#list>",
        "inline": "true"
  }
  </#if>
  ]
}