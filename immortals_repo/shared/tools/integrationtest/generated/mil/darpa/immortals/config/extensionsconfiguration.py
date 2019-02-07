from integrationtest.datatypes.serializable import Serializable
from integrationtest.generated.mil.darpa.immortals.config.extensions.aqlbrassconfiguration import AqlBrassConfiguration
from integrationtest.generated.mil.darpa.immortals.config.extensions.castorconfiguration import CastorConfiguration
from integrationtest.generated.mil.darpa.immortals.config.extensions.hddrassconfiguration import HddRassConfiguration
from integrationtest.generated.mil.darpa.immortals.config.extensions.immortalizerconfiguration import ImmortalizerConfiguration
from integrationtest.generated.mil.darpa.immortals.config.extensions.knowledgerepogradlepluginconfiguration import KnowledgeRepoGradlePluginConfiguration
from integrationtest.generated.mil.darpa.immortals.config.extensions.partiallibraryupdatesconfiguration import PartialLibraryUpdatesConfiguration
from integrationtest.generated.mil.darpa.immortals.config.extensions.voltdbconfiguration import VoltDBConfiguration


# noinspection PyPep8Naming
class ExtensionsConfiguration(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 aqlbrass: AqlBrassConfiguration = None,
                 castor: CastorConfiguration = None,
                 hddrass: HddRassConfiguration = None,
                 immortalizer: ImmortalizerConfiguration = None,
                 krgp: KnowledgeRepoGradlePluginConfiguration = None,
                 partiallibraryupgrade: PartialLibraryUpdatesConfiguration = None,
                 producedTtlOutputDirectory: str = None,
                 voltdb: VoltDBConfiguration = None):
        super().__init__()
        self.aqlbrass = aqlbrass
        self.castor = castor
        self.hddrass = hddrass
        self.immortalizer = immortalizer
        self.krgp = krgp
        self.partiallibraryupgrade = partiallibraryupgrade
        self.producedTtlOutputDirectory = producedTtlOutputDirectory
        self.voltdb = voltdb
