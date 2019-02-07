import copy
import importlib
import inspect
import json
import logging
import os
import re
import uuid
from enum import Enum
from typing import FrozenSet, Set, Dict, List, Union, GenericMeta, Type

_generated_package_base = 'integrationtest.generated.'

_logger = logging.getLogger("Serializable")

_uuid_pattern = re.compile('^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}$')


def _extract_environment_variables(string):
    """
    :type string: str
    :rtype: list[str]
    """
    l = []
    idx = 0
    while idx >= 0:
        lb = string.find('{', idx)
        rb = string.find('}', idx)
        nlb = string.find('{', lb + 1)
        nrb = string.find('}', rb + 1)

        if lb == rb == -1:
            idx = -1

        elif lb > rb or (not nlb == nrb == -1) and ((nlb == -1 or nrb == -1) or (nlb < rb or nrb < lb)):
            raise Exception('Missmatched curly brackets detected in string "' + string + '"!')

        elif (lb + 1) == rb:
            raise Exception('Empty curly brackets detected in string "' + string + '"!')

        elif 0 <= lb < rb:
            if lb > 0 and string[lb - 1:lb] == '$':
                l.append(string[lb + 1:rb])
            idx = rb + 1

        else:
            raise Exception('Missmatched curly brackets detected in string "' + string + '"!')

    return l


def _extract_metavars(string):
    """
    :type string: str
    :rtype: List[str]
    """
    l = []
    idx = 0
    while idx >= 0:
        lb = string.find('{', idx)
        rb = string.find('}', idx)
        nlb = string.find('{', lb + 1)
        nrb = string.find('}', rb + 1)

        if lb == rb == -1:
            idx = -1

        elif lb > rb or (not nlb == nrb == -1) and ((nlb == -1 or nrb == -1) or (nlb < rb or nrb < lb)):
            raise Exception('Missmatched curly brackets detected in string "' + string + '"!')

        elif (lb + 1) == rb:
            raise Exception('Empty curly brackets detected in string "' + string + '"!')

        elif 0 <= lb < rb:
            if lb == 0 or string[lb - 1:lb] != '$':
                l.append(string[lb + 1:rb])
            idx = rb + 1

        else:
            raise Exception('Missmatched curly brackets detected in string "' + string + '"!')

    return l


def _replace_env_var(string, env_var, env_var_value):
    """
    :type string: str
    :type env_var: str
    :type env_var_value: str
    :rtype: str
    """
    return_string = string
    template_string = '${' + env_var + '}'

    while return_string.find(template_string) > -1:
        return_string = return_string.replace(template_string, env_var_value)

    return return_string


def _replace_metavar(string, metavar, metavar_value):
    """
    :type string: str
    :type metavar: str
    :type metavar_value: str
    :rtype: str
    """
    return_string = string
    template_string = '{' + metavar + '}'

    while return_string.find(template_string) > -1:
        return_string = return_string.replace(template_string, metavar_value)

    return return_string


# def fill_dict(d: Dict[str, object], value_pool: Union[Dict[str, object], None]) -> Dict[str, object]:
def fill_dict(target_dict, value_pool):
    """
    :type target_dict: dict[str]
    :type value_pool: dict[str]
    :rtype: dict[str]
    """
    if value_pool is None:
        value_pool = {}

    return _fill_dict(d=target_dict, value_pool=value_pool, parents=None)


# def _fill_dict(d: Dict[str, object], value_pool: Dict[str, object],
#                parents=List[Dict[str, object]]) -> Dict[str, object]:
def _fill_dict(d, value_pool, parents):
    """
    :type d: dict[str]
    :type value_pool: dict[str]
    :type parents: list[dict[str]] or None
    :rtype: dict[str]
    """
    if parents is None:
        child_parents = []
    else:
        child_parents = copy.deepcopy(parents)

    child_parents.append(d)

    # First, update all current level strings until nothing changes
    changed = True
    while changed:
        changed = False
        for key in list(d.keys()):
            val = d[key]
            if isinstance(val, str):
                new_value = _fillout_string(val, value_pool=value_pool, parents=child_parents)
                if new_value != val:
                    changed = True

                d[key] = new_value

    for key in list(d.keys()):
        val = d[key]

        if isinstance(val, list):
            d[key] = _fill_list(l=val, value_pool=value_pool, parents=child_parents)

        elif isinstance(val, dict):
            d[key] = _fill_dict(d=val, value_pool=value_pool, parents=child_parents)

        elif inspect.isclass(val):
            raise Exception("Cannot update values in an already constructed class!")

    return d


