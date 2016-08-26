FROM ubuntu:14.04.4

ENV SHELL /bin/bash

# Installs Oracle jdk 8 from a common ppa
RUN apt-get update && \
    apt-get -y install software-properties-common && \
    add-apt-repository ppa:webupd8team/java && \
    apt-get update && \
    echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections && \
    apt-get install -y oracle-java8-installer
ENV JAVA_HOME=/usr/lib/jvm/java-8-oracle
ENV PATH=${JAVA_HOME}/bin:${PATH}
