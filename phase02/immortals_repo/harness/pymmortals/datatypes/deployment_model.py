import time
from copy import deepcopy
from typing import List, Dict, Union

from pymmortals.datatypes.intermediary.challengeproblem import ChallengeProblem
from pymmortals.datatypes.intermediary.deploymentmodelproperty import \
    DeploymentModelProperty
from pymmortals.datatypes.intermediary.deploymentmodelresource import \
    DeploymentModelResource
from pymmortals.generated.com.securboration.immortals.ontology.android.androidplatform import AndroidPlatform
from pymmortals.generated.com.securboration.immortals.ontology.connectivity.bandwidthkilobitspersecond import \
    BandwidthKiloBitsPerSecond
from pymmortals.generated.com.securboration.immortals.ontology.connectivity.imagereportrate import ImageReportRate
from pymmortals.generated.com.securboration.immortals.ontology.connectivity.numclients import NumClients
from pymmortals.generated.com.securboration.immortals.ontology.connectivity.plireportrate import PliReportRate
from pymmortals.generated.com.securboration.immortals.ontology.constraint.propertycriteriontype import \
    PropertyCriterionType
from pymmortals.generated.com.securboration.immortals.ontology.constraint.valuecriteriontype import ValueCriterionType
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
from .serializable import Serializable


class MartiServer(Serializable):
    _validator_values = {
        'bandwidth': (0, 10000000)
    }

    def __init__(self,
                 bandwidth: int
                 ):
        super().__init__()
        self.bandwidth = bandwidth

    def equals(self, other):
        """
        :param MartiServer other:
        """
        return self.bandwidth == other.bandwidth


# noinspection PyPep8Naming
class ATAKLiteClient(Serializable):
    _validator_values = {
        'imageBroadcastIntervalMS': (1000, 60000),
        'latestSABroadcastIntervalMS': (1000, 60000),
        'count': (2, 6),
        'presentResources': [k.identifier for k in DeploymentModelResource],
        'requiredProperties': [k.identifier for k in DeploymentModelProperty]
    }

    def __init__(self,
                 imageBroadcastIntervalMS: int,
                 latestSABroadcastIntervalMS: int,
                 count: int,
                 presentResources: List[str],
                 requiredProperties: List[str]
                 ):
        super().__init__()
        self.imageBroadcastIntervalMS = imageBroadcastIntervalMS
        self.latestSABroadcastIntervalMS = latestSABroadcastIntervalMS
        self.count = count
        self.presentResources = presentResources
        self.requiredProperties = requiredProperties

    def equals(self, other: 'ATAKLiteClient'):
        isEqual = \
            self.imageBroadcastIntervalMS == other.imageBroadcastIntervalMS \
            and self.latestSABroadcastIntervalMS == other.latestSABroadcastIntervalMS \
            and self.count == other.count

        if not isEqual or len(self.presentResources) != len(other.presentResources) \
                or len(self.requiredProperties) != len(other.requiredProperties):
            return False

        else:
            for res in self.presentResources:
                if res not in other.presentResources:
                    return False

            for prop in self.requiredProperties:
                if prop not in other.requiredProperties:
                    return False

        return True


