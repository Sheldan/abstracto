{
  "author": {
  <#if specifiedUser??>
    "name": "${specifiedUser.member.effectiveName}",
    "avatar": "${specifiedUser.member.user.effectiveAvatarUrl}"
  <#else>
   "name": "${member.effectiveName}",
   "avatar": "${member.user.effectiveAvatarUrl}"
  </#if>
  },
  "title": {
  <#if specifiedUser??>
  <#assign user>${specifiedUser.member.effectiveName}</#assign>
   "title": "<#include "user_notes_embed_user_title">"
  <#else>
   "title": "<#include "user_notes_embed_title">"
  </#if>
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "<#list userNotes as note>
  <#assign user>${note.fullUser.member.asMention}</#assign>
  <#assign noteText>${note.note.note}</#assign>
  <#assign noteId>${note.note.id}</#assign>
  <#assign date>${formatDate(note.note.created, "yyyy-MM-dd HH:mm:ss")}</#assign>
  <#include "user_notes_note_entry"><#else><#include "user_notes_no_notes">
  </#list>"
}