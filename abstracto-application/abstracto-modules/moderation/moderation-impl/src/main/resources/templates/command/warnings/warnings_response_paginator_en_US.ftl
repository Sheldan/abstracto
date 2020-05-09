{
<#assign warnCount>${warnings?size}</#assign>
  "headerText": "<#include "warnings_header_text">",
   "items": [
   <#list warnings as warning>"<#include "warnings_warn_entry">"<#sep>,</#list>
   ]
}