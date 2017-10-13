from pymmortals.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class BytecodeArtifactCoordinate(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 artifactId: str = None,
                 coordinateTag: str = None,
                 groupId: str = None,
                 version: str = None):
        super().__init__()
        self.artifactId = artifactId
        self.coordinateTag = coordinateTag
        self.groupId = groupId
        self.version = version
