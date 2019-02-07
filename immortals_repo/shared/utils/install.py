#!/usr/bin/env python3
import argparse
import json
import os
import subprocess
import traceback
from typing import List, Optional, Dict

import sys

_staging_dir = '/tmp/immortals_deployment_test'

parser = argparse.ArgumentParser(
    description='Test The IMMoRTALS Deployment. By default, everything is tested. Installation of dependencies ' +
                'will be executed and validated if known prerequisites are not present.')

parser.add_argument('--skip-basic-env-setup', action='store_true', default=False,
                    help='Skips installation of the basic deployment dependencies')

parser.add_argument('--skip-deployment-env-setup', action='store_true', default=False,
                    help='Skips installation of the full deployment dependencies prior to validation')

parser.add_argument('-e', '--skip-environment-setup', action='store_true', default=False,
                    help='Skips installation of the deployment dependencies prior to validation')

parser.add_argument('-b', '--skip-basic-build', action='store_true', default=False,
                    help='Skips execution of the basic build')

parser.add_argument('-f', '--skip-full-deployment', action='store_true', default=False,
                    help='skips full project build deployment')

parser.add_argument('-v', '--skip-deployment-validation', action='store_true', default=False,
                    help='Skips the project deployment validation. Automatically enabled if full deployment is skipped')

parser.add_argument('-a', '--skip-api-smoketest', action='store_true', default=False,
                    help='Skips the api smoke test')

parser.add_argument('-m', '--skip-marti-baseline', action='store_true', default=False,
                    help='Skips the baseline Marti validation tests')

parser.add_argument('--dry-run', action='store_true', default=False,
                    help='Performs a dry run, printing out the commands but not executing them.')

parser.add_argument('--clean-first', action='store_true', default=False,
                    help='Executes the clean command on builds before building them')

parser.add_argument('--build-in-current-project', action='store_true', default=False,
                    help="Performs the build sequence in the current project directory instead of the temporary " +
                         "project directory (only applicable to the full deployment)")

parser.add_argument('--use-existing-tmp-project', action='store_true', default=False,
                    help="Uses the existing temporary project instead of forcing you to remove it")

parser.add_argument('-l', '--ll-mode', action='store_true', default=False,
                    help="Uses a set of predefined configuration options optimized for LL testing, " +
                         "ignoring all other parameters other than '--dry-run'.")

parser.add_argument('-x', '--fail-fast', action='store_true', default=False,
                    help="Causes the installation sequence to fail at the first sign of trouble.")

parser.add_argument('--buildsystem-predeploy-setup', action='store_true', default=False,
                    help="Configuration for setting up the predeploy environment used by the build system.")


