import copy
import json
import os
from typing import Dict, List, Set

from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.config.deploymentenvironmentconfiguration import \
    DeploymentEnvironmentConfiguration, AndroidEnivronmentConfiguration
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.atakliterequirements import \
    AtakliteRequirements
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.ataklitesubmissionmodel import \
    ATAKLiteSubmissionModel
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.androidplatformversion import \
    AndroidPlatformVersion
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.clientpartialupgradelibrary import \
    ClientPartialUpgradeLibrary
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.clientupgradelibrary import \
    ClientUpgradeLibrary
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.dasprerequisites import DASPrerequisites, \
    ChallengeProblemRequirements, AndroidEmulatorRequirement
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.functionality.dataintransit import DataInTransit
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.functionality.securitystandard import SecurityStandard
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.globalmodel.globalrequirements import \
    GlobalRequirements
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.globalmodel.globalsubmissionmodel import \
    GlobalSubmissionModel
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.martirequirements import MartiRequirements
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.martisubmissionmodel import \
    MartiSubmissionModel
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.serverpartialupgradelibrary import \
    ServerPartialUpgradeLibrary
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.serverupgradelibrary import \
    ServerUpgradeLibrary
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql.databasecolumns import \
    DatabaseColumns
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql.databaseperturbation import \
    DatabasePerturbation
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql.databasetableconfiguration import \
    DatabaseTableConfiguration
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.adaptationdetails import AdaptationDetails
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.adaptationstateobject import \
    AdaptationStateObject
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.dasoutcome import DasOutcome
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.testoutcome import TestOutcome
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.verdictoutcome import VerdictOutcome
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.testadapterstate import TestAdapterState
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.testdetails import TestDetails
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.teststateobject import TestStateObject
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.result.validationstateobject import \
    ValidationStateObject
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.submissionmodel import SubmissionModel
from pymmortals.markdownifier import Markdownifier
from pymmortals.pojoizer import DocumentationTag

_immortals_root = os.path.abspath(os.path.dirname(os.path.realpath(__file__)) + '/../../') + '/'
_target_package = 'pymmortals.generated'
_target_module_directory = _immortals_root + 'harness/' + _target_package.replace('.',
                                                                                  '/') + '/'
_default_root_dir = 'shared/modules/core/src/main/java/'
_default_packages_root = 'mil/darpa/immortals/core/api/ll/phase2'


def _merge_dict(source_dict: Dict, target_dict: Dict) -> Dict:
    for k, v in source_dict.items():
        if k in target_dict.keys() and target_dict[k] is not None:
            if k == 'globalModel':
                if isinstance(v, Dict):
                    assert isinstance(target_dict[k], Dict)
                    _merge_dict(source_dict=v, target_dict=target_dict[k])

                elif isinstance(v, List):
                    assert isinstance(target_dict[k], List)
                    target_dict[k] = target_dict[k] + v

                elif v is not None:
                    # Clobber it!
                    target_dict[k] = v
        else:
            target_dict[k] = v

    return target_dict


_sample_cp1_submission_model = SubmissionModel(
    martiServerModel=MartiSubmissionModel(
        requirements=MartiRequirements(
            postgresqlPerturbation=DatabasePerturbation(
                tables=[
                    DatabaseTableConfiguration(
                        columns=[
                            DatabaseColumns.CotEvent_SourceId,
                            DatabaseColumns.CotEvent_How,
                            DatabaseColumns.CotEvent_ServerTime,
                            DatabaseColumns.Position_PointCE,
                            DatabaseColumns.Position_PointLE,
                            DatabaseColumns.Position_TileX,
                            DatabaseColumns.Position_Longitude,
                            DatabaseColumns.Position_Latitude
                        ]
                    ),
                    DatabaseTableConfiguration(
                        columns=[
                            DatabaseColumns.Position_PointHae,
                            DatabaseColumns.CotEvent_Detail,
                            DatabaseColumns.Position_TileY,
                            DatabaseColumns.CotEvent_CotType,
                        ]
                    )
                ]
            )
        )
    )
)  # type: SubmissionModel

_sample_cp2_submission_model = SubmissionModel(
    globalModel=GlobalSubmissionModel(
        requirements=GlobalRequirements(
            dataInTransit=DataInTransit(
                securityStandard=SecurityStandard.NIST800Dash171
            )
        )
    )
)  # type: SubmissionModel

