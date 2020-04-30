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
  "description": "<#include "remind_reminder_description">",
  "fields": [
    {
        "name": "<#include "remind_reminder_duration_field_title">",
        "value": "${fmtDuration(duration)}"
    },
    {
        "name": "<#include "remind_reminder_note_field_title">",
        "value": "${reminder.text}"
    },
    {
        "name": "<#include "remind_reminder_link_field_title">",
        "value": "[<#include "remind_reminder_link_content_display_text">](${messageUrl})"
    }
  ],
  "additionalMessage": "${member.asMention}"
}