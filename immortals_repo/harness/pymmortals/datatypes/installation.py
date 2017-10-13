import copy
import sys
from typing import List, Dict, Union

from pymmortals.datatypes.serializable import Serializable
from pymmortals.utils import resolve_platform


# noinspection PyPep8Naming
class BaseApplicationDetails(Serializable):
    """
    :type name: str
    :type version: str
    """

    _validator_values = {}

    def __init__(self, name: str, version: str):
        super().__init__()
        self.name = name
        self.version = version


# noinspection PyPep8Naming
class TargetInstallationPlatform(Serializable):
    _validator_values = {}

    def __init__(self,
                 identifier: str,
                 packageManagerInitCommands: List[str],
                 packageManagerInstallationCommand: str):
        super().__init__()
        self.identifier = identifier
        self.packageManagerInitCommands = packageManagerInitCommands
        self.packageManagerInstallationCommand = packageManagerInstallationCommand


# noinspection PyPep8Naming
class SubInstallerConfiguration(Serializable):
    _validator_values = {}

    def __init__(self, installationCommand: str, packageList: List[str]):
        super().__init__()
        self.installationCommand = installationCommand
        self.packageList = packageList


# noinspection PyPep8Naming
class InstallerConfiguration(Serializable):
    @classmethod
    def _from_dict(cls, source_dict: Dict[str, object], top_level_deserialization: bool,
                   value_pool: Union[Dict[str, object], None], object_map: Union[Dict[str, object], None],
                   do_replacement: bool):
        platform = sys.platform

        d2 = copy.deepcopy(source_dict)
        if 'osx' in d2:
            d2.pop('osx')
        if 'linux' in d2:
            d2.pop('linux')

        if platform == 'darwin' and 'osx' in source_dict:
            k = source_dict['osx']
            d2.update(k)

        elif (platform == 'linux' or platform == 'linux2') and 'linux' in source_dict:
            distro = resolve_platform()

            l_d = copy.deepcopy(source_dict['linux'])
            target = copy.deepcopy(source_dict['linux'])

            if 'ubuntu' in target:
                target.pop('ubuntu')

            if 'fedora' in target:
                target.pop('fedora')

            if distro in l_d:
                k = l_d[distro]
                target.update(k)

            d2.update(target)

        return super()._from_dict(source_dict=d2,
                                  top_level_deserialization=top_level_deserialization,
                                  value_pool=value_pool,
                                  object_map=object_map,
                                  do_replacement=do_replacement)

    _validator_values = {}

    def __init__(self, identifier: str,
                 setupCommands: List[str],
                 requiredExecutables: List[str],
                 homeExportRequired: bool,
                 pathExports: List[str] = None,
                 version: str = None,
                 home: str = None,
                 versionExtractionCommand: str = None,
                 versionExtractionRegex: str = None,
                 homeExportVariable: str = None,
                 packageDependencies: List[str] = None,
                 subInstaller: SubInstallerConfiguration = None):
        super().__init__()
        self.identifier = identifier
        self.version = version
        self.home = home
        self.versionExtractionCommand = versionExtractionCommand
        self.versionExtractionRegex = versionExtractionRegex
        self.setupCommands = setupCommands
        self.requiredExecutables = requiredExecutables
        self.pathExports = pathExports
        self.homeExportVariable = homeExportVariable
        self.homeExportRequired = homeExportRequired
        self.packageDependencies = [] if packageDependencies is None else packageDependencies
        self.subInstaller = subInstaller


# noinspection PyPep8Naming
class InstallationConfiguration(Serializable):
    _validator_values = {}

    def __init__(self,
                 installationRoot: str,
                 tempDirectory: str,
                 targetInstallationPlatforms: Dict[str, TargetInstallationPlatform],
                 applications: List[InstallerConfiguration]):
        super().__init__()
        self.installationRoot = installationRoot
        self.tempDirectory = tempDirectory
        self.applications = applications
        self.targetInstallationPlatforms = targetInstallationPlatforms
