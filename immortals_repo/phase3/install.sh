#!/usr/bin/env bash

../shared/tools.sh installer --installation-dir=${HOME}/.immortals
./setup.sh
cp immortalsrc ~/.immortalsrc

echo "source ~/.immortalsrc" >> ~/.bashrc

if [ "$1" == "--build" ];then
    ./build.sh
fi
