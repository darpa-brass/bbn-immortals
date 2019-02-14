import copy
import os
from typing import Dict, List, Set, Optional

from integrationtest.datatypes.serializable import Serializable
from integrationtest.generated.mil.darpa.immortals.config.deploymentenvironmentconfiguration import \
    DeploymentEnvironmentConfiguration, AndroidEnivronmentConfiguration
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.androidresource import AndroidResource
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.atakliterequirements import \
    AtakliteRequirements
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.ataklitesubmissionmodel import \
    ATAKLiteSubmissionModel
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.androidplatformversion import \
    AndroidPlatformVersion
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.clientpartialupgradelibrary import \
    ClientPartialUpgradeLibrary
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.requirements.clientupgradelibrary import \
    ClientUpgradeLibrary
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.dasprerequisites import DASPrerequisites, \
    ChallengeProblemRequirements, AndroidEmulatorRequirement
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.functionality.dataintransit import DataInTransit
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.functionality.securitystandard import SecurityStandard
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.globalmodel.globalrequirements import \
    GlobalRequirements
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.globalmodel.globalsubmissionmodel import \
    GlobalSubmissionModel
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.javaresource import JavaResource
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.martirequirements import MartiRequirements
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.martisubmissionmodel import \
    MartiSubmissionModel
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.serverpartialupgradelibrary import \
    ServerPartialUpgradeLibrary
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.serverupgradelibrary import \
    ServerUpgradeLibrary
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql.databasecolumns import \
    DatabaseColumns
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql.databaseperturbation import \
    DatabasePerturbation
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.requirements.storage.postgresql.databasetableconfiguration import \
    DatabaseTableConfiguration
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.result.adaptationdetails import AdaptationDetails
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.result.adaptationstateobject import \
    AdaptationStateObject
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.dasoutcome import DasOutcome
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.testoutcome import TestOutcome
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.result.status.verdictoutcome import VerdictOutcome
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.result.testadapterstate import TestAdapterState
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.result.testdetails import TestDetails
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.result.teststateobject import TestStateObject
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.result.validationstateobject import \
    ValidationStateObject
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.submissionmodel import SubmissionModel
from integrationtest.pojoizer import DocumentationTag, PojoEnumBuilderConfig, AbstractPojoBuilderConfig, \
    PojoClassBuilderConfig, import_type_omissions, ConversionMethod, Pojoizer

_immortals_root = os.path.abspath(os.path.dirname(os.path.realpath(__file__)) + '/../../') + '/'
_target_package = 'integrationtest.generated'
_target_module_directory = _immortals_root + 'harness/' + _target_package.replace('.',
                                                                                  '/') + '/'
_default_root_dir = 'shared/modules/core/src/main/java/'
_default_packages_root = 'mil/darpa/immortals/core/api/ll/phase2'


def _fillout_matrix(matrix: List[List[str]]):
    max_column_width = list()  # type: List[int]

    # Seed the initial column values
    for ci in range(len(matrix[0])):
        max_column_width.append(len(matrix[0][ci]))

    # Go through the columns and set the max width to the proper max possible value
    for ri in range(len(matrix)):
        for ci in range(len(matrix[ri])):
            max_column_width[ci] = max(max_column_width[ci], len(matrix[ri][ci]))

    # Pad the values to match that width
    for ri in range(len(matrix)):
        for ci in range(len(matrix[ri])):
            if matrix[ri][ci] == '---':
                matrix[ri][ci] = matrix[ri][ci].ljust(max_column_width[ci], '-')
            else:
                matrix[ri][ci] = matrix[ri][ci].ljust(max_column_width[ci], ' ')


