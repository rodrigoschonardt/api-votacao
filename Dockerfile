FROM ubuntu:latest
LABEL authors="rodrigoschonardt"

ENTRYPOINT ["top", "-b"]