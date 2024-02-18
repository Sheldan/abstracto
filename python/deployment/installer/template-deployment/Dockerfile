
FROM python:3.12.2-slim-bullseye as runtime
MAINTAINER Sheldan
RUN apt-get update && apt-get install -y \
    libpq-dev \
    gcc \
    g++ \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
ARG sql_alchemy_version=2.0.27
ARG psycopg2_version=2.9.9
RUN pip3 install --no-cache-dir psycopg2-binary==${psycopg2_version} SQLAlchemy==${sql_alchemy_version}
ADD python /python
ADD wrapper /

ENTRYPOINT [ "/bin/sh", "/deploy.sh" ]