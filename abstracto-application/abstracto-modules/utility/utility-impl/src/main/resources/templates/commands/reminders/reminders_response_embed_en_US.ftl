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
   "title": {
      "title": "<#include "reminders_reminders_embed_title">"
    },
   "fields": [
       <#list reminders as reminder>
           {
           <#assign id>${reminder.id}</#assign>
            "name": "<#include "reminders_reminder_field_title">",
            "value": "<#include "reminders_due_on">"
           }
       <#sep>,
       </#list>
   ]
}