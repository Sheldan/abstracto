FROM python:3.10.13-alpine3.18
RUN apk --no-cache add msttcorefonts-installer fontconfig && \
    update-ms-fonts && \
    fc-cache -f
ADD wrapper /
ADD python/requirements.txt requirements.txt
RUN pip install -r requirements.txt
ADD python /python
CMD ["/run.sh"]