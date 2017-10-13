import copy
import logging
import os
import re
from collections import OrderedDict
from enum import Enum
from typing import List, Dict, Set, Union, Optional

import javalang
from javalang.tree import EnumDeclaration, TypeDeclaration, ClassDeclaration, MethodDeclaration, ReferenceType, \
    FieldDeclaration, VariableDeclarator, TypeArgument, BasicType, ConstructorDeclaration, Literal, MemberReference, \
    FormalParameter, EnumConstantDeclaration, ClassReference, InterfaceDeclaration, Declaration, AnnotationDeclaration

from pymmortals.datatypes.root_configuration import get_configuration


class ConversionMethod(Enum):
    """
    Defines whether or not the pojo object values should be extracted from the variables or the get methods.
    """
    GETS = 0
    VARS = 1


_uuid_pattern = re.compile('^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$')

_logger = logging.getLogger('Pojoizer')

_java_value_map = {
    'int': 'int',
    'Integer': 'int',
    'short': 'int',
    'Short': 'int',
    'long': 'int',
    'Long': 'int',
    'float': 'float',
    'Float': 'float',
    'double': 'float',
    'Double': 'float',
    'boolean': 'bool',
    'Boolean': 'bool',
    'String': 'str',
    'char': 'str',
    'byte': 'bytes'
}

_built_ins = frozenset(['Serializable', 'List', 'Enum', 'Set', 'FrozenSet', 'Type'])

_import_type_omissions = frozenset(['bool', 'int', 'str', 'float', 'bytes', 'type', 'Description', 'P2CP1',
                                    'P2CP2', 'P2CP3'])


class DocumentationTag(Enum):
    P2CP1 = 0
    P2CP2 = 1
    P2CP3 = 2
    RESULT = 3


def _java_to_python_type(input_type: ReferenceType) -> str:
    """
    Given an input ReferenceType it returns the string reflecting the annotated Python type
    :param input_type: The input java ReferenceType
    :return: The source code annotation string for the corresponding python type
    """
    type_name = input_type.name
    is_list = False
    is_set = False

    # Array check
    if input_type.dimensions is not None:
        array_dimensions = len(input_type.dimensions)
        if array_dimensions == 1 and input_type.dimensions[0] is None:
            is_list = True
        elif array_dimensions > 1:
            raise Exception('Unexpected!')

    if type_name in _java_value_map:
        return_type = _java_value_map[type_name]

    elif type_name == 'LinkedList' or type_name == 'ArrayList' or type_name == 'List':
        assert (len(input_type.arguments) == 1)
        assert (isinstance(input_type.arguments[0], TypeArgument))
        assert (isinstance(input_type.arguments[0].type, ReferenceType))
        inner_type_name = input_type.arguments[0].type.name
        if inner_type_name in _java_value_map:
            return_type = _java_value_map[input_type.arguments[0].type.name]
        else:
            return_type = inner_type_name
        is_list = True

    elif type_name == 'HashSet' or type_name == 'Set':
        assert (len(input_type.arguments) == 1)
        assert (isinstance(input_type.arguments[0], TypeArgument))
        assert (isinstance(input_type.arguments[0].type, ReferenceType))
        inner_type_name = input_type.arguments[0].type.name
        if inner_type_name in _java_value_map:
            return_type = _java_value_map[input_type.arguments[0].type.name]
        else:
            return_type = inner_type_name
        is_set = True

    elif type_name == 'Class':
        if input_type.arguments is None:
            # It is a generic class
            return_type = 'Type'

        elif len(input_type.arguments) == 1:
            # It is a class of a specific type
            if input_type.arguments[0].pattern_type == 'extends':
                return_type = 'Type[' + input_type.arguments[0].type.name + ']'

            elif input_type.arguments[0].pattern_type == '?':
                return_type = 'Type'
            else:
                raise Exception('Unexpected')
        else:
            raise Exception('Unexpected')

    else:
        return_type = input_type.name

    if is_list and return_type != 'bytes':
        return 'List[' + return_type + ']'

    elif is_set:
        return 'Set[' + return_type + ']'

    else:
        return return_type


def _is_uuid(s: str) -> bool:
    """
    Determines if the specified String is a UUID
    :param s: the input string
    :return: If it is a UUID
    """
    return _uuid_pattern.match(s) is not None


def _get_description(declaration: Declaration) -> Union[str, None]:
    for a in declaration.annotations:
        if a.name == 'Description':
            return a.element.value

    return None


def _get_unstable(declaration: Declaration) -> bool:
    for a in declaration.annotations:
        if a.name == 'Unstable':
            return True

    return False


def _get_cps(declaration: Declaration) -> List[DocumentationTag]:
    apis = list()

    for a in declaration.annotations:
        if a.name == 'P2CP1':
            apis.append(DocumentationTag.P2CP1)

        elif a.name == 'P2CP2':
            apis.append(DocumentationTag.P2CP2)

        elif a.name == 'P2CP3':
            apis.append(DocumentationTag.P2CP3)
        elif a.name == 'Result':
            apis.append(DocumentationTag.RESULT)

    return apis


