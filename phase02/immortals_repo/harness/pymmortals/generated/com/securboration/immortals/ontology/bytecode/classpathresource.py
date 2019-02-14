from pymmortals.generated.com.securboration.immortals.ontology.bytecode.classpathelement import ClasspathElement


# noinspection PyPep8Naming
class ClasspathResource(ClasspathElement):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 binaryForm: bytes = None,
                 hash: str = None,
                 name: str = None):
        super().__init__(binaryForm=binaryForm, hash=hash, name=name)
