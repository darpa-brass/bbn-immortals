from integrationtest.datatypes.serializable import Serializable
from integrationtest.generated.mil.darpa.immortals.core.api.applications.analyticsconfig import AnalyticsConfig
from integrationtest.generated.mil.darpa.immortals.core.api.applications.postgresqlconfig import PostGreSqlConfig


# noinspection PyPep8Naming
class MartiConfig(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 analyticsConfig: AnalyticsConfig = None,
                 postGreSqlConfig: PostGreSqlConfig = None,
                 storageDirectory: str = None):
        super().__init__()
        self.analyticsConfig = analyticsConfig
        self.postGreSqlConfig = postGreSqlConfig
        self.storageDirectory = storageDirectory
