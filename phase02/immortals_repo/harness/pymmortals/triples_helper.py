import logging
from typing import List

from pymmortals.datatypes.deployment_model import LLP1Input
from pymmortals.datatypes.intermediary.challengeproblem import ChallengeProblem
from pymmortals.datatypes.intermediary.deploymentmodelproperty import \
    DeploymentModelProperty
from pymmortals.datatypes.intermediary.deploymentmodelresource import \
    DeploymentModelResource
from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.scenariorunnerconfiguration import ScenarioRunnerConfiguration, JavaApplicationConfig, \
    ApplicationConfig, AndroidApplicationConfig, JsonFileOverrideConfiguration, FileCopyConfiguration
from pymmortals.generated.com.securboration.immortals.ontology.android.androidplatform import AndroidPlatform
from pymmortals.generated.com.securboration.immortals.ontology.connectivity.bandwidthkilobitspersecond import \
    BandwidthKiloBitsPerSecond
from pymmortals.generated.com.securboration.immortals.ontology.connectivity.imagereportrate import ImageReportRate
from pymmortals.generated.com.securboration.immortals.ontology.connectivity.numclients import NumClients
from pymmortals.generated.com.securboration.immortals.ontology.connectivity.plireportrate import PliReportRate
from pymmortals.generated.com.securboration.immortals.ontology.constraint.propertycriteriontype import \
    PropertyCriterionType
from pymmortals.generated.com.securboration.immortals.ontology.constraint.valuecriteriontype import ValueCriterionType
from pymmortals.generated.com.securboration.immortals.ontology.core.resource import Resource
from pymmortals.generated.com.securboration.immortals.ontology.cp.functionalityspec import FunctionalitySpec
from pymmortals.generated.com.securboration.immortals.ontology.cp.gmeinterchangeformat import GmeInterchangeFormat
from pymmortals.generated.com.securboration.immortals.ontology.cp.missionspec import MissionSpec
from pymmortals.generated.com.securboration.immortals.ontology.functionality.functionality import Functionality
from pymmortals.generated.com.securboration.immortals.ontology.functionality.imagescaling.numberofpixels import \
    NumberOfPixels
from pymmortals.generated.com.securboration.immortals.ontology.metrics.measurementtype import MeasurementType
from pymmortals.generated.com.securboration.immortals.ontology.metrics.metric import Metric
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.propertyconstraint import \
    PropertyConstraint
from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.bluetoothresource import BluetoothResource
from pymmortals.generated.com.securboration.immortals.ontology.resources.executionplatform import ExecutionPlatform
from pymmortals.generated.com.securboration.immortals.ontology.resources.gps.gpsreceiverembedded import \
    GpsReceiverEmbedded
from pymmortals.generated.com.securboration.immortals.ontology.resources.gps.gpssatelliteconstellation import \
    GpsSatelliteConstellation
from pymmortals.generated.com.securboration.immortals.ontology.resources.gps.properties.trustedproperty import \
    TrustedProperty
from pymmortals.generated.com.securboration.immortals.ontology.resources.network.networkconnection import \
    NetworkConnection
from pymmortals.generated.com.securboration.immortals.ontology.resources.usbresource import UsbResource
from pymmortals.generated.com.securboration.immortals.ontology.resources.userinterface import UserInterface
from pymmortals.generated.com.securboration.immortals.ontology.server.serverplatform import ServerPlatform
from pymmortals.resources import resourcemanager

_logger = logging.getLogger('triples_helper')


def get_android_client_count(gif: GmeInterchangeFormat) -> int:
    return [int(ms.rightValue.value) for ms in gif.missionSpec if (
        ms.assertionCriterion == ValueCriterionType.VALUE_GREATER_THAN_INCLUSIVE and
        ms.rightValue.applicableResourceType == ServerPlatform and
        ms.rightValue.linkId == 'NumberOfClients' and
        ms.rightValue.measurementType.correspondingProperty == NumClients and
        ms.rightValue.unit == 'count')][0]


def get_pli_rate_ms(gif: GmeInterchangeFormat) -> int:
    return int(float(60000 / ([int(float(ms.rightValue.value)) for ms in gif.missionSpec if (
        ms.assertionCriterion == ValueCriterionType.VALUE_GREATER_THAN_INCLUSIVE and
        ms.rightValue.applicableResourceType == AndroidPlatform and
        ms.rightValue.linkId == 'PLIReportRate' and
        ms.rightValue.measurementType.correspondingProperty == PliReportRate and
        ms.rightValue.unit == 'messages/minute')][0])))


