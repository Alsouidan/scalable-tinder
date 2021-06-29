FROM arangodb
COPY arangoInitDB.sh /docker-entrypoint-initdb.d

ENV ARANGO_ROOT_PASSWORD=rootpassword
ENV ARANGO_DB=tinderDB
ENV ARANGODB_USERNAME=root
ENV ARANGODB_PASSWORD=rootpassword
ENV ARANGODB_COL1=users
ENV ARANGODB_COL2=chats
ENV ARANGODB_COL3=profileViews
ENV ARANGODB_COL4=notifications
EXPOSE 8529