class FieldData:
    def __init__(self,
                 name: str,
                 type: str,
                 description: str = None,
                 apis: List[DocumentationTag] = None,
                 unstable: bool = False,
                 value: str = None):
        self.name = name
        self.raw_type = type
        self.description = description
        self.apis = apis
        self.unstable = unstable
        self.value = value

    def clone(self):
        return FieldData(
            name=self.name,
            type=self.raw_type,
            description=self.description,
            apis=list(self.apis),
            unstable=self.unstable,
            value=self.value
        )

    @classmethod
    def from_field_declaration(cls, declaration: FieldDeclaration,
                               include_private: bool) -> 'FieldData':

        if (include_private or 'private' not in declaration.modifiers) and 'transient' not in declaration.modifiers:
            if 'static' in declaration.modifiers:
                raise Exception('Static fields not currently supported!')

            assert (len(declaration.declarators) == 1)
            assert (isinstance(declaration.declarators[0], VariableDeclarator))
            field_name = declaration.declarators[0].name

            if isinstance(declaration.type, ReferenceType):
                field_type = _java_to_python_type(declaration.type)

            elif isinstance(declaration.type, BasicType):
                field_type = _java_value_map[declaration.type.name]

            else:
                raise Exception('Unexpected type "' + str(declaration.type) + "'!")

            unstable = _get_unstable(declaration=declaration)

            initializer = declaration.declarators[0].initializer
            if initializer is None:
                field_value = None
            else:
                assert isinstance(initializer, Literal), "Only Primitive initializers currently supported!"
                field_value = initializer.value

            assert (field_name is not None)
            assert (field_type is not None)

            description = _get_description(declaration=declaration)

            return FieldData(name=field_name, type=field_type,
                             description=description,
                             apis=_get_cps(declaration=declaration),
                             unstable=unstable, value=field_value)

    def all_types(self) -> List[str]:
        types: List[str] = list()

        t = self.raw_type

        unprocessed_inner = True

        while unprocessed_inner:
            unprocessed_inner = False

            if t.startswith('List[') and t.endswith(']'):
                types.append('List')
                t = t[5:-1]
                unprocessed_inner = True

            elif t.startswith('Set[') and t.endswith(']'):
                types.append('Set')
                t = t[4:-1]
                unprocessed_inner = True

            elif t.startswith('Type[') and t.endswith(']'):
                types.append('Type')
                t = t[5:-1]
                unprocessed_inner = True

            elif t.startswith('FrozenSet[') and t.endswith(']'):
                types.append('FrozenSet')
                t = t[10:-1]
                unprocessed_inner = True

            else:
                if t not in types:
                    types.append(t)

        return types

    def core_type(self) -> str:
        return self.all_types()[-1]


class AbstractPojoBuilderConfig:
    def __init__(self,
                 package: str,
                 class_name: str,
                 fields: List[FieldData],
                 description: str = None,
                 unstable: bool = False,
                 apis: List[DocumentationTag] = None,
                 inheritance: List[str] = None,
                 imports: Dict[str, str] = None,
                 wildcard_imports: List[str] = None):
        """
        :param package: The package the class should be placed in
        :param class_name: The name of the class
        :param fields: The data for the fields within the class
        :param inheritance: A list of classes which this class should inherit from
        :param imports: Imports th class needs
        :param wildcard_imports: Wildcard java imports (ex. "imort java.lang.*")
        """
        self.package = package
        self.class_name = class_name
        self.fields = fields
        self.description = description
        self.unstable = unstable
        self.apis = apis if apis is not None else list()
        self.inheritance = inheritance if inheritance is not None else list()
        self.imports: Dict[str, str] = imports if imports is not None else OrderedDict()
        self.wildcard_imports = wildcard_imports if wildcard_imports is not None else list()

    def add_imports_by_type_strings(self, t: List[str], path_classnames: Dict[str, Set[str]],
                                    superclass_imports: Dict[str, str] = None) -> bool:
        changed = False
        for v in t:
            changed = self._add_import_by_type_string(t=v, path_classnames=path_classnames,
                                                      superclass_imports=superclass_imports) or changed

        return changed

    def _add_import_by_type_string(self, t: str, path_classnames: Dict[str, Set[str]],
                                   superclass_imports: Dict[str, str] = None) -> bool:
        """
        Adds imports based on the provided type annotation
        :param t: The python type annotation
        :param path_classnames: a map of packages -> class names used to determine which packages to import classes from
        :return: Whether or not generated packages were added to this class constructor's imports
        """
        changed = False

        if t in _import_type_omissions:
            return False

        if t == 'Type' and 'Type' not in self.imports:
            self.imports['Type'] = 'typing'
            changed = True

        if t == 'List' and 'List' not in self.imports:
            self.imports['List'] = 'typing'
            changed = True

        if t == 'FrozenSet' and 'FrozenSet' not in self.imports:
            self.imports['FrozenSet'] = 'typing'
            changed = True

        if t == 'Enum' and 'Enum' not in self.imports:
            self.imports['Enum'] = 'enum'
            changed = True

        if t == 'Set' and 'Set' not in self.imports:
            self.imports['Set'] = 'typing'
            changed = True

        if t == 'Serializable' and 'Serializable' not in self.imports:
            self.imports['Serializable'] = 'pymmortals.datatypes.serializable'
            changed = True

        if t not in self.imports:
            if superclass_imports is not None and t in superclass_imports:
                self.imports[t] = superclass_imports[t]
                changed = True

            elif t in path_classnames[self.package]:
                if t != self.class_name:
                    self.imports[t] = self.package
                    changed = True

            elif t in _import_type_omissions:
                changed = False

            else:
                success = False
                if len(self.wildcard_imports) > 0:
                    for wi in self.wildcard_imports:
                        if wi in path_classnames.keys():
                            if t in path_classnames[wi]:
                                self.imports[t] = wi
                                success = True
                                changed = True

                if not success:
                    raise Exception('Could not match up an import for type "' + t + "'!")

        return changed