_sample_cp3_submission_model = SubmissionModel(
    martiServerModel=MartiSubmissionModel(
        requirements=MartiRequirements(
            partialLibraryUpgrade=ServerPartialUpgradeLibrary.Dom4jCot_2,
            libraryUpgrade=ServerUpgradeLibrary.ElevationApi_2
        )
    ),
    atakLiteClientModel=ATAKLiteSubmissionModel(
        requirements=AtakliteRequirements(
            deploymentPlatformVersion=AndroidPlatformVersion.Android23,
            partialLibraryUpgrade=ClientPartialUpgradeLibrary.Dropbox_3_0_6,
            libraryUpgrade=ClientUpgradeLibrary.ToBeDetermined_X_X
        )
    )
)  # type: SubmissionModel

_sample_unified_submission_model = copy.deepcopy(
    _sample_cp1_submission_model.to_dict(include_metadata=False, strip_nulls=True))
_merge_dict(_sample_cp2_submission_model.to_dict(include_metadata=False, strip_nulls=True),
            _sample_unified_submission_model)
_merge_dict(_sample_cp3_submission_model.to_dict(include_metadata=False, strip_nulls=True),
            _sample_unified_submission_model)
_sample_unified_submission_model = SubmissionModel.from_dict(_sample_unified_submission_model)

_sample_test_adapter_state = TestAdapterState(
    identifier="PerturbationValidationInstanceIdentifier",
    adaptation=AdaptationStateObject(
        adaptationStatus=DasOutcome.SUCCESS,
        details=AdaptationDetails(
            adaptationIdentifier="PerturbationValidationInstanceIdentifier",
            adaptorIdentifier="PerturbationAdaptationMethodIdentifier",
            dasOutcome=DasOutcome.SUCCESS,
            detailMessages=[
                "Additional Details",
                "More Additional Details"
            ],
            errorMessages=[
                "ErrorMessage",
                "AnotherErrorMessage"
            ]
        )
    ),
    validation=ValidationStateObject(
        verdictOutcome=VerdictOutcome.PASS,
        executedTests=[
            TestStateObject(
                testIdentifier="LocationSendTest",
                intent="SendLocation",
                desiredStatus=TestOutcome.COMPLETE_PASS,
                actualStatus=TestOutcome.COMPLETE_PASS,
                details=TestDetails(
                    testIdentifier="LocationSendTest",
                    currentState=TestOutcome.COMPLETE_PASS,
                    errorMessages=[],
                    detailMessages=["A sent to B",
                                    "B sent to A"]
                )
            ),
            TestStateObject(
                testIdentifier="ImageSendTest",
                intent="SendImage",
                desiredStatus=TestOutcome.COMPLETE_PASS,
                actualStatus=TestOutcome.COMPLETE_PASS,
                details=TestDetails(
                    testIdentifier="ImageSendTest",
                    currentState=TestOutcome.COMPLETE_PASS,
                    errorMessages=[],
                    detailMessages=["A sent image to B",
                                    "B sent image to A"]
                )
            )
        ]
    )
)

_prerequisites = DASPrerequisites(
    cp1=ChallengeProblemRequirements(
        challengeProblemUrl='/action/databaseSchemaPerturbation',
        androidEmulators=[]
    ),
    cp2=ChallengeProblemRequirements(
        challengeProblemUrl='/action/crossApplicationDependencies',
        androidEmulators=[
            AndroidEmulatorRequirement(
                androidVersion=21,
                uploadBandwidthLimitKilobitsPerSecond=None,
                externallyAccessibleUrls=[],
                superuserAccess=False
            ),
            AndroidEmulatorRequirement(
                androidVersion=21,
                uploadBandwidthLimitKilobitsPerSecond=None,
                externallyAccessibleUrls=[],
                superuserAccess=False
            )
        ]
    ),
    cp3=ChallengeProblemRequirements(
        challengeProblemUrl='/action/libraryEvolution',
        androidEmulators=[
            AndroidEmulatorRequirement(
                androidVersion=21,
                uploadBandwidthLimitKilobitsPerSecond=800,
                externallyAccessibleUrls=[
                    "dropbox.com:443",
                    "dropbox.com:80"
                ],
                superuserAccess=True
            ),
            AndroidEmulatorRequirement(
                androidVersion=21,
                uploadBandwidthLimitKilobitsPerSecond=None,
                externallyAccessibleUrls=[],
                superuserAccess=False
            ),
            AndroidEmulatorRequirement(
                androidVersion=21,
                uploadBandwidthLimitKilobitsPerSecond=None,
                externallyAccessibleUrls=[],
                superuserAccess=False
            )
        ]
    )
)