def _generate_markdown(config: AbstractPojoBuilderConfig,
                       apis: Optional[Set[DocumentationTag]],
                       omit_unstable: bool) -> \
        List[str]:
    lines = list()
    api_indentation_level = '#### '

    if apis is None or len(apis.intersection(config.apis)) > 0 and \
            (not omit_unstable or not config.unstable):
        if isinstance(config, PojoEnumBuilderConfig):
            lines.append('\n' + api_indentation_level + config.class_name + '  \n')
            lines.append('__Type__: String Constant  \n')
            lines.append('__Description__: ' + config.description[1:-1] + '  \n\n')

            matrix = list()
            header = list()
            header.append('Values')
            separator = list()
            separator.append('---')

            for field in config.fields:
                if not field.unstable:
                    header.append(field.name[:1].upper() + field.name[1:])
                    separator.append('---')

            matrix.append(header)
            matrix.append(separator)

            for value in config.instance_labels:
                row = [value]
                for data in config.instance_parameter_fields[value]:
                    if not data.unstable:
                        row.append(data.value[1:-1])
                matrix.append(row)

            _fillout_matrix(matrix)

            for l in matrix:
                lines.append('| ' + ' | '.join(l) + ' |  \n')

        elif isinstance(config, PojoClassBuilderConfig):
            lines.append('\n' + api_indentation_level + config.class_name + '  \n')
            lines.append('__Type__: JSON Object  \n')
            lines.append('__Description__: ' + config.description[1:-1] + '  \n\n')

            matrix = list()
            header = list()
            header.append('Field')
            header.append('Type')
            header.append('Description')
            separator = list()
            separator.append('---')
            separator.append('---')
            separator.append('---')

            matrix.append(header)
            matrix.append(separator)

            object_rows = list()
            for field in config.fields:
                if apis is None or len(apis.intersection(field.apis)) > 0:
                    row = list()
                    row.append(field.name)
                    row.append('Generic JSON Object' if (field.unstable and omit_unstable) else field.raw_type)
                    row.append(field.description[1:-1])
                    object_rows.append(row)
            matrix = matrix + sorted(object_rows, key=lambda x: x[0])

            _fillout_matrix(matrix)

            for l in matrix:
                lines.append('| ' + ' | '.join(l) + ' |  \n')

        return lines


