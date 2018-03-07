from pymmortals.datatypes.serializable import Serializable
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.assertionbindingsite import AssertionBindingSite
from pymmortals.generated.com.securboration.immortals.ontology.property.impact.prescriptivecauseeffectassertion import PrescriptiveCauseEffectAssertion


# noinspection PyPep8Naming
class PrescriptiveBindingInstance(Serializable):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 bindingSite: AssertionBindingSite = None,
                 mitigationStrategy: PrescriptiveCauseEffectAssertion = None):
        super().__init__()
        self.bindingSite = bindingSite
        self.mitigationStrategy = mitigationStrategy