# noinspection PyPep8Naming
class LLP1Input(Serializable):
    _validator_values = {}

    @classmethod
    def _from_dict(cls, source_dict: Dict[str, object], top_level_deserialization: bool,
                   value_pool: Union[Dict[str, object], None], object_map: Union[Dict[str, object], None],
                   do_replacement: bool):

        cd = deepcopy(source_dict)

        if 'sessionIdentifier' not in cd:
            cd['sessionIdentifier'] = "S" + str(int(time.time() * 1000))[:12]

        return super()._from_dict(source_dict=cd,
                                  top_level_deserialization=top_level_deserialization,
                                  value_pool=value_pool,
                                  object_map=object_map,
                                  do_replacement=do_replacement)

    @classmethod
    def from_dict(cls, d, attempt_string_replacement: bool = False, value_pool: Dict[str, object] = None,
                  do_replacement: bool = True):
        cd = deepcopy(d)

        if 'sessionIdentifier' not in cd:
            cd['sessionIdentifier'] = "S" + str(int(time.time() * 1000))[:12]

        return cls._from_dict(cd,
                              top_level_deserialization=True,
                              value_pool=value_pool,
                              object_map=None,
                              do_replacement=do_replacement)

    def __init__(self,
                 sessionIdentifier: str,
                 server: MartiServer,
                 clients: List[ATAKLiteClient]
                 ):
        super().__init__()
        self.sessionIdentifier = sessionIdentifier
        self.server = server
        self.clients = clients

    def equals(self, other):
        """
        :param LLP1Input other:
        :rtype: bool
        """

        return self.server.equals(other.server) and len(self.clients) == 1 and len(other.clients) == 1 and self.clients[
            0].equals(other.clients[0])

    def to_triples(self, challenge_problem: ChallengeProblem) -> GmeInterchangeFormat:
        if challenge_problem == ChallengeProblem.Phase01:

            if DeploymentModelProperty.trustedLocations in self.clients[0].requiredProperties:
                constrained_property: List[Property] = [TrustedProperty()]
                property_constraint = PropertyConstraint(
                    constraintCriterion=PropertyCriterionType.PROPERTY_PRESENT,
                    constrainedProperty=constrained_property,
                    humanReadableForm='the implementer must possess TrustedProperty'
                )

            else:
                property_constraint: PropertyConstraint = None

            available_resources = list()
            present_resource_strings = set(self.clients[0].presentResources)

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
                sessionIdentifier=self.sessionIdentifier,
                functionalitySpec=[
                    FunctionalitySpec(
                        functionalityPerformed=Functionality,
                        propertyConstraint=[property_constraint]
                    )
                ],
                missionSpec=[
                    MissionSpec(
                        assertionCriterion=ValueCriterionType.VALUE_GREATER_THAN_EXCLUSIVE,
                        humanReadableForm='the client must issue > 10 PLI messages per minute',
                        rightValue=Metric(
                            measurementType=MeasurementType(
                                measurementType='PLI report rate',
                                correspondingProperty=PliReportRate
                            ),
                            value=str(60000 / int(self.clients[0].latestSABroadcastIntervalMS)),
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
                            value=str(self.server.bandwidth),
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
                            value=str(60000 / int(self.clients[0].imageBroadcastIntervalMS)),
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
                            value=str(self.clients[0].count),
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

# def test():
#     d = {
#         'server': {
#             'bandwidth': 50
#         },
#         'clients': [
#             {
#                 'imageBroadcastIntervalMS': 2000,
#                 'latestSABroadcastIntervalMS': 1000,
#                 'count': 2,
#                 'presentResources': [],
#                 'requiredProperties': []
#             }
#         ]
#     }
#
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is None
#
#     d['server']['bandwidth'] = 0
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is None
#
#     d['server']['bandwidth'] = -1
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
#
#     d['server']['bandwidth'] = 10000001
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
#
#     d['server']['bandwidth'] = 10000000
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is None
#
#     d['clients'][0]['count'] = 1
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
#
#     d['clients'][0]['count'] = -1
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
#
#     d['clients'][0]['count'] = 0
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
#
#     d['clients'][0]['count'] = 1
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
#
#     d['clients'][0]['count'] = 7
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
#
#     d['clients'][0]['count'] = 6
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is None
#
#     d['clients'][0]['presentResources'] = [
#         'bluetooth',
#         'usb',
#         'internalGps',
#         'userInterface',
#         'gpsSatellites'
#     ]
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is None
#
#     d['clients'][0]['presentResources'] = [
#         'bluetooth',
#         'usb',
#         'internalGps',
#         'gpsSatellites'
#     ]
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is None
#
#     d['clients'][0]['presentResources'] = [
#         'bluetooth',
#         'usb',
#         'internalGps',
#         'gpsSatellitez'
#     ]
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
#
#     d['clients'][0]['presentResources'] = [
#         'gpsSatellitez'
#     ]
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
#
#     d['clients'][0]['presentResources'] = [
#         'bluetooth',
#         'usb',
#         'internalGps',
#         'gpsSatellites'
#     ]
#     d['clients'][0]['requiredProperties'] = [
#
#     ]
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is None
#
#     d['clients'][0]['requiredProperties'] = [
#         'trustedLocations'
#     ]
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is None
#
#     d['clients'][0]['requiredProperties'] = [
#         'trustedLocations',
#         'turzlec'
#     ]
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
#
#     d['clients'][0]['requiredProperties'] = [
#         'turkey',
#         'trustedLocations'
#
#     ]
#     scc = LLPhase01Input.from_dict(d=d)
#     err = scc.validate()
#     print err
#     assert err is not None
