from typing import Dict, Type

from pymmortals import triples_helper
from pymmortals.datatypes.interfaces import ValidatorInterface
from pymmortals.datatypes.validation import intent_satisfaction_tests, resource_test_dependencies
from pymmortals.generated.com.securboration.immortals.ontology.cp.gmeinterchangeformat import GmeInterchangeFormat


def calculate_validators(gif: GmeInterchangeFormat) -> Dict[Type[ValidatorInterface], bool]:
    # Get the requirements and available resources from the configuration
    # Assuming all clients are the same for this iteration, so just taking the first one
    requirements = triples_helper.get_mission_properties(gif)
    available_resources = triples_helper.get_client_resources(gif)

    expected_validator_states: Dict[Type[ValidatorInterface], bool] = {}

    # Add base intent tests that must always pass
    for validator in intent_satisfaction_tests['baseline']:
        expected_validator_states[validator] = True

    # Add the additional intent tests based on requirements
    # TODO: Assuming no conflicts, which works at this moment
    for req in requirements:
        for validator in intent_satisfaction_tests[req]:
            expected_validator_states[validator] = True

    """For each validator with resource dependencies, if all of its resources are not available, mark it as must fail"""

    for validator, required_resources in resource_test_dependencies.items():
        must_fail = False

        for resource in required_resources:
            if resource not in available_resources:
                must_fail = True

        if must_fail:
            expected_validator_states[validator] = False

    return expected_validator_states


def calculate_validator_identifiers(gif: GmeInterchangeFormat) -> Dict[str, bool]:
    validators = calculate_validators(gif=gif)
    rval: Dict[str, bool] = dict()

    for key, value in validators.items():
        rval[key.identifier] = value

    return rval
