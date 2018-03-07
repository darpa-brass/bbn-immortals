#!/usr/bin/env python3
import argparse
import os
import subprocess
import sys
import traceback
from typing import List, Optional

_staging_dir = '/tmp/immortals_deployment_test'

parser = argparse.ArgumentParser(
    description="Test The IMMoRTALS Deployment. By default, everything is tested. Installation of dependencies will be executed and validated if known prerequisites are not present.")

parser.add_argument('-b', '--skip-basic', action='store_true', default=False,
                    help='Skips the basic project build')

parser.add_argument('-f', '--skip-full-deployment', action='store_true', default=False,
                    help='Skips the project deployment')

parser.add_argument('-v', '--skip-deployment-validation', action='store_true', default=False,
                    help='Skips the project deployment validation')

parser.add_argument('-a', '--skip-api-smoketest', action='store_true', default=False,
                    help='Skips the api smoke test')

parser.add_argument('-m', '--skip-marti-baseline', action='store_true', default=False,
                    help='Skips the baseline Marti validation tests')

parser.add_argument('--dry-run', action='store_true', default=False,
                    help='Performs a dry run, printing out the commands but not executing them.')

parser.add_argument('--clean-first', action='store_true', default=False,
                    help='Executes the clean command on builds before building them')

parser.add_argument('--use-existing-tmp-project', action='store_true', default=False,
                    help="Uses the existing temporary project instead of forcing you to remove it")


class TestResults:

    def __init__(self):
        self.basic_installation = None
        self.basic_build = None
        self.full_installation = None
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
               '\n| Full Deployment            |' + TestResults._stringify_result(self.full_deployment) + \
               '\n| Full Deployment Validation |' + TestResults._stringify_result(self.full_deployment_validation) + \
               '\n| Baseline Marti             |' + TestResults._stringify_result(self.baseline_marti) + \
               '\n| API Smoke Test             |' + TestResults._stringify_result(self.api_smoke_test)


