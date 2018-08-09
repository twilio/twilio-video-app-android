#!/bin/bash

ANDROID_NDK_HOME="/opt/android-ndk"
ANDROID_NDK_INSTALLED="/opt/android-ndk/installed"

if [ ! -e $ANDROID_NDK_INSTALLED ]; then
  cd $ANDROID_NDK_HOME
  wget -qq https://dl.google.com/android/repository/android-ndk-r12b-linux-x86_64.zip
  unzip -o -qq android-ndk-r12b-linux-x86_64.zip
  mv android-ndk-r12b/* .
  touch $ANDROID_NDK_INSTALLED
  rm -rf android-ndk-r12b
  rm android-ndk-r12b-linux-x86_64.zip
fi

echo "export ANDROID_NDK_HOME=$ANDROID_NDK_HOME" >> $BASH_ENV
source $BASH_ENV