def get_image_rate_ms(gif: GmeInterchangeFormat) -> int:
    return int(float(60000 / ([int(float(ms.rightValue.value)) for ms in gif.missionSpec if (
        ms.assertionCriterion == ValueCriterionType.VALUE_GREATER_THAN_INCLUSIVE and
        ms.rightValue.applicableResourceType == AndroidPlatform and
        ms.rightValue.linkId == 'ImageReportRate' and
        ms.rightValue.measurementType.correspondingProperty == ImageReportRate and
        ms.rightValue.unit == 'images/minute')][0])))


def get_bandwidth_constraint_kbit_per_second(gif: GmeInterchangeFormat) -> int:
    return [int(float(ms.rightValue.value)) for ms in gif.missionSpec if (
        ms.assertionCriterion == ValueCriterionType.VALUE_LESS_THAN_INCLUSIVE and
        ms.rightValue.applicableResourceType == NetworkConnection and
        ms.rightValue.linkId == 'TotalAvailableServerBandwidth' and
        ms.rightValue.measurementType.correspondingProperty == BandwidthKiloBitsPerSecond and
        ms.rightValue.unit == 'kb/s')][0]


def get_client_resources(gif: GmeInterchangeFormat) -> List[DeploymentModelResource]:
    resources = [k for k in gif.availableResources if not isinstance(k, ExecutionPlatform)] \
                + [k for k in gif.availableResources if isinstance(k, AndroidPlatform)][0].platformResources
    return get_deployment_model_resources(resource_list=resources)


def get_mission_properties(gif: GmeInterchangeFormat) -> List[DeploymentModelProperty]:
    properties: List[Property] = list()

    for fs in gif.functionalitySpec:
        if fs.propertyConstraint is not None:
            for pc in fs.propertyConstraint:
                if pc.constrainedProperty is not None:
                    for cp in pc.constrainedProperty:
                        properties.append(cp)

    return get_deployment_model_properties(property_list=properties)


def get_deployment_model_properties(property_list: List[Property]) -> List[DeploymentModelProperty]:
    rval = list()

    for p in property_list:
        if isinstance(p, TrustedProperty):
            rval.append(DeploymentModelProperty.trustedLocations)
        else:
            _logger.warning('Unused property "' + p.__module__ + '.' + p.__name__ + '".')

    return rval


def get_deployment_model_resources(resource_list: List[Resource]) -> List[DeploymentModelResource]:
    rval: List[DeploymentModelResource] = list()

    for r in resource_list:
        if isinstance(r, GpsReceiverEmbedded):
            rval.append(DeploymentModelResource.internalGps)

        elif isinstance(r, UserInterface):
            rval.append(DeploymentModelResource.userInterface)

        elif isinstance(r, UsbResource):
            rval.append(DeploymentModelResource.usb)

        elif isinstance(r, BluetoothResource):
            rval.append(DeploymentModelResource.bluetooth)

        elif isinstance(r, GpsSatelliteConstellation):
            rval.append(DeploymentModelResource.gpsSatellites)

        else:
            # _logger.warning('Unused resource "' + r.__module__ + '.' + r.__name__ + '".')
            _logger.warning('Unused resource "' + str(r) + '".')

    return rval


def get_resource_identifiers(resource_list: List[Resource]) -> List[str]:
    rval: List[DeploymentModelResource] = get_deployment_model_resources(resource_list=resource_list)
    return [k.identifier for k in rval]