_input_das_configuration = DeploymentEnvironmentConfiguration(
    martiAddress="10.0.2.2",
    androidEnvironments=[
        AndroidEnivronmentConfiguration(
            adbPort=5580,
            adbUrl="127.0.0.1",
            adbIdentifier="emulator-5580",
            environmentDetails=AndroidEmulatorRequirement(
                androidVersion=21,
                uploadBandwidthLimitKilobitsPerSecond=800,
                externallyAccessibleUrls=[
                    "dropbox.com:443",
                    "dropbox.com:80"
                ],
                superuserAccess=True
            )
        ),
        AndroidEnivronmentConfiguration(
            adbPort=5578,
            adbUrl="127.0.0.1",
            adbIdentifier="emulator-5578",
            environmentDetails=AndroidEmulatorRequirement(
                androidVersion=21,
                uploadBandwidthLimitKilobitsPerSecond=None,
                externallyAccessibleUrls=[],
                superuserAccess=False
            )
        ),
        AndroidEnivronmentConfiguration(
            adbPort=5576,
            adbUrl="127.0.0.1",
            adbIdentifier="emulator-5576",
            environmentDetails=

            AndroidEmulatorRequirement(
                androidVersion=21,
                uploadBandwidthLimitKilobitsPerSecond=None,
                externallyAccessibleUrls=[],
                superuserAccess=False
            )
        )
    ]
)

_input_das_configuration.to_file_pretty('sample_override_file.json', include_metadata=False)


class ApiGenerator:
    def __init__(self):
        # Pojoize the shared POJOs
        self.markdownifier = Markdownifier()  # type: Markdownifier

    def load_directory(self, root_dir: str, packages_root: str):
        self.markdownifier.load_directory(root_directory=root_dir, packages_root=packages_root)

    def markdownify(self, apis: Set[DocumentationTag], input_example: Serializable):
        lines = list()

        lines.append('#### Sample ' + input_example.__class__.__name__ + ' value\n')
        lines.append('```  \n')
        lines.append(input_example.to_json_str_pretty(include_metadata=False, strip_nulls=True) + '  \n')
        lines.append('```  \n')
        lines.append('\n')
        lines.append('### Data Dictionary')
        lines.append('\n')
        lines = lines + self.markdownifier.markdownify(apis=apis, input_example=input_example)
        return lines


def generate_apis():
    ag = ApiGenerator()
    ag.load_directory(root_dir=os.path.join(_immortals_root,
                                            'das/das-testharness-coordinator/src/main/java/'),
                      packages_root=_default_packages_root)

    ag.load_directory(
        root_dir=os.path.join(_immortals_root, 'das/das-context/src/main/java/'),
        packages_root=_default_packages_root)
    cp1_lines = ag.markdownify(apis={DocumentationTag.P2CP1}, input_example=_sample_cp1_submission_model)
    open('ll_p2_cp1_api.md', 'w').writelines(cp1_lines)

    cp2_lines = ag.markdownify(apis={DocumentationTag.P2CP2}, input_example=_sample_cp2_submission_model)
    open('ll_p2_cp2_api.md', 'w').writelines(cp2_lines)

    cp3_lines = ag.markdownify(apis={DocumentationTag.P2CP3}, input_example=_sample_cp3_submission_model)
    open('ll_p2_cp3_api.md', 'w').writelines(cp3_lines)

    response_lines = ag.markdownify(apis={DocumentationTag.RESULT}, input_example=_sample_test_adapter_state)
    open('ll_response_api.md', 'w').writelines(response_lines)

    prerequisites_lines = ag.markdownify(apis={DocumentationTag.PREREQUISITES}, input_example=_prerequisites)
    open('ll_prerequisites_api.md', 'w').writelines(prerequisites_lines)

    unified_lines = ag.markdownify(apis={DocumentationTag.P2CP1, DocumentationTag.P2CP2, DocumentationTag.P2CP3},
                                   input_example=_sample_unified_submission_model)
    open('ll_unified_api.md', 'w').writelines(unified_lines)


if __name__ == '__main__':
    generate_apis()
