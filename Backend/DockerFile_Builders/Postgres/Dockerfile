FROM postgres:latest
COPY ./initDB.sql /docker-entrypoint-initdb.d

ENV POSTGRES_PASSWORD=vm
ENV POSTGRES_DB=tinderdbTesting
EXPOSE 5432