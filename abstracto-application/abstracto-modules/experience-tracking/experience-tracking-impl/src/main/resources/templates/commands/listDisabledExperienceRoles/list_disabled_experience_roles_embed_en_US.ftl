{
  "author": {
    "name": "${member.effectiveName}",
    "avatar":  "${member.user.effectiveAvatarUrl}"
  },
  "title": {
    "title": "<#include "list_disabled_experience_roles_embed_title">"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "description": "
    <#list roles as role>
    ${role.role.asMention}
    <#else>
    <#include "list_disabled_experience_roles_embed_no_roles">
    </#list>
  "
}