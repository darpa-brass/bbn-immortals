#!/usr/bin/env python

import os
import shutil
import string
import subprocess
import sys

POINT_IDENTIFIER = '$BCC0A94D-C2B1-40AD-8056-E3DDBD46585E'
DECLARATION_POINT = POINT_IDENTIFIER + '-declaration'
INIT_POINT = POINT_IDENTIFIER + '-init'
WORK_POINT = POINT_IDENTIFIER + '-work'
CLEANUP_POINT = POINT_IDENTIFIER + '-cleanup'

TEMPLATE_SOURCE_FILE = 'src-templates/main/java/mil/darpa/immortals/modulerunner/MainActivity.java'
TARGET_SOURCE_FILE = 'src/main/java/mil/darpa/immortals/modulerunner/MainActivity.java'
TARGET_DEPENDENCY_FILE = 'dependencies.gradle'


class TemplateConfiguration:
    class_package = None
    declaration = None
    initialization = None
    cleanup = None
    work = None

    def __init__(self, class_package):
        self.class_package = class_package

        self.declaration = 'private ' + class_package + ' locationProvider;'
        self.initialization = 'locationProvider = new ' + self.class_package + '();'

        self.cleanup = 'locationProvider.onDestroy();'
        self.work = 'locationProvider.getLastKnownLocation();'

        self.dependency_definition = string.join(class_package.split('.')[0:-2], '.') + ":" + class_package.split('.')[-1] + ':+'



androidLP = TemplateConfiguration('mil.darpa.immortals.dfus.location.LocationProviderAndroidGpsBuiltIn')

btLP = TemplateConfiguration('mil.darpa.immortals.dfus.location.LocationProviderBluetoothGpsSimulated')
btLP.work = 'locationProvider.getCurrentLocation();'
btLP.cleanup = ''

mLP = TemplateConfiguration('mil.darpa.immortals.dfus.location.LocationProviderManualSimulated')
mLP.cleanup = ''

smLP = TemplateConfiguration('mil.darpa.immortals.dfus.location.LocationProviderSaasmSimulated')
smLP.work = 'locationProvider.getTrustedLocation();'
smLP.cleanup = ''

usbLP = TemplateConfiguration('mil.darpa.immortals.dfus.location.LocationProviderUsbGpsSimulated')
usbLP.cleanup = ''

dfus = [
androidLP,
btLP,
mLP,
smLP,
usbLP
]


def insert_configuration(source_template, target_file, template_configuration, point_identifier):
    with open(source_template) as source:
        source_lines = source.readlines()


    target_lines = []

    for line in source_lines:
        if point_identifier in line:

            if DECLARATION_POINT in line:
                target_lines.append(line.replace(DECLARATION_POINT, template_configuration.declaration))

            elif INIT_POINT in line:
                target_lines.append(line.replace(INIT_POINT, template_configuration.initialization))

            elif WORK_POINT in line:
                target_lines.append(line.replace(WORK_POINT, template_configuration.work))

            elif CLEANUP_POINT in line:
                target_lines.append(line.replace(CLEANUP_POINT, template_configuration.cleanup))

            else:
                raise Exception('unexpected point identifier in line "' + line + '"!')

        else:
            target_lines.append(line)



    with open(target_file, 'w') as target:
        target.writelines(target_lines)

def generate_dependency_file(target_filepath, template_configuration):
    target_lines = []

    target_lines.append("apply plugin: 'com.android.application'\n")
    target_lines.append("dependencies {\n")
    target_lines.append("    compile '" + template_configuration.dependency_definition + "'\n")
    target_lines.append("}")

    with open(target_filepath, 'w') as target:
        target.writelines(target_lines)

if __name__ == '__main__':
    for dfu in dfus:
        subprocess.check_output(['gradle', 'clean'])
        insert_configuration(TEMPLATE_SOURCE_FILE, TARGET_SOURCE_FILE, dfu, POINT_IDENTIFIER)
        generate_dependency_file(TARGET_DEPENDENCY_FILE, dfu)
        subprocess.check_output(['gradle', 'build'])

        idTag = dfu.class_package.split('.')[-1]
        build_dir = 'build/outputs/apk/'
        source_debug_filepath = os.path.join(build_dir, 'modulerunner-android-debug.apk');
        source_release_filepath = os.path.join(build_dir, 'modulerunner-android-release-unsigned.apk')
        target_debug_filepath = 'modulerunner-android-debug-' + idTag + '.apk'
        target_release_filepath = 'modulerunner-android-release-' + idTag + '.apk'

        if os.path.isfile(source_debug_filepath):
            shutil.copy(source_debug_filepath, target_debug_filepath)

        if os.path.isfile(source_release_filepath):
            shutil.copy(source_release_filepath, target_release_filepath)
