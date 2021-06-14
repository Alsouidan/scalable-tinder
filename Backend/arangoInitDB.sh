#!/bin/bash
/usr/bin/arangosh \
--server.endpoint=unix:///tmp/arangodb-tmp.sock \
--server.password ${ARANGO_ROOT_PASSWORD} \
--javascript.execute-string "db._createDatabase(${ARANGO_DB}, [{username: ${ARANGO_DB_USER}, password: ${ARANGO_DB_PASSWORD}}]);"
echo "DONE CREATING DB"
--javascript.execute-string "db._useDatabase(${ARANGO_DB});db._create(${ARANGODB_COL1});db._create(${ARANGODB_COL2});db._create(${ARANGODB_COL3});db._create(${ARANGODB_COL4});"
echo "DONE CREATING COLLECTIONS"
