
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
