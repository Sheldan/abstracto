FROM ubuntu as base
MAINTAINER Sheldan
ENV DEBIAN_FRONTEND=noninteractive

ARG liquibase_version=4.26.0
ARG postgres_driver_version=42.7.1
# Install prerequisities for Ansible
RUN apt-get update \
&& apt-get install -y unzip wget \
&& rm -rf /var/lib/apt/lists/

# Install liquibase
RUN mkdir -p /liqiubase \
&& wget -nv https://github.com/liquibase/liquibase/releases/download/v${liquibase_version}/liquibase-${liquibase_version}.zip -O /tmp/liquibase.zip \
&& unzip /tmp/liquibase.zip -d /liquibase \
# Split copying to workaround upload limits (increases amount of layers)
&& mv /liquibase/internal/lib/snowflake-jdbc.jar /liquibase-internal-lib-snowflake-jdbc.jar \
&& mv /liquibase/internal/lib /liquibase-internal-lib

RUN mkdir -p /java \
&& wget -nv https://corretto.aws/downloads/latest/amazon-corretto-8-x64-linux-jdk.tar.gz -O /tmp/java.tar.gz \
&& tar -xf /tmp/java.tar.gz --strip-components=1 -C /java \
&& mv /java/jre /java-jre \
&& mv /java/src.zip /java-src.zip

# Install postgres driver
RUN mkdir -p /postgres \
&& wget -nv https://jdbc.postgresql.org/download/postgresql-${postgres_driver_version}.jar -O /postgres/driver.jar

# Install ansible and required libraries

FROM python:3.12.2-slim-bullseye as runtime
ARG sql_alchemy_version=2.0.27
ARG jinja_version=3.1.3
ARG psycopg2_version=2.9.9
# required for psycopg2
RUN apt-get update && apt-get install -y \
    libpq-dev \
    gcc \
    g++ \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
RUN pip3 install --no-cache-dir psycopg2-binary==${psycopg2_version} SQLAlchemy==${sql_alchemy_version} jinja2==${jinja_version}
COPY --from=base /liquibase /liquibase
COPY --from=base /liquibase-internal-lib /liquibase/internal/lib
COPY --from=base /liquibase-internal-lib-snowflake-jdbc.jar /liquibase/internal/lib/snowflake-jdbc.jar
COPY --from=base /postgres /postgres
COPY --from=base /java /java
COPY --from=base /java-jre /java/jre
COPY --from=base /java-src.zip /java/src.zip
ENV JAVA_HOME=/java/jre
ADD python /python
ADD wrapper /

ENV ROOT_PATH=/liquibase
ENV POSTGRES_DRIVER=/postgres/driver.jar
ENTRYPOINT [ "/bin/sh", "/deploy.sh" ]