import copy
import os
import re
import subprocess
import time

from installer import datatypes
from installer.datatypes import InstallerConfiguration, TargetInstallationPlatform, SubInstallerConfiguration
from installer.utils import resolve_platform


def resolve_platform_configuration():
    """
    :rtype: TargetInstallationPlatform
    """
    platform = resolve_platform()

    if platform in datatypes.installation_configuration.targetInstallationPlatforms:
        return datatypes.installation_configuration.targetInstallationPlatforms[platform]
    else:
        raise Exception('No configuration defined for platform "' + platform + '"!')


def parse_path(full_path):
    """
    :type full_path: str
    :rtype: Union[str, None]
    """
    if full_path is None:
        return None

    if os.path.exists(full_path):
        return full_path

    split_path = full_path.split('/')
    if split_path[0] == '':
        split_path.pop(0)
    if split_path[len(split_path) - 1] == '':
        split_path.pop(len(split_path) - 1)
    curr_path = '/'
    for path in split_path:
        if '*' not in path:
            curr_path += (path + '/')
        else:
            dirs = next(os.walk(curr_path))[1]
            path_addition = None
            for d in dirs:  # type: str
                split_path = path.split('*')
                if d.startswith(split_path[0]) and d.endswith(split_path[1]):
                    # Although not guarateed, it is likely the higher ordered version is newer, so clobbering existing
                    path_addition = d + '/'
            if path_addition is None:
                return None
            else:
                curr_path += path_addition
    return curr_path


def _produce_pathing(application_identifier, home, home_path_exports, executables):
    """
    :type application_identifier: str
    :type home: str
    :type home_path_exports: list[str]
    :type executables: list[str]
    :rtype: tuple
    """
    setup_lines = list()
    init_lines = list()

    exports = list()

    if home is not None:

        if home_path_exports is not None and len(home_path_exports) > 0:
            for export in home_path_exports:
                export = os.path.join(home, export)
                if export.endswith('/'):
                    export = export[:-1]

                immortals_path = os.getenv('IMMORTALS_PATH')
                user_path = os.getenv('PATH')

                if export in user_path:
                    exports.append(export)
                    if immortals_path is not None and export in immortals_path:
                        init_lines.append('export IMMORTALS_PATH=' + export + ':${IMMORTALS_PATH}')
                    else:
                        # Already Exists
                        pass

                else:
                    exports.append(export)
                    init_lines.append('export IMMORTALS_PATH=' + export + ':${IMMORTALS_PATH}')

        if len(executables) > 0:
            for exe in executables:
                source_path = os.path.join(home, exe)
                source_path_folder = source_path[:source_path.rfind('/')]

                if source_path_folder not in exports:

                    executable = exe.split('/')[-1]
                    target_path = os.path.join(datatypes.installation_configuration.installationRoot, 'bin',
                                               executable)

                    if not os.path.exists(target_path) or not os.readlink(target_path).startswith(home):
                        if not os.path.exists(source_path):
                            setup_lines.append('chmod +x "' + source_path + '"')

                        if os.path.lexists(target_path) and not os.path.exists(target_path):
                            setup_lines.append(('rm "' + target_path + '"'))

                        setup_lines.append('ln -s "' + source_path + '" "' + target_path + '"')

    if len(init_lines) > 0:
        init_lines.insert(0, '# ' + application_identifier + ': Added paths to PATH...')

    if len(setup_lines) > 0:
        setup_lines.insert(0, '# ' + application_identifier + ': Symlinking required executables.')
        if len(init_lines) > 0:
            # Exporting path variables for other dependent apps during setup
            setup_lines += init_lines

    return setup_lines, init_lines


def produce_init_pathing(application_identifier, home, home_path_exports, executables):
    """
    :type application_identifier: str
    :type home: str
    :type home_path_exports: list[str]
    :type executables: list[str]
    :rtype: list[str]
    """
    return _produce_pathing(application_identifier=application_identifier, home=home,
                            home_path_exports=home_path_exports, executables=executables)[1]


def produce_setup_pathing(application_identifier, home, home_path_exports, executables):
    """
    :type application_identifier: str
    :type home: str
    :type home_path_exports: list[str]
    :type executables: list[str]
    :rtype: tuple
    """
    pathing = _produce_pathing(application_identifier=application_identifier, home=home,
                               home_path_exports=home_path_exports, executables=executables)
    # Need to expose imports to other tools that may need them during installation...
    export_paths = list()

    for p in pathing[1]:  # type: str
        export_paths.append(p.replace('IMMORTALS_PATH', 'PATH'))

    return export_paths + pathing[0]


# noinspection PyPep8Naming
class SubInstaller:
    """
    :type configuration: SubInstallerConfiguration
    """

    def __init__(self, configuration):
        """
        :type configuration: SubInstallerConfiguration
        """
        self.configuration = configuration

    def produce_installation_lines(self, identifier):
        """
        :type identifier: str
        :rtype: list[str]
        """
        r_val = list()

        r_val.append('# ' + identifier + ': Installing additional subcomponents.')

        command = self.configuration.installationCommand
        for pkg in self.configuration.packageList:
            command += (' "' + pkg + '"')

        r_val.append(command)

        # if home is not None and len(self.configuration.requiredExecutables) > 0:
        #     r_val += produce_setup_pathing(application_identifier=identifier,
        #                                    home=home,
        #                                    home_path_exports=None,
        #                                    executables=self.configuration.requiredExecutables)

        return r_val


