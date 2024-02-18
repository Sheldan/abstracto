FROM python:3.12.2-alpine3.19
RUN apk --no-cache add msttcorefonts-installer fontconfig && \
    update-ms-fonts && \
    fc-cache -f
ADD wrapper /
ADD python/requirements.txt requirements.txt
RUN pip install -r requirements.txt
ADD python /python
CMD ["/run.sh"]