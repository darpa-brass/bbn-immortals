import json
import os
import sys
from subprocess import Popen, PIPE

from pymmortals import immortalsglobals as ig
from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.testing_p1 import TestScenario
from pymmortals.testing.phase1.harness_listeners import TABehaviorValidator
from pymmortals.testing.phase1.ll_dummy_server import LLHarness


def smoke_test(args):
    das_process = None

    def start_das(test_scenario, overrides=None):
        """
        :type test_scenario: TestScenario
        :type overrides: dict[str, str] or None
        """
        global das_process

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

        if args.immortalsRoot is None:
            immortals_root = os.path.join('../')
        else:
            immortals_root = os.path.abspath(args.immortalsRoot)
            print(immortals_root)

        with open(os.path.join(get_configuration().resultRoot,
                               test_scenario.scenarioIdentifier + '-start-stdout.txt'), 'a') as stdout, \
                open(os.path.join(get_configuration().resultRoot,
                                  test_scenario.scenarioIdentifier + '-start-stderr.txt'), 'a') as stderr:
            os.putenv('IMMORTALS_OVERRIDES', target_override_filepath)

            das_process = \
                Popen(['python3.6', 'start_p1.py'],
                      cwd=os.path.abspath(os.path.join(immortals_root, 'harness')),
                      stdin=PIPE, stderr=stderr, stdout=stdout)

    def stop_das():
        global das_process
        das_process.terminate()
        das_process.wait(timeout=20)
        das_process.kill()

    # noinspection PyUnusedLocal
    def done_listener(scenario_identifier, session_identifier, next_test_scenario):
        """
        :type scenario_identifier: str
        :type session_identifier: str
        :type next_test_scenario: TestScenario
        """
        stop_das()

        if next_test_scenario is not None:
            start_das(next_test_scenario)
        else:
            ig.force_exit()

    test_listener = TABehaviorValidator(done_listener=done_listener, test_suite_identifier=args.mode)

    harness = LLHarness(host=get_configuration().testHarness.url,
                        port=get_configuration().testHarness.port,
                        logger=test_listener)

    start_das(test_scenario=test_listener.get_current_test_scenario())
    harness.start()


def _compute_bandwidth(client_count, image_report_rate_ms, expected_image_size):
    formula = "float(NumberOfClients*float(PLIReportRate/60.0)*3.2) + float(NumberOfClients*" + \
              "float(ImageReportRate/60.0)*float(float(float(float(DefaultImageSize*1000000)*24)/15.0)/1000)) + " + \
              "float(float(NumberOfClients-1)*NumberOfClients*float(PLIReportRate/60.0)*3.2)+ " + \
              "float(float(NumberOfClients-1)*NumberOfClients*float(ImageReportRate/60.0)*" + \
              "float(float(float(float(DefaultImageSize*1000000)*24)/15.0)/1000))"
    exec('NumberOfClients = ' + str(client_count))
    exec('PLIReportRate = 60')
    exec('ImageReportRate = ' + str(float(60000.0 / image_report_rate_ms)))
    exec('DefaultImageSize = ' + str(expected_image_size))
    return eval(formula)


def generate_test_scenarios():
    number_of_clients = [2, 6]

    image_report_rates = [1000, 60000]

    expected_image_sizes = [5, 3.75, 2.5, 1.25, 0.078125]

    test_scenarios = []

    test_scenarios_d = []

    for client_count in number_of_clients:
        for image_report_rate_ms in image_report_rates:
            for expected_image_size in expected_image_sizes:
                deployment_model_dict = {
                    'server': {},
                    'clients': [
                        {
                            # "imageBroadcastIntervalMS": "1980",
                            "latestSABroadcastIntervalMS": "60000",
                            # "count": 2,
                            "presentResources": [
                                "bluetooth",
                                "usb",
                                "internalGps",
                                "userInterface",
                                "gpsSatellites"
                            ],
                            "requiredProperties": []
                        }
                    ]
                }

                bandwidth = int(_compute_bandwidth(client_count=client_count,
                                                   image_report_rate_ms=image_report_rate_ms,
                                                   expected_image_size=expected_image_size) + 1)

                deployment_model_dict['server']['bandwidth'] = bandwidth

                deployment_model_dict['clients'][0]['imageBroadcastIntervalMS'] = str(image_report_rate_ms)
                deployment_model_dict['clients'][0]['count'] = client_count

                test_scenario_d = {
                    "scenarioIdentifier": str(client_count) + "c" + str(image_report_rate_ms) + 'irr' + str(
                        bandwidth) + 'bw',
                    "submissionFlow": "challenge",
                    "expectedResult": "valid",
                    "deploymentModel": deployment_model_dict,
                    "androidEmulatorQemuParameters": [],
                    "rootConfigurationModifications": {}
                }

                test_scenario = TestScenario.from_dict(test_scenario_d)
                test_scenarios.append(test_scenario)
                test_scenarios_d.append(test_scenario_d)

    print((json.dumps(test_scenarios_d, indent=4, separators=(',', ': '))))