def _fill_list(l, value_pool, parents):
    """
    :type l: list
    :type value_pool: dict[str, object]
    :type parents: list[dict[str, object]]
    :rtype: list
    """
    if parents is None:
        child_parents = []
    else:
        child_parents = copy.deepcopy(parents)

    # First, update all current level strings until nothing changes
    changed = True
    while changed:
        changed = False
        for idx in range(len(l)):
            val = l[idx]
            if isinstance(val, str):
                new_value = _fillout_string(val, value_pool=value_pool, parents=child_parents)
                if new_value != val:
                    changed = True

                l[idx] = new_value

    for idx in range(len(l)):
        val = l[idx]

        if isinstance(val, list):
            l[idx] = _fill_list(l=val, value_pool=value_pool, parents=child_parents)

        elif isinstance(val, dict):
            l[idx] = _fill_dict(d=val, value_pool=value_pool, parents=child_parents)

        elif inspect.isclass(val):
            raise Exception("Cannot update values in an already constructed class!")

    return l


def _fillout_string(string, value_pool, parents):
    """
    :type string: str
    :type value_pool: dict[str, object]
    :type parents: list[dict[str, object]]
    :rtype: str
    """
    env_vars = _extract_environment_variables(string)

    for env_var in env_vars:
        ev = os.getenv(env_var)
        if ev is None:
            raise Exception('Environment variable ' + env_var + ' is not defined!')
        else:
            string = _replace_env_var(string, env_var, ev)

    metavars = _extract_metavars(string)

    if len(metavars) == 0:
        return string

    for metavar in metavars:
        if metavar in value_pool:
            val = value_pool[metavar]
            assert (isinstance(val, str))
            string = _replace_metavar(string, metavar, val)
        else:
            for p in reversed(parents):
                if metavar in p:
                    val = p[metavar]
                    assert (isinstance(val, str))
                    string = _replace_metavar(string, metavar, val)
                    break
    return string


def _is_uuid(s: str):
    return _uuid_pattern.match(s) is not None


def nonenullifier(d: Dict):
    for k, v in list(d.items()):
        if v is None:
            del d[k]
        elif isinstance(v, dict):
            nonenullifier(d=v)


def _get_type_from_java_classpath(java_classpath: str) -> Union[type, None]:
    split = java_classpath.split('.')
    path = '.'.join(split[:-1])
    name = split[-1]

    if java_classpath.startswith('com.securboration'):
        path = _generated_package_base + path + '.' + name.lower()

    elif java_classpath.startswith('builtins'):
        path = 'builtins'

    elif not java_classpath.startswith('integrationtest.'):
        return None

    mod = importlib.import_module(path)
    return getattr(mod, name)


def _get_java_classpath_from_type(t: type) -> str:
    m = t.__module__
    n = t.__name__

    if m.startswith(_generated_package_base) and m.endswith(n.lower()):
        return m[len(_generated_package_base): -len(n)] + n
    else:
        return m + '.' + n


def _get_str_type(val: str) -> Union[type, None]:
    if val == 'True' or val == 'true':
        return bool
    elif val == 'False' or val == 'false':
        return bool
    elif val.isdigit():
        return int
    elif val == 'None' or val == 'null':
        return None
    else:
        try:
            float(val)
            return float
        except ValueError:
            return str


def _string_extractor(val: str) -> Union[bool, int, float, str, None]:
    if val == 'True' or val == 'true':
        return True
    elif val == 'False' or val == 'false':
        return False
    elif val.isdigit():
        return int(val)
    elif val == 'None' or val == 'null':
        return None
    else:
        try:
            rval = float(val)
            return rval
        except ValueError:
            return val


def _from_list_to_list(source_data, list_target_value_type: Union[type, object], object_map: Dict[str, object],
                       do_replacement: bool = False) -> List[object]:
    l = []

    pojo_type = None

    if len(source_data) > 0 and isinstance(source_data[0], str) and source_data[0].startswith('[L'):
        pojo_type = _get_type_from_java_classpath(source_data[0][2:-1])
        assert (pojo_type is not None
                and (isinstance(pojo_type, list_target_value_type) or issubclass(pojo_type, list_target_value_type)))

        source_data = source_data[1]

    for val in source_data:
        if pojo_type is not None:
            assert ('@type' in val and '@id' in val)
        l.append(_deserializer(source_data=val,
                               target_type=list_target_value_type,
                               object_map=object_map,
                               do_replacement=do_replacement))
    return l


