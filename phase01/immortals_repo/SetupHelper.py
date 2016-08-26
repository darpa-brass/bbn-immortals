#!/usr/bin/env python

import os
import sys
import subprocess
import string
import textwrap
from sys import platform as _platform
from glob import glob
import argparse

parser = argparse.ArgumentParser(formatter_class=argparse.RawDescriptionHelpFormatter,
description=textwrap.dedent('''\
IMMoRTALS Environment Setup Helper

This tool is meant to aid in configuration of the environment used by IMMoRTALS.

Please make sure your ANDROID_HOME environment variable is set prior to this tool being run.

If you do not have the android sdk set up yet, run the "--install-android-sdk" option and it will populate the directory specified by your ANDROID_HOME environment variable with the necessary SDK.

Once the SDK has been installed, perform the options to update the libraries for either the main project or the emulator.
'''))

argumentCommandGroup = parser.add_mutually_exclusive_group()
argumentCommandGroup.add_argument('-ia', '--install-android-sdk', action='store_true', help='Install the android SDK to the location indicated by the ANDROID_HOME environment variable if it is not yet installed.')
argumentCommandGroup.add_argument('-ua', '--update-android-sdk', action='store_true', help='Update the sdk files needed to build IMMoRTALS')
argumentCommandGroup.add_argument('-uas', '--update-android-simulator-sdk', action='store_true', help='Update the sdk files needed to run the emulator for tests')
parser.add_argument('-s', '--simulate', action='store_true', help='Does not actually execute the commands, but instead displays the commands to the command line')

OSX_ANDROID_SDK_LOCATION = 'http://dl.google.com/android/android-sdk_r24.4.1-macosx.zip'
LINUX_ANDROID_SDK_LOCATION = 'http://dl.google.com/android/android-sdk_r24.4.1-linux.tgz'

SDK_UPDATE_COMMAND_STRING = ['android', 'update', 'sdk', '--all', '--no-ui', '--filter']

PKGLIST_BASE = ['tools','platform-tools','build-tools-21.1.2','android-21','extra-android-m2repository','extra-android-support','extra-google-google_play_services','extra-google-m2repository']

PKGLIST_SIMULATOR = ['android-23','sys-img-x86_64-android-21','sys-img-x86_64-android-23']
PKGLIST_SIMULATOR.extend(PKGLIST_BASE)

ANDROID_HOME = os.environ['ANDROID_HOME']

EXECUTE_COMMANDS = True

if ANDROID_HOME is not None:
    ANDROID_EXECUTABLE = os.path.join(ANDROID_HOME, 'tools/android')

def subprocess_call(command_list):
    global EXECUTE_COMMANDS
    if EXECUTE_COMMANDS:
        subprocess.call(command_list)
    else:
        print '> ' + string.join(command_list)


def install_android_sdk(target_directory):
    if not os.path.isdir(target_directory):
        os.mkdir(target_directory)

    if _platform == "linux" or _platform == "linux2":
        print "Now downloading the Android SDK... Please wait...."

        wget_command = ['wget', LINUX_ANDROID_SDK_LOCATION, '-P', target_directory]
        subprocess_call(wget_command)

        sdk_file_name = os.path.split(LINUX_ANDROID_SDK_LOCATION)[1]

        untar_command = ['tar', 'xvzf', os.path.join(target_directory, sdk_file_name), '-C', target_directory];
        subprocess_call(untar_command)


        for mvFile in glob(os.path.join(target_directory, 'android-sdk-linux', '*')):
            mv_command = ['mv', mvFile, target_directory]
            subprocess_call(mv_command)

        rm_command = ['rm', '-r', os.path.join(target_directory, 'android-sdk-linux'), os.path.join(target_directory, sdk_file_name)]
        subprocess_call(rm_command)

    elif _platform == "darwin":
        print "Now downloading the Android SDK... Please wait...."
        wget_command = ['wget', OSX_ANDROID_SDK_LOCATION, '-P', target_directory]
        subprocess_call(wget_command)

        sdk_file_name = os.path.split(OSX_ANDROID_SDK_LOCATION)[1]

        unzip_command = ['unzip', os.path.join(target_directory, sdk_file_name), '-d', target_directory]
        subprocess_call(unzip_command)

        for mvFile in glob(os.path.join(target_directory, 'android-sdk-macosx', '*')):
            mv_command = ['mv', mvFile, target_directory]
            subprocess_call(mv_command)

        rm_command = ['rm', '-r', os.path.join(target_directory, 'android-sdk-macosx'), os.path.join(target_directory, sdk_file_name)]
        subprocess_call(rm_command)

    else:
        print 'Cannot install Android SDK for unknown platform "' + _platform + '"!'
        return

def update_android_sdk(package_list):
    update_command = []
    update_command.extend(SDK_UPDATE_COMMAND_STRING)

    pkg_list_single_string = string.join(package_list, ',')

    update_command.append(pkg_list_single_string)
    subprocess_call(update_command)


def main():
    global ANDROID_HOME
    global EXECUTE_COMMANDS

    if ANDROID_HOME is None:
        print "ANDROID_HOME is unset. Please set your ANDROID_HOME environment variable to where you would like to install the SDK and be sure the parent directory structure exists."
        sys.exit()

    args = parser.parse_args()

    if args.simulate:
        EXECUTE_COMMANDS = False

    if args.install_android_sdk:
        parent = os.path.split(ANDROID_HOME)[0]

        if not os.path.exists(parent):
            print 'ERROR: Parent path "' + parent + '" of ANDROID_HOME environment variable does not exist!'

        elif os.path.exists(ANDROID_HOME) and len(os.listdir(ANDROID_HOME)) < 1:
            print 'ERROR: Cannot install the andrdoid SDK to the non-empty directory "' + ANDROID_HOME + '" set by the ANDROID_HOME environment variable!'
        else:
            if not ANDROID_HOME.endswith('/'):
                ANDROID_HOME = ANDROID_HOME + '/'

            install_android_sdk(ANDROID_HOME)

    elif args.update_android_sdk:
        update_android_sdk(PKGLIST_BASE)

    elif args.update_android_simulator_sdk:
        update_android_sdk(PKGLIST_SIMULATOR)

    else:
        parser.print_help()

if __name__ == '__main__':
    main()
