FROM ubuntu as base
MAINTAINER Sheldan
ENV DEBIAN_FRONTEND=noninteractive

ARG liquibase_version=3.8.9
ARG postgres_driver_version=42.2.14
# Install prerequisities for Ansible
RUN apt-get update \
&& apt-get install -y unzip wget \
&& rm -rf /var/lib/apt/lists/

# Install liquibase
RUN mkdir -p /liqiubase \
&& wget -nv https://github.com/liquibase/liquibase/releases/download/v${liquibase_version}/liquibase-${liquibase_version}.zip -O /tmp/liquibase.zip \
&& unzip /tmp/liquibase.zip -d /liquibase

RUN mkdir -p /java \
&& wget -nv https://corretto.aws/downloads/latest/amazon-corretto-8-x64-linux-jdk.tar.gz -O /tmp/java.tar.gz \
&& tar -xf /tmp/java.tar.gz --strip-components=1 -C /java

# Install postgres driver
RUN mkdir -p /postgres \
&& wget -nv https://jdbc.postgresql.org/download/postgresql-${postgres_driver_version}.jar -O /postgres/driver.jar

# Install ansible and required libraries

FROM python:3.7-slim-buster as runtime
RUN pip3 install --no-cache-dir psycopg2-binary SQLAlchemy jinja2
COPY --from=base /liquibase /liquibase
COPY --from=base /postgres /postgres
COPY --from=base /java /java
ENV JAVA_HOME=/java/jre
ADD python/ /python
ADD wrapper/ /
RUN chmod +x /deploy.sh
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.7.3/wait /wait
RUN chmod +x /wait

ENV LIQUIBASE_PATH=/liquibase
ENV POSTGRES_DRIVER=/postgres/driver.jar
CMD /wait && /deploy.sh