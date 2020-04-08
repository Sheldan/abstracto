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
      "title": "Currently active reminders"
    },
   "fields": [
       <#list reminders as reminder>
           {
            "name": "Reminder ${reminder.id}",
            "value": "Due on ${reminder.targetDate} with text ${reminder.text}"
           }
       <#sep>,
       </#list>
   ]
}