class PojoClassBuilderConfig(AbstractPojoBuilderConfig):
    def __init__(self,
                 package: str,
                 class_name: str,
                 fields: List[FieldData],
                 description: str = None,
                 unstable: bool = False,
                 apis: List[DocumentationTag] = None,
                 inherited_fields: List[FieldData] = None,
                 inheritance: List[str] = None,
                 imports: Dict[str, str] = None,
                 wildcard_imports: List[str] = None):
        """
        :param package: The package the class should be placed in
        :param class_name: The name of the class
        :param fields: The data for the fields within the class
        :param inherited_fields: Properties inherited from a superclass
        :param inheritance: A list of classes which this class should inherit from
        :param imports: Imports th class needs
        :param wildcard_imports: Wildcard java imports (ex. "imort java.lang.*")
        """
        super().__init__(package=package, class_name=class_name, fields=fields, description=description,
                         unstable=unstable, apis=apis, inheritance=inheritance,
                         imports=imports, wildcard_imports=wildcard_imports)
        self.inherited_fields: List[FieldData] = inherited_fields if inherited_fields is not None else list()

        # Add Serializable since very generated python object inherits it to aid serialization
        if 'Serializable' not in self.imports:
            self.imports['Serializable'] = 'pymmortals.datatypes.serializable'

        if 'Serializable' not in self.inheritance:
            self.inheritance.append('Serializable')

    def add_inherited_field(self, field: FieldData, path_classnames: Dict[str, Set[str]],
                            superclass_imports: Dict[str, str] = None) -> bool:
        """
        Adds a field to the config if it is not in it already
        :param field: The field to add
        :param path_classnames: A map package paths to sets of all the classnames contained within the path
        :param superclass_imports: The imports from the superclass
        :return: Whether or not the config was modified
        """
        changed = False
        # If the superclass field isn't in the subclass, add it
        if not any(x.name == field.name for x in self.inherited_fields):
            self.inherited_fields.append(field)
            changed = True

        # Try to add the imports
        changed = self.add_imports_by_type_strings(t=field.all_types(), path_classnames=path_classnames,
                                                   superclass_imports=superclass_imports) or changed

        return changed

    def swallow_superclass_typename(self, superclass_name: str, path_classnames: Dict[str, Set[str]],
                                    path_classes: Dict[str, Set[AbstractPojoBuilderConfig]]) -> bool:
        # Add the import for that class if necessary
        changed = self.add_imports_by_type_strings(t=[superclass_name], path_classnames=path_classnames)

        # And if it is not a built in
        if superclass_name not in _built_ins:
            # Get the configuration from the path_classdata
            superclass = next(
                (x for x in path_classes[self.imports[superclass_name]] if x.class_name == superclass_name),
                None)

            # Assert it is also a class and not an enum
            assert isinstance(superclass, PojoClassBuilderConfig), 'A Class cannot inherit from an Enum!'

            # And for each superclass field
            for field in superclass.fields:
                # Add it to the object
                changed = self.add_inherited_field(field=field, path_classnames=path_classnames,
                                                   superclass_imports=superclass.imports) or changed

            # And for each superclass inherited field
            for field in superclass.inherited_fields:
                # If the import has been added to the superclass

                if field.core_type() in superclass.imports or field.core_type() in _import_type_omissions:
                    # Add it to the object
                    changed = self.add_inherited_field(field=field, path_classnames=path_classnames,
                                                       superclass_imports=superclass.imports) or changed

        return changed


class PojoEnumBuilderConfig(AbstractPojoBuilderConfig):
    def __init__(self,
                 package: str,
                 class_name: str,
                 instance_labels: List[str],
                 fields: List[FieldData],
                 instance_parameter_fields: Dict[str, List[FieldData]],
                 apis: List[DocumentationTag] = None,
                 description: str = None,
                 unstable: bool = False,
                 inheritance: List[str] = None,
                 imports: Dict[str, str] = None,
                 wildcard_imports: List[str] = None):
        """
        :param package: The package the class should be placed in
        :param class_name: The name of the class
        :param instance_labels: The name of each enum
        :param fields: The data for the fields within the class
        :param inheritance: A list of classes which this class should inherit from
        :param imports: Imports th class needs
        :param wildcard_imports: Wildcard java imports (ex. "imort java.lang.*")
        """
        super().__init__(package=package, class_name=class_name, fields=fields, description=description,
                         unstable=unstable, apis=apis, inheritance=inheritance,
                         imports=imports, wildcard_imports=wildcard_imports)
        self.instance_labels: List[str] = instance_labels
        self.instance_parameter_fields: Dict[str, List[FieldData]] = instance_parameter_fields

        self.inherited_properties = list()

        if 'Enum' not in self.imports:
            self.imports['Enum'] = 'enum'

        if 'Enum' not in self.inheritance:
            self.inheritance.append('Enum')


def _fillout_matrix(matrix: List[List[str]]):
    max_column_width: List[int] = list()

    # Seed the initial column values
    for ci in range(len(matrix[0])):
        max_column_width.append(len(matrix[0][ci]))

    # Go through the columns and set the max width to the proper max possible value
    for ri in range(len(matrix)):
        for ci in range(len(matrix[ri])):
            max_column_width[ci] = max(max_column_width[ci], len(matrix[ri][ci]))

    # Pad the values to match that width
    for ri in range(len(matrix)):
        for ci in range(len(matrix[ri])):
            if matrix[ri][ci] == '---':
                matrix[ri][ci] = matrix[ri][ci].ljust(max_column_width[ci], '-')
            else:
                matrix[ri][ci] = matrix[ri][ci].ljust(max_column_width[ci], ' ')


