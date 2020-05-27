{
  "title": {
      "title": "<#include "modmail_closing_notification_title">"
    },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  <#assign messageCount>${closedThread.messages?size}</#assign>
  <#assign user>user</#assign>
  <#assign startDate>${formatDate(closedThread.created,"yyyy-MM-dd HH:mm:ss")}</#assign>
  <#assign duration>${fmtDuration(duration)}</#assign>
  "description": "<#include "close_closing_description">"
}
