import copy
import logging
import os
import re
from collections import OrderedDict
from enum import Enum
from typing import List, Dict, Set, Union

import javalang
from javalang.tree import EnumDeclaration, TypeDeclaration, ClassDeclaration, MethodDeclaration, ReferenceType, \
    FieldDeclaration, VariableDeclarator, TypeArgument, BasicType, ConstructorDeclaration, Literal, MemberReference, \
    FormalParameter, EnumConstantDeclaration, ClassReference, InterfaceDeclaration, Declaration, \
    AnnotationDeclaration, ClassCreator, TryStatement


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

import_type_omissions = frozenset(['bool', 'int', 'str', 'float', 'bytes', 'type', 'Description', 'P2CP1',
                                   'P2CP2', 'P2CP3'])

_types_to_ignore = [
    MethodDeclaration,
    ConstructorDeclaration,
    EnumDeclaration,
    TryStatement
]

_immortals_root = os.path.abspath(os.path.join(os.path.dirname(os.path.realpath(__file__)), "../../"))

_target_package = 'pymmortals.generated'
_target_module_directory = _immortals_root + '/harness/' + _target_package.replace('.', '/') + '/'
_ind = '    '


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
    is_map = False

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

    elif type_name == 'HashMap' or type_name == 'Map':
        assert (len(input_type.arguments) == 2)
        assert (isinstance(input_type.arguments[0], TypeArgument))
        assert (isinstance(input_type.arguments[0].type, ReferenceType))
        assert (isinstance(input_type.arguments[1], TypeArgument))
        assert (isinstance(input_type.arguments[1].type, ReferenceType))

        inner_key_type_name = input_type.arguments[0].type.name
        if inner_key_type_name in _java_value_map:
            return_type = _java_value_map[inner_key_type_name]
        else:
            return_type = inner_key_type_name

        inner_value_type_name = input_type.arguments[1].type.name
        if inner_value_type_name in _java_value_map:
            return_type = return_type + ',' + _java_value_map[inner_value_type_name]
        else:
            return_type = return_type + ',' + inner_value_type_name
        is_map = True

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

    elif is_map:
        return 'Dict[' + return_type + ']'

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
                               include_private: bool, ignore_default_param_values: bool) -> 'FieldData':

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

            initializer = declaration.declarators[0].initializer  # type: ClassCreator
            if initializer is None or ignore_default_param_values:
                field_value = None

            elif isinstance(initializer, Literal):
                field_value = initializer.value

            else:
                raise Exception('Only Primitive and empty constructor initializers currently supported!')

            assert (field_name is not None)
            assert (field_type is not None)

            description = _get_description(declaration=declaration)

            return FieldData(name=field_name, type=field_type,
                             description=description,
                             apis=_get_cps(declaration=declaration),
                             unstable=unstable, value=field_value)

    def all_types(self) -> List[str]:
        types = list()  # type: List[str]

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

            elif t.startswith('Dict[') and t.endswith(']'):
                types.append('Dict')
                t = t[5:-1]
                unprocessed_inner = True

            elif t.count(',') == 1:
                types.append(t[0:t.find(',')])
                t = t[t.find(',') + 1:]
                unprocessed_inner = True

            elif t.count(',') > 1:
                raise Exception("No more than two generic types are supported for variables at this time!")

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
                 wildcard_imports: List[str] = None,
                 nested_class_configs: Dict[str, "AbstractPojoBuilderConfig"] = None):
        """
        :param package: The package the class should be placed in
        :param class_name: The name of the class
        :param fields: The data for the fields within the class
        :param inheritance: A list of classes which this class should inherit from
        :param imports: Imports th class needs
        :param wildcard_imports: Wildcard java imports (ex. "imort java.lang.*")
        :param nested_class_configs: configurations for nested classes
        """
        self.package = package
        self.class_name = class_name
        self.fields = fields
        self.description = description
        self.unstable = unstable
        self.apis = apis if apis is not None else list()
        self.inheritance = inheritance if inheritance is not None else list()
        self.imports = imports if imports is not None else OrderedDict()  # type: Dict[str, str]
        self.wildcard_imports = wildcard_imports if wildcard_imports is not None else list()
        self.nested_class_configs = \
            nested_class_configs if nested_class_configs is not None else dict()  # type: Dict[str, AbstractPojoBuilderConfig]

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

        if t in import_type_omissions:
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

        if t == 'Dict' and 'Dict' not in self.imports:
            self.imports['Dict'] = 'typing'

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

            elif t in import_type_omissions:
                changed = False

            elif t in self.nested_class_configs.keys():
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
                 wildcard_imports: List[str] = None,
                 nested_class_configs: Dict[str, AbstractPojoBuilderConfig] = None):
        """
        :param package: The package the class should be placed in
        :param class_name: The name of the class
        :param fields: The data for the fields within the class
        :param inherited_fields: Properties inherited from a superclass
        :param inheritance: A list of classes which this class should inherit from
        :param imports: Imports th class needs
        :param wildcard_imports: Wildcard java imports (ex. "imort java.lang.*")
        :param nested_class_configs: configurations for nested classes
        """
        super().__init__(package=package, class_name=class_name, fields=fields, description=description,
                         unstable=unstable, apis=apis, inheritance=inheritance, imports=imports,
                         wildcard_imports=wildcard_imports, nested_class_configs=nested_class_configs)
        self.inherited_fields = inherited_fields if inherited_fields is not None else list()  # type: List[FieldData]

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
                (x for x in path_classes[self.imports[superclass_name].replace('pymmortals.generated.', '').replace(
                    '.' + superclass_name.lower(), '')] if x.class_name == superclass_name),
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

                if field.core_type() in superclass.imports or field.core_type() in import_type_omissions:
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
                 wildcard_imports: List[str] = None,
                 nested_class_configs: Dict[str, AbstractPojoBuilderConfig] = None):
        """
        :param package: The package the class should be placed in
        :param class_name: The name of the class
        :param instance_labels: The name of each enum
        :param fields: The data for the fields within the class
        :param inheritance: A list of classes which this class should inherit from
        :param imports: Imports th class needs
        :param wildcard_imports: Wildcard java imports (ex. "imort java.lang.*")
        :param nested_class_configs: configurations for nested classes
        """
        super().__init__(package=package, class_name=class_name, fields=fields, description=description,
                         unstable=unstable, apis=apis, inheritance=inheritance, imports=imports,
                         nested_class_configs=nested_class_configs, wildcard_imports=wildcard_imports)
        self.instance_labels = instance_labels  # type: List[str]
        self.instance_parameter_fields = instance_parameter_fields  # type: Dict[str, List[FieldData]]

        self.inherited_properties = list()

        if 'Enum' not in self.imports:
            self.imports['Enum'] = 'enum'

        if 'Enum' not in self.inheritance:
            self.inheritance.append('Enum')


