#!/usr/bin/env bash

cd /

sudo ./droidscope_art/objs/emulator -no-audio -no-window -partition-size 1000 -sysdir /out/target/product/generic/ -sdcard ${2} -kernel /boot/zImage -memory 4000 -port ${1} -qemu -monitor telnet:127.0.0.1:36143,server,nowait&
#sudo ./droidscope_art/objs/emulator -no-audio -no-window -partition-size 1000 -sysdir /out/target/product/generic/ -sdcard ${2} -kernel /boot/zImage -memory 4000 -ports {1},{2} -qemu -monitor telnet:127.0.0.1:36143,server,nowait&
#sudo ./droidscope_art/objs/emulator -no-audio -no-window -partition-size 1000 -sysdir /out/target/product/generic/ -sdcard ${HOME}/droidscope01.img -kernel /boot/zImage -memory 4000 -ports 5560,5561 -qemu -monitor telnet:127.0.0.1:36143,server,nowait&

#svn checkout --username awellman https://dsl-external.bbn.com/svn/immortals/trunk/
