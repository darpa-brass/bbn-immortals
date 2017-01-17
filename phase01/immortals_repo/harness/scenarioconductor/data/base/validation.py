# noinspection PyClassHasNoInit
class RESOURCES:
    bluetooth = 'bluetooth'
    usb = 'usb'
    internal_gps = 'internalGps'
    user_interface = 'userInterface'
    gps_satellites = 'gpsSatellites'


# noinspection PyClassHasNoInit
class PROPERTIES:
    trustedLocations = 'trustedLocations'


# noinspection PyClassHasNoInit
class VALIDATORS:
    client_location_produce = 'client-location-produce'
    client_location_share = 'client-location-share'
    client_location_source_trusted = 'client-location-source-trusted'
    client_location_source_usb = 'client-location-source-usb'
    client_location_source_bluetooth = 'client-location-source-bluetooth'
    client_location_source_androidgps = 'client-location-source-androidgps'
    client_location_source_manual = 'client-location-source-manual'
    client_image_produce = 'client-image-produce'
    client_image_share = 'client-image-share'
    client_location_trusted = 'client-location-trusted'


"""
This indicates the tests that must have a specified state in order to pass an intent check.
"""
intent_satisfaction_tests = {
    # The baseline set of tests that must pass in order to pass the basic mission intent of sending LatestSA
    'baseline': frozenset([
        VALIDATORS.client_location_produce,
        VALIDATORS.client_location_share,
        VALIDATORS.client_image_produce,
        VALIDATORS.client_image_share
    ]),
    # Additional properties that can be specified to add new mission requirements to the scenario
    PROPERTIES.trustedLocations: frozenset([
        VALIDATORS.client_location_trusted
    ])
}

# Lists the mandatory requirements to pass each resource usage validator
resource_test_dependencies = {
    VALIDATORS.client_location_source_trusted: frozenset([RESOURCES.gps_satellites, RESOURCES.usb]),
    VALIDATORS.client_location_source_usb: frozenset([RESOURCES.gps_satellites, RESOURCES.usb]),
    VALIDATORS.client_location_source_bluetooth: frozenset([RESOURCES.gps_satellites, RESOURCES.bluetooth]),
    VALIDATORS.client_location_source_androidgps: frozenset([RESOURCES.gps_satellites, RESOURCES.internal_gps]),
    VALIDATORS.client_location_source_manual: frozenset([RESOURCES.user_interface])
}

"""
Test sets in which if one passes, the others must fail
"""
mutually_exclusive_validator_sets = {
    'client-location-source': frozenset([
        VALIDATORS.client_location_source_trusted,
        VALIDATORS.client_location_source_usb,
        VALIDATORS.client_location_source_bluetooth,
        VALIDATORS.client_location_source_androidgps,
        VALIDATORS.client_location_source_manual
    ])
}


# """
# This indicates the possible behavioral tests that could satisfy each intent test
# """
# intent_fulfillment_options = {
#     VALIDATORS.client_location_produce: frozenset([
#         VALIDATORS.client_location_source_trusted,
#         VALIDATORS.client_location_source_usb,
#         VALIDATORS.client_location_source_bluetooth,
#         VALIDATORS.client_location_source_androidgps,
#         VALIDATORS.client_location_source_manual
#     ]),
#     VALIDATORS.client_location_share: frozenset([
#         VALIDATORS.client_location_source_trusted,
#         VALIDATORS.client_location_source_usb,
#         VALIDATORS.client_location_source_bluetooth,
#         VALIDATORS.client_location_source_androidgps,
#         VALIDATORS.client_location_source_manual
#     ]),
#     VALIDATORS.client_image_produce: frozenset([
#         VALIDATORS.client_location_source_trusted,
#         VALIDATORS.client_location_source_usb,
#         VALIDATORS.client_location_source_bluetooth,
#         VALIDATORS.client_location_source_androidgps,
#         VALIDATORS.client_location_source_manual
#     ]),
#     VALIDATORS.client_image_share: frozenset([
#         VALIDATORS.client_location_source_trusted,
#         VALIDATORS.client_location_source_usb,
#         VALIDATORS.client_location_source_bluetooth,
#         VALIDATORS.client_location_source_androidgps,
#         VALIDATORS.client_location_source_manual
#     ]),
#     VALIDATORS.client_location_source_trusted: frozenset([
#         VALIDATORS.client_location_source_trusted
#     ])
# }
