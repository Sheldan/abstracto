
FROM python:3.7-slim-buster as runtime
MAINTAINER Sheldan
ARG sql_alchemy_version=1.4.46
ARG psycopg2_version=2.9.5
RUN pip3 install --no-cache-dir psycopg2-binary==${psycopg2_version} SQLAlchemy==${sql_alchemy_version}
ADD python /python
ADD wrapper /

ENTRYPOINT [ "/bin/sh", "/deploy.sh" ]