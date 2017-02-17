"""
Used to link platforms, applications, and identifiers together in a unified place
"""

from android import androidplatform_docker
from android import androidplatform_dynamicanalysis
from android import androidplatform_emulator
from android import androidplatform_staticanalysis
from java import javaplatform

"""
Creates a platform instance for a given application coniguration. This should never need to be called from anywhere other than an application instance (such as what is obtained by calling the above)
"""


def create_platform_instance(application_configuration, command_processor=None):
    platform = application_configuration.deploymentPlatformEnvironment

    if platform == "android_emulator":
        return androidplatform_emulator.AndroidEmulatorInstance(application_configuration=application_configuration,
                                                                command_processor=command_processor)

    elif platform == "android_docker":
        return androidplatform_docker.AndroidDockerEmulatorInstance(application_configuration)

    elif platform == "android_dynamicanalysis":
        return androidplatform_dynamicanalysis.AndroidDynamicAnalysisInstance(application_configuration)

    elif platform == "android_staticanalysis":
        return androidplatform_staticanalysis.AndroidStaticAnalysisInstance(application_configuration)

    elif platform == 'java_local':
        return javaplatform.JavaPlatform(application_configuration)
    else:
        raise Exception('Unexepcted platform "' + platform + '"!')