class ImmortalsRootDeploymentTester:

    def __init__(self, dry_run: bool, clean_first: bool, use_existing_tmp_project: bool):
        self._dry_run = dry_run
        self._clean_first = clean_first
        self._results = TestResults()

        self._immortals_root_basic = os.path.join(_staging_dir, 'immortals_root_basic') + '/'
        self._immortals_root_deployment = os.path.join(_staging_dir, 'immortals_root_deploy') + '/'

        if use_existing_tmp_project:
            if not os.path.exists(self._immortals_root_basic) or not os.path.exists(self._immortals_root_deployment):
                raise Exception("ERROR: You cannot use an existing temporary project that does not exist!")

        else:
            if os.path.exists(_staging_dir):
                raise Exception('ERROR: You must manually remove the staging directory "'
                                + _staging_dir + '" To run this tool!')

            print('EXEC: `mkdir ' + _staging_dir + '`\n')
            os.mkdir(_staging_dir)

            real_immortals_root = os.path.abspath(os.path.dirname(os.path.realpath(__file__)) + '/../../') + '/'
            self._std_path = _staging_dir

            cmd = ['rsync', '-a', '-v', '--progress', real_immortals_root, self._immortals_root_basic]
            cwd = os.environ.get('HOME')
            print('IN DIRECTORY "' + cwd + '"')
            print('EXEC: `' + ' '.join(cmd) + '`\n')
            if not self._dry_run:
                rcode = subprocess.run(cmd, cwd=cwd).returncode
                assert rcode == 0, 'ERROR: Could not copy immortals root to staging directory!'

            cmd = ['rsync', '-a', '-v', '--progress', real_immortals_root, self._immortals_root_deployment]
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

    def test_basic(self):
        # If necessary, setup the base environment
        try:
            if not ('JAVA_HOME' in os.environ and subprocess.run(['which', 'mvn'],
                                                                 stdout=subprocess.DEVNULL).returncode == 0):
                self._results.basic_installation = False
                self._results.basic_installation = self._run(
                    'setup_base_environment',
                    ['sudo', 'apt-get', '-y', 'install', 'maven', 'openjdk-8-jdk-headless'],
                    cwd=self._immortals_root_basic)
        except Exception:
            self._results.basic_installation = False
            return

        try:
            # Perform a basic build
            if self._clean_first:
                self._run('perform_basic_build_clean', ['bash', 'gradlew', 'clean'], cwd=self._immortals_root_basic)

            self._results.basic_build = False
            self._results.basic_build = \
                self._run('perform_basic_build', ['bash', 'gradlew', 'build'], cwd=self._immortals_root_basic)
        except Exception:
            self._results.basic_build = False

    def test_deployment(self):
        try:
            # Perform the full environment setup if necessary
            if not (os.path.exists(os.path.join(os.environ.get('HOME'), '.immortalsrc'))):
                if os.path.exists(os.path.join(os.environ.get('HOME'), ".immortals")):
                    raise Exception(
                        '~/.immortals exists but not ~/.immortalsrc! Please copy your "immortralsrc" or' +
                        ' ".immortalsrc" from your local repository root to ~/.immortalsrc!')

                cwd = os.path.join(self._immortals_root_deployment, 'harness')

                self._results.full_installation = False
                self._results.full_installation = self._run('setup_full_environment', ['bash', 'prepare_setup.sh'],
                                                            cwd=cwd)
                if self._results.full_installation is not None and self._results.full_installation:
                    self._results.full_installation = self._run('setup_full_environment', ['bash', 'setup.sh'], cwd=cwd)

                if self._results.full_installation is not None and self._results.full_installation:
                    self._results.full_installation = \
                        self._run('setup_full_deployment',
                                  ['cp', 'immortalsrc', os.path.join(os.environ.get('HOME'), '.immortalsrc')], cwd=cwd)

        except Exception:
            self._results.full_installation = False
            return

        try:
            # Perform a basic build
            if self._clean_first:
                self._run('perform_basic_deployment_clean', ['bash', 'gradlew', 'clean'],
                          cwd=self._immortals_root_deployment)

            self._results.full_deployment = False
            self._results.full_deployment = self._run('perform_deployment', ['bash', 'gradlew', 'deploy'],
                                                      cwd=self._immortals_root_deployment)
        except Exception:
            self._results.full_deployment = False

    def validate_deployment(self):
        if not self._dry_run:
            try:
                # Make sure an in-use DFU has been analyzed
                expected_artifacts = {
                    "analysis": [
                        'knowledge-repo/vocabulary/ontology-static/ontology/_ANALYSIS/_krgp/TakServerDataManager/',
                        'knowledge-repo/vocabulary/ontology-static/ontology/_ANALYSIS/_krgp/ElevationApi-2',
                        'knowledge-repo/vocabulary/ontology-static/ontology/_ANALYSIS/_krgp/ATAKLite',
                        'knowledge-repo/vocabulary/ontology-static/ontology/_ANALYSIS/_krgp/Marti'
                    ],
                    "build": [
                        'das/das-launcher-2.0-LOCAL.jar',
                        'applications/server/Marti/Marti-immortals.jar',
                        'applications/client/ATAKLite/ATAKLite-debug.apk'
                    ],
                    "database": [
                        'castor/takserver/source.csv',
                        'castor/takserver/cot_event.csv',
                        'castor/takserver/cot_event_position.csv',
                        'castor/takserver/master_cot_event.csv',
                        'database/server/source.csv',
                        'database/server/cot_event.csv',
                        'database/server/cot_event_position.csv',
                        'database/server/master_cot_event.csv'

                    ]
                }

                for label in expected_artifacts.keys():
                    for path in expected_artifacts.get(label):
                        assert os.path.exists(os.path.join(self._immortals_root_deployment, path)), \
                            'ERROR: ' + label + ' artifact "' + path + '" was not found!'

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
                                cmd) + '` had a non-zero return code of ' + rcode + '!'

            except Exception as e:
                self._results.full_deployment_validation = False
                raise e
            
            self._results.full_deployment_validation = True

    def test_api_smoke(self):
        try:
            cwd = os.path.join(self._immortals_root_deployment, 'harness')
            self._results.api_smoke_test = self._run('api_smoke_test', ['bash', 'smoke.sh'], cwd=cwd)
        except Exception:
            self._results.api_smoke_test = False

    def test_baseline_marti(self):
        try:
            cwd = os.path.join(self._immortals_root_basic, 'applications/server/Marti')
            self._results.baseline_marti = self._run('baseline_marti', ['../../../gradlew', 'clean', 'validate'],
                                                     cwd=cwd)
        except Exception:
            self._results.api_smoke_test = False

    def get_displayable_results(self):
        return self._results.to_displayable_results()


def main():
    args = parser.parse_args()
    tester = ImmortalsRootDeploymentTester(dry_run=args.dry_run, clean_first=args.clean_first,
                                           use_existing_tmp_project=args.use_existing_tmp_project)

    # noinspection PyBroadException
    try:
        if not args.skip_basic:
            tester.test_basic()

        if not args.skip_full_deployment:
            tester.test_deployment()

        if not args.skip_deployment_validation:
            tester.validate_deployment()

        if not args.skip_marti_baseline:
            tester.test_baseline_marti()

        if not args.skip_api_smoketest:
            tester.test_api_smoke()

    except Exception:
        traceback.print_exc()

    finally:
        print(tester.get_displayable_results())


if __name__ == '__main__':
    main()
