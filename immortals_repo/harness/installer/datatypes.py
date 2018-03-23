import copy
import json
import sys
from enum import Enum

from pkg_resources import resource_string

from installer.utils import resolve_platform, clean_json_str, fill_dict


class EnvironmentTag(Enum):
    BASE = ('Dependencies for building the Java-based components', ['BASE'])
    ANDROID = ('Dependencies for building and executing the Android-based components', ['BASE', 'ANDROID'])
    DATABASE = ('Dependencies for running the database in java-based components', ['BASE', 'DATABASE'])
    DSL = ('Dependencies for building the DSL', ['BASE', 'DSL'])
    FULL = ('Everything needed for building and executing the DAS', ['BASE', 'ANDROID', 'DATABASE', 'DSL', 'FULL'])

    @classmethod
    def from_dict(cls, val):
        """
        :type val: str
        :rtype: EnvironmentTag
        """
        return EnvironmentTag[val]

    def __init__(self, description, included_tags):
        """
        :type description: str
        :param included_tags: list[str]
        """
        self.description = description
        self.included_tags = included_tags


# noinspection PyPep8Naming
class BaseApplicationDetails:
    """
    :type name: str
    :type version: str
    """

    @classmethod
    def from_dict(cls, d):
        return cls(**d)

    def __init__(self, name, version):
        """
        :type name: str
        :type version: str
        """
        self.name = name
        self.version = version


# noinspection PyPep8Naming
class TargetInstallationPlatform:
    """
    :type identifier: str
    :type packageManagerInitCommands: list[str]
    :type packageManagerInstallationCommand: str
    :type requiredPlatformPackages: list[str]
    :type sudoFileLocation: str
    :type sudoFileEnableCommands: list[str]
    """

    @classmethod
    def from_dict(cls, d):
        return cls(**d)

    def __init__(self, identifier, packageManagerInitCommands, packageManagerInstallationCommand,
                 requiredPlatformPackages, sudoFileLocation, sudoFileEnableCommands):
        """
        :type identifier: str
        :type packageManagerInitCommands: list[str]
        :type packageManagerInstallationCommand: str
        :type requiredPlatformPackages: list[str]
        :type sudoFileLocation: str
        :type sudoFileEnableCommands: list[str]
        """
        self.identifier = identifier
        self.packageManagerInitCommands = packageManagerInitCommands
        self.packageManagerInstallationCommand = packageManagerInstallationCommand
        self.requiredPlatformPackages = requiredPlatformPackages
        self.sudoFileLocation = sudoFileLocation
        self.sudoFileEnableCommands = sudoFileEnableCommands


# noinspection PyPep8Naming
class SubInstallerConfiguration:
    """
    :type installationCommand: str
    :type packageList: list[str]
    """

    @classmethod
    def from_dict(cls, d):
        return cls(**d)

    def __init__(self, installationCommand, packageList):
        """
        :type installationCommand: str
        :type packageList: list[str]
        """
        self.installationCommand = installationCommand
        self.packageList = packageList


# noinspection PyPep8Naming
class InstallerConfiguration:
    @classmethod
    def from_dict(cls, source_dict):
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

            if distro in l_d:
                k = l_d[distro]
                target.update(k)

            d2.update(target)

        d2['environmentTag'] = EnvironmentTag.from_dict(d2['environmentTag'])

        if 'subInstaller' in d2:
            d2['subInstaller'] = SubInstallerConfiguration.from_dict(d2['subInstaller'])
        else:
            d2['subInstaller'] = None

        if 'version' not in d2:
            d2['version'] = None
        if 'versionExtractionCommand' not in d2:
            d2['versionExtractionCommand'] = None
        if 'versionExtractionRegex' not in d2:
            d2['versionExtractionRegex'] = None
        if 'home' not in d2:
            d2['home'] = None
        if 'homeExportVariable' not in d2:
            d2['homeExportVariable'] = None
        if 'pathExports' not in d2:
            d2['pathExports'] = list()

        return cls(**d2)

    def __init__(self, identifier, environmentTag, setupCommands, requiredExecutables, homeExportRequired, pathExports,
                 version, home, versionExtractionCommand, versionExtractionRegex, homeExportVariable,
                 packageDependencies, subInstaller, bootCommands = None):
        """
        :type identifier: str
        :type environmentTag:  EnvironmentTag
        :type setupCommands: list[str]
        :type requiredExecutables: list[str]
        :type homeExportRequired: bool
        :type pathExports: list[str]
        :type version: str
        :type home: str
        :type versionExtractionCommand: str
        :type versionExtractionRegex: str
        :type homeExportVariable: str
        :type packageDependencies: list[str]
        :type subInstaller: SubInstallerConfiguration
        :type bootCommands: list[str]
        """
        self.environmentTag = environmentTag
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
        self.bootCommands = [] if bootCommands is None else bootCommands


# noinspection PyPep8Naming
class InstallationConfiguration:
    @classmethod
    def from_dict(cls, d):
        """
        :type d: dict[str]
        :rtype: InstallerConfiguration
        """

        d2 = copy.deepcopy(d)
        target_installation_platforms = dict()
        for key in d['targetInstallationPlatforms']:
            target_installation_platforms[key] = \
                TargetInstallationPlatform.from_dict(d['targetInstallationPlatforms'][key])
        d2['targetInstallationPlatforms'] = target_installation_platforms

        applications = list()
        for val in d['applications']:
            applications.append(InstallerConfiguration.from_dict(val))
        d2['applications'] = applications

        return cls(**d2)

    def __init__(self, installationRoot, tempDirectory, targetInstallationPlatforms, applications):
        """
        :type installationRoot: str
        :type tempDirectory: str
        :type targetInstallationPlatforms: dict[str, TargetInstallationPlatform]
        :type applications: list[InstallerConfiguration]
        """
        self.installationRoot = installationRoot
        self.tempDirectory = tempDirectory
        self.applications = applications
        self.targetInstallationPlatforms = targetInstallationPlatforms


installation_configuration_d = None  # type: dict

installation_configuration = None  # type: InstallationConfiguration


def init_configuration(installation_root):
    """
    :type installation_root: str
    :rtype: InstallationConfiguration
    """
    global installation_configuration_d, installation_configuration
    installation_configuration_d = fill_dict(json.loads(
        clean_json_str(resource_string('installer.resources', 'installation_configuration.json'))),
        {'installationRoot': installation_root,
         "dataRoot": '/dev/null',
         "logFile": "/dev/null",
         "artifactRoot": '/dev/null'
         })

    installation_configuration = InstallationConfiguration.from_dict(installation_configuration_d)
