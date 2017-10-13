FROM ubuntu:16.04

ENV SHELL /bin/bash

# Installs Oracle jdk 8 from a common ppa
RUN apt-get update
RUN apt-get -y install software-properties-common vim tmux screen \
    build-essential ant subversion ant-optional cmake ntp ccache \
    git git-arch git-completion git-core git-svn git-doc \
    openjdk-8-jdk-headless \
    python python-dev python-setuptools python-httplib2