def load_phase01_scenario_runner_configuration(deployment_model: GmeInterchangeFormat) -> ScenarioRunnerConfiguration:
    client_count = get_android_client_count(deployment_model)
    pli_rate_ms = get_pli_rate_ms(deployment_model)
    image_rate_ms = get_image_rate_ms(deployment_model)

    config_dict = resourcemanager.load_configuration_dict('scenario_runner_configuration')

    vp = {
        'sessionIdentifier': deployment_model.sessionIdentifier,
        'runtimeRoot': get_configuration().runtimeRoot,
    }

    src: ScenarioRunnerConfiguration = ScenarioRunnerConfiguration.from_dict(d=config_dict, value_pool=vp)

    vp['deploymentDirectory'] = src.deploymentDirectory

    global_resources: List[Resource] = \
        [k for k in deployment_model.availableResources if not isinstance(k, ExecutionPlatform)]

    for resource in deployment_model.availableResources:
        if isinstance(resource, ServerPlatform):
            server_config: ApplicationConfig = JavaApplicationConfig.from_dict(
                d=resourcemanager.load_configuration_dict('server_marti'),
                value_pool=vp
            )
            src.scenario.deploymentApplications.append(server_config)

        elif isinstance(resource, AndroidPlatform):
            present_resources: List[str] = get_resource_identifiers(global_resources + resource.platformResources)

            # Construct the clients and add them to the scenario runner configuration
            client_j = resourcemanager.load_configuration_dict('client_ataklite')

            ccid = 0
            for q in range(client_count):
                # client_configuration = scenario_configuration.clients[j]  # type: ATAKLiteClient

                i_str = str(q)
                if len(i_str) == 1:
                    i_str = '00' + i_str
                elif len(i_str) == 2:
                    i_str = '0' + i_str

                client_vp = vp.copy()
                client_vp.update({
                    'CCID': str(ccid),
                    'CID': i_str,
                })

                client: AndroidApplicationConfig = AndroidApplicationConfig.from_dict(client_j,
                                                                                      value_pool=client_vp)

                for f in client.filesToCopy:  # type: FileCopyConfiguration

                    if f.targetFilepath == '/sdcard/ataklite/ATAKLite-Config.json':

                        override_file_map = {k.sourceFilepath: k for k in client.configurationOverrides}

                        if f.sourceFilepath in override_file_map:
                            override_obj = override_file_map[f.sourceFilepath]
                        else:
                            override_obj = JsonFileOverrideConfiguration(f.sourceFilepath, {})
                            client.configurationOverrides.append(override_obj)

                        override_obj.overridePairs['callsign'] = client.instanceIdentifier
                        override_obj.overridePairs['latestSABroadcastIntervalMS'] = pli_rate_ms
                        override_obj.overridePairs['imageBroadcastIntervalMS'] = image_rate_ms

                    elif f.targetFilepath == '/sdcard/ataklite/env.json':

                        override_file_map = {k.sourceFilepath: k for k in client.configurationOverrides}

                        if f.sourceFilepath in override_file_map:
                            override_obj = override_file_map[f.sourceFilepath]
                        else:
                            override_obj = JsonFileOverrideConfiguration(f.sourceFilepath, {})
                            client.configurationOverrides.append(override_obj)

                        override_obj.overridePairs['availableResources'] = present_resources

                src.scenario.deploymentApplications.append(client)
            ccid += 1

    return src


