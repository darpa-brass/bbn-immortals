import inspect
import os
import subprocess
import sys
from copy import deepcopy


def clean_json_lines(lines):
    """
    :type lines: list[str] or list[bytes]
    :rtype: list[str]
    """
    return_lines = list()
    for l in lines:
        if isinstance(l, bytes):
            l = l.decode()

        s_l = l.strip()
        if not s_l.startswith('//') and not s_l.startswith('#'):
            return_lines.append(s_l)

    return return_lines


def clean_json_str(s):
    """
    :type s: str or bytes
    :rtype: str
    """
    if isinstance(s, bytes):
        s = s.decode()
    stripped = s.strip()

    if '\n' in stripped:
        lines = stripped.split('\n')
        return ''.join(clean_json_lines(lines))


# 
# 
# def parse_json_file(f: Union[TextIO, TextIOWrapper]) -> dict:
#     lines = f.readlines()
#     return json.loads(''.join(clean_json_lines(lines=lines)))
# 
# 
# def replace(target_list: List[str], string_to_replace: str, replacement_string: str) -> List[str]:
#     """
#     Given the targetList of Strings, it removes the first instance of
#     stringToReplace and replaces it with replacementString
#     """
#     current_list = list(target_list)
# 
#     for list_item in current_list:
#         if string_to_replace in list_item:
#             idx = current_list.index(list_item)
#             new_string = list_item.replace(string_to_replace, replacement_string)
#             target_list.remove(list_item)
#             target_list.insert(idx, new_string)
# 
#     return target_list
# 
# 
# def get_formatted_string_value(formatter_string: str, formatted_string: str, value_key: str) -> str:
#     """
#     Given a String formatted_string that was formatted using formatter_string,
#     returns the value that replaced value_key
#     """
# 
#     return_value = None
# 
#     formatter_substrings = []
#     formatter_tags = []
# 
#     # parse the values from the template string
#     while formatter_string is not None:
#         tag_start = formatter_string.find('{')
#         tag_end = formatter_string.find('}')
# 
#         if tag_start != -1 and tag_end != -1:
#             formatter_substrings.append(formatter_string[0:tag_start])
#             formatter_tags.append(formatter_string[tag_start + 1:tag_end])
#             formatter_string = formatter_string[tag_end + 1:]
# 
#         elif tag_start == -1 and tag_end == -1:
#             formatter_substrings.append(formatter_string)
#             formatter_string = None
# 
#         else:
#             raise Exception("Unbalanced formatting tags found!")
# 
#     while return_value is None:
#         garbage0 = formatter_substrings.pop(0)
#         garbage1 = formatter_substrings[0]
#         tag = formatter_tags.pop(0)
# 
#         value_start = formatted_string.index(garbage0) + len(garbage0)
# 
#         if garbage1 == '':
#             value_end = len(formatted_string)
#         else:
#             value_end = formatted_string.index(garbage1)
# 
#         if tag == value_key:
#             return_value = formatted_string[value_start:value_end]
#         else:
#             formatted_string = formatted_string[value_end:]
# 
#     return return_value
# 
# 
# def path_helper(dir_should_exist: bool, root_path: str, subpath: str) -> str:
#     """
#     Used for determining the validity and proper paths.
#     If should_exist is true, an exception will be thrown if it does not exist.
#     If len(args) > 1 and the path omitting the first path value (the default root) is absolute,
#      the first path value will be ignored.
#     """
#     if os.path.isabs(subpath):
#         path = subpath
#     else:
#         path = os.path.abspath(os.path.join(root_path, subpath))
# 
#     if dir_should_exist and not os.path.isdir(path) and not os.path.isdir(os.path.dirname(path)):
#         raise Exception('The file or directory "' + path + '" does not exist!')
# 
#     return path
# 
# 
def extract_environment_variables(string):
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


def extract_metavars(string):
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
    :type value_pool: dict[str] or None
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
        child_parents = deepcopy(parents)

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
        child_parents = deepcopy(parents)

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
    env_vars = extract_environment_variables(string)

    for env_var in env_vars:
        ev = os.getenv(env_var)
        if ev is None:
            raise Exception('Environment variable ' + env_var + ' is not defined!')
        else:
            string = _replace_env_var(string, env_var, ev)

    metavars = extract_metavars(string)

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


# 
# 
# def _fill_out_string_from_object_parents(string: str, value_pool: Dict[str, object],
#                                          parents: List[Dict[str, object]]) -> str:
#     metavars = extract_metavars(string)
# 
#     if len(metavars) == 0:
#         return string
# 
#     for metavar in metavars:
#         if metavar in value_pool:
#             val = value_pool[metavar]
#             assert (isinstance(val, str))
#             string = _replace_metavar(string, metavar, val)
#         else:
#             for p in reversed(parents):
#                 if metavar in p.__dict__:
#                     string = _replace_metavar(string, metavar, p.__dict__[metavar])
#                     break
#     return string
# 
# 
# def get_th_timestamp(time_seconds=None):
#     if time_seconds is None:
#         time_seconds = time.time()
#     return time.strftime("%Y-%m-%dT%H:%m:%S", time.gmtime(time_seconds)) + '.' + str(time_seconds % 1)[2:5] + 'Z'
# 
# 


def resolve_platform():
    """
    :rtype: str
    """

    platform = None

    if sys.platform == 'darwin':
        if subprocess.call(['which', 'brew'], stdout=subprocess.PIPE, stderr=subprocess.PIPE) != 0:
            raise Exception('Mac OSX detected, but homebrew is not installed! '
                            + 'Please install it from https://brew.sh/ (You won\'t regret it!)')
        else:
            platform = 'osx'

    elif sys.platform == 'linux2' or sys.platform == 'linux':
        if subprocess.call(['which', 'apt'], stdout=subprocess.PIPE, stderr=subprocess.PIPE) == 0:
            platform = 'ubuntu'

    if platform is None:
        raise Exception('Could not determine distribution of platform  "' + sys.platform + '"!')

    else:
        return platform

# 
# def order_dict_list(obj: Union[List, Dict]) -> Union[List, Dict]:
#     if isinstance(obj, dict):
#         return sorted((k, order_dict_list(v)) for k, v in obj.items())
#     if isinstance(obj, list):
#         return sorted(order_dict_list(x) for x in obj)
#     else:
#         return obj
# 
# 
# def load_class_by_classpath(classpath: str) -> Type:
#     split = classpath.split('.')
#     name = split[-1]
#     package = '.'.join(split[:-1])
#     m = importlib.import_module(package)
#     return getattr(m, name)