class Pojoizer:
    """
    Used to convert java pojos into python pojos
    """

    def __init__(self,
                 conversion_method: ConversionMethod,
                 target_directory: str = _target_module_directory,
                 target_package: str = _target_package,
                 do_generate_markdown: bool = False,
                 ignore_default_param_values: bool = False):
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
        self.ignore_default_param_values = ignore_default_param_values

        self.path_classes = OrderedDict()  # type: Dict[str, Set[AbstractPojoBuilderConfig]]
        self.path_classnames = OrderedDict()  # type: Dict[str, Set[str]]

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

    def load_type_declaration(self, t: TypeDeclaration, class_path: str, class_imports: Dict[str, str],
                              class_wildcard_imports: List[str]) -> AbstractPojoBuilderConfig:
        assert self.conversion_method == ConversionMethod.VARS

        inheritance = list()  # type: List[str]

        fields = list()  # type: List[FieldData]
        apis = None  # type: List[DocumentationTag]
        nested_classes = dict()  # type: Dict[str, AbstractPojoBuilderConfig]
        unstable = False

        if 'extends' in t.attrs and t.extends is not None:
            if isinstance(t.extends, list):
                assert len(t.extends) == 1
                assert isinstance(t.extends[0], ReferenceType)
                inheritance.append(t.extends[0].name)
            else:
                assert isinstance(t.extends, ReferenceType)
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
            instance_labels = list()  # type: List[str]
            instance_fields = OrderedDict()  # type: Dict[str, List[FieldData]]
            param_names = list()  # type: List[str]
            param_types = list()  # type: List[str]

            # Search the declarations for the constructor and extract the parameter types from it
            for declaration in t.body.declarations:

                # Also extract the field information to match it with the parameters and any additional metadata
                if isinstance(declaration, FieldDeclaration):
                    fields.append(FieldData.from_field_declaration(
                        declaration=declaration,
                        include_private=False,
                        ignore_default_param_values=self.ignore_default_param_values))

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
            pn = list()  # type: List[str]
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
                instance_field_values = list()  # type: List[FieldData]

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
            return ccc

        elif isinstance(t, ClassDeclaration):
            ignore = len([k for k in t.annotations if k.name == 'Ignore']) > 0
            if ignore:
                _logger.warning('Ignoring "@Ignore"ed class ' + class_path + '.' + class_name)

            else:
                for declaration in t.body:
                    if any([k for k in _types_to_ignore if isinstance(declaration, k)]):
                        # Ignore these types for now
                        pass

                    elif (isinstance(declaration, List) and len(declaration) == 1 and
                          any([k for k in _types_to_ignore if isinstance(declaration[0], k)])):
                        # Ignore these types for now
                        pass

                    elif isinstance(declaration, FieldDeclaration):
                        if 'static' in declaration.modifiers:
                            _logger.warning(
                                "Ignoring static variable of type '" + declaration.type.name + "' in class '" + class_path + '.' + class_name + "'")
                        else:
                            field = FieldData.from_field_declaration(
                                declaration=declaration, include_private=True,
                                ignore_default_param_values=self.ignore_default_param_values)
                            if field is not None:
                                fields.append(field)

                    elif isinstance(declaration, ClassDeclaration):
                        cp = class_path + '-' + class_name
                        if cp not in self.path_classnames:
                            self.path_classnames[cp] = set()
                        if cp not in self.path_classes:
                            self.path_classes[cp] = set()
                        nested_class = self.load_type_declaration(t=declaration,
                                                                  class_path=cp,
                                                                  class_imports=class_imports,
                                                                  class_wildcard_imports=class_wildcard_imports)
                        nested_classes[nested_class.class_name] = nested_class

                    else:
                        raise Exception(
                            'Unexpected type "' + str(declaration) +
                            '" for class ' + class_path + '.' + class_name + '!')

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
                    wildcard_imports=class_wildcard_imports,
                    nested_class_configs=nested_classes
                )

                self.path_classes[class_path].add(ccc)
                self.path_classnames[class_path].add(ccc.class_name)
                return ccc

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
            return ccc

        elif not isinstance(t, AnnotationDeclaration):
            raise Exception('Unexpected type declaration "' + str(t) + '"!')

    def load_file(self, filepath):
        if self.conversion_method == ConversionMethod.VARS:
            self._load_file_vars(filepath=filepath)

        elif self.conversion_method == ConversionMethod.GETS:
            self._load_file_gets(filepath=filepath)

        else:
            raise Exception('Unexpected conversion method "' + str(self.conversion_method) + '"!')

    def _load_file_gets(self, filepath):
        # parse the ast
        tree = javalang.parse.parse(open(filepath, 'r').read())

        if len(tree.types) == 0:
            return

        elif len(tree.types) != 1:
            raise Exception('Cannot support multiple types!')

        class_path = tree.package.name
        class_imports = OrderedDict()  # type: Dict[str, str]
        class_wildcard_imports = list()  # type: List[str]

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
            inheritance = list()  # type: List[str]
            fields = list()  # type: List[FieldData]
            apis = None  # type: List[DocumentationTag]
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
                instance_labels = list()  # type: List[str]
                instance_fields = OrderedDict()  # type: Dict[str, List[FieldData]
                param_names = list()  # type: List[str]
                param_types = list()  # type: List[str]

                # Search the declarations for the constructor and extract the parameter types from it
                for declaration in t.body.declarations:

                    # Also extract the field information to match it with the parameters and any additional metadata
                    if isinstance(declaration, FieldDeclaration):
                        fields.append(FieldData.from_field_declaration(
                            declaration=declaration,
                            include_private=True,
                            ignore_default_param_values=self.ignore_default_param_values))

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
                pn = list()  # type: List[str]
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
                    instance_field_values = list()  # type: List[FieldData]

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

    def _load_file_vars(self, filepath):
        # parse the ast
        tree = javalang.parse.parse(open(filepath, 'r').read())

        if len(tree.types) == 0:
            return

        elif len(tree.types) != 1:
            raise Exception('Cannot support multiple types!')

        class_path = tree.package.name
        class_imports = OrderedDict()  # type:Dict[str, str]
        class_wildcard_imports = list()  # type: List[str]

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
            self.load_type_declaration(t=t, class_path=class_path, class_imports=class_imports,
                                       class_wildcard_imports=class_wildcard_imports)

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
                full_import_strings = dict()  # type: Dict[str, str]
                # For each import
                for k in sorted(c.imports.keys()):
                    v = c.imports[k]

                    # Determine the full object path
                    if v in self.path_classnames and k in self.path_classnames[v]:
                        full_import_strings[self.target_package + '.' + v + '.' + k.lower() + '.' + k] = k
                    else:
                        full_import_strings[v + '.' + k] = k

                sorted_imports = OrderedDict()  # type: Dict[str, str]

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
                types = set(config.inheritance)  # type: Set[str]
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

    def _construct_class(self, config: AbstractPojoBuilderConfig, indentation_level: int):
        lines = list()  # type: List[str]

        def lappend(line: str, ind_level: int = 0, target_list: List = None):
            if target_list is None:
                target_list = lines
            target_list.append((indentation_level + ind_level) * _ind + line + '\n')

        # Class Declaration Line
        lappend('# noinspection PyPep8Naming')
        if len(config.inheritance) <= 0:
            lappend('class ' + config.class_name + ':')
        else:
            lappend('class ' + config.class_name + '(' + ', '.join(config.inheritance) + '):')

        for nested_class in config.nested_class_configs.values():
            lines.append('\n')
            lines = lines + self._construct_class(nested_class, indentation_level + 1)
            lines.append('\n')

        if isinstance(config, PojoEnumBuilderConfig):
            instantiation_lines = list()
            get_all_lines = list()

            if config.instance_parameter_fields is not None and len(config.instance_parameter_fields) > 0:
                parameters = 'key_idx_value: str, '

                for idx in range(0, len(config.fields)):
                    field = config.fields[idx]
                    has_next = idx < (len(config.fields) - 1)

                    parameters = parameters + (field.name + ': ' + field.raw_type + (', ' if has_next else ''))

                    lappend('self._key_idx_value = key_idx_value', 2, instantiation_lines)
                    lappend('self.' + field.name + ' = ' + field.name + '  # type: ' + field.raw_type, 2,
                            instantiation_lines)

                    lappend('@classmethod', 1, get_all_lines)
                    lappend('def all_' + field.name + '(cls) -> FrozenSet[' + field.raw_type + ']:',
                            1, get_all_lines)
                    lappend('return frozenset([k.' + field.name + ' for k in list(cls)])',
                            2, get_all_lines)

                    if has_next:
                        get_all_lines.append('\n')

                if parameters != '':
                    lappend('def __init__(self, ' + parameters + '):', 1)
                else:
                    lappend('def __init__(self):', 1)

                lines = lines + instantiation_lines
                lines.append('\n')

                instance_lines = list()
                for label in config.instance_labels:
                    parameter_values = [k.value for k in config.instance_parameter_fields[label]]

                    lappend(label + ' = ("' + label + '",', 1, instance_lines)

                    for idx in range(0, len(parameter_values)):
                        pv = parameter_values[idx]
                        has_next = (len(parameter_values) - 1) != idx
                        if has_next:
                            lappend(pv + ',', 2, instance_lines)

                        else:
                            lappend(pv + ')', 2, instance_lines)
                            instance_lines.append('\n')

                lines = lines + instance_lines
                lines = lines + get_all_lines

            else:
                counter = 0
                for value in config.instance_labels:
                    lappend(value + " = '" + value + "'", 1)
                    counter += 1

        elif isinstance(config, PojoClassBuilderConfig):
            lappend('_validator_values = dict()', 1)
            lines.append('\n')
            lappend('_types = dict()', 1)
            lines.append('\n')

            all_fields = sorted(config.fields + config.inherited_fields, key=lambda x: x.name)  # type: List[FieldData]

            if len(all_fields) == 0:
                lappend('def __init__(self):', 1)
                lappend('super().__init__()', 2)

            else:
                sig = list()
                instantiation = list()
                lappend('def __init__(self,', 1)

                inherited_properties_string = None
                for field in config.inherited_fields:
                    if inherited_properties_string is None:
                        inherited_properties_string = field.name + '=' + field.name
                    else:
                        inherited_properties_string += (', ' + field.name + '=' + field.name)

                if inherited_properties_string is not None:
                    lappend('super().__init__(' + inherited_properties_string + ')', 2, instantiation)
                else:
                    lappend('super().__init__()', 2, instantiation)

                for field in all_fields:  # type: FieldData
                    if field.raw_type == config.class_name:
                        raw_type = "'" + field.raw_type + "'"
                    elif ('[' + config.class_name + ']') in field.raw_type:
                        raw_type = field.raw_type.replace('[' + config.class_name + ']',
                                                          "['" + config.class_name + "']")
                    else:
                        raw_type = field.raw_type

                    if field.name == all_fields[-1].name:
                        lappend(' ' + field.name + ': ' + raw_type + ' = None):', 4, sig)
                    else:
                        lappend(' ' + field.name + ': ' + raw_type + ' = None,', 4, sig)

                    if not any(x.name == field.name for x in config.inherited_fields):
                        lappend('self.' + field.name + ' = ' + field.name, 2, instantiation)

                lines = lines + sig + instantiation

        return lines

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

        lines = lines + self._construct_class(config, 0)

        pkg = config.package + '.' + config.class_name.lower()
        if pkg in self.path_classes:
            target = os.path.join(self.target_directory, pkg.replace('.', '/'), '__init__.py')
            open(target, 'w').writelines(lines)

        else:
            open(os.path.join(target_dir, config.class_name.lower() + '.py'), 'w').writelines(lines)

    def do_construction(self):
        self._preprocess_class_data()

        for key in self.path_classes:
            for config in self.path_classes[key]:
                self._construct_file(config=config)


