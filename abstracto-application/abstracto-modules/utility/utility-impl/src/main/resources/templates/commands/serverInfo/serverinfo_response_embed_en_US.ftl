{
  "title": {
    "title": "<#assign name=guild.name><#include "serverinfo_embed_title">"
  },
  "color" : {
    "r": 200,
    "g": 0,
    "b": 255
  },
  "fields": [
      {
            "name": "<#include "serverinfo_embed_id_field_title">",
            "value": "${guild.id}",
            "inline": "true"
      },
      {
            "name": "<#include "serverinfo_embed_owner_field_title">",
            "value": "${guild.owner.effectiveName}#${guild.owner.user.discriminator}",
            "inline": "true"
      },
      {
            "name": "<#include "serverinfo_embed_members_field_title">",
            "value": "${guild.memberCount}",
            "inline": "true"
      },
      {
            "name": "<#include "serverinfo_embed_role_count_field_title">",
            "value": "${guild.roles?size}",
            "inline": "true"
      },
      {
            "name": "<#include "serverinfo_embed_created_field_title">",
            "value": "${guild.timeCreated}",
            "inline": "true"
      },
      ${safeFieldLength(guild.emotes, 'emote_mention', 'serverinfo_embed_emotes_field_title', 'false')},
      {
            "name": "<#include "serverinfo_embed_features_field_title">",
            "value": "<#list guild.features as feature>${feature}<#else>No features</#list>",
            "inline": "true"
      }
  ]
}