def triplify_p1_input(input: LLP1Input, challenge_problem: ChallengeProblem) -> GmeInterchangeFormat:
    if challenge_problem == ChallengeProblem.Phase01:

        if DeploymentModelProperty.trustedLocations in input.clients[0].requiredProperties:
            constrained_property: List[Property] = [TrustedProperty()]
            property_constraint = PropertyConstraint(
                constraintCriterion=PropertyCriterionType.PROPERTY_PRESENT,
                constrainedProperty=constrained_property,
                humanReadableForm='the implementer must possess TrustedProperty'
            )

        else:
            property_constraint: PropertyConstraint = None

        available_resources = list()
        present_resource_strings = set(input.clients[0].presentResources)

        if 'bluetooth' in present_resource_strings:
            available_resources.append(BluetoothResource)

        if 'usb' in present_resource_strings:
            available_resources.append(UsbResource)

        if 'internalGps' in present_resource_strings:
            available_resources.append(GpsReceiverEmbedded)

        if 'userInterface' in present_resource_strings:
            available_resources.append(UserInterface)

        if 'gpsSatellites' in present_resource_strings:
            available_resources.append(GpsSatelliteConstellation)

        android_platform: AndroidPlatform = AndroidPlatform(
            humanReadableDescription='Marshmallow device with the following hardware: a USB port, a Bluetooth ' +
                                     'transceiver, a physical UI, an embedded GPS receiver, a network connection',
            androidPlatformVersion='6.0.1 Marshmallow',
            platformResources=list()
        )

        server_platform: ExecutionPlatform = ServerPlatform()

        android_platform.platformResources.append(
            NetworkConnection(
                humanReadableDescription='a bidirectional connection between a MARTI server and ATAK client',
                localDevice=android_platform,
                remoteDevice=server_platform,
                oneWay=False
            )
        )

        if 'bluetooth' in present_resource_strings:
            android_platform.platformResources.append(
                BluetoothResource(
                    humanReadableDescription='a Bluetooth transceiver'
                )
            )

        if 'usb' in present_resource_strings:
            android_platform.platformResources.append(
                UsbResource(
                    humanReadableDescription='a USB port'
                )
            )

        if 'internalGps' in present_resource_strings:
            android_platform.platformResources.append(
                GpsReceiverEmbedded(
                    humanReadableDescription='an embedded GPS receiver',
                    numChannels=0
                )
            )

        if 'userInterface' in present_resource_strings:
            android_platform.platformResources.append(
                UserInterface(
                    humanReadableDescription='a user interface'
                )
            )

        gif = GmeInterchangeFormat(
            sessionIdentifier=input.sessionIdentifier,
            functionalitySpec=[
                FunctionalitySpec(
                    functionalityPerformed=Functionality,
                    propertyConstraint=property_constraint
                )
            ],
            missionSpec=[
                MissionSpec(
                    assertionCriterion=ValueCriterionType.VALUE_GREATER_THAN_INCLUSIVE,
                    humanReadableForm='the client must issue > 10 PLI messages per minute',
                    rightValue=Metric(
                        measurementType=MeasurementType(
                            measurementType='PLI report rate',
                            correspondingProperty=PliReportRate
                        ),
                        value=str(60000 / int(input.clients[0].latestSABroadcastIntervalMS)),
                        unit='messages/minute',
                        linkId='PLIReportRate',
                        applicableResourceType=AndroidPlatform
                    ),
                ),
                MissionSpec(
                    assertionCriterion=ValueCriterionType.VALUE_LESS_THAN_INCLUSIVE,
                    humanReadableForm='the link must never see > 25kbps in traffic',
                    rightValue=Metric(
                        measurementType=MeasurementType(
                            measurementType="EWMA bandwidth consumption",
                            correspondingProperty=BandwidthKiloBitsPerSecond
                        ),
                        value=str(input.server.bandwidth),
                        unit='kb/s',
                        linkId='TotalAvailableServerBandwidth',
                        applicableResourceType=NetworkConnection
                    )
                ),
                MissionSpec(
                    assertionCriterion=ValueCriterionType.VALUE_GREATER_THAN_INCLUSIVE,
                    humanReadableForm='the software must provide at least 1 image updates per minute',
                    rightValue=Metric(
                        measurementType=MeasurementType(
                            measurementType='Image report rate',
                            correspondingProperty=ImageReportRate
                        ),
                        value=str(60000 / int(input.clients[0].imageBroadcastIntervalMS)),
                        unit='images/minute',
                        linkId='ImageReportRate',
                        applicableResourceType=AndroidPlatform
                    )
                ),
                MissionSpec(
                    assertionCriterion=ValueCriterionType.VALUE_EQUALS,
                    humanReadableForm='the default camera image size is 5.0 megapixels',
                    rightValue=Metric(
                        measurementType=MeasurementType(
                            measurementType='Number of Pixels',
                            correspondingProperty=NumberOfPixels
                        ),
                        value='5.0',
                        unit='megapixels',
                        linkId='DefaultImageSize',
                        applicableResourceType=AndroidPlatform
                    )
                ),
                MissionSpec(
                    humanReadableForm='the software must support at least 25 concurrent clients',
                    assertionCriterion=ValueCriterionType.VALUE_GREATER_THAN_INCLUSIVE,
                    rightValue=Metric(
                        measurementType=MeasurementType(
                            measurementType='Number of clients',
                            correspondingProperty=NumClients
                        ),
                        value=str(input.clients[0].count),
                        unit='count',
                        linkId='NumberOfClients',
                        applicableResourceType=ServerPlatform
                    )
                )
            ],
            availableResources=[
                android_platform,
                server_platform
            ]

        )

        if 'gpsSatellites' in present_resource_strings:
            gif.availableResources.append(
                GpsSatelliteConstellation(
                    humanReadableDescription='a constellation of satellites',
                    constellationName='GPS'
                )
            )

        return gif

    else:
        raise Exception("Only Phase 01 is supported at this time!")
