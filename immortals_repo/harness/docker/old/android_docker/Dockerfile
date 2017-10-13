FROM java_docker

# The android sdk and build tools version to use
ENV ANDROID_SDK_VERSION r24.4.1
ENV ANDROID_BUILD_TOOLS_VERSION 23.0.3

# The android sdk components to install
ENV ANDROID_SDK_INSTALLS build-tools-21.1.2,android-21,android-23,extra-android-m2repository,extra-android-support,extra-google-google_play_services,extra-google-m2repository,sys-img-x86_64-android-21,sys-img-x86_64-android-23

## Installs Android SDK
# THe sdk filename value
ENV ANDROID_SDK_FILENAME android-sdk_${ANDROID_SDK_VERSION}-linux.tgz

# THe sdk download location
ENV ANDROID_SDK_URL http://dl.google.com/android/${ANDROID_SDK_FILENAME}
ENV ANDROID_HOME /opt/android-sdk-linux/
ENV PATH ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools

ADD setup_resources/${ANDROID_SDK_FILENAME} /opt/

# Installs i386 architecture required for running 32 bit Android tools
RUN dpkg --add-architecture i386 && \
    apt-get update -y && \
    apt-get install -y libc6:i386 libncurses5:i386 libstdc++6:i386 lib32z1 unzip

RUN cd /opt
RUN if [ ! -e ${ANDROID_HOME} ];then wget ${ANDROID_SDK_URL};fi
RUN if [ ! -e ${ANDROID_HOME} ];then tar -xvzf ${ANDROID_SDK_FILENAME};fi
RUN if [ ! -e ${ANDROID_HOME} ];then rm ${ANDROID_SDK_FILENAME};fi
RUN echo y | android update sdk --no-ui -a --filter platform-tools
# On Docker the android emulator has issues updating the tools the normal way
RUN echo y | android update sdk --no-ui -a --filter tools
RUN apt-get install unzip
RUN cd ${ANDROID_HOME};unzip temp/tools_*-linux.zip
RUN cd ${ANDROID_HOME};rm temp/tools_*-linux.zip

ENV SHELL /bin/bash

RUN apt-get install -y libpulse0 libx11-6:i386 libx11-6 libgl1-mesa-glx