def _generate_markdown(config: AbstractPojoBuilderConfig,
                       apis: Optional[Set[DocumentationTag]],
                       omit_unstable: bool) -> \
        List[str]:
    lines = list()
    api_indentation_level = '#### '

    if apis is None or len(apis.intersection(config.apis)) > 0 and \
            (not omit_unstable or not config.unstable):
        if isinstance(config, PojoEnumBuilderConfig):
            lines.append('\n' + api_indentation_level + config.class_name + '  \n')
            lines.append('__Type__: String Constant  \n')
            lines.append('__Description__: ' + config.description[1:-1] + '  \n\n')

            matrix = list()
            header = list()
            header.append('Values')
            separator = list()
            separator.append('---')

            for field in config.fields:
                header.append(field.name[:1].upper() + field.name[1:])
                separator.append('---')

            matrix.append(header)
            matrix.append(separator)

            for value in config.instance_labels:
                row = [value] + [k.value[1:-1] for k in config.instance_parameter_fields[value]]
                matrix.append(row)

            _fillout_matrix(matrix)

            for l in matrix:
                lines.append('| ' + ' | '.join(l) + ' |  \n')

        elif isinstance(config, PojoClassBuilderConfig):
            lines.append('\n' + api_indentation_level + config.class_name + '  \n')
            lines.append('__Type__: JSON Object  \n')
            lines.append('__Description__: ' + config.description[1:-1] + '  \n\n')

            matrix = list()
            header = list()
            header.append('Field')
            header.append('Type')
            header.append('Description')
            separator = list()
            separator.append('---')
            separator.append('---')
            separator.append('---')

            matrix.append(header)
            matrix.append(separator)

            object_rows = list()
            for field in config.fields:
                if apis is None or len(apis.intersection(field.apis)) > 0:
                    row = list()
                    row.append(field.name)
                    row.append('Generic JSON Object' if (field.unstable and omit_unstable) else field.raw_type)
                    row.append(field.description[1:-1])
                    object_rows.append(row)
            matrix = matrix + sorted(object_rows, key=lambda x: x[0])

            _fillout_matrix(matrix)

            for l in matrix:
                lines.append('| ' + ' | '.join(l) + ' |  \n')

        return lines


