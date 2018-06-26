import os
import shutil
import subprocess
import sys
from subprocess import Popen, PIPE
from typing import Optional

from pymmortals import immortalsglobals as ig
from pymmortals.datatypes import root_configuration
from pymmortals.datatypes.testing import Phase2TestScenario, Phase2SubmissionFlow, PerturbationScenario
from pymmortals.immortalsglobals import get_configuration
from pymmortals.testing.harness_listeners import TABehaviorValidator
from pymmortals.testing.ll_dummy_server import LLHarness


class SystemValidator:

    def __init__(self, immortals_root: Optional[str] = None):
        self.immortals_root = immortals_root
        self.das_process = None  # type: Popen
        self.das_stdout = None
        self.das_stderr = None
        self.test_listener = None  # type: TABehaviorValidator
        self.harness = None  # type: LLHarness
        self.test_suite_identifier = None  # type: str
        self.test_identifier = None  # type: str
        self._current_test_scenario = None  # type: Phase2TestScenario

    def _start_das(self):
        # target_override_filepath = _construct_override_file(test_scenario=test_scenario, overrides=overrides)

        if self.immortals_root is None:
            immortals_root = os.path.join('../')
        else:
            immortals_root = os.path.abspath(self.immortals_root)
            print(immortals_root)

        self.das_stdout = open(os.path.join(get_configuration().globals.globalLogDirectory,
                                            self._current_test_scenario.scenarioIdentifier + '-start-stdout.txt'), 'a')
        self.das_stderr = open(os.path.join(get_configuration().globals.globalLogDirectory,
                                            self._current_test_scenario.scenarioIdentifier + '-start-stderr.txt'), 'a')
        try:
            self.das_process = Popen([
                'bash',
                os.path.join(root_configuration.get_configuration().immortalsRoot, 'das/start.sh'), "-v", "DEBUG"],
                cwd=os.path.abspath(os.path.join(immortals_root, 'harness')),
                stdin=PIPE,
                stderr=self.das_stderr,
                stdout=self.das_stdout)
        except ResourceWarning:
            # Ignore this...
            pass

    def _stop_das(self):
        self.das_process.terminate()
        self.das_process.wait(timeout=10)
        self.das_process.kill()

        if not self.das_stdout.closed:
            self.das_stdout.flush()
            self.das_stdout.close()
        if not self.das_stderr.closed:
            self.das_stderr.flush()
            self.das_stderr.close()

    def done_listener(self, next_test_scenario: Phase2TestScenario):
        self._stop_das()

        if not get_configuration().debug.useMockDas and \
                self._current_test_scenario.submissionFlow is not Phase2SubmissionFlow.BaselineA:
            ir = get_configuration().globals.immortalsRoot
            results = subprocess.run(['bash', 'setup.sh', '--unattended'],
                                     cwd=os.path.join(ir, 'database/server'),
                                     stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
            results.check_returncode()

            if self._current_test_scenario.perturbationScenario == PerturbationScenario.P2CP1DatabaseSchema and \
                    (self._current_test_scenario.submissionFlow == Phase2SubmissionFlow.BaselineB or
                     self._current_test_scenario.submissionFlow == Phase2SubmissionFlow.Challenge):
                cwd = os.path.join(ir, 'das/das-service')
                results = subprocess.run(['java', '-jar', os.path.join(cwd, 'das.jar'), '--analyze'],
                                         cwd=cwd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
                results.check_returncode()

            gradle_original = os.path.join(ir, 'settings.gradle.original')
            if os.path.exists(gradle_original):
                shutil.copy2(gradle_original, os.path.join(ir, 'settings.gradle'))

        if next_test_scenario is not None:
            self.harness.reset_sequence_counter()
            self._current_test_scenario = next_test_scenario
            self._start_das()

        else:
            ig.force_exit()

    def start(self, test_suite_identifier: str, test_identifier: str = None):
        """
        :param test_suite_identifier: The test suite to execute
        :param test_identifier: THe test to execute from the test suite. If None, all test suite tests are run
        """
        self.harness = LLHarness(host=get_configuration().testHarness.url,
                                 port=get_configuration().testHarness.port,
                                 done_listener=self.done_listener)

        initial_test_scenario = self.harness.load_test(test_suite_identifier=test_suite_identifier,
                                                       test_identifier=test_identifier)

        ig.add_exit_handler(self.exit_handler)

        self._current_test_scenario = initial_test_scenario
        self._start_das()

        self.harness.start()
        ig.force_exit()
        sys.exit(ig.get_exit_code())

    def exit_handler(self):
        self.harness.stop()
        self._stop_das()
