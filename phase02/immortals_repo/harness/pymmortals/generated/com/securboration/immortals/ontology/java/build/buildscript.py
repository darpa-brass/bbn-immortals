from pymmortals.generated.com.securboration.immortals.ontology.identifier.hasuuid import HasUuid


# noinspection PyPep8Naming
class BuildScript(HasUuid):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 buildScriptContents: str = None,
                 hash: str = None,
                 uuid: str = None):
        super().__init__()
        self.buildScriptContents = buildScriptContents
        self.hash = hash
        self.uuid = uuid
