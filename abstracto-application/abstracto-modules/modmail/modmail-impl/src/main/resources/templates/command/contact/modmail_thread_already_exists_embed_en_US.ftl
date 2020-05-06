{
  "author": {
    "name": "${member.effectiveName}",
    "avatar":  "${member.user.effectiveAvatarUrl}"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "<#include "modmail_thread_already_exists">",
  "fields": [
     {
        "name": "<#include "modmail_existing_thread_link_field_title">",
        "value": "[<#include "modmail_existing_thread_link_content_display_text">](${threadUrl})"
     }
  ]
}
