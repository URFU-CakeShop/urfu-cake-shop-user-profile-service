FROM ubuntu:latest
LABEL authors="titun"

ENTRYPOINT ["top", "-b"]