#!/bin/sh

DEPLOY_LIQUIBASE=no
DEPLOY_TEMPLATES=no

echo "Starting deployment."

if [ "x$EXECUTE_LIQUIBASE" = 'xtrue' ]; then
  DEPLOY_LIQUIBASE=yes
fi

if [ "x$EXECUTE_TEMPLATES" = 'xtrue' ]; then
  DEPLOY_TEMPLATES=yes
fi
exit_code = 0
if [ "x$EXECUTE_DEPLOYMENT" = 'xtrue' ]; then
   python3 -u python/main.py $DEPLOY_TEMPLATES $DEPLOY_LIQUIBASE
   exit_code=$?
fi

echo "Finished deployment."
exit $exit_code