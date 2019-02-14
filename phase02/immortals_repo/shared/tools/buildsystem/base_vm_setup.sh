#!/usr/bin/env bash

if [ $# != 1 ] || [ "$1" != "das" -a "$1" != "android" ];then
echo Please provide 'das' or 'android' to indicate which environment you would like to perform a base configuration of.
exit 1
fi



## Install necessary packages
sudo apt-get update
sudo apt-get -y upgrade

# Allow routing external ports to internal ports. For some reason bring up the VMs results in either ens4 or ens6 as the interface, so adding dhcp and routing overrides for both...
#echo "auto ens4
#iface ens4 inet dhcp" | sudo tee -a /etc/network/interfaces.d/ens4.cfg
#
#echo "auto ens6
#iface ens6 inet dhcp" | sudo tee -a /etc/network/interfaces.d/ens6.cfg
#
#sudo /etc/init.d/networking restart


# Setup SSH Credentials
if [ ! -f "${HOME}/.ssh/id_rsa" ];then
    ssh-keygen -f ${HOME}/.ssh/id_rsa -N ''
fi

echo "
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCqbqxCgaSt7CEBJUjoMrvVxFhXWmu8sXtKPER6/yu2YOJTXKxFvSmNdoLb7zp+vV3mxBAXOFw1ZrA/7OIWO4L99M8p896vfqXKYYKuDFBAElWe3GvZ/EW+uFUS+3AtorMCY9utXTXIvHk6AAKvhVYgv+FqzOeSBDdgZ6/a2Oe2NWaxOka+Dt5jmveFGgw2B5fAMuay0z9qHcDKWM0HtSe6Iis9kmwThdpcnVWJoT0uJV7t/Mbpt7IvPI/jp9gc6Jt22X6fXJbrJ8t7UI1fc8kiHjKq8+HSm5WR9Q2fu5p22dbh1p9L1snMnDBNg/j1kzq3+t3f3ax6TNBu4ZXlIknX awellman@dhcp89-77-152.bbn.com
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDlf3obRqfc84UwEHMn6eTEIw7Ew3AWvVR6w//jPef/xDTpdpYt65J3bCR/VRTqhajNKl1EShIYStYq86sSfLA2kEd4srsBebWa2FuWqXPErGPMzKTYkX2UH9lMCdt49xvO04MlqMJH4uuB3kPlybgSh3oYTCBuWfH57vgIUY9likp06cPNHNSUsNjytBdfL2MUTCYyCHffrqBkU2i0kbl09rfHdvMBqb9147em2mXECV5O0nf+Ty284bYZMrxyUxBqKbzmzX22m6MiSXawrKhSxjP+A2W8FyxX2PBbJnOKox/BuE09bU2RgXVMQX9zzj/IPSmLt7F7ODpyEreWAaEj buildbot
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC8Kd54pe7M+Gx6lZFUgsaxSLUIafu9fM2bJvFE9tAuxXRkRtF5+XAcCfmQ5DiwX7VV0f02dlCvgWgfA2SSNU7yps1fWoNsPFwBoxjZ1t7V5sZV6CEgjMiVavRE25k5E99rR4loGEQjfhgVCYtW/5/6VN8r2EAqvIBNQ72NhjobAWx8zoxnvrCwVk+obWrcksPmJQ2T4eBWUYXbaj8GO8dTAGleovLb/ysFLcvImCVfNTxDkz/JFcIv3Jd8ZSPqQ8XOXJj71hCMViJUsC0X6C76AAmoKKUvWfgxWIIIZjNH8w3UAMuFOCzVDY0HXSYILHpvPhTk0i4QUCrg3S23prv9
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDD0OER8wrVlmK2gCr+sICqwjSOJDIyBYvTMcUrx/zbgxdBl20BOtV+JOl29tylVlJ4TIUhQ4iOd0V2feyEXE16CMacaopPvD4f81puM1BfdPdEKEOjmy8mpBknUU7YTqCeSObOyJH53cSEbsnnJ4/ZFJ9xYvngLjAzdG7IJk/SDcDpb8eeMRPrJ5gv8lMGBP+/Rjf/bL+l09CkS6pNbFCxf/QBhtMc/iToFmXUI6c3JoFED9cLnb1OS4PU6DvpZCowaC+vTXXSVvI+/yziCBF8esjaxl1XJJpCTwkuABoWbxImdloffyP16gCeZKoJWpHBD3+fXo7nnXzHUbY03CuH
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC/ldeIVxQYMBDB7a0ZaOHWEudQHMbT78HcxaCc85pJyxJh3+5KHwu+SYj58OPX7Q6FZ/L2pbkK27id3ssIjXlmP3+qmneXr3X63N+ApJQJig7YIcQFgQvvIcYDxbkvvb7bvYOT8NnWbP9b+VSXA3rgy9+FFRSiU3XxmK+eQU4AXfXecIwgmyL/YUuQtmLXYxb+DuZcw981EGCbrn8KELtGKAmBRuFnMouhqcJTnFy7V9z2rFMc6i1sJQIK4m2nAnGRqhgdu3YaWH2nZuiWAfc7YCdtLsXJ48OwU5cA1M8lB+wO8pEioLqG+VbZdmFZ7kecahHkkCus/A3/YYqEryRN
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC6uEsG0TUbMr0zDTjDDarct4klBGS/IVib2vK0+RDjKF5EYI7i+HZyJHYQ++wmxf9ScuSpIl71xmyVH5n7IwNJdQwcU24tVLR2qdItImDZa1qM/FmjQPCFrKNfnlCqpU7dbZfb/glrbJVIoV9zc2l36a75CfEv6woHYytJvxe88UC5U23pEYylv+heXFQHgLl61eLR7YWzIH4yKZTD9hn+0M7RV6xCCbhxbbLpD2G9unC5WVfGoF6X2ixeTDwUwTYBWVyfq+djv4qQJ21lmxQ6p3CwQ6FFA93/sKA5Byh3uBSpAqyuxyGZsNeAOZm5f/7T79SSFjQTyWzPgERz1neT
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDlfJJrBG/Ud8qriW2TKWK166GMg02WL5TRelcfx9/y3udQ1QITFo10iyzyr5lcVdgzk0g2A/tZd/C8EExzePgVQbbuA6J3MO2NShhm5I95zI8BrTOQ+RgSxF85vCyybXMl+KETgfyu3a3l3SZuspFVjZ925blpvxjKe4a9Wn1ZUleQEgDbDw6ZJ8rfVn30GG+B0RM8s9IHDLyu5bAiTcb3ZTwDF8zII0aC/56q47sNGOHuPzTVuoQnLRU47d78gTVr6tTo0UsyhcccZoI+ofpq2Z9PhduEDrGI9yuTCcVeKiy25Rf51NWTIkVeCBGHrQ+nWkmC6gSJYfel7NybM3kd
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCYGmVSLyDVQ5BnQu1YCZ66I9VM4MUC/ICh+cZExMZTlrTCGmxWRi3teAmofgzTAfK4LJOWkeqM+CUK/JvsK9DWqpfo/NGeuGXEP2fQLwtK7FOUC8WbwIlI42p6vXZzGsYgBldKouZvaV33E+n5sRy4A5QVtG708L8EL5JBacz7KlI6cEPNqJWhuEvifsAIrg4P/d4SezlDPx5oYvwsGuetIqpFOw2ABnVf/u2hbpOIOyeN8QbRyY8iAGnT6r32Z9mvjpuT2x7vcUtdPwh+DZmUaCuPWGAhXADTAKglYEMleH8ExX9QGG6fJleVgMwtCOIyL3hHZBgGK3V4cs6sqJ8L
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDAo+0FeXLqt2dYJ/c6e/lIbk0fx0FrD7JpR0zXh9hjZzKAPCy1XwcSqpbxT4Gb0IggjdqfnpfeL4wwfoyyVrUGcHM+0EYNQ+hilWVG2VqsORzI5EX5E08m9xp57oVzk20qAGy0FSZslHSk7A6V7ja+fQZCNZPCDT5Ioyw1c2mEaG2zHLs+BcW51djqzOfUS+fVMBRdmpf2QJJK/iqU5+TZvbmg7ChbhCQ/Y/khUxFInCeSRckRPwfM2n1fSprX1OWBBQ+uoiH09cimqToxS7FqFvHAGyDZCp3qX2oYZfov/CR+cWFxbvqlQf+6mvrRhJcQUYlS5nGxlHEsv6BNC7hV
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDJwIf76QE9ES+N0dcn0ECGWdnYvRgsKfAw+73sWxD53A5Xqbh6vTSHOlwkl6JzwZWyTNkq4sqcXXy0yrMZZQb0L1qTNjrtaamf17zvYHk6V5o+ovqZcJzMKXmdUJPbnwkY4MQxjQ5RaUVFp7yjTm69/k63S0rrR3fmJ26fBJmznjEDq+pbIEvCrHXmfSVkoHC62TfrR5RPqNjpzw+KfMplEyOkvVS2ugQQdemGuHwTJW7xRqt1toc6gArJck6Rl2SH32osHp8YhncuVXxSs9Q4j/5wCi4mim7Q7d8GTDohMFm1YMLCt5/a+hL1c1gIxS9lPb1A+mtTBWIYRbvERIHF
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDzJZUAhdPEIXn0xbnmFiyqfM7nuC2Z45HYAsX5JAM+g1zxaTk06kMfDRA9m49uH/8FHvu3ZOTnr3tGmgBcfztL0Go73R+FAxfs7kPbuN9kLv2dSCJ52JojhHck2t53P0UgmAJZgcqM2QKYASq7rsPgl6jjtK94+JHvf3yRyZBBjqMBMhIOLzAHSRSNe4Cyfkzap4gBEbuQ2dl7ij5X+JJFvTkb529hS5CnNOiWtxoFBU8mV37fEch4hqakHLvBm21TF52EtG3ZJswHOUV8V46Sgtx0h4UcPJvwKkL2k+021nV4nmBNRJUXzFAJSmxilE0CKmzB/uhE2Z+n0K+btqsR
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDRMdCYtj5ql4WM2wRKE0ZcKu5fmJONhwZakJihFAo3VtAWciGH/iV5TDovzXK8SbFvIwCzbxgVQgm6dc3XdjFs5othOrwv3t0/jBgNAAOBn7KCSPMlDlgYq9gtjv8aE8lRnMH+TyPxNvKy4kh4/XxvolABvVP1G91lDp/Q9ofXvy3IDqeJODb6du2fflWl4PZOJhLFBitqpxeJ8CxBZv7Yu/4DHzFwnIHhGahdb4ogMEF2yzFw4P2fXLh0daMsyUmy4TZXrzZ/uFBxWpDIbVCwVC6rmJaikx0zSE10Z8FiIYPI1mWrkyniTx0OrdYzs/mHgx2EwoN/9ArcOlc40PAl
" >> ${HOME}/.ssh/authorized_keys


if [ "$1" == "das" ];then

sudo apt-get -y install subversion

echo "
Host immortal
        User buildbot
        ForwardX11Trusted no
        ForwardAgent no
        Hostname=dsl-external.bbn.com
        IdentityFile=~/.ssh/immortals_buildbot-id_rsa
        IdentitiesOnly yes
" >> ${HOME}/.ssh/config

echo "
Base DAS has been configured. Please copy the buildbot private key to 
${HOME}/.ssh/immortals_buildbot-id_rsa and execute the following command from 
${HOME} to complete DAS setup:

svn co svn+ssh://immortal/svn/immortals
"

elif [ "$1" == "android" ];then

#!/usr/bin/env bash

ANDROID_PLATFORM=21
ANDROID_HOME=${HOME}/.android

ADB_PORT=5432

# Install necessary packages
sudo apt-get update
sudo apt-get -y install software-properties-common curl wget vim build-essential maven unzip python python-pip cmake pkg-config openjdk-8-jdk-headless iptables-persistent

# Install x86 packages needed by the emulator
sudo dpkg --add-architecture i386
sudo apt-get update
sudo apt-get -y upgrade
sudo apt-get -y install libc6:i386 qemu libncurses5:i386 libstdc++6:i386 lib32z1 libpulse0 libx11-6:i386 libx11-6 libgl1-mesa-glx

# Create the license acceptence files
if [ ! -d ${ANDROID_HOME}/licenses ];then
  mkdir -p ${ANDROID_HOME}/licenses
  echo '
8933bad161af4178b1185d1a37fbf41ea5269c55
d56f5187479451eabf01fb78af6dfcb131a6481e' > ${ANDROID_HOME}/licenses/android-sdk-license
fi

cd ${ANDROID_HOME}

# Install the Android SDK
wget https://dl.google.com/android/repository/sdk-tools-linux-4333796.zip
unzip sdk-tools-linux-4333796.zip
rm sdk-tools-linux-4333796.zip

# Install the needed Android packages
${ANDROID_HOME}/tools/bin/sdkmanager "tools" "platform-tools" "emulator" "platforms;android-${ANDROID_PLATFORM}" "system-images;android-${ANDROID_PLATFORM};default;x86_64"

# Export necessary environment variables and add executables to the path
echo '
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/
export ANDROID_HOME=${HOME}/.android
export PATH=${HOME}/.android/platform-tools:${HOME}/.android/tools/bin:${HOME}/.android/emulator:${PATH}
' >> ~/.bashrc

source ~/.bashrc

# Add the user to the kvm group for emulator usage
sudo usermod -a -G kvm ${USER}

# Allow routing external ports to internal ports. For some reason bring up the VMs results in either ens4 or ens6 as the interface, so adding dhcp and routing overrides for both...
echo "net.ipv4.conf.ens3.route_localnet=1" | sudo tee -a /etc/sysctl.conf
#echo "net.ipv4.conf.ens4.route_localnet=1" | sudo tee -a /etc/sysctl.conf
#echo "net.ipv4.conf.ens6.route_localnet=1" | sudo tee -a /etc/sysctl.conf

sudo sysctl -p
sudo sysctl --system

# Set up a routing rule to allow external connections to ADB
sudo apt-get -y install iptables-persistent
sudo iptables -t nat -I PREROUTING -p tcp --dport ${ADB_PORT} -j DNAT --to-destination 127.0.0.1:5555
sudo iptables-save | sudo tee /etc/iptables/rules.v4

${ANDROID_HOME}/tools/bin/avdmanager create avd --package "system-images;android-${ANDROID_PLATFORM};default;x86_64" --device "Nexus 5X" --sdcard 100M --name Emu00
sleep 4
sudo ${ANDROID_HOME}/emulator/emulator -memory 3072 -cores 4 -netspeed gprs -selinux disabled -no-window -avd Emu00&
${ANDROID_HOME}/platform-tools/adb wait-for-device
echo "Now waiting ten minutes to capture a fully booted emulator snapshot..."
sleep 540
sudo ${ANDROID_HOME}/platform-tools/adb emu kill
sudo chown -R ubuntu ${ANDROID_HOME}
sudo chown -R ubuntu ${HOME}/.emulator_console_auth_token
sleep 60

echo '
[Unit]
Description="Job that starts the android emulator"

[Service]
Type=simple
User=ubuntu
ExecStart=/home/ubuntu/.android/emulator/emulator -memory 3072 -cores 4 -netspeed gprs -selinux disabled -no-window -avd Emu00

[Install]
WantedBy=multi-user.target
' | sudo tee /etc/systemd/system/android_emulator.service
sudo systemctl enable android_emulator.service

echo "The emulator should be ready to auto-boot and be usable from port ${ADB_PORT} of the interface ens4 or ens6"

fi
