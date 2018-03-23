from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.mil.darpa.immortals.config.extensions.aqlbrassconfiguration import AqlBrassConfiguration
from pymmortals.generated.mil.darpa.immortals.config.extensions.castorconfiguration import CastorConfiguration
from pymmortals.generated.mil.darpa.immortals.config.extensions.hddrassconfiguration import HddRassConfiguration
from pymmortals.generated.mil.darpa.immortals.config.extensions.knowledgerepogradlepluginconfiguration import KnowledgeRepoGradlePluginConfiguration
from pymmortals.generated.mil.darpa.immortals.config.extensions.voltdbconfiguration import VoltDBConfiguration


# noinspection PyPep8Naming
class ExtensionsConfiguration(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 aqlbrass: AqlBrassConfiguration = None,
                 castor: CastorConfiguration = None,
                 hddrass: HddRassConfiguration = None,
                 krgp: KnowledgeRepoGradlePluginConfiguration = None,
                 producedTtlOutputDirectory: str = None,
                 voltdb: VoltDBConfiguration = None):
        super().__init__()
        self.aqlbrass = aqlbrass
        self.castor = castor
        self.hddrass = hddrass
        self.krgp = krgp
        self.producedTtlOutputDirectory = producedTtlOutputDirectory
        self.voltdb = voltdb