def _deserializer(source_data, target_type: Union[GenericMeta, object], object_map: Dict[str, object],
                  do_replacement: bool = False) \
        -> Union[List[object], FrozenSet[object], Dict[str, object], Enum, bool, int, float, str]:
    if isinstance(source_data, str):
        # if inspect.isclass(target_type)
        pojo_type = _get_type_from_java_classpath(source_data)

        if pojo_type is not None:
            assert isinstance(target_type, Type)
            # noinspection PyUnresolvedReferences
            assert (isinstance(pojo_type, target_type.__args__[0]) or issubclass(pojo_type, target_type.__args__[0]))
            return pojo_type

        elif issubclass(target_type, Enum):
            try:
                return target_type[source_data]
            except KeyError:
                return target_type(source_data)

        elif _get_str_type(source_data) == target_type:
            return _string_extractor(source_data)

        elif (isinstance(target_type, Serializable) or issubclass(target_type, Serializable)) and _is_uuid(
                source_data):
            if source_data in object_map:
                return object_map[source_data]

            else:
                # noinspection PyProtectedMember
                obj = target_type._from_dict(source_dict=dict(),
                                             top_level_deserialization=False,
                                             value_pool=None,
                                             object_map=object_map,
                                             do_replacement=do_replacement)
                object_map[source_data] = obj
                return obj

        elif target_type == str:
            return source_data

    elif isinstance(source_data, bool) and target_type == bool \
            or isinstance(source_data, int) and target_type == int \
            or isinstance(source_data, float) and target_type == float:
        return source_data

    elif isinstance(source_data, List):
        assert (issubclass(target_type, List) or issubclass(target_type, FrozenSet) or issubclass(target_type, Set))

        # noinspection PyUnresolvedReferences
        list_tt = target_type.__args__[0]

        if len(source_data) > 0 and isinstance(source_data[0], str) and source_data[0].startswith('[L'):
            pojo_type = _get_type_from_java_classpath(source_data[0][2:-1])
            assert (pojo_type is not None and (isinstance(pojo_type, list_tt) or issubclass(pojo_type, list_tt)))
            source_data = source_data[1]
            list_tt = pojo_type

        return [_deserializer(
            source_data=k,
            target_type=list_tt,
            object_map=object_map,
            do_replacement=do_replacement
        ) for k in source_data]

    elif isinstance(source_data, Dict):

        if issubclass(target_type, Dict):
            dc = copy.deepcopy(source_data)

            for key in list(source_data.keys()):
                # noinspection PyUnresolvedReferences
                dc[key] = _deserializer(source_data=source_data[key],
                                        target_type=target_type.__args__[1],
                                        object_map=object_map,
                                        do_replacement=do_replacement)
            return dc

        elif issubclass(target_type, Serializable):
            tt = target_type
            # noinspection PyProtectedMember
            return tt._from_dict(source_dict=source_data,
                                 top_level_deserialization=False,
                                 value_pool=None,
                                 object_map=object_map,
                                 do_replacement=do_replacement)

    raise Exception(
        'Cannot deserialize object ' + str(source_data) + ' to target type of ' + str(target_type) + '!')


class ValuedEnum(Enum):
    @property
    def value(self):
        raise Exception('Please define a specific property to get the value to ensure proper semantic usage!')

    @classmethod
    def _values(cls) -> FrozenSet:
        # TODO: Make this internal and apply similar enforcement ot the above 'value' function
        # noinspection PyProtectedMember
        return frozenset([k._value_ for k in cls.__members__.values()])

    @classmethod
    def keys(cls) -> FrozenSet:
        return frozenset(cls.__members__.keys())