def validate_deployment(immortals_root_deployment: str):
    try:
        # Make sure an in-use DFU has been analyzed
        expected_artifacts = {
            "analysis": [
                'knowledge-repo/vocabulary/ontology-static/ontology/_ANALYSIS/_krgp/TakServerDataManager/structures/TakServerDataManager-projectOutput.ttl',
                'knowledge-repo/vocabulary/ontology-static/ontology/_ANALYSIS/_krgp/ElevationApi-1/structures/dfus/ElevationApi.class-DFU.ttl',
                'knowledge-repo/vocabulary/ontology-static/ontology/_ANALYSIS/_krgp/ElevationApi-2/structures/dfus/ElevationApi.class-DFU.ttl',
                'knowledge-repo/vocabulary/ontology-static/ontology/_ANALYSIS/_krgp/ATAKLite/structures/ATAKLite-projectOutput.ttl',
                'knowledge-repo/vocabulary/ontology-static/ontology/_ANALYSIS/_krgp/Marti/structures/dfus/Tests.class-DFU.ttl',
                'knowledge-repo/vocabulary/ontology-static/ontology/_ANALYSIS/_krgp/ThirdPartyLibAnalysisAndroidApp/structures/ThirdPartyLibAnalysisAndroidApp-projectOutput.ttl',
                'knowledge-repo/vocabulary/ontology-static/ontology/_ANALYSIS/_krgp/BouncyCastleCipher/structures/dfus/CipherImplBouncyCrypto.class-DFU.ttl',
                'knowledge-repo/vocabulary/ontology-static/ontology/_ANALYSIS/_krgp/JavaxCrypto/structures/dfus/CipherImplJavaxCrypto.class-DFU.ttl',
                'knowledge-repo/vocabulary/ontology-static/ontology/_ANALYSIS/_krgp/NoOpCipher/structures/dfus/CipherImplNoop.class-DFU.ttl'
            ],
            "build": [
                'das/das-launcher-2.0-LOCAL.jar',
                'applications/server/Marti/Marti-immortals.jar',
                'applications/client/ATAKLite/ATAKLite-debug.apk'
            ],
            "extensions": [
                'extensions/vanderbilt/aql-brass-server-aql-brass-server.jar',
                'extensions/ucr/thirdPartyLibAnalysis/yuesLib.py'
            ],
            "database": [
                'database/server/data/source.csv',
                'database/server/data/cot_event.csv',
                'database/server/data/cot_event_position.csv',
                'database/server/data/master_cot_event.csv'
            ]
        }

        for label in expected_artifacts.keys():
            for path in expected_artifacts.get(label):
                assert os.path.exists(os.path.join(immortals_root_deployment, path)), \
                    'ERROR: ' + label + ' artifact "' + os.path.join(immortals_root_deployment,
                                                                     path) + '" was not found!'

        expected_good_commands = {
            "database": [
                ['psql', 'immortals', '--command', '']
            ]
        }

        for label in expected_good_commands.keys():
            for cmd in expected_good_commands.get(label):
                rcode = subprocess.run(cmd).returncode
                assert rcode == 0, \
                    'ERROR: ' + label + ' command `' + ' '.join(
                        cmd) + '` had a non-zero return code of ' + str(rcode) + '!'

        # Relative to immortals_root
        expected_json_values = {
            immortals_root_deployment + 'ARTIFACT_DATA.json': {
                immortals_root_deployment + "shared/modules/dfus/ElevationApi-1": {
                    "targetName": "ElevationApi-1",
                    "targetGroup": "mil.darpa.immortals.dfus",
                    "targetVersion": "2.0-LOCAL",
                    "deploymentTarget": "JAVA",
                    "deploymentTargetVersion": "1.7",
                    "buildFile": "build.gradle",
                    "sourceSubdirectory": "src/main/java/",
                    "buildPlatform": "GRADLE",
                    "buildToolBuildParameters": [
                        "clean",
                        "build",
                        "-x",
                        "test"
                    ],
                    "testing": {
                        "buildToolValidationParameters": [
                            "--daemon",
                            "clean",
                            "test"
                        ],
                        "testResultXmlSubdirectory": "build/test-results/test/",
                        "testCoverageReportXmlFileSubpath": "build/reports/jacoco/test/jacocoTestReport.xml"
                    },
                    "publishing": {
                        "groupId": "mil.darpa.immortals.dfus",
                        "artifactId": "ElevationApi-1",
                        "version": "2.0-LOCAL",
                        "buildToolPublishParameters": [
                            "publish"
                        ]
                    },
                    "execution": {
                        "executionStartSettleTimeMS": 2000,
                        "executableFilename": "ElevationApi-1-2.0-LOCAL.jar",
                        "executionPackageIdentifier": "mil.darpa.immortals.dfus",
                        "executionMainMethodClasspath": "mil.darpa.immortals.dfus.ElevationApi"
                    }
                },
                immortals_root_deployment + "shared/modules/dfus/ElevationApi-2": {
                    "targetName": "ElevationApi-2",
                    "targetGroup": "mil.darpa.immortals.dfus",
                    "targetVersion": "2.0-LOCAL",
                    "deploymentTarget": "JAVA",
                    "deploymentTargetVersion": "1.7",
                    "rootProjectSubdirectory": "shared/modules/dfus/ElevationApi-2/",
                    "buildFile": "build.gradle",
                    "sourceSubdirectory": "src/main/java/",
                    "buildPlatform": "GRADLE",
                    "buildToolBuildParameters": [
                        "clean",
                        "build",
                        "-x",
                        "test"
                    ],
                    "testing": {
                        "buildToolValidationParameters": [
                            "--daemon",
                            "clean",
                            "test"
                        ],
                        "testResultXmlSubdirectory": "build/test-results/test/",
                        "testCoverageReportXmlFileSubpath": "build/reports/jacoco/test/jacocoTestReport.xml"
                    },
                    "publishing": {
                        "groupId": "mil.darpa.immortals.dfus",
                        "artifactId": "ElevationApi-2",
                        "version": "2.0-LOCAL",
                        "buildToolPublishParameters": [
                            "publish"
                        ]
                    },
                    "execution": {
                        "executionStartSettleTimeMS": 2000,
                        "executableFilename": "ElevationApi-2-2.0-LOCAL.jar"
                    }
                },
                immortals_root_deployment + "shared/modules/dfus/TakServerDataManager": {
                    "targetName": "TakServerDataManager",
                    "targetGroup": "mil.darpa.immortals.dfus",
                    "targetVersion": "2.0-LOCAL",
                    "deploymentTarget": "JAVA",
                    "deploymentTargetVersion": "1.7",
                    "rootProjectSubdirectory": "shared/modules/dfus/TakServerDataManager/",
                    "buildFile": "build.gradle",
                    "sourceSubdirectory": "src/main/java/",
                    "buildPlatform": "GRADLE",
                    "buildToolBuildParameters": [
                        "clean",
                        "build",
                        "-x",
                        "test"
                    ],
                    "testing": {
                        "buildToolValidationParameters": [
                            "--daemon",
                            "clean",
                            "validate"
                        ],
                        "testResultXmlSubdirectory": "build/test-results/test/",
                        "testCoverageReportXmlFileSubpath": "build/reports/jacoco/test/jacocoTestReport.xml"
                    },
                    "publishing": {
                        "groupId": "mil.darpa.immortals.dfus",
                        "artifactId": "TakServerDataManager",
                        "version": "2.0-LOCAL",
                        "buildToolPublishParameters": [
                            "publish"
                        ]
                    },
                    "execution": {
                        "executionStartSettleTimeMS": 2000,
                        "executableFilename": "TakServerDataManager-2.0-LOCAL.jar",
                        "executionPackageIdentifier": "mil.darpa.immortals.TakServerDataManager",
                        "executionMainMethodClasspath": "mil.darpa.immortals.TakServerDataManager.DatabaseManager"
                    },
                    "baseTestCaseReports": [
                        {
                            "testCaseTarget": "TakServerDataManager",
                            "testCaseIdentifier": "mil.darpa.immortals.dfus.TakServerDataManager.DataManagerTest.testCotEventsForConstantChannelJoin"
                        },
                        {
                            "testCaseTarget": "TakServerDataManager",
                            "testCaseIdentifier": "mil.darpa.immortals.dfus.TakServerDataManager.DataManagerTest.testCotEventsForConstantChannelJoin2"
                        },

                        {
                            "testCaseTarget": "TakServerDataManager",
                            "testCaseIdentifier": "mil.darpa.immortals.dfus.TakServerDataManager.DataManagerTest.testCotEventsForConstantTimeInterval"
                        },
                        {
                            "testCaseTarget": "TakServerDataManager",
                            "testCaseIdentifier": "mil.darpa.immortals.dfus.TakServerDataManager.DataManagerTest.testCotEventsForConstantCotType"
                        },
                        {
                            "testCaseTarget": "TakServerDataManager",
                            "testCaseIdentifier": "mil.darpa.immortals.dfus.TakServerDataManager.DataManagerTest.testCotEventsForConstantCompoundFilter"
                        },
                        {
                            "testCaseTarget": "TakServerDataManager",
                            "testCaseIdentifier": "mil.darpa.immortals.dfus.TakServerDataManager.DataManagerTest.testCotEventsForUidAndInterval"
                        },
                        {
                            "testCaseTarget": "TakServerDataManager",
                            "testCaseIdentifier": "mil.darpa.immortals.dfus.TakServerDataManager.DataManagerTest.testCotEventsOnChannelInRegion"
                        },
                        {
                            "testCaseTarget": "TakServerDataManager",
                            "testCaseIdentifier": "mil.darpa.immortals.dfus.TakServerDataManager.DataManagerTest.testCotEventsForConstantMixedJoin"
                        }
                    ]
                },
                immortals_root_deployment + "applications/server/Marti": {
                    "targetName": "Marti",
                    "targetGroup": "mil.darpa.immortals",
                    "targetVersion": "2.0-LOCAL",
                    "deploymentTarget": "JAVA",
                    "deploymentTargetVersion": "1.8",
                    "rootProjectSubdirectory": "",
                    "buildFile": "build.gradle",
                    "sourceSubdirectory": "src/",
                    "buildPlatform": "GRADLE",
                    "buildToolBuildParameters": [
                        "clean",
                        "build",
                        "-x",
                        "test"
                    ],
                    "testing": {
                        "buildToolValidationParameters": [
                            "--daemon",
                            "clean",
                            "test"
                        ],
                        "testResultXmlSubdirectory": "build/test-results/test/",
                        "testCoverageReportXmlFileSubpath": "build/reports/jacoco/test/jacocoTestReport.xml"
                    },
                    "execution": {
                        "executionStartSettleTimeMS": 2000,
                        "executableFilename": "Marti-immortals.jar",
                        "executionFileMap": {
                            "Marti-Config.json": "Marti-Config.json"
                        },
                        "executionPackageIdentifier": "com.bbn.marti.service",
                        "executionMainMethodClasspath": "com.bbn.marti.service.MartiMain"
                    },
                    "baseTestCaseReports": [
                        {
                            "testCaseTarget": "Marti",
                            "testCaseIdentifier": "com.bbn.marti.Tests.testCotByteBufferPipe"
                        },
                        {
                            "testCaseTarget": "Marti",
                            "testCaseIdentifier": "com.bbn.marti.Tests.testDbIntegration"
                        },
                        {
                            "testCaseTarget": "Marti",
                            "testCaseIdentifier": "com.bbn.marti.Tests.testElevationAccuracyEnhancement"
                        },
                        {
                            "testCaseTarget": "Marti",
                            "testCaseIdentifier": "com.bbn.marti.Tests.testImageSave"
                        },
                        {
                            "testCaseTarget": "Marti",
                            "testCaseIdentifier": "com.bbn.marti.Tests.testImageTransmission"
                        },
                        {
                            "testCaseTarget": "Marti",
                            "testCaseIdentifier": "com.bbn.marti.Tests.testLatestSaTransmission"
                        }
                    ],
                    "baseTestClassFileCoverage": {
                        "com.bbn.marti.Tests.testCotByteBufferPipe": [
                            {
                                "identifier": "com/bbn/marti/immortals/pipes/CotByteBufferPipe"
                            },
                            {
                                "identifier": "com/bbn/cot/CotEventContainer"
                            }
                        ],
                        "com.bbn.marti.Tests.testElevationAccuracyEnhancement": [
                            {
                                "identifier": "com/bbn/marti/immortals/pipes/CotByteBufferPipe"
                            },
                            {
                                "identifier": "com/bbn/cot/CotEventContainer"
                            }
                        ]
                    }
                },
                immortals_root_deployment + "applications/client/ATAKLite": {
                    "targetName": "ATAKLite",
                    "targetGroup": "mil.darpa.immortals",
                    "targetVersion": "2.0-LOCAL",
                    "deploymentTarget": "ANDROID",
                    "deploymentTargetVersion": "21",
                    "rootProjectSubdirectory": "",
                    "buildFile": "build.gradle",
                    "sourceSubdirectory": "src/",
                    "buildPlatform": "GRADLE",
                    "buildToolBuildParameters": [
                        "clean",
                        "build",
                        "-x",
                        "test"
                    ],
                    "testing": {
                        "buildToolValidationParameters": [
                            "clean",
                            "connectedAndroidTest"
                        ],
                        "testResultXmlSubdirectory": "build/outputs/androidTest-results/connected/"
                    },
                    "execution": {
                        "executionStartSettleTimeMS": 2000,
                        "executableFilename": "ATAKLite-debug.apk",
                        "executionPackageIdentifier": "com.bbn.ataklite",
                        "executionMainMethodClasspath": "com.bbn.ataklite.MainActivity"
                    }
                },
                immortals_root_deployment + "applications/examples/ThirdPartyLibAnalysisAndroidApp": {
                    "targetName": "ThirdPartyLibAnalysisAndroidApp",
                    "targetGroup": "mil.darpa.immortals",
                    "targetVersion": "2.0-LOCAL",
                    "deploymentTarget": "ANDROID",
                    "deploymentTargetVersion": "21",
                    "rootProjectSubdirectory": "",
                    "buildFile": "build.gradle",
                    "sourceSubdirectory": "src/main/java/",
                    "buildPlatform": "GRADLE",
                    "buildToolBuildParameters": [
                        "clean",
                        "build",
                        "-x",
                        "test"
                    ],
                    "testing": {
                        "buildToolValidationParameters": [
                            "clean",
                            "connectedAndroidTest"
                        ],
                        "testResultXmlSubdirectory": "build/outputs/androidTest-results/connected/"
                    },
                    "execution": {
                        "executionStartSettleTimeMS": 2000,
                        "executableFilename": "ThirdPartyLibAnalysisAndroidApp-debug.apk",
                        "executionFileMap": {},
                        "executionPackageIdentifier": "mil.darpa.immortals.examples"
                    }
                }
            }
        }

        def count_terminal_nodes(obj):
            rval = 0

            if isinstance(obj, Dict):
                for key in obj.keys():
                    val = obj[key]
                    rval = rval + count_terminal_nodes(val)

            elif isinstance(obj, List):
                for val in obj:
                    rval = rval + count_terminal_nodes(val)

            elif isinstance(obj, str) or isinstance(obj, int) or isinstance(obj, float):
                rval = rval + 1

            else:
                raise Exception("Unable to count terminal nodes in expected values!")

            return rval

        validated_nodes = 0

        for file_name in expected_json_values.keys():
            read_data = json.load(open(file_name))
            validation_data = expected_json_values[file_name]

            def check(read_object, validation_object, err_string):
                validated_nodes = 0
                if isinstance(validation_object, Dict):
                    assert isinstance(read_object, Dict), err_string + ': Not a Map!!'
                    for key in validation_object.keys():
                        assert key in read_object, err_string + 'Does not contain the key "' + key
                        validated_nodes = validated_nodes + check(read_object[key], validation_object[key],
                                                                  (err_string + '["' + key + '"]'))

                elif isinstance(validation_object, List):
                    assert isinstance(read_object, List), err_string + ': Not a List!'

                    for val1 in validation_object:
                        exception = None
                        success = False

                        for val2 in read_object:
                            try:
                                validated_nodes = validated_nodes + check(val2, val1, err_string)
                                success = True
                                break
                            except AssertionError as ae:
                                exception = ae

                        if not success:
                            raise exception

                elif (isinstance(validation_object, str) or isinstance(validation_object, int) or
                      isinstance(validation_data, float)):
                    assert validation_object == read_object, err_string + ': Value should be "' + str(
                        validation_object) + '", not "' + str(read_object) + '"!'
                    validated_nodes = validated_nodes + 1
                    print(err_string + ' == ' + str(validation_object))

                return validated_nodes

            validated_nodes = validated_nodes + check(read_data, validation_data, file_name)

        assert validated_nodes == count_terminal_nodes(
            expected_json_values), "Not all nodes appear to have been validated in the configuration files!"

    except Exception as e:
        raise e


