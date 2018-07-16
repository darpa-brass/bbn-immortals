#!/usr/bin/env python3
import argparse
import csv
import json
import os
import shutil
import subprocess
import time
from enum import Enum
from typing import FrozenSet, List

_parser = argparse.ArgumentParser(description='DAS Runner.')
_parser.add_argument('--mode', type=str, required=True, choices=['cp1-test', 'cp3-hddrass-test', 'cp2-exerciser'])
_parser.add_argument('--test-iterations', type=int)

IMMORTALS_ROOT = os.getcwd() + '/'
MARTI_ROOT = IMMORTALS_ROOT + 'applications/server/Marti/'
RESULT_DIR = IMMORTALS_ROOT + 'EXECUTION_RESULTS/'
TEST_OVERRIDE_FILE = IMMORTALS_ROOT + 'harness/test_override_file.json'

EMULATOR_VERSION = '21'
EMULATOR_ARCH = 'x64'
MEMORY = '2048'
CORES = '4'

wrapper_files = [
    IMMORTALS_ROOT + 'applications/client/CLITAK/src/com/bbn/ataklite/net/WrapperSocketChannel.java',
    IMMORTALS_ROOT + 'applications/client/ATAKLite/src/com/bbn/ataklite/net/WrapperSocketChannel.java',
    IMMORTALS_ROOT + 'applications/server/Marti/src/com/bbn/marti/immortals/net/tcp/WrapperSocket.java'
]


# TODO: Not this copy and paste...
# noinspection PyPep8Naming
class SecurityStandard(Enum):
    def __init__(self, key_idx_value: str, algorithm: str, cipherChainingMode: str, description: str, keySize: int):
        self._key_idx_value = key_idx_value
        self.algorithm = algorithm  # type: str
        self.cipherChainingMode = cipherChainingMode  # type: str
        self.description = description  # type: str
        self.keySize = keySize  # type: int

    AES_128 = ("AES_128",
               "AES",
               None,
               "AES encryption algorithm with 128bit+ key",
               16)

    AES_256 = ("AES_256",
               "AES",
               None,
               "AES encryption algorithm with 256bit+ key",
               32)

    Blowfish_128 = ("Blowfish_128",
                    "Blowfish",
                    None,
                    "Blowfish encryption algorithm with 128bit+ key",
                    16)

    DESEDE_128 = ("DESEDE_128",
                  "DESede",
                  None,
                  "DESEDE encryption algorithm with 128bit+ key",
                  16)

    DES_56 = ("DES_56",
              "DES",
              None,
              "DES encryption algorithm with 56bit+ key",
              8)

    GCM_128 = ("GCM_128",
               "GCM",
               None,
               "GCM encryption algorithm with 128bit+ key",
               16)

    ARIA = ("ARIA",
            "ARIA",
            None,
            "ARIA encryption algorithm",
            None)

    XTEA = ("XTEA",
            "XTEA",
            None,
            "XTEA encryption algorithm",
            None)

    TWOFISH = ("TWOFISH",
               "Twofish",
               None,
               "Twofish encryption algorithm",
               None)

    THREEFISH_256 = ("THREEFISH_256",
                     "Threefish_256",
                     None,
                     "Threefish encryption algorithm with 256bit+ key",
                     32)

    @classmethod
    def all_algorithm(cls) -> FrozenSet[str]:
        return frozenset([k.algorithm for k in list(cls)])

    @classmethod
    def all_cipherChainingMode(cls) -> FrozenSet[str]:
        return frozenset([k.cipherChainingMode for k in list(cls)])

    @classmethod
    def all_description(cls) -> FrozenSet[str]:
        return frozenset([k.description for k in list(cls)])

    @classmethod
    def all_keySize(cls) -> FrozenSet[int]:
        return frozenset([k.keySize for k in list(cls)])


