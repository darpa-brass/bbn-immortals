"""
Creates an application instance for a given application configuration
"""
from .platforms.android import androidapplication

from .platforms.java import javaapplication
from pymmortals.interfaces import AbstractApplication


def create_application_instance(application_configuration):
    """
    :rtype: AbstractApplication
    """
    platform = application_configuration.deploymentPlatformEnvironment

    if platform == 'android' or platform == 'android_emulator' or platform == 'android_docker' \
            or platform == 'android_dynamicanalysis' or platform == 'android_staticanalysis':
        return androidapplication.AndroidApplication(application_configuration)

    elif platform == 'java' or platform == 'java_local':
        return javaapplication.JavaApplication(application_configuration)
    else:
        raise Exception('Unexpected platform "' + platform + '"!')
