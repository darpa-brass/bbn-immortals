#!/usr/bin/env bash

if [ ! -e "docker/android_docker/setup_resources/android-sdk_r24.4.1-linux.tgz" ];then
  if [ -e "setup_resources/android-sdk_r24.4.1-linux.tgz" ]; then
    cp setup_resources/android-sdk_r24.4.1-linux.tgz docker/android_docker/setup_resources/
  else
    wget http://dl.google.com/android/android-sdk_r24.4.1-linux.tgz --directory-prefix docker/android_docker/setup_resources
  fi
fi

sudo docker build --tag=java_docker:latest docker/java_docker
sudo docker build --tag=java_docker:0.1 docker/java_docker
sudo docker build --tag=android_docker:latest docker/android_docker
sudo docker build --tag=android_docker:0.1 docker/android_docker

if [ -e "android_staticanalysis/bbnAnalysis.tar.gz" ];then
  sudo docker build --tag=android_staticanalysis:latest docker/android_staticanalysis
fi