class Scenario(Enum):
    CP1_A = 'cp1.p2cp1baselineA'
    CP1_B = 'cp1.p2cp1baselineB'
    CP1_C = 'cp1.p2cp1challenge'
    CP2_A = 'cp2.p2cp2baselineA'
    CP2_B = 'cp2.p2cp2baselineB'
    CP2_C_0 = 'cp2_0.p2cp2challenge'
    CP2_C_1 = 'cp2_1.p2cp2challenge'
    CP2_C_2 = 'cp2_2.p2cp2challenge'
    CP2_C_3 = 'cp2_3.p2cp2challenge'
    CP3_A = 'cp3hddrass.p2cp3baselineA'
    CP3_HDDRASS_B = 'cp3hddrass.p2cp3baselineB'
    CP3_HDDRASS_C = 'cp3hddrass.p2cp3challenge'
    CP3_PLUG_B = 'cp3plu.p2cp3baselineB'
    CP3_PLUG_C = 'cp3plu.p2cp3challenge'
    CP3_LITL_B = 'cp3litl.p2cp3litlbaselineB'
    CP3_LITL_C = 'cp3litl.p2cp3litlchallenge'


emu_processes = list()


def exec(cmd: List[str], identifier: str, cwd: str = None, command_processor=subprocess, throw_exception: bool = True):
    print('EXEC[' + ' '.join(cmd) + ']')
    if cwd is None:
        result = command_processor.run(cmd)
    else:
        result = command_processor.run(cmd, cwd=cwd)

    if result.returncode != 0 and throw_exception:
        raise Exception('Bad return value of "' + str(result.returncode) + '" from ' + identifier + '!')
    else:
        return result.returncode == 0


def build_das():
    exec([IMMORTALS_ROOT + 'gradlew', '--stop'], 'gradle-stop', cwd=IMMORTALS_ROOT)
    time.sleep(2)

    exec([IMMORTALS_ROOT + 'gradlew', 'clean', 'deploy'], cwd=IMMORTALS_ROOT)
    time.sleep(2)

    exec([IMMORTALS_ROOT + 'gradlew', '--stop'], 'gradle-stop', cwd=IMMORTALS_ROOT)
    time.sleep(2)


def execute_das(scenario: Scenario):
    exec([IMMORTALS_ROOT + 'gradlew', '--stop'], 'gradle-stop', cwd=IMMORTALS_ROOT)
    time.sleep(2)

    if os.path.exists(IMMORTALS_ROOT + 'settings.gradle.original'):
        shutil.copy(IMMORTALS_ROOT + 'settings.gradle.original', IMMORTALS_ROOT + 'settings.gradle')

    timestamp = time.strftime("%Y%m%d_%H%M%S")

    rval = exec(['bash', 'tools.sh', 'orchestrate', scenario.value], 'executeDas', cwd=IMMORTALS_ROOT + 'harness',
                throw_exception=False)
    if scenario == Scenario.CP2_C_0 or scenario == Scenario.CP2_C_1 or \
            scenario == Scenario.CP2_C_2 or scenario == Scenario.CP2_C_3:
        rval = subprocess.run(['adb', '-s', 'emulator-5554', 'logcat', '-d'], stdout=subprocess.PIPE)
        with open(IMMORTALS_ROOT + 'DAS_DEPLOYMENT/emulator-5554.log', 'w') as outfile:
            outfile.write(rval.stdout.decode())

        rval = subprocess.run(['adb', '-s', 'emulator-5556', 'logcat', '-d'], stdout=subprocess.PIPE)
        with open(IMMORTALS_ROOT + 'DAS_DEPLOYMENT/emulator-5556.log', 'w') as outfile:
            outfile.write(rval.stdout.decode())

    shutil.move(os.path.join(IMMORTALS_ROOT, 'DAS_DEPLOYMENT'),
                os.path.join(RESULT_DIR, scenario.value + '-' + timestamp))


def create_emulator(adb_device_identifier: str, name: str, command_processor=subprocess):
    cmd = ['avdmanager', 'create', 'avd', '--package', 'system-images;android-21;default;x86_64', '--device',
           'Nexus 5X', '--sdcard', '100M', '--name', name]
    exec(cmd, 'create_emulator', command_processor=command_processor)
    time.sleep(2)


