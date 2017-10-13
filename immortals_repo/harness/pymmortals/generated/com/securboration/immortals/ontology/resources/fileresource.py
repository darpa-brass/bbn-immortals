from pymmortals.generated.com.securboration.immortals.ontology.property.property import Property
from pymmortals.generated.com.securboration.immortals.ontology.resources.filepermission import FilePermission
from pymmortals.generated.com.securboration.immortals.ontology.resources.filesystemresource import FileSystemResource
from typing import List


# noinspection PyPep8Naming
class FileResource(FileSystemResource):
    _validator_values = dict()

    _types = dict()

    def __init__(self,
                 absolutePath: str = None,
                 humanReadableDescription: str = None,
                 permission: List[FilePermission] = None,
                 resourceProperty: List[Property] = None):
        super().__init__(humanReadableDescription=humanReadableDescription, resourceProperty=resourceProperty)
        self.absolutePath = absolutePath
        self.permission = permission
