import os

"""
Given the targetList of Strings, it removes the first instance of
stringToReplace and replaces it with replacementString
"""
def replace(target_list, string_to_replace, replacement_string):
    current_list = list(target_list)

    for list_item in current_list:
        if string_to_replace in list_item:
            idx = current_list.index(list_item)
            new_string = list_item.replace(string_to_replace, replacement_string)
            target_list.remove(list_item)
            target_list.insert(idx, new_string)

    return target_list

"""
Given a String formatted_string that was formatted using formatter_string,
returns the value that replaced value_key
"""
def get_formatted_string_value(formatter_string, formatted_string, value_key):
    return_value = None

    formatter_substrings = []
    formatter_tags = []

    # parse the values from the template string
    while formatter_string is not None:
        tag_start = formatter_string.find('{')
        tag_end = formatter_string.find('}')

        if tag_start != -1 and tag_end != -1:
            formatter_substrings.append(formatter_string[0:tag_start])
            formatter_tags.append(formatter_string[tag_start+1:tag_end])
            formatter_string = formatter_string[tag_end+1:]

        elif tag_start == -1 and tag_end == -1:
            formatter_substrings.append(formatter_string)
            formatter_string = None;

        else:
            raise Exception("Unbalanced formatting tags found!")

    while return_value is None:
        garbage0 = formatter_substrings.pop(0)
        garbage1 = formatter_substrings[0]
        tag = formatter_tags.pop(0)

        value_start = formatted_string.index(garbage0)+len(garbage0)

        if garbage1 == '':
            value_end = len(formatted_string)
        else:
            value_end = formatted_string.index(garbage1)

        if tag == value_key:
            return_value = formatted_string[value_start:value_end]
        else:
            formatted_string = formatted_string[value_end:]

    return return_value


"""
Used for determining the validity and proper paths.

If should_exist is true, an exception will be thrown if it does not exist.

If len(args) > 1 and the path omitting the first path value (the default root) is absolute, the first path value will be ignored.
"""
def path_helper(dir_should_exist, root_path, subpath):

    if os.path.isabs(subpath):
        path = subpath
    else:
        path = os.path.abspath(os.path.join(root_path, subpath))

    if dir_should_exist and not os.path.isdir(path) and not os.path.isdir(os.path.dirname(path)):
        raise Exception('The file or directory "' + path + '" does not exist!')

    return path
