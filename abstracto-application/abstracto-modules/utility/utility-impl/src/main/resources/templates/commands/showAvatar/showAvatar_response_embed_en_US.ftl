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
  "imageUrl":  "${memberInfo.user.effectiveAvatarUrl}",
  "fields": [
  {
        "name": "<#include "showAvatar_response_embed_user_field_title">",
        "value": "${memberInfo.user.name}#${memberInfo.user.discriminator}",
        "inline": "true"
  },
  {
        "name": "<#include "showAvatar_response_embed_image_field_title">",
        "value": "[<#include "showAvatar_response_embed_image_field_value_display">](${memberInfo.user.effectiveAvatarUrl})",
        "inline": "true"
  }
  ]
}