class TestResults:

    def __init__(self):
        self.basic_installation = None
        self.basic_build = None
        self.full_installation = None
        self.buildsystem_predeploy_prep = None
        self.full_deployment = None
        self.full_deployment_validation = None
        self.baseline_marti = None
        self.api_smoke_test = None

    @staticmethod
    def _stringify_result(value: Optional[bool] = None):
        if value is None:
            return ' NOT RUN |'
        elif value:
            return '  PASS   |'
        else:
            return '  FAIL   |'

    def to_displayable_results(self) -> str:
        return 'RESULTS:' + \
               '\n| Basic Installation         |' + TestResults._stringify_result(self.basic_installation) + \
               '\n| Basic Build                |' + TestResults._stringify_result(self.basic_build) + \
               '\n| Full Installation          |' + TestResults._stringify_result(self.full_installation) + \
               '\n| Buildsystem Predeploy Prep |' + TestResults._stringify_result(self.full_installation) + \
               '\n| Full Deployment            |' + TestResults._stringify_result(self.full_deployment) + \
               '\n| Full Deployment Validation |' + TestResults._stringify_result(self.full_deployment_validation) + \
               '\n| Baseline Marti             |' + TestResults._stringify_result(self.baseline_marti) + \
               '\n| API Smoke Test             |' + TestResults._stringify_result(self.api_smoke_test)


