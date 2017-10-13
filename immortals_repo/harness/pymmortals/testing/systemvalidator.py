import json
import os
import sys
from subprocess import Popen, PIPE
from typing import Optional, Dict

from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.testing import Phase2TestScenario
from pymmortals import immortalsglobals as ig
from pymmortals.testing.harness_listeners import TABehaviorValidator
from pymmortals.testing.ll_dummy_server import LLHarness


class SystemValidator:
    def __init__(self, test_identifier: str, immortals_root: Optional[str] = None):
        self.test_identifier = test_identifier
        self.immortals_root = immortals_root
        self.das_process: Popen = None
        self.test_listener: TABehaviorValidator = None
        self.harness: LLHarness = None

    def _start_das(self, test_scenario: Phase2TestScenario, overrides: Optional[Dict[str, str]] = None):
        environment_config = get_configuration().to_dict(include_metadata=False)

        if test_scenario.rootConfigurationModifications is not None:
            for key in list(test_scenario.rootConfigurationModifications.keys()):
                environment_config[key] = test_scenario.rootConfigurationModifications[key]

        if overrides is not None:
            for key in list(overrides.keys()):
                environment_config[key] = overrides[key]

        target_override_filepath = os.path.join(get_configuration().runtimeRoot,
                                                test_scenario.scenarioIdentifier + '-environment.json')

        json.dump(environment_config, open(target_override_filepath, 'w'))

        if self.immortals_root is None:
            immortals_root = os.path.join('../')
        else:
            immortals_root = os.path.abspath(self.immortals_root)
            print(immortals_root)

        with open(os.path.join(get_configuration().resultRoot,
                               test_scenario.scenarioIdentifier + '-start-stdout.txt'), 'a') as stdout, \
                open(os.path.join(get_configuration().resultRoot,
                                  test_scenario.scenarioIdentifier + '-start-stderr.txt'), 'a') as stderr:
            os.putenv('IMMORTALS_OVERRIDES', target_override_filepath)

            self.das_process = \
                Popen(['python3.6', 'start.py'],
                      cwd=os.path.abspath(os.path.join(immortals_root, 'harness')),
                      stdin=PIPE, stderr=stderr, stdout=stdout)

    def _stop_das(self):
        self.das_process.terminate()
        self.das_process.wait(timeout=8)
        self.das_process.kill()

    def done_listener(self, next_test_scenario: Phase2TestScenario):
        self._stop_das()

        if next_test_scenario is not None:
            self._start_das(next_test_scenario)
        else:
            sys.exit()

    def start(self):
        self.test_listener = TABehaviorValidator(done_listener=self.done_listener,
                                                 test_suite_identifier=self.test_identifier)

        self.harness = LLHarness(host=get_configuration().testHarness.url,
                                 port=get_configuration().testHarness.port,
                                 logger=self.test_listener)

        ig.add_exit_handler(self.exit_handler)

        self._start_das(test_scenario=self.test_listener.get_current_test_scenario())
        self.harness.start()

    def exit_handler(self):
        self.harness.stop()
        self._stop_das()
