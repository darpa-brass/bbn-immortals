from integrationtest.datatypes.serializable import Serializable


# noinspection PyPep8Naming
class ClassFileCoverage(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 branchesCovered: int = None,
                 branchesMissed: int = None,
                 complexityCovered: int = None,
                 complexityMissed: int = None,
                 identifier: str = None,
                 instructionsCovered: int = None,
                 instructionsMissed: int = None,
                 linesCovered: int = None,
                 linesMissed: int = None,
                 methodsCovered: int = None,
                 methodsMissed: int = None):
        super().__init__()
        self.branchesCovered = branchesCovered
        self.branchesMissed = branchesMissed
        self.complexityCovered = complexityCovered
        self.complexityMissed = complexityMissed
        self.identifier = identifier
        self.instructionsCovered = instructionsCovered
        self.instructionsMissed = instructionsMissed
        self.linesCovered = linesCovered
        self.linesMissed = linesMissed
        self.methodsCovered = methodsCovered
        self.methodsMissed = methodsMissed