class ImmortalsRootDeploymentTester:

    def __init__(self, dry_run: bool, clean_first: bool, build_in_current_project: bool,
                 use_existing_tmp_project: bool):
        self._dry_run = dry_run
        self._clean_first = clean_first
        self._results = TestResults()
        self._build_in_current_project = build_in_current_project
        self._use_existing_tmp_project = use_existing_tmp_project
        self._real_immortals_root = os.path.abspath(os.path.dirname(os.path.realpath(__file__)) + '/../../') + '/'

        self._immortals_root_basic = os.path.join(_staging_dir, 'immortals_root_basic') + '/'
        self._immortals_root_deployment = os.path.join(_staging_dir, 'immortals_root_deploy') + '/'
        self._test_root = self._immortals_root_basic

        self._create_build_dir(self._immortals_root_basic)

    def _create_build_dir(self, target_path: str):
        dir_exists = os.path.exists(target_path)
        if self._use_existing_tmp_project:
            if not dir_exists:
                raise Exception("ERROR: You cannot use an existing temporary project that does not exist!")

        else:
            if dir_exists:
                raise Exception(
                    'ERROR: You must manually remove the staging directory "' + target_path + '" To run this tool!')

            if not os.path.exists(_staging_dir):
                print('EXEC: `mkdir ' + _staging_dir + '`\n')
                os.mkdir(_staging_dir)

            cmd = ['rsync', '-a', '-v', '--progress', self._real_immortals_root, target_path]
            cwd = os.environ.get('HOME')
            print('IN DIRECTORY "' + cwd + '"')
            print('EXEC: `' + ' '.join(cmd) + '`\n')
            if not self._dry_run:
                rcode = subprocess.run(cmd, cwd=cwd).returncode
                assert rcode == 0, 'ERROR: Could not copy immortals root to staging directory!'

    def _run(self, identifier: str, args: List[str], cwd: str) -> Optional[bool]:
        print('IN DIRECTORY "' + cwd + '"')
        print('EXEC: `' + ' '.join(args) + '`\n')

        if self._dry_run:
            return None

        else:
            # noinspection PyBroadException
            try:
                cp = subprocess.run(args,
                                    cwd=cwd,
                                    stderr=sys.stderr,
                                    stdout=sys.stdout)

                assert cp.returncode == 0, \
                    'ERROR: Could not successfully execute "' + identifier + '"!'
                return cp.returncode == 0
            except Exception:
                return False

    def test_basic_environment_setup(self):
        # noinspection PyBroadException
        try:
            self._results.basic_installation = False
            self._results.basic_installation = self._run('setup_base_environment', ['sudo', 'apt-get', 'update'],
                                                         cwd=self._immortals_root_basic)

            if self._results.basic_installation:
                self._results.basic_installation = self._run(
                    'setup_base_environment',
                    ['sudo', 'apt-get', '-y', 'install', 'maven', 'openjdk-8-jdk-headless'],
                    cwd=self._immortals_root_basic)

        except Exception:
            self._results.basic_installation = False
        return self._results.basic_installation

    def test_basic_build(self):
        # noinspection PyBroadException
        try:
            # Perform a basic build
            if self._clean_first:
                self._run('perform_basic_build_clean', ['bash', 'gradlew', 'clean'], cwd=self._immortals_root_basic)

            self._results.basic_build = False
            self._results.basic_build = \
                self._run('perform_basic_build', ['bash', 'gradlew', 'build'], cwd=self._immortals_root_basic)
        except Exception:
            self._results.basic_build = False
        return self._results.basic_build

    def test_deployment_environment_setup(self):
        # noinspection PyBroadException
        try:
            # Perform the full environment setup if necessary
            if not (os.path.exists(os.path.join(os.environ.get('HOME'), '.immortalsrc'))):
                if os.path.exists(os.path.join(os.environ.get('HOME'), ".immortals")):
                    raise Exception(
                        '~/.immortals exists but not ~/.immortalsrc! Please copy your "immortralsrc" or' +
                        ' ".immortalsrc" from your local repository root to ~/.immortalsrc!')

                cwd = os.path.join(self._real_immortals_root, 'harness')

                self._results.full_installation = False
                self._results.full_installation = \
                    self._run('setup_full_environment', ['bash', 'prepare_setup.sh', '--installation-dir',
                                                         os.path.join(os.environ.get('HOME'), ".immortals")], cwd=cwd)
                if self._results.full_installation is not None and self._results.full_installation:
                    self._results.full_installation = self._run('setup_full_environment', ['bash', 'setup.sh'], cwd=cwd)

                if self._results.full_installation is not None and self._results.full_installation:
                    self._results.full_installation = \
                        self._run('setup_full_deployment',
                                  ['cp', 'immortalsrc', os.path.join(os.environ.get('HOME'), '.immortalsrc')], cwd=cwd)

        except Exception:
            self._results.full_installation = False
        return self._results.full_installation

    def perform_buildsystem_predeploy_prep(self) -> bool:
        try:
            self._results.buildsystem_predeploy_prep = False
            cwd = self._real_immortals_root


            # TODO: There must be a better way to get the dependency stuff out of the way without building it...
            if not self._run('predeploy_prep_dsl_setup', ['stack', 'setup'],
                             cwd=os.path.join(cwd, 'dsl', 'resource-dsl')):
                self._results.buildsystem_predeploy_prep = False
                return False

            if not self._run('predeploy_prep_dsl_build', ['stack', 'build'],
                             cwd=os.path.join(cwd, 'dsl', 'resource-dsl')):
                self._results.buildsystem_predeploy_prep = False
                return False


            self._results.buildsystem_predeploy_prep = True

        except Exception:
            self._results.buildsystem_predeploy_prep = False

        return self._results.buildsystem_predeploy_prep

    def test_deployment(self):
        # noinspection PyBroadException
        try:
            if self._build_in_current_project:
                cwd = self._real_immortals_root
            else:
                self._create_build_dir(self._immortals_root_deployment)
                cwd = self._immortals_root_deployment

            if self._clean_first:
                self._run('perform_basic_deployment_clean', ['bash', 'gradlew', 'clean'], cwd=cwd)

            self._results.full_deployment = False
            self._results.full_deployment = self._run('perform_deployment',
                                                      ['bash', 'gradlew', 'deploy'], cwd=cwd)

        except Exception:
            self._results.full_deployment = False

        if self._build_in_current_project:
            self._create_build_dir(self._immortals_root_deployment)

        self._test_root = self._immortals_root_deployment

        return self._results.full_deployment

    def validate_deployment(self):
        if not self._dry_run:
            try:
                if self._build_in_current_project:
                    validate_deployment(immortals_root_deployment=self._real_immortals_root)
                else:
                    validate_deployment(immortals_root_deployment=self._immortals_root_deployment)
            except Exception as e:
                self._results.full_deployment_validation = False
                raise e

            self._results.full_deployment_validation = True
        return self._results.full_deployment_validation

    def test_api_smoke(self):
        # noinspection PyBroadException
        try:
            self._results.api_smoke_test = False
            cwd = os.path.join(self._immortals_root_deployment, 'harness')
            self._results.api_smoke_test = self._run('api_smoke_test', ['bash', 'smoke.sh'], cwd=cwd)
        except Exception:
            self._results.api_smoke_test = False
        return self._results.api_smoke_test

    def test_baseline_marti(self):
        # noinspection PyBroadException
        try:
            self._results.baseline_marti = False
            cwd = os.path.join(self._test_root, 'applications/server/Marti')
            self._results.baseline_marti = self._run('baseline_marti',
                                                     ['../../../gradlew', '-Dmil.darpa.immortals.mock=true', 'clean',
                                                      'test'],
                                                     cwd=cwd)
        except Exception:
            self._results.baseline_marti = False
        return self._results.baseline_marti

    def get_displayable_results(self):
        return self._results.to_displayable_results()