def pojoize_knowledge_repo():
    # # Pojoize the Triples
    p = Pojoizer(ConversionMethod.GETS,
                 target_directory=_target_module_directory,
                 target_package=_target_package,
                 do_generate_markdown=False)

    kr_root = os.path.join(_immortals_root, 'knowledge-repo/vocabulary/ontology-vocab-domains/')

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

    kr_ignore_files = [
        'ontology/gmei/GmeInterchangeFormatExample.java',
        'ontology/gmei/GmeInterchangeFormatUberExample.java'
    ]

    for d in kr_dirs:
        p.load_directory(root_directory=(os.path.join(kr_root, d)),
                         packages_root=kr_root_package,
                         ignore_files=kr_ignore_files)

    p.do_construction()


def pojoize_ll_api_p2():
    # Pojoize the shared POJOs
    p = Pojoizer(conversion_method=ConversionMethod.VARS,
                 target_directory=_target_module_directory,
                 target_package=_target_package,
                 do_generate_markdown=False,
                 ignore_default_param_values=True
                 )
    packages_root = 'mil/darpa/immortals/core/api'

    p.load_directory(
        root_directory=os.path.join(_immortals_root,
                                    'das/das-context/src/main/java/'),
        packages_root=packages_root
    )

    p.load_directory(
        root_directory=os.path.join(_immortals_root,
                                    'das/das-testharness-coordinator/src/main/java/'),
        packages_root=packages_root
    )
    p.do_construction()


def pojoize_immortals_config():
    # Pojoize ImmortalsConfig
    p = Pojoizer(conversion_method=ConversionMethod.VARS,
                 target_directory=_target_module_directory,
                 target_package=_target_package,
                 do_generate_markdown=False,
                 ignore_default_param_values=True
                 )
    root_dir = os.path.join(_immortals_root, 'buildSrc/ImmortalsConfig/src/main/java/')
    # packages_root = 'mil/darpa/immortals/core/api/ll/phase2'
    packages_root = 'mil/darpa/immortals/config'
    p.load_directory(root_directory=root_dir,
                     packages_root=packages_root,
                     ignore_files=[
                         'mil/darpa/immortals/config/StaticConfig.java'
                     ])
    p.do_construction()


# def test():
#     import json
#     from pymmortals.generated.com.securboration.immortals.ontology.cp.gmeinterchangeformat import GmeInterchangeFormat
#     from pymmortals.datatypes.intermediary.challengeproblem import ChallengeProblem
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
    # pojoize_knowledge_repo()
    # pojoize_ll_api_p2()
    pojoize_immortals_config()
    pojoize_ll_api_p2()
