from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.measurement.cp1cp2.imagesizemegapixels import ImageSizeMegapixels


# noinspection PyPep8Naming
class OutputImageSizeMegapixels(ImageSizeMegapixels):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 numMegapixels: float = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(numMegapixels=numMegapixels, truthConstraint=truthConstraint)
