#!/usr/bin/env bash
if [ "`whoami`" != "root" ];then
    echo This script must be run as root! Exiting...
    exit
fi

if [ ! -e "setup_resources" ];then
    mkdir "setup_resources"
fi;

# Blacklist framebuffer for qemu... What's the point of blocking a curses
# display with a screen that says it's in 1024x768 display mode!?!?
echo 'blacklist cirrus' >> /etc/modprobe.d/blacklist-cirrus.conf
update-initramfs -u -k all

echo "immortals" > /etc/hostname
echo "127.0.0.1 immortals" >> /etc/hosts

ANDROID_BUILD_TOOLS_VERSION="21.1.2"

# The android sdk components to install
ANDROID_SDK_INSTALLS="tools,platform-tools,build-tools-21.1.2,android-21,android-23,extra-android-m2repository,extra-android-support,extra-google-google_play_services,extra-google-m2repository"
ANDROID_EMULATOR_INSTALLS="sys-img-x86_64-android-21,sys-img-x86_64-android-23"

INSTALLATION_DIR=/opt

ANDROID_SDK_VERSION=r24.4.1
ANDROID_SDK_INSTALLER=android-sdk_${ANDROID_SDK_VERSION}-linux.tgz
ANDROID_HOME=${INSTALLATION_DIR}/android-sdk-linux/
ANDROID_SDK_URL=https://dl.google.com/android/${ANDROID_SDK_INSTALLER}

GRADLE_VERSION=2.8
GRADLE_INSTALLER=gradle-${GRADLE_VERSION}-bin.zip
GRADLE_HOME=${INSTALLATION_DIR}/gradle-${GRADLE_VERSION}/
GRADLE_INSTALLER_URL=https://services.gradle.org/distributions/${GRADLE_INSTALLER}

FUSEKI_VERSION=2.3.1
FUSEKI_INSTALLER=apache-jena-fuseki-${FUSEKI_VERSION}.tar.gz
FUSEKI_HOME=/opt/fuseki/apache-jena-fuseki-${FUSEKI_VERSION}/
FUSEKI_INSTALLER_URL=http://archive.apache.org/dist/jena/binaries/${FUSEKI_INSTALLER}

Z3_VERSION=4.4.1
Z3_INSTALLER=z3-${Z3_VERSION}-x64-ubuntu-14.04.zip
Z3_HOME=/opt/z3-${Z3_VERSION}-x64-ubuntu-14.04/
Z3_INSTALLER_URL=https://github.com/Z3Prover/z3/releases/download/z3-${Z3_VERSION}/${Z3_INSTALLER}

mkdir /opt/bin
echo 'export PATH=/opt/bin:${PATH}' >> ~/.bashrc
source ~/.bashrc

apt-get update
apt-get install apt-transport-https ca-certificates -y



######## Install Docker ########
apt-key adv --keyserver hkp://p80.pool.sks-keyservers.net:80 --recv-keys 58118E89F3A912897C070ADBF76221572C52609D
echo "deb https://apt.dockerproject.org/repo ubuntu-trusty main" > /etc/apt/sources.list.d/docker.list
apt-get update
apt-get install linux-image-extra-$(uname -r) linux-image-extra-virtual -y
apt-get install docker-engine -y



######## Install Java ########
apt-get update
apt-get -y install software-properties-common
add-apt-repository -y ppa:webupd8team/java
apt-get update

echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | sudo /usr/bin/debconf-set-selections
apt-get install -y oracle-java8-installer

echo 'export JAVA_HOME=/usr/lib/jvm/java-8-oracle' >> ~/.bashrc
source ~/.bashrc



#Install the Android SDK
dpkg --add-architecture i386
apt-get update -y
apt-get install -y libc6:i386 libncurses5:i386 libstdc++6:i386 lib32z1 libpulse0 libx11-6:i386 libx11-6 libgl1-mesa-glx

# If the android sdk tarball hasn't been downloaded manually, download it.
# Manual download via the website is _HIGHLY_ recommended. It appears they 
# Throttle it significantly without the right header information, and simply
# specifying a proper browser user agent doesn't seem to be enough...`
if [ ! -e "setup_resources/${ANDROID_SDK_INSTALLER}" ];then
    wget "$ANDROID_SDK_URL" --directory-prefix setup_resources
fi

tar xvzf "setup_resources/${ANDROID_SDK_INSTALLER}" -C /opt/

echo 'export ANDROID_HOME=${ANDROID_HOME}' >> ~/.bashrc
source ~/.bashrc

echo y | ${ANDROID_HOME}tools/android update sdk --no-ui -a --filter ${ANDROID_SDK_INSTALLS}

if [ $ANDROID_EMULATOR_INSTALLS ];then
    echo y | ${ANDROID_HOME}tools/android update sdk --no-ui -a --filter ${ANDROID_EMULATOR_INSTALLS}
fi

ln -s ${ANDROID_HOME}tools/android /opt/bin/android
ln -s ${ANDROID_HOME}tools/ddms /opt/bin/ddms
ln -s ${ANDROID_HOME}tools/emulator /opt/bin/emulator
ln -s ${ANDROID_HOME}tools/emulator64-arm /opt/bin/emulator64-arm
ln -s ${ANDROID_HOME}tools/emulator64-x86 /opt/bin/emulator64-x86
ln -s ${ANDROID_HOME}tools/mksdcard /opt/bin/mksdcard
ln -s ${ANDROID_HOME}tools/monitor /opt/bin/monitor
ln -s ${ANDROID_HOME}platform-tools/adb /opt/bin/adb



######## Install Gradle ########
apt-get update -y
apt-get install -y unzip
if [ ! -e "setup_resources/${GRADLE_INSTALLER}" ];then
    wget "$GRADLE_INSTALLER_URL" --directory-prefix setup_resources
fi
unzip "setup_resources/${GRADLE_INSTALLER}" -d /opt

echo "export GRADLE_HOME=${GRADLE_HOME}" >> ~/.bashrc
ln -s ${GRADLE_HOME}/bin/gradle /opt/bin/gradle



######## Install Fuseki ########
if [ ! -e "setup_resources/${FUSEKI_INSTALLER}" ];then
    wget "$FUSEKI_INSTALLER_URL" --directory-prefix setup_resources
fi
tar xvzf "setup_resources/${FUSEKI_INSTALLER}" -C /opt/

echo "export FUSEKI_HOME=${FUSEKI_HOME}" >> ~/.bashrc
source ~/.bashrc



######## Install Haskell ########
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 575159689BEFB442
echo 'deb http://download.fpcomplete.com/ubuntu trusty main'|sudo tee /etc/apt/sources.list.d/fpco.list
apt-get update
apt-get install stack -y



######## Install Z3 ########
apt-get install unzip -y
if [ ! -e "setup_resources/${Z3_INSTALLER}" ];then
    wget "$Z3_INSTALLER_URL" --directory-prefix setup_resources
fi

unzip "setup_resources/${Z3_INSTALLER}" -d /opt/
ln -s ${Z3_HOME}/bin/z3 /opt/bin/z3



######## Install Maven ########
apt-get install maven -y



######## Perform the initial build to populate the dependency tree
cd ../
${GRADLE_HOME}/bin/gradle
${GRADLE_HOME}/bin/gradle dslSetup
${GRADLE_HOME}/bin/gradle buildAll
