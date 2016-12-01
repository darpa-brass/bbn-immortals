from ..adaptationresult import AdaptationResult


# noinspection PyPep8Naming
class SubmitResponse:
    """
    :type identifier: str
    :type adaptationResult: AdaptationResult
    """

    def __init__(self,
                 identifier,
                 adaptationResult
                 ):
        self.identifier = identifier
        self.adaptationResult = adaptationResult
