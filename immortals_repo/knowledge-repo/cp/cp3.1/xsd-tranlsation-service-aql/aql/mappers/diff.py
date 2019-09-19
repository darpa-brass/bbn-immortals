
def _join_paths(elems):
    return '/{}'.format('/'.join([i.name for i in elems]))


def _type_in_path(element_type, path):
    for el in path:
        if el.element_type == element_type:
            return True

    return False


# TODO: A LOT OF THINGS TO IMPROVE IN THIS FUNCTION! WHEN WE HAVE TIME. PLEASE!
def _compare(first_el, second_el, path=None, result=None):
    result = result or {
        'renames': [],
        'additions': [],
        'relocations': [],
        'removals': [],
    }

    path = path or []

    # Avoiding circular dependency
    if _type_in_path(first_el.element_type, path):
        return result

    first_set = set(first_el.children)
    second_set = set(second_el.children)

    removals = first_set - second_set
    additions = second_set - first_set

    if len(removals) == 0 and len(additions) == 0:
        return result

    path.append(first_el)

    to_add = additions.copy()
    to_remove = removals.copy()

    # Store renaming options between all field combinations
    renaming_options = set()

    for removed in removals:
        if removed not in to_remove:
            continue

        for added in additions:
            if added not in to_add:
                continue

            # They are equal but subtrees are diferent, so we need to
            # search deeper for this difference
            if removed.self_hash == added.self_hash:
                # Remove both elements from removals add additions
                # as they are the same
                to_add.remove(added)
                to_remove.remove(removed)

                result = _compare(removed, added, path, result)
                break

            # Here, store best rename option to analize in next step
            if removed.type_hash == added.type_hash:
                renaming_options.add(
                    (removed.similarity(added),
                     _join_paths(path + [removed]),
                     removed,
                     _join_paths(path + [added]),
                     added))

    # Now, sorting renaming possibilities
    # by best similarity between all field options at beginning
    renaming_options_list = sorted(list(renaming_options),
                                   key=lambda i: i[0], reverse=True)

    # Now, just iterate over it and add it to rename options
    for _, removed_path, removed, added_path, added in renaming_options_list:
        # Ignore already checked nodes
        if removed not in to_remove or added not in to_add:
            continue

        result['renames'].append((removed_path, added_path))
        to_remove.remove(removed)
        to_add.remove(added)

    # Let's check now similarity between elements
    for removed in removals:
        if removed not in to_remove:
            continue

        for added in additions:
            if added not in to_add:
                continue

            if not removed.is_similar(added):
                continue

            # Similar elements

            # Only add rename if they have different names
            if removed.name != added.name:
                result['renames'].append((_join_paths(path + [removed]), _join_paths(path + [added])))

            # Remove both elements from removals add additions
            # as they are the same
            to_add.remove(added)
            to_remove.remove(removed)

            result = _compare(removed, added, path, result)
            break

    for elem in to_add:
        result['additions'].append(_join_paths(path + [elem]))

    # After additions, we need to check one last time if removals were not
    # moved to a deeper level
    for removed in removals:
        for added in additions:
            moved_path = _find_moved_path(removed, added, path)
            if moved_path:
                # Check if this moved path is present in additions (or its parent node)
                found = False
                for added_path in result['additions']:
                    if added_path == moved_path or moved_path.startswith(moved_path):
                        found = True
                        break

                if not found:
                    continue

                result['relocations'].append((_join_paths(path + [removed]), moved_path))

                # Remove from additions if it self is present
                if moved_path in result['additions']:
                    result['additions'].remove(moved_path)

    # Store removals
    for elem in to_remove:
        result['removals'].append(_join_paths(path + [elem]))

    path.pop()

    return result


def _find_moved_path(needle, haystack, path):
    '''
    Search for an element (needle) in haystack deeply, if it founds, returns
    its proper path
    '''

    # Avoiding circular dependency
    if haystack in path:
        return None

    path.append(haystack)

    moved_path = None

    for child in haystack.children:
        if needle == child:
            moved_path = _join_paths(path + [child])
            break

        moved_path = _find_moved_path(needle, child, path)
        if moved_path:
            break

    path.pop()
    return moved_path


def compare(first_el, second_el):
    return _compare(first_el, second_el)
