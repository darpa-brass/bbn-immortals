from pymmortals.generated.mil.darpa.immortals.config.extensioninterface import ExtensionInterface


# noinspection PyPep8Naming
class AppConfigInterface(ExtensionInterface):
    _validator_values = dict()

    _types = dict()

    def __init__(self):
        super().__init__()
