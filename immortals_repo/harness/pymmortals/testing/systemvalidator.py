import os
import sys
from subprocess import Popen, PIPE
from typing import Optional, Dict

from pymmortals import immortalsglobals as ig
from pymmortals.datatypes import root_configuration
# from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.testing import Phase2TestScenario
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

    def _start_das(self, test_scenario: Phase2TestScenario, overrides: Optional[Dict[str, str]] = None):
        # target_override_filepath = _construct_override_file(test_scenario=test_scenario, overrides=overrides)

        if self.immortals_root is None:
            immortals_root = os.path.join('../')
        else:
            immortals_root = os.path.abspath(self.immortals_root)
            print(immortals_root)

        self.das_stdout = open(os.path.join(get_configuration().globals.globalLogDirectory,
                                            test_scenario.scenarioIdentifier + '-start-stdout.txt'), 'a')
        self.das_stderr = open(os.path.join(get_configuration().globals.globalLogDirectory,
                                            test_scenario.scenarioIdentifier + '-start-stderr.txt'), 'a')
        try:
            self.das_process = Popen([
                'bash',
                os.path.join(root_configuration.get_configuration().immortalsRoot, 'das/start.sh'), "-v", "DEBUG"],
                cwd=os.path.abspath(os.path.join(immortals_root, 'harness')),
                stdin=PIPE,
                stderr=self.das_stderr,
                stdout=self.das_stdout)
        except ResourceWarning as r:
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

        if next_test_scenario is not None:
            self._start_das(next_test_scenario)
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

        self._start_das(test_scenario=initial_test_scenario)

        self.harness.start()
        ig.force_exit()
        sys.exit(ig.get_exit_code())

    def exit_handler(self):
        self.harness.stop()
        self._stop_das()
