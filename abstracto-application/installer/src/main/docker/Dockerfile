FROM ubuntu as base
MAINTAINER Sheldan
ENV DEBIAN_FRONTEND=noninteractive

ARG maven_version=3.6.3
ARG liquibase_version=3.8.9
ARG postgres_driver_version=42.2.14
# Install prerequisities for Ansible
RUN apt-get update \
&& apt-get install -y unzip wget \
&& rm -rf /var/lib/apt/lists/

# Install liquibase
RUN mkdir -p /liqiubase \
&& wget https://github.com/liquibase/liquibase/releases/download/v${liquibase_version}/liquibase-${liquibase_version}.zip -O /tmp/liquibase.zip \
&& unzip /tmp/liquibase.zip -d /liquibase

RUN mkdir -p /java \
&& wget https://corretto.aws/downloads/latest/amazon-corretto-8-x64-linux-jdk.tar.gz -O /tmp/java.tar.gz \
&& tar -xf /tmp/java.tar.gz --strip-components=1 -C /java

# Install postgres driver
RUN mkdir -p /postgres \
&& wget https://jdbc.postgresql.org/download/postgresql-${postgres_driver_version}.jar -O /postgres/driver.jar

# Install ansible and required libraries

FROM python:3.7-slim-buster as runtime
RUN pip3 install --no-cache-dir ansible psycopg2-binary SQLAlchemy lxml
RUN apt-get update && apt-get install unzip && rm -rf /var/lib/apt/lists/
COPY --from=base /liquibase /liquibase
COPY --from=base /postgres /postgres
COPY --from=base /java /java
ENV JAVA_HOME=/java/jre
ADD resources/ /
RUN chmod +x /deploy.sh
ENTRYPOINT ["/deploy.sh"]