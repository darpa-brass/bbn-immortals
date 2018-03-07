from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class AdaptationsConfiguration(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 androidBuildToolsVersion: str = None,
                 androidCompileSdkVersion: int = None,
                 androidGradleToolsVersion: str = None,
                 androidMinSdkVersion: int = None,
                 androidSdkRoot: str = None,
                 androidTargetSdkVersion: int = None,
                 javaHome: str = None,
                 javaVersionCompatibility: str = None,
                 mavenPublishRepo: str = None,
                 publishVersion: str = None):
        super().__init__()
        self.androidBuildToolsVersion = androidBuildToolsVersion
        self.androidCompileSdkVersion = androidCompileSdkVersion
        self.androidGradleToolsVersion = androidGradleToolsVersion
        self.androidMinSdkVersion = androidMinSdkVersion
        self.androidSdkRoot = androidSdkRoot
        self.androidTargetSdkVersion = androidTargetSdkVersion
        self.javaHome = javaHome
        self.javaVersionCompatibility = javaVersionCompatibility
        self.mavenPublishRepo = mavenPublishRepo
        self.publishVersion = publishVersion