class Pojoizer:
    """
    Used to convert java pojos into python pojos
    """

    def __init__(self, conversion_method: ConversionMethod, target_directory: str, target_package: str,
                 do_generate_markdown: bool):
        """
        :param conversion_method: The method to use for conversion
        :param target_directory: The root source directory for the generated pojos
        :param target_package: The target package for the pojos (will precede the pojos existing package structure)
        :param do_generate_markdown: Whether or not to check for API documentation values
        """
        self.conversion_method = conversion_method
        self.target_directory = target_directory
        self.target_package = target_package
        self.do_generate_markdown = do_generate_markdown

        self.path_classes: Dict[str, Set[AbstractPojoBuilderConfig]] = OrderedDict()
        self.path_classnames: Dict[str, Set[str]] = OrderedDict()

        # If the target directory does not exist, create it along with the init file
        if not os.path.exists(self.target_directory):
            os.mkdir(self.target_directory)
            open(os.path.join(self.target_directory, '__init__.py'), 'a').close()

    def load_directory(self, root_directory: str, packages_root: str, ignore_files: List[str] = None):
        """
        :param root_directory: The root directory of the packages to load
        :param packages_root: The package paths within that root directory (with slashes, not dots)
        :param ignore_files: filepaths to ignore within the packages_root
        """

        full_path = os.path.join(root_directory, packages_root)
        for path, subdirs, files in os.walk(full_path):

            if len(files) > 0:
                subpath = path.replace(root_directory, '')
                if '/' in subpath:
                    for name in files:
                        file_path = os.path.join(path, name)
                        if ignore_files is None or not (file_path.replace(full_path, '')) in ignore_files:
                            self.load_file(filepath=file_path)
                        else:
                            _logger.warning('Ignoring file "' + file_path + '"')

    def load_file(self, filepath):
        # parse the ast
        tree = javalang.parse.parse(open(filepath, 'r').read())

        if len(tree.types) == 0:
            return

        elif len(tree.types) != 1:
            raise Exception('Cannot support multiple types!')

        class_path = tree.package.name
        class_imports: Dict[str, str] = OrderedDict()
        class_wildcard_imports: List[str] = list()

        # Add the class path if it is not known
        if class_path not in self.path_classes:
            self.path_classes[class_path] = set()

        if class_path not in self.path_classnames:
            self.path_classnames[class_path] = set()

        for i in tree.imports:
            # TODO: Scan for unused imports that aren't necessary instead of this sloppy sloppy check
            if not i.path.startswith('java.') \
                    and not i.path.startswith('com.google.gson') \
                    and not i.path.startswith('com.securboration.immortals.ontology.pojos') \
                    and not i.path.startswith('com.securboration.immortals.ontology.annotations') \
                    and not i.path.startswith('mil.darpa.immortals.core.api.annotations') \
                    and not i.path.startswith('com.securboration.immortals.uris'):
                i_split = i.path.split('.')
                class_imports['.'.join(i_split[-1:])] = '.'.join(i_split[:-1])
                if i.wildcard:
                    class_wildcard_imports.append('.'.join(i_split))
                else:
                    class_imports['.'.join(i_split[-1:])] = '.'.join(i_split[:-1])

        for t in tree.types:  # type: TypeDeclaration

            inheritance: List[str] = list()

            fields: List[FieldData] = list()
            apis: List[DocumentationTag] = None
            unstable = False

            if 'extends' in t.attrs and t.extends is not None:
                inheritance.append(t.extends.name)

            if 'implements' in t.attrs and t.implements is not None:
                for i in t.implements:
                    inheritance.append(i.name)

            class_name = t.name
            class_description = None
            if self.do_generate_markdown:
                class_description = _get_description(t)
                apis = _get_cps(t)
                unstable = _get_unstable(t)

            # if it is an enum
            if isinstance(t, EnumDeclaration):
                instance_labels: List[str] = list()
                instance_fields: Dict[str, List[FieldData]] = OrderedDict()
                param_names: List[str] = list()
                param_types: List[str] = list()

                # Search the declarations for the constructor and extract the parameter types from it
                for declaration in t.body.declarations:

                    # Also extract the field information to match it with the parameters and any additional metadata
                    if isinstance(declaration, FieldDeclaration):
                        fields.append(FieldData.from_field_declaration(
                            declaration=declaration,
                            include_private=(self.conversion_method == ConversionMethod.GETS)))

                    elif isinstance(declaration, ConstructorDeclaration):
                        param_type = None

                        for param in declaration.parameters:
                            assert (isinstance(param, FormalParameter))
                            param_name = param.name
                            if isinstance(param.type, ReferenceType):
                                param_type = _java_to_python_type(param.type)

                            elif isinstance(param.type, BasicType):
                                param_type = _java_value_map[param.type.name]

                            assert (param_name is not None)
                            assert (param_type is not None)
                            param_names.append(param_name)
                            param_types.append(param_type)

                    elif isinstance(declaration, MethodDeclaration):
                        _logger.warning('Ignoring method "' + declaration.name + '" in Enum "' + class_name + '"')

                    else:
                        raise Exception('Unexpected parameter type ' + declaration)

                # Validate each parameter name has a matching field
                # after a simple attempt a normalization
                pn: List[str] = list()
                for n in param_names:
                    if n.startswith("_"):
                        pn.append(n[1:])
                    else:
                        pn.append(n)

                param_names = pn

                assert (set(param_names).issubset(set([x.name for x in fields])))

                # Validate each parameter and field type match
                for i in range(0, len(param_names)):
                    pn = param_names[i]
                    pt = param_types[i]
                    assert (pt == next((x.raw_type for x in fields if x.name == pn), None))

                # Get the label and values for each enum instance
                for constant in t.body.constants:
                    instance_field_values: List[FieldData] = list()

                    assert (isinstance(constant, EnumConstantDeclaration))
                    instance_name = constant.name

                    if constant.arguments is not None and len(constant.arguments) > 0:

                        for idx in range(len(constant.arguments)):
                            arg = constant.arguments[idx]
                            instance_param_field = fields[idx].clone()

                            if isinstance(arg, Literal):
                                if arg.value == 'null':
                                    instance_param_field.value = 'None'
                                else:
                                    instance_param_field.value = arg.value

                            elif isinstance(arg, MemberReference):
                                instance_param_field.value = arg.qualifier + '.' + arg.member

                            elif isinstance(arg, ClassReference):
                                assert (isinstance(arg.type, ReferenceType))
                                instance_param_field.value = _java_to_python_type(arg.type)

                            else:
                                raise Exception('Unexpected argument type ' + arg)

                            instance_field_values.append(instance_param_field)

                        instance_fields[instance_name] = instance_field_values

                    assert (instance_name is not None)
                    instance_labels.append(instance_name)

                ccc = PojoEnumBuilderConfig(
                    package=class_path,
                    class_name=class_name,
                    instance_labels=instance_labels,
                    fields=fields,
                    description=class_description,
                    unstable=unstable,
                    apis=apis,
                    instance_parameter_fields=instance_fields,
                    inheritance=inheritance,
                    imports=class_imports,
                    wildcard_imports=class_wildcard_imports)

                self.path_classes[class_path].add(ccc)
                self.path_classnames[class_path].add(ccc.class_name)

            elif isinstance(t, ClassDeclaration):
                ignore = len([k for k in t.annotations if k.name == 'Ignore']) > 0
                if ignore:
                    _logger.warning('Ignoring "@Ignore"ed class ' + tree.package.name + '.' + class_name)

                else:
                    for declaration in t.body:
                        if self.conversion_method == ConversionMethod.GETS:
                            if isinstance(declaration, MethodDeclaration):
                                if declaration.name.startswith('get'):
                                    field_name = declaration.name[3].lower() + declaration.name[4:]
                                elif declaration.name.startswith('is'):
                                    field_name = declaration.name[2].lower() + declaration.name[3:]
                                else:
                                    field_name = None

                                if field_name is not None and declaration.return_type is not None:
                                    fields.append(
                                        FieldData(name=field_name, type=_java_to_python_type(declaration.return_type)))

                            elif not (isinstance(declaration, FieldDeclaration)
                                      or isinstance(declaration, ConstructorDeclaration)
                                      or isinstance(declaration, EnumDeclaration)):
                                if isinstance(declaration, ClassDeclaration):
                                    if len([k for k in declaration.annotations if k.name == 'Ignore']) > 0:
                                        _logger.warning(
                                            'Ignoring "@Ignore"ed subclass ' + tree.package.name + '.'
                                            + class_name + '.' + declaration.name)

                                    else:
                                        raise Exception('Unexpected non-ignored type "'
                                                        + str(declaration) + '" for file "' + filepath + ''"!")

                                raise Exception(
                                    'Unexpected type "' + str(declaration) + '" for file "' + filepath + ''"!")

                        elif self.conversion_method == ConversionMethod.VARS:
                            if isinstance(declaration, FieldDeclaration):
                                field = FieldData.from_field_declaration(declaration=declaration, include_private=False)
                                if field is not None:
                                    fields.append(field)

                            elif not (isinstance(declaration, MethodDeclaration)
                                      or isinstance(declaration, ConstructorDeclaration)
                                      or isinstance(declaration, EnumDeclaration)):
                                raise Exception(
                                    'Unexpected type "' + str(declaration) + '" for file "' + filepath + ''"!")

                        else:
                            raise Exception('Unexpected conversion method "' + self.conversion_method.name + '"!')

                    ccc = PojoClassBuilderConfig(
                        package=class_path,
                        class_name=class_name,
                        fields=fields,
                        description=class_description,
                        unstable=unstable,
                        apis=apis,
                        inherited_fields=None,
                        inheritance=inheritance,
                        imports=class_imports,
                        wildcard_imports=class_wildcard_imports
                    )

                    self.path_classes[class_path].add(ccc)
                    self.path_classnames[class_path].add(ccc.class_name)

            elif isinstance(t, InterfaceDeclaration):
                ccc = PojoClassBuilderConfig(
                    package=class_path,
                    class_name=class_name,
                    fields=list(),
                    description=None,
                    unstable=unstable,
                    apis=apis,
                    inheritance=inheritance,
                    imports=class_imports,
                    wildcard_imports=class_wildcard_imports
                )

                self.path_classes[class_path].add(ccc)
                self.path_classnames[class_path].add(ccc.class_name)

            elif not isinstance(t, AnnotationDeclaration):
                raise Exception('Unexpected type declaration "' + str(t) + '"!')

    def _preprocess_class_data(self):
        """
        Considers all the available packages and fills in missing cross-package implementation details
        """

        changed = True
        while changed:
            changed = False

            # For each class builder
            for k in self.path_classes.keys():
                for config in self.path_classes[k]:

                    # For each field in the class
                    for field in config.fields:
                        # Add the necessary import
                        changed = config.add_imports_by_type_strings(field.all_types(), self.path_classnames) or changed

                    # If it is a PojoEnumBuilder
                    if isinstance(config, PojoEnumBuilderConfig):
                        # And it has fields
                        if len(config.fields) > 0:
                            # Add FrozenSet since 'all_<field_name>' getters will be added for each field
                            changed = config.add_imports_by_type_strings(['FrozenSet'], self.path_classnames) or changed

                            for values in config.instance_parameter_fields.values():
                                for f in values:
                                    if f.raw_type == 'Type':
                                        if f.value is not 'None':
                                            config.add_imports_by_type_strings([f.value], self.path_classnames)
                                    elif '[Type' in f.raw_type or 'Type]' in f.raw_type:
                                        raise Exception("Pojoizing of nested types currently not supported!")

                    # Otherwise, if it is a class builder
                    elif isinstance(config, PojoClassBuilderConfig):
                        # For each inherited class
                        inherited_classnames = copy.deepcopy(config.inheritance)
                        for inherited_classname in inherited_classnames:
                            # If it is not the base serializable class
                            if inherited_classname != 'Serializable':
                                # Add it to the class. And if the class contins Serializable,
                                # remove it to prevent abstraction conflicts
                                if 'Serializable' in config.inheritance:
                                    config.inheritance.remove('Serializable')
                                changed = \
                                    config.swallow_superclass_typename(superclass_name=inherited_classname,
                                                                       path_classnames=self.path_classnames,
                                                                       path_classes=self.path_classes) or changed

                    else:
                        raise Exception("Unexpected config type provided!!")

        # Then sort the information within the builder configs
        for pc in self.path_classes.keys():
            values = self.path_classes[pc]
            for c in values:  # type: AbstractPojoBuilderConfig
                full_import_strings: Dict[str, str] = dict()
                # For each import
                for k in sorted(c.imports.keys()):
                    v = c.imports[k]

                    # Determine the full object path
                    if v in self.path_classnames and k in self.path_classnames[v]:
                        full_import_strings[self.target_package + '.' + v + '.' + k.lower() + '.' + k] = k
                    else:
                        full_import_strings[v + '.' + k] = k

                sorted_imports: Dict[str, str] = OrderedDict()

                # And in the sorted full object paths
                for k in sorted(full_import_strings.keys()):
                    # Updated the order and values of the paths referenced in the new imports list
                    v = full_import_strings[k]
                    sorted_imports[v] = k[:-(len(v) + 1)]

                # And set the new imports in the object
                c.imports = sorted_imports

                # Then sort the list objects
                c.inheritance = sorted(c.inheritance)
                c.wildcard_imports = sorted(c.wildcard_imports)
                c.fields.sort(key=lambda x: x.name)

                if isinstance(c, PojoClassBuilderConfig):
                    if c.inherited_fields is not None and len(c.inherited_fields) > 0:
                        c.inherited_fields.sort(key=lambda x: x.name)

                elif isinstance(c, PojoEnumBuilderConfig):
                    for p in c.instance_parameter_fields.values():  # type: List[FieldData]
                        p.sort(key=lambda x: x.name)

                else:
                    raise Exception('Unexpected type "' + c.__class__.__name__ + '"!')

        # Then remove unnecessary imports
        for k in self.path_classes.keys():
            for config in self.path_classes[k]:
                types: Set[str] = set(config.inheritance)
                for field in config.fields:
                    types = types.union(field.all_types())

                if isinstance(config, PojoClassBuilderConfig):
                    for field in config.inherited_fields:
                        types = types.union(field.all_types())

                if isinstance(config, PojoEnumBuilderConfig):
                    if config.fields is not None and len(config.fields) > 0:
                        types.add('FrozenSet')
                        for field in config.fields:
                            types = types.union(field.all_types())

                        for values in config.instance_parameter_fields.values():
                            for f in values:
                                if f.raw_type == 'Type':
                                    types.add(f.value)
                                elif '[Type' in f.raw_type or 'Type]' in f.raw_type:
                                    raise Exception("Pojoizing of nested types currently not supported!")
                                    # if enum which returns stuff with params add frozen set and do not remove
                                    # if value.

                import_keys = [k for k in config.imports.keys()]
                for i in import_keys:
                    if i not in types:
                        config.imports.pop(i)

    def do_construction(self):
        self._preprocess_class_data()

        for key in self.path_classes:
            for config in self.path_classes[key]:
                self._construct_file(config=config)

    def generate_pojo_spec_lines(self, apis: Set[DocumentationTag], omit_unstable: bool, root_class_name: str) -> \
            List[str]:
        self._preprocess_class_data()

        lines: List[str] = list()
        name_lines_map: Dict[str, List[str]] = dict()
        display_order = None

        for key in self.path_classes:
            for config in self.path_classes[key]:
                if root_class_name is not None and root_class_name == config.class_name:
                    display_order = self.get_display_order(config, apis, omit_unstable)

                if not config.unstable and (apis is None
                                            or len(apis.intersection(config.apis)) > 0):
                    md_lines = _generate_markdown(
                        config=config, apis=apis, omit_unstable=omit_unstable)

                    if root_class_name is None:
                        lines = lines + md_lines
                    else:
                        name_lines_map[config.class_name] = md_lines

        if root_class_name is not None:
            if display_order is None:
                raise Exception('Unable to find root class "' + root_class_name + '"!')

            else:
                # Remove unstable items from the display order if necessary
                for key in self.path_classes:
                    for config in self.path_classes[key]:
                        if config.unstable and config.class_name in display_order:
                            display_order.remove(config.class_name)

                if set(display_order) != set(name_lines_map.keys()):
                    display_extras = set(display_order).difference(set(name_lines_map.keys()))
                    found_extras = set(name_lines_map.keys()).difference(set(display_order))

                    msg = ''
                    if len(display_extras) != 0:
                        msg += ('Extra values found in the ordering determination: ' + str(display_extras) + '.\n')

                    if len(found_extras) != 0:
                        msg += ('Extra values found in tagged classes: ' + str(found_extras) + '.\n')

                    msg += 'Are you sure your provided example matches the API tags the java is annotated with?'

                    raise Exception(msg)

                for key in display_order:
                    lines = lines + name_lines_map[key]

        return lines

    def get_display_order(self, root_config: AbstractPojoBuilderConfig, apis: Set[DocumentationTag],
                          omit_unstable: bool):
        rval: List[str] = list()

        def _get_display_order(config: AbstractPojoBuilderConfig, display_order: List[str]):
            if isinstance(config, PojoClassBuilderConfig):
                all_fields = sorted((config.fields + config.inherited_fields), key=lambda x: x.name)
            else:
                all_fields = config.fields

            for field in all_fields:
                if not (field.unstable and omit_unstable):
                    if len(apis.intersection(field.apis)) > 0:
                        if field.core_type() not in _import_type_omissions and field.core_type() != config.class_name:
                            if field.core_type() not in display_order:
                                display_order.append(field.core_type())

                            field_classpath = config.imports[field.core_type()] \
                                [len(self.target_package)+1:-(len(field.core_type())+1)]
                            next_config: AbstractPojoBuilderConfig = next(
                                (x for x in self.path_classes[field_classpath] if x.class_name == field.core_type()),
                                None)
                            _get_display_order(next_config, display_order)

        rval.append(root_config.class_name)

        _get_display_order(root_config, rval)
        return rval

    def _construct_file(self, config: AbstractPojoBuilderConfig):

        pkg_dir = config.package.replace('.', '/')
        target_dir = os.path.join(self.target_directory, pkg_dir)

        # Create the directory structure if necessary
        if not (os.path.exists(target_dir)):
            parent_dir = self.target_directory

            for new_dir in pkg_dir.split('/'):
                parent_dir = os.path.join(parent_dir, new_dir)
                if not os.path.exists(parent_dir):
                    os.mkdir(parent_dir)
                    open(os.path.join(parent_dir, '__init__.py'), 'a').close()

        pkg = config.package + '.' + config.class_name.lower()
        if pkg in self.path_classes:
            target = os.path.join(self.target_directory, pkg.replace('.', '/'))
            if not os.path.exists(target):
                os.mkdir(target)

        lines = list()

        for clazz in config.imports.keys():
            package = config.imports[clazz]
            lines.append('from ' + package + ' import ' + clazz + '\n')

        lines.append('\n')
        lines.append('\n')

        # Class Declaration Line
        lines.append('# noinspection PyPep8Naming\n')
        if len(config.inheritance) <= 0:
            lines.append('class ' + config.class_name + ':\n')
        else:
            lines.append('class ' + config.class_name + '(' + ', '.join(config.inheritance) + '):\n')

        if isinstance(config, PojoEnumBuilderConfig):
            instantiation_lines = list()
            get_all_lines = list()

            if config.instance_parameter_fields is not None and len(config.instance_parameter_fields) > 0:
                lines.append('    def __init__(self')

                for field in config.fields:
                    lines.append(', ' + field.name + ': ' + field.raw_type)

                    instantiation_lines.append(
                        '        self.' + field.name + ': ' + field.raw_type + ' = ' +
                        field.name + '\n')

                    get_all_lines.append('\n    @classmethod')
                    get_all_lines.append('    def all_' + field.name +
                                         '(cls) -> FrozenSet[' + field.raw_type + ']:')
                    get_all_lines.append(
                        '        return frozenset([k.' + field.name + ' for k in list(cls)])')

                lines.append('):\n')
                lines = lines + instantiation_lines
                lines.append('\n')

                instance_lines = list()
                for label in config.instance_labels:
                    parameter_values = (k.value for k in config.instance_parameter_fields[label])

                    instance_lines.append('    ' + label + ' = (')
                    instance_lines.append(
                        '        ' + ',\n        '.join(parameter_values) + ')' + '\n')

                lines.append('\n'.join(instance_lines))

                lines.append('\n'.join(get_all_lines))

            else:
                counter = 0
                for value in config.instance_labels:
                    lines.append('    ' + value + " = '" + value + "'\n")
                    counter += 1

        elif isinstance(config, PojoClassBuilderConfig):
            lines.append('    _validator_values = dict()\n\n')
            lines.append('    _types = dict()\n\n')

            all_fields: List[FieldData] = sorted(config.fields + config.inherited_fields, key=lambda x: x.name)

            if len(all_fields) == 0:
                lines.append('    def __init__(self):\n')
                lines.append('        super().__init__()\n')

            else:
                sig = list()
                instantiation = list()
                lines.append('    def __init__(self,\n')

                inherited_properties_string = None
                for field in config.inherited_fields:
                    if inherited_properties_string is None:
                        inherited_properties_string = field.name + '=' + field.name
                    else:
                        inherited_properties_string += (', ' + field.name + '=' + field.name)

                if inherited_properties_string is not None:
                    instantiation.append('        super().__init__(' + inherited_properties_string + ')\n')
                else:
                    instantiation.append('        super().__init__()\n')

                for field in all_fields:  # type: FieldData
                    if field.raw_type == config.class_name:
                        raw_type = "'" + field.raw_type + "'"
                    elif ('[' + config.class_name + ']') in field.raw_type:
                        raw_type = field.raw_type.replace('[' + config.class_name + ']',
                                                          "['" + config.class_name + "']")
                    else:
                        raw_type = field.raw_type

                    if field.name == all_fields[-1].name:
                        sig.append('                 ' + field.name + ': ' + raw_type + ' = None):\n')
                    else:
                        sig.append('                 ' + field.name + ': ' + raw_type + ' = None,\n')

                    if not any(x.name == field.name for x in config.inherited_fields):
                        instantiation.append('        self.' + field.name + ' = ' + field.name + '\n')

                lines = lines + sig + instantiation

        pkg = config.package + '.' + config.class_name.lower()
        if pkg in self.path_classes:
            target = os.path.join(self.target_directory, pkg.replace('.', '/'), '__init__.py')
            open(target, 'w').writelines(lines)

        else:
            open(os.path.join(target_dir, config.class_name.lower() + '.py'), 'w').writelines(lines)


