from integrationtest.datatypes.serializable import Serializable
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.ataklitesubmissionmodel import ATAKLiteSubmissionModel
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.globalmodel.globalsubmissionmodel import GlobalSubmissionModel
from integrationtest.generated.mil.darpa.immortals.core.api.ll.phase2.martimodel.martisubmissionmodel import MartiSubmissionModel


# noinspection PyPep8Naming
class SubmissionModel(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 atakLiteClientModel: ATAKLiteSubmissionModel = None,
                 globalModel: GlobalSubmissionModel = None,
                 martiServerModel: MartiSubmissionModel = None,
                 sessionIdentifier: str = None):
        super().__init__()
        self.atakLiteClientModel = atakLiteClientModel
        self.globalModel = globalModel
        self.martiServerModel = martiServerModel
        self.sessionIdentifier = sessionIdentifier