def start_emulator(adb_device_identifier: str, name: str, command_processor=subprocess):
    cmd = ['emulator', '-memory', MEMORY, '-cores', CORES, '-netspeed', 'gprs', '-selinux', 'disabled', '-avd', name]
    print('EXEC[' + ' '.join(cmd) + ']')
    process = subprocess.Popen(cmd)
    emu_processes.append(subprocess.Popen(cmd))
    time.sleep(12)

    cmd = ['adb', '-s', adb_device_identifier, 'wait-for-device']
    print('EXEC[' + ' '.join(cmd) + ']')
    command_processor.run(cmd)
    time.sleep(10)
    rval = subprocess.run(['adb', 'devices'], stdout=subprocess.PIPE)
    if not adb_device_identifier in rval.stdout.decode():
        raise Exception('Could not find emulator "' + adb_device_identifier + '" in the output of `adb devices`!')


def delete_emulator(adb_device_identifier: str, name: str, command_processor=subprocess):
    cmd = ['avdmanager', 'delete', 'avd', '--name', name]
    exec(cmd, 'deleteEmulator')
    time.sleep(2)


def stop_emulator(adb_device_identifier: str, name: str, command_processor=subprocess):
    cmd = ['adb', '-s', adb_device_identifier, 'emu', 'kill']
    exec(cmd, 'stopEmulator')
    time.sleep(8)


def cycle_emulator(adb_device_identifier: str, name: str, command_processor=subprocess):
    try:
        stop_emulator(adb_device_identifier, name, command_processor)
        delete_emulator(adb_device_identifier, name, command_processor)
    except:
        pass
    create_emulator(adb_device_identifier, name, command_processor)
    start_emulator(adb_device_identifier, name, command_processor)


def cycle_emulators():
    exec(['adb', 'kill-server'], 'adbKillServer')
    time.sleep(2)
    exec(['adb', 'start-server'], 'adbStartServer')
    time.sleep(2)
    cycle_emulator('emulator-5554', 'Emu000')
    cycle_emulator('emulator-5556', 'Emu001')


def main():
    if not os.path.exists(RESULT_DIR):
        os.mkdir(RESULT_DIR)

    args = _parser.parse_args()

    if args.mode == 'cp1-test':
        if args.test_iterations is None:
            print('Please specify the number of test iterations!')
        else:
            for i in range(0, args.test_iterations):
                execute_das(Scenario.CP1_C)

    elif args.mode == 'cp3-hddrass-test':
        if args.test_iterations is None:
            print('Please specify the number of test iterations!')
        else:
            for i in range(0, args.test_iterations):
                execute_das(Scenario.CP3_HDDRASS_C)

    elif args.mode == 'cp2-exerciser':
        for ss in SecurityStandard:
            update_cp2_files(ss)

            cycle_emulators()
            execute_das(Scenario.CP2_C_0)
            cycle_emulators()
            execute_das(Scenario.CP2_C_1)
            cycle_emulators()
            execute_das(Scenario.CP2_C_2)
            cycle_emulators()
            execute_das(Scenario.CP2_C_3)

        gather_results()


def update_cp2_files(security_standard: SecurityStandard):
    filepaths = [
        os.path.join(IMMORTALS_ROOT, "harness/pymmortals/resources/p2_test_scenarios/cp2_0.json"),
        os.path.join(IMMORTALS_ROOT, "harness/pymmortals/resources/p2_test_scenarios/cp2_1.json"),
        os.path.join(IMMORTALS_ROOT, "harness/pymmortals/resources/p2_test_scenarios/cp2_2.json"),
        os.path.join(IMMORTALS_ROOT, "harness/pymmortals/resources/p2_test_scenarios/cp2_3.json")
    ]
    orig_filepaths = [
        os.path.join(IMMORTALS_ROOT, "harness/pymmortals/resources/p2_test_scenarios/cp2_0_orig.json"),
        os.path.join(IMMORTALS_ROOT, "harness/pymmortals/resources/p2_test_scenarios/cp2_1_orig.json"),
        os.path.join(IMMORTALS_ROOT, "harness/pymmortals/resources/p2_test_scenarios/cp2_2_orig.json"),
        os.path.join(IMMORTALS_ROOT, "harness/pymmortals/resources/p2_test_scenarios/cp2_3_orig.json")
    ]

    for idx in range(0, 4):
        if not os.path.exists(orig_filepaths[idx]):
            shutil.copy(filepaths[idx], orig_filepaths[idx])

        lines = list()
        for line in open(orig_filepaths[idx], 'r').readlines():
            if "AES_128" in line:
                lines.append(line.replace("AES_128", security_standard.name))
            else:
                lines.append(line)

        with open(filepaths[idx], 'w') as target:
            target.writelines(lines)


