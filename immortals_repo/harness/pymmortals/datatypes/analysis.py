from typing import List

from .serializable import Serializable


# noinspection PyPep8Naming
class Parameter(Serializable):
    _validator_values = {}

    def __init__(self,
                 providedByApplication: bool,
                 applicationVariableName: str = None,
                 classType: str = None,
                 values: List[str] = None):
        super().__init__()
        self.providedByApplication = providedByApplication
        self.applicationVariableName = applicationVariableName
        self.classType = classType
        self.values = values


# noinspection PyPep8Naming
class ConsumingPipeSpecification(Serializable):
    _validator_values = {}

    def __init__(self,
                 dependencyString: str,
                 classPackage: str,
                 constructorParameters: List[Parameter]):
        super().__init__()
        self.dependencyString = dependencyString
        self.classPackage = classPackage
        self.constructorParameters = constructorParameters


# noinspection PyPep8Naming
class DfuConfiguration(Serializable):
    _validator_values = {}

    def __init__(self,
                 dependencyString: str,
                 consumingPipeSpecification: ConsumingPipeSpecification):
        super().__init__()
        self.dependencyString = dependencyString
        self.consumingPipeSpecification = consumingPipeSpecification


# noinspection PyPep8Naming
class AugmentationSpecification(Serializable):
    _validator_values = {}

    def __init__(self,
                 sessionIdentifier: str,
                 controlPointUuid: str,
                 compositionTarget: str,
                 dfuCompositionSequence: List[DfuConfiguration]):
        super().__init__()
        self.sessionIdentifier = sessionIdentifier
        self.controlPointUuid = controlPointUuid
        self.compositionTarget = compositionTarget
        self.dfuCompositionSequence = dfuCompositionSequence


# noinspection PyPep8Naming
class AnalysisConfiguration(Serializable):
    _validator_values = {}

    def __init__(self, augmentationSpecifications: List[AugmentationSpecification]):
        super().__init__()
        self.augmentationSpecifications = augmentationSpecifications
