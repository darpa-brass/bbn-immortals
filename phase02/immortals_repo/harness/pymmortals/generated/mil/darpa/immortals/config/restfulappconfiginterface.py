from pymmortals.generated.mil.darpa.immortals.config.appconfiginterface import AppConfigInterface


# noinspection PyPep8Naming
class RestfulAppConfigInterface(AppConfigInterface):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()