def pojoize():
    _target_package = 'pymmortals.generated'
    _target_module_directory = get_configuration().immortalsRoot + '/harness/' + _target_package.replace('.', '/') + '/'

    # # Pojoize the Triples
    p = Pojoizer(ConversionMethod.GETS,
                 target_directory=_target_module_directory,
                 target_package=_target_package,
                 do_generate_markdown=False)

    kr_root = os.path.join(get_configuration().immortalsRoot, 'knowledge-repo/vocabulary/ontology-vocab-domains/')

    kr_dirs = [
        'analysis/src/main/java/',
        'assertional/src/main/java/',
        'bytecode/src/main/java/',
        'core/src/main/java/',
        'cp/src/main/java/',
        'fm/src/main/java/',
        'functional/src/main/java/',
        'java-project/src/main/java/',
        'resources/src/main/java/',
        'sa/src/main/java/',
        'scratchpad/src/main/java/'
    ]

    kr_root_package = 'com/securboration/immortals/'
    # p.load_directory(p_dir)

    kr_ignore_files = [
        'ontology/gmei/GmeInterchangeFormatExample.java',
        'ontology/gmei/GmeInterchangeFormatUberExample.java'
    ]

    for d in kr_dirs:
        p.load_directory(root_directory=(os.path.join(kr_root, d)),
                         packages_root=kr_root_package,
                         ignore_files=kr_ignore_files)

    p.do_construction()

    # Pojoize the shared POJOs
    p = Pojoizer(conversion_method=ConversionMethod.VARS,
                 target_directory=_target_module_directory,
                 target_package=_target_package,
                 do_generate_markdown=True
                 )

    root_dir = os.path.join(get_configuration().immortalsRoot, 'shared/modules/core/src/main/java/')
    # packages_root = 'mil/darpa/immortals/core/api/ll/phase2'
    packages_root = 'mil/darpa/immortals/core/api'
    p.load_directory(root_directory=root_dir,
                     packages_root=packages_root)
    p.do_construction()


