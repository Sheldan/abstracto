#!/bin/sh

DEPLOY_LIQUIBASE=no
DEPLOY_TEMPLATES=no

if [ "x$EXECUTE_LIQUIBASE" = 'xtrue' ]; then
  DEPLOY_LIQUIBASE=yes
fi

if [ "x$EXECUTE_TEMPLATES" = 'xtrue' ]; then
  DEPLOY_TEMPLATES=yes
fi

if [ "x$EXECUTE_DEPLOYMENT" = 'xtrue' ]; then
   ansible-playbook playbook.yaml -e "execute_liquibase_input=${DEPLOY_LIQUIBASE}" -e "execute_templates_input=${DEPLOY_TEMPLATES}";
fi