# def __init__(self, adb_device_identifier: str, console_port: int,
#              command_processor,
#              instance_identifier: str = None):

# emuhelper1 = EmuHelper('emulator-5556', 5557, subprocess, 'emulator-5556')
# emuhelper1.create_emulator()


# class TestApp:
#     def __init__(self, build_string_root_args: List[str], build_string_args: List[str]):
#         self.build_string_root_args = build_string_root_args
#         self.build_string_args = build_string_args
# 
# 
# apps = [
# 
#     TestApp(
#         build_string_root_args=[IMMORTALS_ROOT + 'gradlew', '--build-file',
#                                 IMMORTALS_ROOT + 'shared/modules/dfus/BouncyCastleCipher/build.gradle'],
#         build_string_args=['-Dmil.darpa.immortals.mock=true', 'clean', 'test']
#     ),
#     TestApp(
#         build_string_root_args=[IMMORTALS_ROOT + 'gradlew', '--build-file',
#                                 IMMORTALS_ROOT + 'shared/modules/dfus/ElevationApi-1/build.gradle'],
#         build_string_args=['-Dmil.darpa.immortals.mock=true', 'clean', 'test']
#     ),
#     TestApp(
#         build_string_root_args=[IMMORTALS_ROOT + 'gradlew', '--build-file',
#                                 IMMORTALS_ROOT + 'shared/modules/dfus/JavaxCrypto/build.gradle'],
#         build_string_args=['-Dmil.darpa.immortals.mock=true', 'clean', 'test']
#     ),
#     TestApp(
#         build_string_root_args=[IMMORTALS_ROOT + 'gradlew', '--build-file',
#                                 IMMORTALS_ROOT + 'applications/server/Marti/build.gradle'],
#         build_string_args=['-Dmil.darpa.immortals.mock=true', 'clean', 'test',
#                            '--tests', 'com.bbn.marti.Tests.testElevationAccuracyEnhancement',
#                            '--tests', 'com.bbn.marti.Tests.testCotByteBufferPipe',
#                            '--tests', 'com.bbn.marti.Tests.testDbIntegration',
#                            '--tests', 'com.bbn.marti.Tests.testImageTransmission',
#                            '--tests', 'com.bbn.marti.Tests.testLatestSaTransmission',
#                            '--tests', 'com.bbn.marti.Tests.testImageSave']
#     )
# ]

# BUILD_STRING_ROOT_ARGS = [IMMORTALS_ROOT + 'gradlew', '--build-file', MARTI_ROOT + 'build.gradle']
# BUILD_STRING_ARGS = ['-Dmil.darpa.immortals.mock=true', 'clean', 'test',
#                      '--tests com.bbn.marti.Tests.testElevationAccuracyEnhancement',
#                      '--tests', 'com.bbn.marti.Tests.testCotByteBufferPipe',
#                      '--tests', 'com.bbn.marti.Tests.testDbIntegration',
#                      '--tests', 'com.bbn.marti.Tests.testImageTransmission',
#                      '--tests', 'com.bbn.marti.Tests.testLatestSaTransmission',
#                      '--tests', 'com.bbn.marti.Tests.testImageSave']

# def main():
#     failure = False
#     for i in range(0, 100):
#         if not failure:
#             for test_app in apps:
#                 cmd = test_app.build_string_root_args + ['--daemon'] + test_app.build_string_args
#                 print(' '.join(cmd))
#                 cp = subprocess.run(cmd)
#                 if (cp.returncode != 0):
#                     print("FAILURE!!")
#                     failure = True
#                     break
# 
#     for i in range(0, 100):
#         if not failure:
#             for test_app in apps:
#                 cmd = test_app.build_string_root_args + ['--no-daemon'] + test_app.build_string_args
#                 print(' '.join(cmd))
#                 cp = subprocess.run(cmd)
#                 if (cp.returncode != 0):
#                     failure = True
#                     break