class Markdownifier(Pojoizer):
    def __init__(self):
        super().__init__(conversion_method=ConversionMethod.VARS, do_generate_markdown=True)

    def get_display_order(self, root_config: AbstractPojoBuilderConfig, apis: Set[DocumentationTag],
                          omit_unstable: bool):
        rval = list()  # type: List[str]

        def _get_display_order(config: AbstractPojoBuilderConfig, display_order: List[str]):
            if isinstance(config, PojoClassBuilderConfig):
                all_fields = sorted((config.fields + config.inherited_fields), key=lambda x: x.name)
            else:
                all_fields = config.fields

            for field in all_fields:
                if not (field.unstable and omit_unstable):
                    if len(apis.intersection(field.apis)) > 0:
                        if field.core_type() not in import_type_omissions and field.core_type() != config.class_name:
                            if field.core_type() not in display_order:
                                display_order.append(field.core_type())

                            if field.raw_type in config.nested_class_configs.keys():
                                _get_display_order(config.nested_class_configs[field.raw_type], display_order)

                            else:
                                field_classpath = config.imports[field.core_type()] \
                                    [len(self.target_package) + 1:-(len(field.core_type()) + 1)]
                                next_config = next(
                                    (x for x in self.path_classes[field_classpath] if
                                     x.class_name == field.core_type()),
                                    None)  # type: AbstractPojoBuilderConfig
                                _get_display_order(next_config, display_order)

        rval.append(root_config.class_name)

        _get_display_order(root_config, rval)
        return rval

    def markdownify(self, apis: Set[DocumentationTag], input_example: Serializable):
        return self._generate_pojo_spec_lines(apis=apis, omit_unstable=True,
                                              root_class_name=input_example.__class__.__name__)

    def _generate_pojo_spec_lines(self, apis: Set[DocumentationTag], omit_unstable: bool, root_class_name: str) -> \
            List[str]:
        self._preprocess_class_data()

        lines = list()  # type: List[str]
        name_lines_map = dict()  # type: Dict[str, List[str]]
        display_order = None

        for key in self.path_classes:
            for config in self.path_classes[key]:
                if root_class_name is not None and root_class_name == config.class_name:
                    display_order = self.get_display_order(config, apis, omit_unstable)

                if not config.unstable and (apis is None
                                            or len(apis.intersection(config.apis)) > 0):
                    md_lines = _generate_markdown(
                        config=config, apis=apis, omit_unstable=omit_unstable)

                    if root_class_name is None:
                        lines = lines + md_lines
                    else:
                        name_lines_map[config.class_name] = md_lines

        if root_class_name is not None:
            if display_order is None:
                raise Exception('Unable to find root class "' + root_class_name + '"!')

            else:
                # Remove unstable items from the display order if necessary
                for key in self.path_classes:
                    for config in self.path_classes[key]:
                        if config.unstable and config.class_name in display_order:
                            display_order.remove(config.class_name)

                if set(display_order) != set(name_lines_map.keys()):
                    display_extras = set(display_order).difference(set(name_lines_map.keys()))
                    found_extras = set(name_lines_map.keys()).difference(set(display_order))

                    msg = ''
                    if len(display_extras) != 0:
                        msg += ('Extra values found in the ordering determination: ' + str(display_extras) + '.\n')

                    if len(found_extras) != 0:
                        msg += ('Extra values found in tagged classes: ' + str(found_extras) + '.\n')

                    msg += 'Are you sure your provided example matches the API tags the java is annotated with?'

                    raise Exception(msg)

                for key in display_order:
                    lines = lines + name_lines_map[key]

        return lines


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
                securityStandard=SecurityStandard.AES_128
            )
        ),
    ),
    martiServerModel=MartiSubmissionModel(
        resources=[
            JavaResource.HARWARE_AES,
            JavaResource.STRONG_CRYPTO
        ]
    ),
    atakLiteClientModel=ATAKLiteSubmissionModel(
        resources=[
            AndroidResource.STRONG_CRYPTO
        ]
    )
)  # type: SubmissionModel

_sample_cp3_submission_model = SubmissionModel(
    martiServerModel=MartiSubmissionModel(
        requirements=MartiRequirements(
            partialLibraryUpgrade=ServerPartialUpgradeLibrary.NONE,
            libraryUpgrade=ServerUpgradeLibrary.ElevationApi_2
        )
    ),
    atakLiteClientModel=ATAKLiteSubmissionModel(
        requirements=AtakliteRequirements(
            deploymentPlatformVersion=AndroidPlatformVersion.Android23,
            partialLibraryUpgrade=ClientPartialUpgradeLibrary.Dropbox_3_0_6,
            libraryUpgrade=ClientUpgradeLibrary.NONE
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
        testsPassedPercent=100.00,
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
                externallyAccessibleUrls=[]
            ),
            AndroidEmulatorRequirement(
                androidVersion=21,
                uploadBandwidthLimitKilobitsPerSecond=None,
                externallyAccessibleUrls=[]
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
                ]
            ),
            AndroidEmulatorRequirement(
                androidVersion=21,
                uploadBandwidthLimitKilobitsPerSecond=None,
                externallyAccessibleUrls=[]
            ),
            AndroidEmulatorRequirement(
                androidVersion=21,
                uploadBandwidthLimitKilobitsPerSecond=None,
                externallyAccessibleUrls=[]
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
                ]
            )
        ),
        AndroidEnivronmentConfiguration(
            adbPort=5578,
            adbUrl="127.0.0.1",
            adbIdentifier="emulator-5578",
            environmentDetails=AndroidEmulatorRequirement(
                androidVersion=21,
                uploadBandwidthLimitKilobitsPerSecond=None,
                externallyAccessibleUrls=[]
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
                externallyAccessibleUrls=[]
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