# def test():
#     import json
#     from pymmortals.generated.com.securboration.immortals.ontology.cp.gmeinterchangeformat import GmeInterchangeFormat
#     from pymmortals.generated.mil.darpa.immortals.core.api.deploymentmodel.challengeproblem import ChallengeProblem
#     from pymmortals.datatypes.deployment_model import LLP1Input
#
#     filepath = '/Users/austin/Documents/workspaces/primary/immortals/repo/immortals/harness/GmeiSample.json'
#     gif = GmeInterchangeFormat.from_file(filepath=filepath)
#
#     j = json.load(open(filepath))
#     json.dump(
#         j,
#         open('/Users/austin/Documents/workspaces/primary/immortals/repo/immortals/harness/GmeiSample0.json', 'w'),
#         indent=4, separators=(',', ': '), sort_keys=True)
#
#     filepath2 = '/Users/austin/Documents/workspaces/primary/immortals/repo/immortals/harness/GmeiSample1.json'
#     gif.to_file_pretty(filepath=filepath2, include_metadata=True)
#
#     gif = GmeInterchangeFormat.from_file(
#         '/Users/austin/Documents/workspaces/primary/immortals/repo/immortals/harness/loaded_config_from_java.json')
#     gif.to_file_pretty(
#         '/Users/austin/Documents/workspaces/primary/immortals/repo/immortals/harness/loaded_config_dumped.json',
#         include_metadata=True)
#
#     j = json.load(open(
#         '/Users/austin/Documents/workspaces/primary/immortals/repo/immortals/harness/loaded_config_from_java.json'))
#     json.dump(j,
#               open('/Users/austin/Documents/workspaces/primary/immortals/repo/immortals/harness/loaded_config.json',
#                    'w'),
#               indent=4, separators=(',', ': '), sort_keys=True)
#
#     dm = LLP1Input.from_file(
#         '/Users/austin/Documents/workspaces/primary/immortals/repo/immortals/harness/sample_submission.json')
#     gif = dm.to_triples(ChallengeProblem.Phase01)
#     gif.to_file_pretty(
#         '/Users/austin/Documents/workspaces/primary/immortals/repo/immortals/harness/loaded_config_dumped.json',
#         include_metadata=True)


if __name__ == '__main__':
    pojoize()
