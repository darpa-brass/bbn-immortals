import copy
import json
import sys

from pkg_resources import resource_string, resource_listdir

from installer.utils import resolve_platform, clean_json_str, fill_dict


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
    """

    @classmethod
    def from_dict(cls, d):
        return cls(**d)

    def __init__(self, identifier, packageManagerInitCommands, packageManagerInstallationCommand,
                 requiredPlatformPackages):
        """
        :type identifier: str
        :type packageManagerInitCommands: list[str]
        :type packageManagerInstallationCommand: str
        :type requiredPlatformPackages: list[str]
        """
        self.identifier = identifier
        self.packageManagerInitCommands = packageManagerInitCommands
        self.packageManagerInstallationCommand = packageManagerInstallationCommand
        self.requiredPlatformPackages = requiredPlatformPackages


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

    def __init__(self, identifier, setupCommands, requiredExecutables, homeExportRequired, pathExports,
                 version, home, versionExtractionCommand, versionExtractionRegex, homeExportVariable,
                 packageDependencies, subInstaller, sudoSetupCommands=None):
        """
        :type identifier: str
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
        :type sudoSetupCommands: list[str]
        """
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
        self.sudoSetupCommands = [] if sudoSetupCommands is None else sudoSetupCommands


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


def init_configuration(installation_root, configuration_profile):
    """
    :type installation_root: str
    :type configuration_profile: str
    """
    global installation_configuration_d, installation_configuration

    basic_configuration_d = json.loads(clean_json_str(
        resource_string('installer.resources', 'installation_configuration.json')))

    if configuration_profile not in basic_configuration_d['configurationProfiles']:
        raise Exception("Invalid override value of '" + configuration_profile + "'! Valid values: " +
                        str(list(basic_configuration_d['configurationProfiles'].keys())))
    else:
        configuration_override_d = basic_configuration_d['configurationProfiles'][configuration_profile]
        basic_configuration_d.pop('configurationProfiles')
        basic_configuration_d.update(configuration_override_d)

    app_packages = list(filter(lambda x: not x.startswith('_') and x.endswith('.json'),
                               resource_listdir('installer.resources.app_configurations', '')))

    for appIdentifier in basic_configuration_d['applicationIdentifiers']:
        if appIdentifier.startswith('__') or not appIdentifier + '.json' in app_packages:
            raise Exception("Invalid package name '" + appIdentifier +
                            "' specified within configuration file! Valid values: " +
                            str(list(map(lambda x: x[0:-5], app_packages))))

        else:
            basic_configuration_d['applications'].append(json.loads(clean_json_str(
                resource_string('installer.resources.app_configurations', appIdentifier + ".json")
            )))

    basic_configuration_d.pop("applicationIdentifiers")

    installation_configuration_d = fill_dict(basic_configuration_d,
                                             {'installationRoot': installation_root,
                                              "dataRoot": '/dev/null',
                                              "logFile": "/dev/null",
                                              "artifactRoot": '/dev/null'
                                              })

    installation_configuration = InstallationConfiguration.from_dict(installation_configuration_d)


def get_configuration_profiles():
    """
    :rtype: list[str]
    """
    basic_configuration_d = json.loads(clean_json_str(
        resource_string('installer.resources', 'installation_configuration.json')))
    return list(basic_configuration_d['configurationProfiles'].keys())


if __name__ == '__main__':
    init_configuration('/meh', "phase2")
    config = installation_configuration
    print('hi')
