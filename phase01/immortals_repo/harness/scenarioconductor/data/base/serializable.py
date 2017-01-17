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


# class TestClass0(Serializable):
#     _types = {}
# 
#     def __init__(self, val0, val1):
#         self.val0 = val0
#         self.val1 = val1
# 
#     def do_this(self):
#         pass
# 
#     @staticmethod
#     def do_this_too():
#         pass
# 
# 
# # noinspection PyPep8Naming,PyPep8Naming
# class TestClass1(Serializable):
#     _types = {
#         'valA': TestClass0,
#         'valB': (list, TestClass0),
#         'valC': (dict, TestClass0)
#     }
# 
#     def __init__(self, valA, valB, valC, valD):
#         self.valA = valA
#         self.valB = valB
#         self.valC = valC
#         self.valD = valD
