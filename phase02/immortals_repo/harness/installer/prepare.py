#!/usr/bin/env python3.4

import argparse
import json
import os
import stat

from installer import datatypes
from installer.datatypes import EnvironmentTag, TargetInstallationPlatform
from installer.installation import Installer
from installer.utils import resolve_platform

_parser = argparse.ArgumentParser(formatter_class=argparse.RawDescriptionHelpFormatter)
_parser.add_argument('--installation-dir', action='store',
                     help="Specifies the directory to install dependencies to and runs in unattended mode.")


def construct_epilog():
    datatypes.init_configuration("")
    epilog = "Environment Options [default=FULL]:\n"

    tag_length = 0
    for et in EnvironmentTag:
        tag_length = max(tag_length, len(et.name))
    tag_length += 4

    for et in EnvironmentTag:  # type: EnvironmentTag
        epilog += ('  ' + et.name.ljust(tag_length) + et.description) + ':\n'

        deps = [a['identifier']
                for a in datatypes.installation_configuration_d['applications']
                if a['environmentTag'] in et.included_tags]

        deps.sort()

        epilog += ('  ' + ''.ljust(tag_length) + ', '.join(deps) + '\n\n')
    return epilog


_parser.epilog = construct_epilog()


def install(environment_tags, install_dir):
    """
    :type environment_tags: List[str]
    :type install_dir: str
    """

    all_environment_tags = set()
    for et in environment_tags:
        all_environment_tags = all_environment_tags.union(EnvironmentTag[et].included_tags)

    parent_installation_root = install_dir

    if parent_installation_root.endswith('/'):
        parent_installation_root = parent_installation_root[:-1]
    parent_installation_root = parent_installation_root[:parent_installation_root.rfind('/')]
    if not os.path.exists(parent_installation_root):
        raise Exception(
            'Parent directory "' + parent_installation_root + '" of installation root "'
            + install_dir + '" does not exist! Exiting setup!')

    datatypes.init_configuration(installation_root=install_dir)
    config = datatypes.installation_configuration
    applications = config.applications

    platform = config.targetInstallationPlatforms[resolve_platform()]  # type: TargetInstallationPlatform

    master_setup_lines = list()
    master_setup_lines.append('#!/usr/bin/env bash')
    master_setup_lines.append('')
    master_setup_lines.append('set -e')
    init_lines = list()
    init_lines.append('#!/usr/bin/env bash')
    init_lines.append('')

    if not os.path.exists(install_dir):
        master_setup_lines.append('mkdir ' + install_dir)

    bin_path = os.path.join(install_dir, 'bin')
    if not os.path.exists(bin_path):
        master_setup_lines.append('mkdir ' + bin_path)

    env_path = os.getenv('PATH')
    if bin_path not in env_path:
        master_setup_lines.append('PATH=' + bin_path + ':' + env_path)

    parent_temp_dir = config.tempDirectory
    if parent_temp_dir.endswith('/'):
        parent_temp_dir = parent_temp_dir[:-1]
    parent_temp_dir = parent_temp_dir[:parent_temp_dir.rfind('/')]
    if not parent_temp_dir.endswith('/'):
        parent_temp_dir += '/'

    if not install_dir.endswith('/'):
        install_dir += '/'

    if install_dir != parent_temp_dir and not os.path.exists(parent_temp_dir):
        raise Exception(
            'Parent directory "' + parent_temp_dir + '" of temporary directory "'
            + install_dir + '" does not exist! Exiting setup!')

    if not os.path.exists(config.tempDirectory):
        master_setup_lines.append('mkdir ' + config.tempDirectory)

    master_setup_lines.append('')

    immortals_environment_config = {
        "dataRoot": "{immortalsRoot}/INPUT_DATA",
        "logFile": "{immortalsRoot}/LOG",
        "artifactRoot": "{immortalsRoot}/ARTIFACTS",
        "testAdapter.port": 55555,
        "testAdapter.url": "127.0.0.1",
        "testAdapter.reportRawData": False,
        "testHarness.port": 44444,
        "testHarness.url": "127.0.0.1",
        "validation.pcapyMonitorInterface": "lo",
        "validation.minimumTestDurationMS": 70000,
        "validationEnvironment.displayAndroidEmulatorGui": False,
        "validationEnvironment.androidEmulatorQemuArgs": ["-m", "512M", "-smp", "2"],
        "visualization.enabled": False,
        "visualization.enableImmortalsDashboard": False,
        "debugMode": True
    }
    immortals_environment_config_str = json.dumps(immortals_environment_config, indent=4, separators=(',', ': '))

    master_setup_lines.append("if [ ! -e '" + os.path.join(install_dir, 'environment.json') + "' ];then")
    master_setup_lines.append(
        "  echo '" + immortals_environment_config_str + "' > " + os.path.join(install_dir, 'environment.json'))
    master_setup_lines.append("fi")
    master_setup_lines.append('\n')

    packages = platform.requiredPlatformPackages
    if packages is not None and len(packages) > 0:
        master_setup_lines.append("# Global System Requirements")
        master_setup_lines += platform.packageManagerInitCommands
        master_setup_lines.append(
            platform.packageManagerInstallationCommand + ' ' + ' '.join(platform.requiredPlatformPackages)
        )

    boot_lines = list()

    all_app_setup_lines = list()

    for app in applications:
        if app.environmentTag.name in all_environment_tags:
            app = Installer(configuration=app)

            all_app_setup_lines.append("cd ${HOME}")

            all_app_setup_lines += app.get_installation_commands()
            all_app_setup_lines.append('')

            init_lines += app.get_initialization_commands()
            init_lines.append('')

            boot_lines += app.get_superuser_setup_commands()

    master_setup_lines.append('')

    master_setup_lines += boot_lines
    master_setup_lines.append("")
    master_setup_lines += all_app_setup_lines

    init_lines.append('export IMMORTALS_PATH=' + install_dir + 'bin:${IMMORTALS_PATH}')
    init_lines.append('export IMMORTALS_OVERRIDES=' + os.path.join(install_dir, 'environment.json'))
    init_lines.append('export PATH=${IMMORTALS_PATH}:${PATH}')

    finish_line = """echo '


IMMoRTALS dependencies successfully intalled.
Within the same directory where the "setup.sh" script was produced is an
 "immortalsrc" file. Please copy it to your home directory or the immortals
 repository root as ".immortalsrc". Please manually remove the "tmp" folder
 in the immortals components installation directory along with any previous
 versions of needed components with the timestamp appended to them.'"""

    master_setup_lines.append(finish_line)
    init_lines.append('if shopt -q login_shell; then')
    init_lines.append('  echo "IMMoRTALS dependencies loaded."')
    init_lines.append('fi')

    with open('setup.sh', 'w') as setup_file:
        for l in master_setup_lines:
            setup_file.write(l + '\n')

    st = os.stat('setup.sh')
    os.chmod('setup.sh', st.st_mode | stat.S_IEXEC)

    with open('immortalsrc', 'w') as init_file:
        for l in init_lines:
            init_file.write(l + '\n')

    print('Successfully produced "setup.sh" script for installation.')
    print('Please examine this script and execute it to install dependencies.')


def main():
    args = _parser.parse_args()
    environment_tags = [et.name for et in EnvironmentTag]

    if args.installation_dir is not None:
        install_dir = args.installation_dir
        if not install_dir.endswith('/'):
            install_dir += '/'

    else:
        install_dir = os.getenv('HOME') + '/.immortals/'
        try:
            val = input('Please enter a path for installing dependencies [' + install_dir + ']:')
        except SyntaxError or EOFError:
            val = install_dir

        if val is not "":
            if not val.endswith('/'):
                val += '/'
            install_dir = val

    install(environment_tags=environment_tags, install_dir=install_dir)


if __name__ == '__main__':
    main()
