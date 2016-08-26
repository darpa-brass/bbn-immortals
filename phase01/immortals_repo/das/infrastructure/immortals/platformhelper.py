"""
Used to link platforms, applications, and identifiers together in a unified place
"""
import androidplatform
import androidapplication
import javaapplication
import androidplatform_emulator
import androidplatform_docker
import androidplatform_dynamicanalysis
import androidplatform_staticanalysis
import javaplatform

"""
Creates an application instance for a given application configuration (see configurationmanager.py)
"""
def create_application_instance(execution_path, application_configuration, wipe_existing):
    platform = application_configuration.deployment_platform_environment

    if platform == 'android' or platform == 'android_emulator' or platform == 'android_docker' or platform == 'android_dynamicanalysis' or platform == 'android_staticanalysis':
        return androidapplication.AndroidApplication(execution_path, application_configuration, wipe_existing)

    elif platform == 'java' or platform == 'java_local':
        return javaapplication.JavaApplication(execution_path, application_configuration, wipe_existing)
    else:
        raise Exception('Unexpected platform "' + platform + '"!')


"""
Creates a platform instance for a given application coniguration. This should never need to be called from anywhere other than an application instance (such as what is obtained by calling the above)
"""
def create_platform_instance(execution_path, application_configuration, wipe_existing):
    platform = application_configuration.deployment_platform_environment

    if platform == "android_emulator":
        return androidplatform_emulator.AndroidEmulatorInstance(execution_path, application_configuration, wipe_existing)

    elif platform == "android_docker":
        return androidplatform_docker.AndroidDockerEmulatorInstance(execution_path, application_configuration, wipe_existing)

    elif platform == "android_dynamicanalysis":
        return androidplatform_dynamicanalysis.AndroidDynamicAnalysisInstance(execution_path, application_configuration, wipe_existing)

    elif platform == "android_staticanalysis":
        return androidplatform_staticanalysis.AndroidStaticAnalysisInstance(execution_path, application_configuration, wipe_existing)

    elif platform == 'java_local':
        return javaplatform.JavaPlatform(execution_path, application_configuration, wipe_existing)
    else:
        raise Exception('Unexepcted platform "' + platform + '"!')
