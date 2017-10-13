"""
Used to link platforms, applications, and identifiers together in a unified place
"""

from pymmortals.datatypes.scenariorunnerconfiguration import ApplicationConfig, AndroidApplicationConfig, \
    JavaApplicationConfig
from pymmortals.scenariorunner.deploymentplatform import DeploymentPlatformInterface
from pymmortals.threadprocessrouter import ImmortalsSubprocess
# from .android import androidplatform_docker
# from .android import androidplatform_dynamicanalysis
from .android import androidplatform_emulator
# from .android import androidplatform_staticanalysis
from .java import javaplatform

"""
Creates a platform instance for a given application coniguration. This should never need to be called from anywhere
 other than an application instance (such as what is obtained by calling the above)
"""


def create_platform_instance(application_configuration: ApplicationConfig,
                             command_processor: ImmortalsSubprocess or None = None) -> DeploymentPlatformInterface:
    platform = application_configuration.deploymentPlatformEnvironment

    if platform == "android_emulator":
        if not isinstance(application_configuration, AndroidApplicationConfig):
            raise Exception('Cannot set up platform "' + platform + '" with configuration type "' +
                            application_configuration.__class__.__name__)
        return androidplatform_emulator.AndroidEmulatorInstance(application_configuration=application_configuration,
                                                                command_processor=command_processor)

    # elif platform == "android_docker":
    #     if not isinstance(application_configuration, AndroidApplicationConfig):
    #         raise Exception('Cannot set up platform "' + platform + '" with configuration type "' +
    #                         application_configuration.__class__.__name__)
    #
    #     return androidplatform_docker.AndroidDockerEmulatorInstance(application_configuration)
    #
    # elif platform == "android_dynamicanalysis":
    #     if not isinstance(application_configuration, AndroidApplicationConfig):
    #         raise Exception('Cannot set up platform "' + platform + '" with configuration type "' +
    #                         application_configuration.__class__.__name__)
    #
    #     return androidplatform_dynamicanalysis.AndroidDynamicAnalysisInstance(application_configuration)
    #
    # elif platform == "android_staticanalysis":
    #     if not isinstance(application_configuration, AndroidApplicationConfig):
    #         raise Exception('Cannot set up platform "' + platform + '" with configuration type "' +
    #                         application_configuration.__class__.__name__)
    #
    #     return androidplatform_staticanalysis.AndroidStaticAnalysisInstance(application_configuration)

    elif platform == 'java_local':
        if not isinstance(application_configuration, JavaApplicationConfig):
            raise Exception('Cannot set up platform "' + platform + '" with configuration type "' +
                            application_configuration.__class__.__name__)
        return javaplatform.JavaPlatform(application_configuration)
    else:
        raise Exception('Unexepcted platform "' + platform + '"!')
