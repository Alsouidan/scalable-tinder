FROM postgres:latest
COPY ./finalInitDB.sql /docker-entrypoint-initdb.d

ENV POSTGRES_PASSWORD=vm
ENV POSTGRES_DB=tinderDB
EXPOSE 5432