from pymmortals.generated.com.securboration.immortals.ontology.algorithm.algorithm import Algorithm
from pymmortals.generated.com.securboration.immortals.ontology.core.truthconstraint import TruthConstraint
from pymmortals.generated.com.securboration.immortals.ontology.functionality.dataproperties.compressed import Compressed
from typing import Type


# noinspection PyPep8Naming
class CompressedLossless(Compressed):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 compressionAlgorithm: Type[Algorithm] = None,
                 hidden: bool = None,
                 truthConstraint: TruthConstraint = None):
        super().__init__(compressionAlgorithm=compressionAlgorithm, hidden=hidden, truthConstraint=truthConstraint)
