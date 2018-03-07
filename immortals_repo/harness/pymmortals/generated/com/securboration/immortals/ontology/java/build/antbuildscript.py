from pymmortals.generated.com.securboration.immortals.ontology.java.build.buildscript import BuildScript


# noinspection PyPep8Naming
class AntBuildScript(BuildScript):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 buildScriptContents: str = None,
                 hash: str = None,
                 uuid: str = None):
        super().__init__(buildScriptContents=buildScriptContents, hash=hash, uuid=uuid)
