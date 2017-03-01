import copy
import inspect
import json


# noinspection PyClassHasNoInit
class Serializable:
    """
    Provides A base class to ease conversion of objects from/to dictionaries
    """

    # noinspection PyPropertyDefinition
    @classmethod
    @property
    def _types(cls):
        raise NotImplementedError

    @classmethod
    def from_dict(cls, d):
        return cls._from_dict(copy.deepcopy(d))

    def to_dict(self):
        return Serializable._to_dict(self)

    def to_json_str(self):
        return json.dumps(self.to_dict())

    def to_json_str_pretty(self):
        return json.dumps(self.to_dict(), indent=4, separators=(',', ': '))

    @classmethod
    def from_json_str(cls, s):
        return cls.from_dict(json.loads(s))

    @classmethod
    def from_file(cls, filepath, override_values=None):
        return cls.from_dict(json.load(open(filepath, 'r')))

    @staticmethod
    def _to_dict(obj):

        if isinstance(obj, dict):
            return {k: Serializable._to_dict(obj=obj[k]) for k in obj.keys()}

        elif isinstance(obj, list):
            return map(lambda o: Serializable._to_dict(o), obj)

        elif isinstance(obj, Serializable):
            return Serializable._to_dict(copy.deepcopy(obj.__dict__))

        elif inspect.isclass(obj):
            raise Exception('Cannot serialize child object "' + obj.__class__.__name__)

        else:
            return obj

    # noinspection PyProtectedMember
    @classmethod
    def _from_dict(cls, source_dict):
        source_dict = copy.deepcopy(source_dict)

        # If the target_type is serializable and the source is a dictionary
        if not isinstance(source_dict, dict):
            raise Exception(
                'Cannot deserialize non-dict type into target type of \"' + cls.__class__.__name__ + '!')

        # Get the exepcted values for the constructor
        target_obj_args = inspect.getargspec(cls.__dict__['__init__']).args[1:]

        # Declare the output dictionary
        top_target_dict = {}

        # Iterate through argument identifiers
        for target_arg_identifier in target_obj_args:

            # If the identifier is a custom type
            if target_arg_identifier in cls._types:
                target_arg_type = cls._types[target_arg_identifier]

                # And if the custom type is a tuple, it is a collection of objects
                if isinstance(target_arg_type, tuple):

                    if target_arg_type[0] is dict:
                        target_dict_value_type = target_arg_type[1]
                        target_dict = {}
                        top_target_dict[target_arg_identifier] = target_dict

                        for target_dict_key in source_dict[target_arg_identifier]:
                            if issubclass(target_dict_value_type, Serializable):
                                target_dict[target_dict_key] = target_dict_value_type._from_dict(
                                    source_dict=source_dict[target_arg_identifier][target_dict_key])

                            else:
                                target_dict[target_dict_key] = source_dict[target_arg_identifier][target_dict_key]

                    elif target_arg_type[0] is list:
                        target_list_value_type = target_arg_type[1]
                        target_list = []
                        top_target_dict[target_arg_identifier] = target_list

                        for target_list_item in source_dict[target_arg_identifier]:
                            target_list.append(
                                target_list_value_type._from_dict(
                                    source_dict=target_list_item
                                )
                            )

                    else:
                        raise Exception('Undefined deserializable collection type "' + str(target_arg_type[1]) + '"!')

                else:
                    top_target_dict[target_arg_identifier] = target_arg_type._from_dict(
                        source_dict=source_dict[target_arg_identifier])

            else:
                # Otherwise it is a single object that should be serizliaed directly
                top_target_dict[target_arg_identifier] = source_dict[target_arg_identifier]

        return cls(**top_target_dict)


# noinspection PyClassHasNoInit
class ValidationCapable:
    # noinspection PyPropertyDefinition
    @classmethod
    @property
    def _valid_values(cls):
        raise NotImplementedError

    def validate(self):
        for k in self.__dict__.keys():
            v = self.__dict__[k]

            if not k.startswith('__'):

                if isinstance(v, ValidationCapable) and k != 'parent_config':
                    err = v.validate()
                    if err is not None:
                        return k + ':' + err

                elif isinstance(v, list):
                    for i in range(len(v)):
                        if isinstance(v[i], ValidationCapable):
                            err2 = v[i].validate()
                            if err2 is not None:
                                return k + '[' + str(i) + ']:' + err2

        for k in self._valid_values.keys():
            v = self._valid_values[k]

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
