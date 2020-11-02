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
   python3 python/main.py $DEPLOY_TEMPLATES $DEPLOY_LIQUIBASE
fi

echo "Finished deployment"
