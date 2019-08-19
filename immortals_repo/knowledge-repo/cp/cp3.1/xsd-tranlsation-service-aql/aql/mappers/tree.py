
def recalculate_hashes(el, path=None):
    ''''
    Recalculate hashes deeply in our elements tree, O(N) time complexity
    '''

    path = path or []
    if not el.children:
        return

    path.append(el.element_type)

    for i in el.children:
        # Avoid circular dependency
        if i.element_type not in path:
            recalculate_hashes(i, path)

        i.changed()

    path.pop()

    el.changed()
