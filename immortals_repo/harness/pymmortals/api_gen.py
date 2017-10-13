import copy
import os
from typing import Dict, List, Set

from pymmortals.datatypes.root_configuration import get_configuration
from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.atakliterequirements import \
    AtakliteRequirements
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.ataklitesubmissionmodel import \
    ATAKLiteSubmissionModel
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.androidplatformversion import \
    AndroidPlatformVersion
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.clientlibraryupgraderequirements import \
    ClientLibraryUpgradeRequirements
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.clientpartiallibraryupgraderequirements import \
    ClientPartialLibraryUpgradeRequirements
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.clientpartialupgradelibrary import \
    ClientPartialUpgradeLibrary
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.clientupgradelibrary import \
    ClientUpgradeLibrary
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.functionality.dataintransit import DataInTransit
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.functionality.securitystandard import SecurityStandard
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.globalmodel.globalrequirements import \
    GlobalRequirements
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.globalmodel.globalsubmissionmodel import \
    GlobalSubmissionModel
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.martirequirements import MartiRequirements
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.martisubmissionmodel import \
    MartiSubmissionModel
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.serverlibraryupgraderequirements import \
    ServerLibraryUpgradeRequirements
from pymmortals.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.serverpartiallibraryupgraderequirements import \
    ServerPartialLibraryUpgradeRequirements
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
from pymmortals.pojoizer import Pojoizer, ConversionMethod, DocumentationTag

_target_package = 'pymmortals.generated'
_target_module_directory = get_configuration().immortalsRoot + '/harness/' + _target_package.replace('.', '/') + '/'
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


_sample_cp1_submission_model: SubmissionModel = SubmissionModel(
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
)

_sample_cp2_submission_model = SubmissionModel(
    globalModel=GlobalSubmissionModel(
        requirements=GlobalRequirements(
            dataInTransit=DataInTransit(
                securityStandard=SecurityStandard.NIST800Dash171
            )
        )
    )
)

_sample_cp3_submission_model = SubmissionModel(
    martiServerModel=MartiSubmissionModel(
        requirements=MartiRequirements(
            partialLibraryUpgrade=ServerPartialLibraryUpgradeRequirements(
                libraryIdentifier=ServerPartialUpgradeLibrary.Dom4jCot,
                libraryVersion=ServerPartialUpgradeLibrary.Dom4jCot.latestVersion
            ),
            libraryUpgrade=ServerLibraryUpgradeRequirements(
                libraryIdentifier=ServerUpgradeLibrary.ImageSaverLibrary,
                libraryVersion=ServerUpgradeLibrary.ImageSaverLibrary.latestVersion
            )
        )
    ),
    atakLiteClientModel=ATAKLiteSubmissionModel(
        requirements=AtakliteRequirements(
            deploymentPlatformVersion=AndroidPlatformVersion.Android23,
            partialLibraryUpgrade=ClientPartialLibraryUpgradeRequirements(
                libraryIdentifier=ClientPartialUpgradeLibrary.ToBeDetermined,
                libraryVersion=ClientPartialUpgradeLibrary.ToBeDetermined.latestVersion
            ),
            libraryUpgrade=ClientLibraryUpgradeRequirements(
                libraryIdentifier=ClientUpgradeLibrary.ToBeDetermined,
                libraryVersion=ClientUpgradeLibrary.ToBeDetermined.latestVersion
            )
        )
    )
)

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
            adaptationStatusValue="SomeValue",
            audits=[
                "DAS did this",
                "DAS did that",
                "DAS is done"
            ],
            auditsAsString="DAS did this\nDAS did that\ DAS is done",
            details="Some Additional Details",
            selectedDfu="Phase 1 input here",
            sessionIdentifier="PerturbationValidationInstanceIdentifier"
        )
    ),
    validation=ValidationStateObject(
        verdictOutcome=VerdictOutcome.PASS,
        executedTests=[
            TestStateObject(
                testIdentifier="LocationSendTest",
                intent="SendLocation",
                desiredStatus=TestOutcome.COMPLETE,
                actualStatus=TestOutcome.COMPLETE,
                details=TestDetails(
                    testIdentifier="LocationSendTest",
                    currentState=TestOutcome.COMPLETE,
                    errorMessages=[],
                    detailMessages=["A sent to B",
                                    "B sent to A"]
                )
            ),
            TestStateObject(
                testIdentifier="ImageSendTest",
                intent="SendImage",
                desiredStatus=TestOutcome.COMPLETE,
                actualStatus=TestOutcome.COMPLETE,
                details=TestDetails(
                    testIdentifier="ImageSendTest",
                    currentState=TestOutcome.COMPLETE,
                    errorMessages=[],
                    detailMessages=["A sent image to B",
                                    "B sent image to A"]
                )
            )
        ]
    )
)


class ApiGenerator:
    def __init__(self, root_dir: str, packages_root: str):
        # Pojoize the shared POJOs
        self.pojoizer: Pojoizer = Pojoizer(conversion_method=ConversionMethod.VARS,
                                           target_directory=_target_module_directory,
                                           target_package=_target_package,
                                           do_generate_markdown=True)
        self.pojoizer.load_directory(root_directory=root_dir,
                                     packages_root=packages_root)

    def markdownify(self, apis: Set[DocumentationTag], input_example: Serializable):
        lines = list()

        lines.append('#### Sample ' + input_example.__class__.__name__ + ' value\n')
        lines.append('```  \n')
        lines.append(input_example.to_json_str_pretty(include_metadata=False, strip_nulls=True) + '  \n')
        lines.append('```  \n')
        lines.append('\n')
        lines.append('### Data Dictionary')
        lines.append('\n')
        lines = lines + self.pojoizer.generate_pojo_spec_lines(apis=apis, omit_unstable=True,
                                                               root_class_name=input_example.__class__.__name__)

        return lines


def generate_apis():
    ag = ApiGenerator(root_dir=os.path.join(get_configuration().immortalsRoot, 'shared/modules/core/src/main/java/'),
                      packages_root=_default_packages_root)
    cp1_lines = ag.markdownify(apis={DocumentationTag.P2CP1}, input_example=_sample_cp1_submission_model)
    open('ll_p2_cp1_api.md', 'w').writelines(cp1_lines)

    cp2_lines = ag.markdownify(apis={DocumentationTag.P2CP2}, input_example=_sample_cp2_submission_model)
    open('ll_p2_cp2_api.md', 'w').writelines(cp2_lines)

    cp3_lines = ag.markdownify(apis={DocumentationTag.P2CP3}, input_example=_sample_cp3_submission_model)
    open('ll_p2_cp3_api.md', 'w').writelines(cp3_lines)

    response_lines = ag.markdownify(apis={DocumentationTag.RESULT}, input_example=_sample_test_adapter_state)
    open('ll_response_api.md', 'w').writelines(response_lines)

    unified_lines = ag.markdownify(apis={DocumentationTag.P2CP1, DocumentationTag.P2CP2, DocumentationTag.P2CP3},
                                   input_example=_sample_unified_submission_model)
    open('ll_unified_api.md', 'w').writelines(unified_lines)


if __name__ == '__main__':
    generate_apis()