# noinspection PyClassHasNoInit
class Serializable:
    """
    Provides A base class to ease conversion of objects from/to dictionaries
    """

    def __init__(self):
        self._uuid = str(uuid.uuid4())

    # noinspection PyPropertyDefinition
    @classmethod
    @property
    def _validator_values(cls):
        raise NotImplementedError

    @classmethod
    def from_dict(cls, d: Dict[str, object], value_pool: Dict[str, object] = None, do_replacement: bool = True):
        if value_pool is None:
            value_pool = {}

        return cls._from_dict(source_dict=copy.deepcopy(d),
                              top_level_deserialization=True,
                              value_pool=value_pool,
                              object_map=None,
                              do_replacement=do_replacement)

    def to_dict(self, include_metadata: bool = False, strip_nulls=False) -> Dict[str, object]:
        d = Serializable._to_dict(obj=self,
                                  object_map=dict(),
                                  parent_object=None,
                                  include_metadata=include_metadata)

        if strip_nulls:
            nonenullifier(d)

        return d

    def to_json_str(self, include_metadata: bool = True, strip_nulls=False) -> str:
        return json.dumps(self.to_dict(include_metadata=include_metadata, strip_nulls=strip_nulls))

    def to_json_str_pretty(self, include_metadata: bool = True, strip_nulls=False) -> str:
        return json.dumps(self.to_dict(include_metadata=include_metadata, strip_nulls=strip_nulls), indent=4,
                          sort_keys=True,
                          separators=(',', ': '))

    def to_file_pretty(self, filepath, include_metadata: bool = True, strip_nulls=False):
        json.dump(self.to_dict(include_metadata=include_metadata, strip_nulls=strip_nulls), indent=4, sort_keys=True,
                  separators=(',', ': '), fp=open(filepath, 'w'))

    @classmethod
    def from_json_str(cls, s: str, value_pool: Dict[str, object] = None, do_replacement: bool = True):
        return cls.from_dict(d=json.loads(s),
                             value_pool=value_pool,
                             do_replacement=do_replacement)

    @classmethod
    def from_file(cls, filepath: str, value_pool: Dict[str, object] = None, do_replacement: bool = True):
        return cls.from_dict(d=json.load(open(filepath, 'r')),
                             value_pool=value_pool, do_replacement=do_replacement)

    @staticmethod
    def _to_dict(obj: [GenericMeta, Dict, object], object_map: Dict[object, str], parent_object: Union[object, None],
                 include_metadata: bool) -> Union[Dict[str, object], List[object], FrozenSet]:

        if isinstance(obj, Dict):
            rval = dict()

            for k, v in obj.items():
                rval[k] = Serializable._to_dict(obj=v,
                                                object_map=object_map,
                                                parent_object=None,
                                                include_metadata=include_metadata)

                if include_metadata:
                    if isinstance(v, List) or isinstance(v, FrozenSet):
                        sig = inspect.signature(parent_object.__class__)
                        a = sig.parameters[k].annotation
                        assert (issubclass(a, List))
                        assert (len(a.__args__) == 1)
                        jcp = '[L' + _get_java_classpath_from_type(a.__args__[0]) + ';'
                        rval[k] = [jcp, rval[k]]

            return rval

        elif isinstance(obj, List) or isinstance(obj, FrozenSet):
            rval = list()
            for o in obj:
                rval.append(Serializable._to_dict(obj=o,
                                                  object_map=object_map,
                                                  parent_object=None,
                                                  include_metadata=include_metadata))
            return rval

        elif isinstance(obj, Serializable):
            if obj._uuid in object_map.keys():
                return object_map[obj._uuid]
            else:

                object_map[obj._uuid] = obj._uuid

                rval = Serializable._to_dict(obj=copy.deepcopy(obj.__dict__),
                                             object_map=object_map,
                                             parent_object=obj,
                                             include_metadata=include_metadata)

                if include_metadata:
                    rval['@id'] = obj._uuid
                    rval['@type'] = _get_java_classpath_from_type(obj.__class__)

                rval.pop('_uuid')

                return rval

        elif isinstance(obj, Enum):
            return obj.name

        elif inspect.isclass(obj):
            return _get_java_classpath_from_type(obj)

        else:
            return obj

    # noinspection PyProtectedMember
    @classmethod
    def _from_dict(cls, source_dict: Dict[str, object], top_level_deserialization: bool,
                   value_pool: Union[Dict[str, object], None], object_map: Union[Dict[str, object], None],
                   do_replacement):
        source_dict = copy.deepcopy(source_dict)

        pojo_identifier = None

        if top_level_deserialization:
            if do_replacement:
                source_dict = fill_dict(source_dict, value_pool)
            object_map = dict()
        else:
            assert object_map is not None

        if '@type' in source_dict or '@id' in source_dict:
            type_str = source_dict['@type']
            assert (isinstance(type_str, str))
            pojo_type = _get_type_from_java_classpath(type_str)
            assert (pojo_type == cls or isinstance(pojo_type, cls) or issubclass(pojo_type, cls))
            cls = pojo_type
            source_dict.pop('@type')

            pojo_identifier = source_dict['@id']
            source_dict.pop('@id')

        # If the target_type is serializable and the source is a dictionary
        if not isinstance(source_dict, dict):
            # noinspection PyUnresolvedReferences
            raise Exception(
                'Cannot deserialize non-dict type into target type of \"' + cls.__class__.__name__ + '!')

        # Get the exepected values for the constructor
        c_signature = inspect.signature(cls)
        c_params = c_signature.parameters

        # Iterate through argument identifiers
        for target_arg_identifier in list(c_params.keys()):
            param = c_params[target_arg_identifier]
            target_arg_identifier = param.name
            default = param.default

            if target_arg_identifier in c_params:
                target_arg_type = c_params[target_arg_identifier].annotation

                if target_arg_identifier in source_dict:
                    value = source_dict[target_arg_identifier]

                else:
                    if default != inspect.Parameter.empty:
                        value = default

                    else:
                        raise Exception('Class "' + cls.__name__ + '" ran into problems parsing argument "' +
                                        target_arg_identifier + '"!')

                if value is None:
                    source_dict[target_arg_identifier] = None

                else:
                    source_dict[target_arg_identifier] = _deserializer(
                        source_data=value,
                        target_type=target_arg_type,
                        object_map=object_map,
                        do_replacement=do_replacement
                    )

        if pojo_identifier in object_map:
            obj = object_map[pojo_identifier]
            assert (issubclass(cls, obj.__class__))
            obj.__class__ = cls
            d = copy.deepcopy(object_map[pojo_identifier].__dict__)
            for k in object_map[pojo_identifier].__dict__:
                if k.startswith('_'):
                    d.pop(k)
            source_dict = {**d, **source_dict}
            obj.__init__(**source_dict)

        else:
            input_dict = dict()
            for name in list(c_params.keys()):
                if name in source_dict:
                    input_dict[name] = source_dict[name]
                else:
                    _logger.info("Ignoring provided value for '" + name + "' because it is not a parameter for the " +
                                 "constructor of " + cls.__name__ + ".")

            obj = cls(**input_dict)

        if pojo_identifier is not None:
            if pojo_identifier not in object_map.keys():
                object_map[pojo_identifier] = obj
                obj._uuid = pojo_identifier

        return obj

    def validate(self) -> str:
        for k in list(self.__dict__.keys()):
            v = self.__dict__[k]

            if not k.startswith('_'):

                if isinstance(v, Serializable):
                    err = v.validate()
                    if err is not None:
                        return k + ':' + err

                elif isinstance(v, list):
                    for i in range(len(v)):
                        if isinstance(v[i], Serializable):
                            err2 = v[i].validate()
                            if err2 is not None:
                                return k + '[' + str(i) + ']:' + err2

        # noinspection PyTypeChecker
        for k in self.__class__._validator_values:
            # noinspection PyUnresolvedReferences
            v = self.__class__._validator_values[k]

            if isinstance(v, list):
                true_val = self.__dict__[k]

                if isinstance(true_val, list):
                    for vv in true_val:
                        if vv not in v:
                            return 'The value "' + str(vv) + '" is not valid! Valid values: [' + ','.join(v) + '].'

                else:
                    if true_val not in v:
                        return 'The value "' + str(true_val) + '" is not valid! Valid values: [' + ','.join(v) + '].'

            elif isinstance(v, tuple):
                true_val = self.__dict__[k]

                if isinstance(true_val, list):
                    for vv in true_val:
                        if not (v[0] <= float(vv) <= v[1]):
                            return (
                                    'The value for "' + k + '" of "' + str(vv) + '" is beyond the valid range of '
                                    + str(v[0]) + ' - ' + str(v[1]) + '!')

                else:
                    if not (v[0] <= float(true_val) <= v[1]):
                        return ('The value for "' + k + '" of "' + str(true_val) + '" is beyond the valid range of '
                                + str(v[0]) + ' - ' + str(v[1]) + '!')

            else:
                raise Exception('Unexpected type for validation: ' + type(v) + '!')


def deserialize(type: Type, value: str):
    if type == bool:
        if value.lower() == 'true':
            return True

        elif value.lower() == 'false':
            return False

        else:
            raise Exception('Cannot deserialize the value "' + value + '" to a boolean!')

    elif type == int:
        if value.isdigit():
            return int(value)

        else:
            raise Exception('Cannot deserialize the value "' + value + '" to an int!')

    elif type == float:
        try:
            rval = float(value)
            return rval
        except ValueError:
            raise Exception('Cannot deserialize the value "' + value + '" to a float!')

        pass

    elif type == str:
        return value

    elif issubclass(type, Enum):
        if value.startswith('"') and value.endswith('"'):
            value = value[1:-1]
        return type[value]

    elif issubclass(type, Serializable):
        return type.from_json_str(value)

    else:
        raise Exception('Cannot deserialize object of type "' + str(type) + '" from  "' + value + '"!')
