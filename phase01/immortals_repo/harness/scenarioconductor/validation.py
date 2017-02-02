from data.base.scenarioapiconfiguration import ScenarioConductorConfiguration

from data.base.validation import *


class ValidatorConfiguration:
    """
    :type expected_validator_states: dict[str,bool]
    """

    def __init__(self):
        self.expected_validator_states = {}


def calculate_validators(scenario_configuration):
    """
    :type scenario_configuration: ScenarioConductorConfiguration
    :rtype dict[str,bool]
    """

    # Get the requirements and available resources from the configuration
    # Assuming all clients are the same for this iteration, so just taking the first one
    requirements = scenario_configuration.clients[0].requiredProperties
    available_resources = scenario_configuration.clients[0].presentResources

    # vc = ValidatorConfiguration()
    expected_validator_states = {}

    # Add base intent tests that must always pass
    for validator_identifier in intent_satisfaction_tests['baseline']:
        expected_validator_states[validator_identifier] = True

    # Add the additional intent tests based on requirements
    # TODO: Assuming no conflicts, which works at this moment
    for req in requirements:
        for validator_identifier in intent_satisfaction_tests[req]:
            expected_validator_states[validator_identifier] = True

    """For each validator with resource dependencies, if all of its resources are not available, mark it as must fail"""

    for test_identifier in resource_test_dependencies:
        must_fail = False

        for resource in resource_test_dependencies[test_identifier]:
            if resource not in available_resources:
                must_fail = True

        if must_fail:
            expected_validator_states[test_identifier] = False

    return expected_validator_states

    # class ValidatorConfiguration:
    #     """
    #     :type primary_intent_validators: set[str]
    #     :type passable_behavior_validators: set[str]
    #     :type resultant_validator_possibilities: list[dict[str,bool]]
    #     """
    #
    #     def __init__(self):
    #         self.primary_intent_validators = set()
    #         self.passable_behavior_validators = set()
    #         self.resultant_validator_possibilities = []
    #
    # def calculate_validator_configuration(scenario_configuration):
    #     """
    #     :type scenario_configuration: ScenarioConductorConfiguration
    #     """
    #
    #     # Get the requirements and available resources from the configuration
    #     requirements = scenario_configuration.clients[0].requiredProperties
    #     available_resources = scenario_configuration.clients[0].presentResources
    #
    #     vc = ValidatorConfiguration()
    #
    #     """Add the intent validators"""
    #
    #     # Add base intent tests that must always pass
    #     vc.primary_intent_validators = vc.primary_intent_validators.union(intent_satisfaction_tests['baseline'])
    #
    #     # Add the additional intent tests based on requirements
    #     # TODO: Assuming no conflicts, which works at this moment
    #     for req in requirements:
    #         vc.primary_intent_validators = vc.primary_intent_validators.union(intent_satisfaction_tests[req])
    #
    #     """Add the behavior validators that may pass given the configuration"""
    #
    #     # For each available resource test, if the requirements are met, add it to the list of possible validators
    #     # utilized
    #     for validator in resource_test_dependencies.keys():
    #         has_resources = True
    #         for needed_resource in resource_test_dependencies[validator]:
    #             if needed_resource not in available_resources:
    #                 has_resources = False
    #
    #         if has_resources:
    #             vc.passable_behavior_validators.add(validator)
    #
    #     """Match the behavior validators with the intent validators"""
    #
    #
    #     # For each intent validator that must pass, take the intersection of the dependencies that fulfill them all
    #     intent_validator_dependencies = None
    #     for primary_intent_validator in vc.primary_intent_validators:
    #         if intent_validator_dependencies is None:
    #             intent_validator_dependencies = intent_fulfillment_options[primary_intent_validator]
    #         else:
    #             intent_validator_dependencies = intent_validator_dependencies.intersection(
    #                 intent_fulfillment_options[primary_intent_validator])
    #
    #     # For all dependencies that must pass
    #     for v in intent_validator_dependencies:
    #
    #         # Take the intersection of them with the validators that can pass given the environment configuration
    #         passable_intent_validator_dependencies = vc.passable_behavior_validators.intersection([v])
    #
    #         # And if a passing selection exists, take the union of them with the primary intent validators
    #         if len(passable_intent_validator_dependencies) > 0:
    #             passing_test_set = passable_intent_validator_dependencies.union(vc.primary_intent_validators)
    #
    #             # Gather the tests that must fail due to mutual exclusion
    #             failing_test_set = None
    #             for passing_test in passing_test_set:
    #                 for mutually_exclusive_set in mutually_exclusive_validator_sets:
    #                     if passing_test in mutually_exclusive_set:
    #                         if failing_test_set is None:
    #                             failing_test_set = mutually_exclusive_set.difference([passing_test])
    #                         else:
    #                             failing_test_set.union(mutually_exclusive_set.difference([passing_test]))
    #
    #             # Add the passing tests to a dictionary with pass=true and the faling tests with pass=false
    #             valid_result_set = {k: True for k in passing_test_set}
    #             valid_result_set.update({k: False for k in failing_test_set})
    #             vc.resultant_validator_possibilities.append(valid_result_set)
    #
    #     return vc