class ResultObject:
    def __init__(self, security_standard: str, server_aesni: bool, server_sep: bool, client_sep: bool, outcome: bool, details: str = None):
        self.security_standard = security_standard
        self.server_aesni = server_aesni
        self.server_sep = server_sep
        self.client_sep = client_sep
        self.outcome = outcome
        self.details = '' if details is None else details 


class LabelObject:
    def __init__(self, security_standard: str):
        self.security_standard = security_standard
        self.no_resources_result = None
        self.all_resources_result = None
        self.aes_only_result = None
        self.sep_only_result = None

    @classmethod
    def from_result_objects(cls, result_objects: List[ResultObject]):
        label_map = dict()
        for ro in result_objects:
            if ro.security_standard not in label_map:
                label_map[ro.security_standard] = LabelObject(security_standard=ro.security_standard)

            label_object = label_map[ro.security_standard]

            if ro.client_sep and ro.server_aesni and ro.server_sep:
                label_object.all_resources_result = ro.outcome
            elif ro.server_aesni and not (ro.client_sep and ro.server_sep):
                label_object.aes_only_result = ro.outcome
            elif not ro.server_aesni and not (ro.client_sep and ro.server_sep):
                label_object.no_resources_result = ro.outcome
            elif not ro.server_aesni and ro.client_sep and ro.server_sep:
                label_object.sep_only_result = ro.outcome
            else:
                raise Exception("BAD COMBO!")

        return label_map.values()


def gather_results():
    results = list()

    for dirname in os.listdir(RESULT_DIR):
        input_path = os.path.join(RESULT_DIR, dirname, 'input.json')
        output_path = os.path.join(RESULT_DIR, dirname, 'result.json')
        if os.path.exists(input_path) and os.path.exists(output_path):
            input = json.load(open(input_path))
            output = json.load(open(output_path))

            client_resources = input['atakLiteClientModel']['resources']
            server_resources = input['martiServerModel']['resources']

            server_aesni = 'HARWARE_AES' in server_resources
            server_sep = 'STRONG_CRYPTO' in server_resources
            client_sep = 'STRONG_CRYPTO' in client_resources
            
            detail_messages = output['adaptation']['details']['detailMessages']
            detail_msg = '' if len(detail_messages) == 0 else detail_messages[0]

            results.append(ResultObject(
                security_standard=input['globalModel']['requirements']['dataInTransit']['securityStandard'],
                server_aesni=server_aesni,
                server_sep=server_sep,
                client_sep=client_sep,
                outcome=output['validation']['verdictOutcome'],
                details=detail_msg
            ))

    labeled_results = LabelObject.from_result_objects(results)

    with open(os.path.join(IMMORTALS_ROOT, 'cp2_test_results.csv'), 'w', newline='') as output_file:
        resultwriter = csv.writer(output_file, quoting=csv.QUOTE_MINIMAL)
        resultwriter.writerow(['Label', 'AllResources', 'OnlySEP', 'OnlyAESNI', 'NoResources'])
        for result in labeled_results:
            resultwriter.writerow([result.security_standard, result.all_resources_result,
                                   result.sep_only_result, result.aes_only_result, result.no_resources_result])

    with open(os.path.join(IMMORTALS_ROOT, 'cp2_execution_results.csv'), 'w', newline='') as output_file:
        resultwriter = csv.writer(output_file, quoting=csv.QUOTE_MINIMAL)
        resultwriter.writerow(['Label', 'ServerAESNI', 'ServerSEP', 'ClientSEP', 'Outcome', 'Details'])
        for result in results:
            resultwriter.writerow([result.security_standard, result.server_aesni, result.server_sep,
                                  result.client_sep, result.outcome, result.details])
        

if __name__ == '__main__':
    main()