def main():
    args = parser.parse_args()
    tester = None

    # noinspection PyBroadException
    try:
        if args.ll_mode:
            args.clean_first = False
            args.build_in_current_project = True
            args.use_existing_tmp_project = False

            args.skip_basic_build = False
            args.skip_full_deployment = False

            args.skip_deployment_env_setup = False
            args.buildsystem_predeploy_prep = False
            args.skip_basic_env_setup = False
            args.skip_deployment_validation = False
            args.skip_marti_baseline = False
            args.skip_api_smoketest = False
            args.fail_fast = True

        elif args.buildsystem_predeploy_setup:
            args.clean_first = False
            args.build_in_current_project = False
            args.use_existing_tmp_project = False

            args.skip_basic_build = False
            args.skip_full_deployment = True
            args.skip_deployment_env_setup = False
            args.buildsystem_predeploy_prep = True
            args.skip_basic_env_setup = False
            args.skip_deployment_validation = True
            args.skip_marti_baseline = True
            args.skip_api_smoketest = True
            args.fail_fast = True

        ff = args.fail_fast

        tester = ImmortalsRootDeploymentTester(dry_run=args.dry_run, clean_first=args.clean_first,
                                               build_in_current_project=args.build_in_current_project,
                                               use_existing_tmp_project=args.use_existing_tmp_project)

        execution_sequence = list()

        if not args.skip_basic_env_setup:
            execution_sequence.append(tester.test_basic_environment_setup)

        if not args.skip_basic_build:
            execution_sequence.append(tester.test_basic_build)

        if not args.skip_deployment_env_setup:
            execution_sequence.append(tester.test_deployment_environment_setup)

        if not args.skip_full_deployment:
            execution_sequence.append(tester.test_deployment)
        elif args.buildsystem_predeploy_prep:
            execution_sequence.append(tester.perform_buildsystem_predeploy_prep)

        if not args.skip_full_deployment and not args.skip_deployment_validation:
            execution_sequence.append(tester.validate_deployment)

        if not args.skip_marti_baseline:
            execution_sequence.append(tester.test_baseline_marti)

        if not args.skip_api_smoketest:
            execution_sequence.append(tester.test_api_smoke)

        for method in execution_sequence:
            if not method() and ff:
                break

    except Exception:
        traceback.print_exc()

    finally:
        if tester is not None:
            print(tester.get_displayable_results())


if __name__ == '__main__':
    main()
