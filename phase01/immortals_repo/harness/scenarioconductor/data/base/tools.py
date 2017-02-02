import __builtin__
import os
import types


def path_helper(dir_should_exist, root_path, subpath):
    if os.path.isabs(subpath):
        path = subpath
    else:
        path = os.path.abspath(os.path.join(root_path, subpath))

    if dir_should_exist and not os.path.isdir(path) and not os.path.isdir(os.path.dirname(path)):
        raise Exception('The file or directory "' + path + '" does not exist!')

    return path


def fillout_object(obj, value_pool={}):
    r_count = 1

    while r_count > 0:
        r_count = 0

        for key in obj.__dict__:
            # For all values in the obj dictionary
            a = obj.__dict__[key]
            t = type(a)

            if t is str or t is unicode:
                # If it is a string and it contains the replacement formatting
                value, inner_r_count = value_helper(a, obj, value_pool)

                if inner_r_count > 0:
                    r_count += inner_r_count
                    obj.__dict__[key] = value

            elif t is list:
                for i in range(len(a)):
                    i_t = type(a[i])
                    if i_t is str or i_t is unicode:
                        value, inner_r_count = value_helper(a[i], obj, value_pool)

                        if inner_r_count > 0:
                            r_count += inner_r_count
                            a[i] = value

            elif t is dict:
                swap_map = {}
                keys = a.keys()
                for k in keys:
                    k_t = type(k)

                    if k_t is str or k_t is unicode:
                        value, inner_r_count = value_helper(k, obj, value_pool)

                        if inner_r_count > 0:
                            r_count += inner_r_count
                            swap_map[k] = value

                for s in swap_map.keys():
                    a[swap_map[s]] = a[s]
                    a.pop(s)

                for k in a.keys():
                    v = a[k]
                    v_t = type(v)

                    if v_t is str or v_t is unicode:
                        value, inner_r_count = value_helper(v, obj, value_pool)

                        if inner_r_count > 0:
                            r_count += inner_r_count
                            a[k] = value


def value_helper(input_string, parent_object=None, value_pool={}):
    output_string = input_string  # type: str

    replacement_count = 0

    # Get first replaceable value location
    idx0 = output_string.find('{', 0)
    idx1 = output_string.find('}', idx0)

    # while there is a valid value for replacement
    while idx0 >= 0 and idx1 >= 0:
        # Get the identifier
        val = output_string[idx0 + 1:idx1]

        new_value = _determine_value(val, parent_object, value_pool)
        if val != new_value:
            # If the value has changed, set it, and increment the replacement count
            output_string = output_string[:idx0] + _determine_value(val, parent_object, value_pool) + output_string[
                                                                                                      idx1 + 1:]
            replacement_count += 1

        # Update the indices
        idx0 = output_string.find('{', idx1)
        idx1 = output_string.find('}', idx0)

    if output_string != input_string:
        return [output_string, replacement_count]
    else:
        return [input_string, 0]


def _determine_value(value_identifier, parent_object=None, value_pool={}):
    if value_identifier in value_pool:
        # Return the value from the value pool if it exists
        return value_pool[value_identifier]

    else:
        # Otherwise, loop through the parents looking for it
        parent = parent_object
        while parent is not None:
            attr = None if not hasattr(parent, value_identifier) else getattr(parent, value_identifier)

            # If it is not a single replacement value, return it
            if attr is not None and attr[0:1] is not '{' and attr[-1:] is not '}':
                return attr

            elif hasattr(parent, 'parent_config'):
                # If there is another parent, set it
                parent = getattr(parent, 'parent_config')

            else:
                # Otherwise, clear the parent to exit the loop
                parent = None

    # If a value has not been returned at this point, none exists given the configuration
    err = "could not resolve a value for {VAL}in {VPOOL0} and its parent configs!"
    raise Exception(err.format(VAL=value_identifier, VPOOL0=parent_object))


def dictify(obj):
    t = type(obj)

    if t is types.NoneType or t is types.BooleanType or t is types.IntType or t is types.LongType or t is types.FloatType or t is types.StringType or t is types.UnicodeType:
        return obj

    elif t is types.DictType:
        d = {}
        for key in obj:
            print key
            d[key] = dictify(obj[key])
        return d

    elif t is types.ListType:
        l = []
        for v in obj:
            l.append(dictify(v))
        return l

    elif t is types.ClassType or t is types.InstanceType:
        d = {}
        for key in obj.__dict__:
            print key
            v = obj.__dict__[key]
            t = type(v)
            if not (key == 'parent_config' or t is __builtin__.classmethod or t is types.FunctionType):
                d[key] = dictify(v)
        return d

    else:
        raise RuntimeError('Unexpected object type \'' + str(t) + '\'!')
