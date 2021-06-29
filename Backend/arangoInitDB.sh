#!/bin/bash
/usr/bin/arangosh \
--server.endpoint=tcp://127.0.0.1:8999 \
--server.password ${ARANGO_ROOT_PASSWORD} \
echo "CREATING DB" \
--javascript.execute-string "db._createDatabase('${ARANGO_DB}');" \
echo "DONE CREATING DB" \
--javascript.execute-string "db._useDatabase('${ARANGO_DB}');db._create('${ARANGODB_COL1}');db._create('${ARANGODB_COL2}');db._create('${ARANGODB_COL3}');db._create('${ARANGODB_COL4}');"
echo "DONE CREATING COLLECTIONS"
