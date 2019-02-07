from integrationtest.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class DasConfiguration(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 gradleVersion: str = None,
                 javaVersionCompatibility: str = None,
                 publishVersion: str = None,
                 rootGroup: str = None,
                 slf4jVersion: str = None):
        super().__init__()
        self.gradleVersion = gradleVersion
        self.javaVersionCompatibility = javaVersionCompatibility
        self.publishVersion = publishVersion
        self.rootGroup = rootGroup
        self.slf4jVersion = slf4jVersion