# noinspection PyPep8Naming
class Installer:
    """
    :type configuration: InstallerConfiguration
    :type sub_installer: SubInstaller
    """

    def __init__(self, configuration):
        """
        :type configuration: InstallerConfiguration
        """
        self.configuration = configuration
        self.sub_installer = None if configuration.subInstaller is None else SubInstaller(configuration.subInstaller)

        self._generated_installation_commands = None
        self._generated_initialization_commands = None

    def _get_existing_home(self):
        """
        :rtype: str or None
        """
        if self.configuration.homeExportVariable is None:
            return None
        else:
            return os.getenv(self.configuration.homeExportVariable)

    def _executables_exist_in_existing_home(self):
        """
        :rtype: bool
        """

        if self.configuration.homeExportVariable is None:
            return False

        home = self._get_existing_home()

        if home is None:
            return False

        for executable in self.configuration.requiredExecutables:
            if not os.path.exists(os.path.join(home, executable)):
                return False

        return True

    def _executables_exist_in_target_home(self, home):
        """
        :type home: str
        :rtype: bool
        """
        if home is None:
            return False

        for executable in self.configuration.requiredExecutables:
            if not os.path.exists(os.path.join(home, executable)):
                return False

        return True

    def _executables_exist_in_path(self):
        """
        :rtype: bool
        """
        for executable in self.configuration.requiredExecutables:
            exe = executable.split('/')[-1]
            if subprocess.call(['which', exe], stderr=subprocess.PIPE, stdout=subprocess.PIPE) != 0:
                return False

        return True

    def _get_installed_version(self, home):
        """
        :type home: str
        :rtype: str
        """
        command = self.configuration.versionExtractionCommand.split(' ')

        if home is None:
            command[0] = subprocess.check_output(['which', command[0]]).decode().replace('\n', '')

        else:
            for exe in self.configuration.requiredExecutables:
                if exe.endswith(command[0]):
                    command[0] = exe
                    break

        if command[0].endswith('.jar'):
            command.insert(0, '-jar')
            command.insert(0, 'java')

        if home is None:
            version_string = subprocess.check_output(command, stderr=subprocess.STDOUT).decode()
        else:
            version_string = subprocess.check_output(command, stderr=subprocess.STDOUT, cwd=home).decode()

        match = re.search(self.configuration.versionExtractionRegex, version_string)
        return match.group()

    def _is_installed_proper_version(self, home):
        """
        :type home: str or None
        :rtype: bool
        """
        if self.configuration.versionExtractionCommand is None and self.configuration.versionExtractionRegex is None:
            # Version does not matter
            return True
        else:
            version = self._get_installed_version(home=home)
            return version.startswith(self.configuration.version)

    def _has_missing_components(self):
        """
        :rtype: bool
        """
        if not os.path.exists(parse_path(full_path=self.configuration.home)):
            return True

        for exe in self.configuration.requiredExecutables:
            if not os.path.exists(os.path.join(parse_path(full_path=self.configuration.home), exe)):
                return True

        return False

    def _produce_package_installation_lines(self, identifier):
        """
        :type identifier: str
        :rtype: list[str]
        """
        r_val = list()

        if len(self.configuration.packageDependencies) > 0:
            r_val.append('# ' + identifier + ': Installing package dependencies.')
            config = resolve_platform_configuration()

            installation_command = config.packageManagerInstallationCommand

            for c in config.packageManagerInitCommands:
                r_val.append(c)

            for p in list(self.configuration.packageDependencies):
                installation_command += ' ' + p

            r_val.append(installation_command)

        return r_val

    def _get_install_lines_use_current_app(self):
        """
        :rtype: list[str]
        """
        r_val = list()
        r_val.append('# ' + self.configuration.identifier + ': Currently installed version meets requirements.')

        # r_val += self._produce_package_installation_lines(self.configuration.identifier)

        home = self._get_existing_home()

        if home is not None and (len(self.configuration.requiredExecutables) > 0):
            r_val += produce_setup_pathing(application_identifier=self.configuration.identifier,
                                           home=home,
                                           home_path_exports=self.configuration.pathExports,
                                           executables=self.configuration.requiredExecutables)

        if self.sub_installer is not None:
            r_val += self.sub_installer.produce_installation_lines(self.configuration.identifier)

        return r_val

    def _get_init_lines_use_current_home(self):
        """
        :rtype: list[str]
        """
        r_val = list()

        if parse_path(full_path=self.configuration.home) == self._get_existing_home():
            r_val.append('# ' + self.configuration.identifier + ': Exporting required home variable')
        else:
            r_val.append('# ' + self.configuration.identifier + ': Exporting required existing home variable')

        r_val.append('export ' + self.configuration.homeExportVariable +
                     '=' + parse_path(full_path=self._get_existing_home()))

        r_val += produce_init_pathing(application_identifier=self.configuration.identifier,
                                      home=self._get_existing_home(),
                                      home_path_exports=self.configuration.pathExports,
                                      executables=self.configuration.requiredExecutables)

        return r_val

    def _get_install_lines_install_new_app(self):
        """
        :rtype: list[str]
        """
        r_val = list()

        r_val.append('# ' + self.configuration.identifier +
                     ': Not installed, unresolvable, wrong version, or missing executables. Installing...')

        target_home = parse_path(self.configuration.home)

        if target_home is not None and os.path.exists(target_home) \
                and datatypes.installation_configuration.installationRoot in target_home \
                and not (self._executables_exist_in_target_home(target_home)
                         and self._is_installed_proper_version(target_home)):
            tmp = (target_home[:-1] if target_home.endswith('/') else target_home)
            tmp = os.path.join(datatypes.installation_configuration.tempDirectory, tmp[tmp.rfind('/') + 1:])
            tmp = (tmp[:-1] if tmp.endswith('/') else tmp) + '-' + str(int(time.time()))
            r_val.append('mv "' + target_home + '" "' + tmp + '"')

        r_val += self._produce_package_installation_lines(self.configuration.identifier)

        if not (self._executables_exist_in_target_home(target_home)
                and self._is_installed_proper_version(target_home)):
            r_val += self.configuration.setupCommands

        if target_home is not None:
            r_val += produce_setup_pathing(application_identifier=self.configuration.identifier,
                                           home=target_home,
                                           home_path_exports=self.configuration.pathExports,
                                           executables=self.configuration.requiredExecutables)

        if self.sub_installer is not None:
            r_val += self.sub_installer.produce_installation_lines(self.configuration.identifier)

        return r_val

    def _get_init_lines_install_new_app(self):
        """
        :rtype: list[str]
        """
        return produce_init_pathing(application_identifier=self.configuration.identifier,
                                    home=parse_path(full_path=self.configuration.home),
                                    home_path_exports=self.configuration.pathExports,
                                    executables=self.configuration.requiredExecutables)

    def _get_init_lines_export_new_home(self):
        """
        :rtype: list[str]
        """
        r_val = list()
        r_val.append('# ' + self.configuration.identifier + ': Exporting required home variable')
        r_val.append('export ' + self.configuration.homeExportVariable +
                     '=' + parse_path(full_path=self.configuration.home))

        r_val += produce_init_pathing(application_identifier=self.configuration.identifier,
                                      home=parse_path(full_path=self.configuration.home),
                                      home_path_exports=self.configuration.pathExports,
                                      executables=self.configuration.requiredExecutables)

        return r_val

    def _get_init_lines_ignore_home(self):
        """
        :rtype: list[str]
        """
        return [
            '# ' + self.configuration.identifier + ': Not exporting home since it is not required.'
        ]

    def _get_init_lines_use_current_executables(self):
        """
        :rtype: list[str]
        """
        return [
            '# ' + self.configuration.identifier + ': Using existing executables.'
        ]

    def _generate_all_commands(self):
        installation_lines = list()
        initialization_lilnes = list()

        if self.configuration.homeExportRequired:
            if self._get_existing_home() is not None \
                    and self._executables_exist_in_existing_home() \
                    and self._is_installed_proper_version(self._get_existing_home()):
                installation_lines += self._get_install_lines_use_current_app()
                initialization_lilnes += self._get_init_lines_use_current_home()

            else:
                installation_lines += self._get_install_lines_install_new_app()
                initialization_lilnes += self._get_init_lines_export_new_home()

        else:
            if self._executables_exist_in_path() \
                    and self._is_installed_proper_version(home=None):
                installation_lines += self._get_install_lines_use_current_app()
                initialization_lilnes += self._get_init_lines_ignore_home()
                initialization_lilnes += self._get_init_lines_use_current_executables()

            elif self._get_existing_home() is not None \
                    and self._executables_exist_in_existing_home() \
                    and self._is_installed_proper_version(self._get_existing_home()):
                installation_lines += self._get_install_lines_use_current_app()
                initialization_lilnes += self._get_init_lines_ignore_home()

            else:
                installation_lines += self._get_install_lines_install_new_app()
                initialization_lilnes += self._get_init_lines_install_new_app()

        self._generated_installation_commands = installation_lines
        self._generated_initialization_commands = initialization_lilnes

    def get_installation_commands(self):
        """
        :rtype: list[str]
        """
        if self._generated_installation_commands is None:
            self._generate_all_commands()

        return copy.deepcopy(self._generated_installation_commands)

    def get_initialization_commands(self):
        """
        :rtype: list[str]
        """
        if self._generated_initialization_commands is None:
            self._generate_all_commands()

        return copy.deepcopy(self._generated_initialization_commands)

    def get_superuser_setup_commands(self):
        """
        :rtype: list[str]
        """
        if self.configuration.sudoSetupCommands is not None:
            return copy.copy(self.configuration.sudoSetupCommands)
