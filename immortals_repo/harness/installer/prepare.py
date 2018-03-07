#!/usr/bin/env python3.4

import argparse
import json
import os
import stat
import sys

from installer import datatypes
from installer.datatypes import EnvironmentTag, TargetInstallationPlatform
from installer.installation import Installer
from installer.utils import resolve_platform

_parser = argparse.ArgumentParser(formatter_class=argparse.RawDescriptionHelpFormatter)
_parser.add_argument('--unattended-setup', action='store_true')

_parser.add_argument('--environment', '-e', action='append', choices=[e.name for e in EnvironmentTag])


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


def install(environment_tags, unattended):
    """
    :type environment_tags: list[str]
    :type unattended: bool
    """
    install_dir = os.getenv('HOME') + '/.immortals/'

    all_environment_tags = set()
    for et in environment_tags:
        all_environment_tags = all_environment_tags.union(EnvironmentTag[et].included_tags)

    if not unattended:
        try:
            val = input('Please enter a path for installing dependencies [' + install_dir + ']:')
        except SyntaxError or EOFError:
            val = install_dir

        if val is not "":
            if not val.endswith('/'):
                val += '/'
            install_dir = val

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

    setup_lines = list()
    setup_lines.append('#!/usr/bin/env bash')
    setup_lines.append('')
    setup_lines.append('set -e')
    init_lines = list()
    init_lines.append('#!/usr/bin/env bash')
    init_lines.append('')

    if not os.path.exists(install_dir):
        setup_lines.append('mkdir ' + install_dir)

    bin_path = os.path.join(install_dir, 'bin')
    if not os.path.exists(bin_path):
        setup_lines.append('mkdir ' + bin_path)

    env_path = os.getenv('PATH')
    if bin_path not in env_path:
        setup_lines.append('PATH=' + bin_path + ':' + env_path)

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
        setup_lines.append('mkdir ' + config.tempDirectory)

    setup_lines.append('')

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

    setup_lines.append("if [ ! -e '" + os.path.join(install_dir, 'environment.json') + "' ];then")
    setup_lines.append(
        "  echo '" + immortals_environment_config_str + "' > " + os.path.join(install_dir, 'environment.json'))
    setup_lines.append("fi")
    setup_lines.append('\n')

    packages = platform.requiredPlatformPackages
    if packages is not None and len(packages) > 0:
        setup_lines.append("# Global System Requirements")
        setup_lines += platform.packageManagerInitCommands
        setup_lines.append(
            platform.packageManagerInstallationCommand + ' ' + ' '.join(platform.requiredPlatformPackages)
        )

    for app in applications:
        if app.environmentTag.name in all_environment_tags:
            app = Installer(configuration=app)
            setup_lines += app.get_installation_commands()
            setup_lines.append('')

            init_lines += app.get_initialization_commands()
            init_lines.append('')

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

    setup_lines.append(finish_line)
    init_lines.append('if shopt -q login_shell; then')
    if sys.platform == 'linux' or sys.platform == 'linux2':
        init_lines.append('  echo "IMMoRTALS dependencies loaded. Python granted raw network access."')
        pass
    else:
        init_lines.append('  echo "IMMoRTALS dependencies loaded."')
    init_lines.append('fi')

    with open('setup.sh', 'w') as setup_file:
        for l in setup_lines:
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

    if args.environment is None:
        environment_tags = [et.name for et in EnvironmentTag]
    else:
        environment_tags = args.environment

    install(unattended=args.unattended_setup, environment_tags=environment_tags)


if __name__ == '__main__':
    main()
