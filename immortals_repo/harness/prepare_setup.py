#!/usr/bin/env python3

import json
import os
import stat
import sys

from pymmortals.datatypes.installation import InstallationConfiguration
from pymmortals.installation import Installer
from pymmortals.resources import resourcemanager
from pymmortals.utils import resolve_platform


def install(unattended=False):
    install_dir = os.getenv('HOME') + '/.immortals/'

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

    config = InstallationConfiguration.from_dict(resourcemanager.load_installation_configuration(),
                                                 {'installationRoot': install_dir,
                                                  "dataRoot": '/dev/null',
                                                  "logFile": "/dev/null",
                                                  "artifactRoot": '/dev/null'
                                                  })
    applications = config.applications

    resolve_platform()

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
        "validationEnvironment.displayAndroidEmulatorGui": True,
        "validationEnvironment.androidEmulatorQemuArgs": ["-m", "512M", "-smp", "2"],
        "visualization.enabled": True,
        "visualization.enableImmortalsDashboard": True,
        "debugMode": True
    }
    immortals_environment_config_str = json.dumps(immortals_environment_config, indent=4, separators=(',', ': '))

    setup_lines.append("if [ ! -e '" + os.path.join(install_dir, 'environment.json') + "' ];then")
    setup_lines.append(
        "  echo '" + immortals_environment_config_str + "' > " + os.path.join(install_dir, 'environment.json'))
    setup_lines.append("fi")

    if sys.platform == 'darwin':
        immortals_environment_config["validation.pcapyMonitorInterface"] = "lo0"

    for app in applications:
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
Within the same directory as the "setup.sh" script is an "immortalsrc" file.
 Please copy it to your home directory or the immortals repository root as
 ".immortalsrc". Please manually remove the "tmp" folder in the immortals
 components installation directory along with any previous versions of
 needed components with the timestamp appended to them.'"""

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
    print('')
    print('Also, "immortalsrc" was produce and must be copied to ~/.immortalsrc'
          ' or ../.immortalsrc (immortals home) to be picked up by the platform.')


def main():
    if len(sys.argv) == 2 and sys.argv[1] == '--unattended-setup':
        install(True)
    else:
        install()


if __name__ == '__main__':
    main()
