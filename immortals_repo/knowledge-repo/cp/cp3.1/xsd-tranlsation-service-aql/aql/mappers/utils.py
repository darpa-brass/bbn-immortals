from constants import PRIMITIVE_TYPES_DEFAULT, SIMPLE_TYPES_DEFAULT


def gen_default_value(type_name):
    if not type_name:
        return None

    type_name = str(type_name).strip()
    if ':' in type_name:
        _, name = type_name.split(':')
    else:
        name = type_name

    if type_name.lower() in SIMPLE_TYPES_DEFAULT:
        return SIMPLE_TYPES_DEFAULT.get(type_name.lower())

    return PRIMITIVE_TYPES_DEFAULT.get(name)


def tree_print(element, path=None):
    '''
    Utility for debuging, print all given tree
    '''

    path = path or []

    for i in range(len(path)):
        print('--', end='')

    print('>', len(path), element, hash(element), element.attrs, end='')

    for i, k in enumerate(path):
        if k.element_type == element.element_type:
            print(f' --->> [CIRCULAR DEPENDENCY OF "{element}" original level {i}]')
            return

    print('')

    # Adding current field to our "tree path" to avoid circular dependency
    path.append(element)

    for i in element.children:
        tree_print(i, path)

    path.pop()
