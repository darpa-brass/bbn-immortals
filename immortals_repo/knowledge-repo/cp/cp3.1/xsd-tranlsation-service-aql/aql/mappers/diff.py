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
        'reorders': [],
    }

    path = path or []

    # Avoiding circular dependency
    if _type_in_path(first_el.element_type, path):
        return result

    # Checking elements order
    if not is_children_with_same_order(first_el, second_el):
        result['reorders'].append(_join_paths(path + [first_el]))

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
                renaming_options.add((removed.similarity(added), removed, added))

    # Now, sorting renaming possibilities
    # by best similarity between all field options at beginning
    renaming_options_list = sorted(list(renaming_options),
                                   key=lambda i: i[0], reverse=True)

    # Now, just iterate over it and add it to rename options
    for _, removed, added in renaming_options_list:
        # Ignore already checked nodes
        if removed not in to_remove or added not in to_add:
            continue

        # Lets check if this element renamed is not in a choices group
        # and other element being added is in the same choices group
        # if so, we need to remove this other element being added.
        for added_with_group in additions:
            if added_with_group not in to_add or added_with_group == added:
                continue

            if second_el.element_type.in_same_choices_group(added, added_with_group):
                to_add.remove(added_with_group)

        result['renames'].append((_join_paths(path + [removed]), _join_paths(path + [added])))

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
        if elem.lxml_must_create():
            result['additions'].append(_join_paths(path + [elem]))

    # After additions, we need to check one last time if removals were not
    # moved to a deeper level
    for removed in removals:
        for added in additions:
            # Searching for elements moved UP
            moved_path = _find_moved_path(added, removed, path)
            if moved_path:
                original_moved_path = _join_paths(path + [added])
                result['relocations'].append((moved_path, original_moved_path))

                if original_moved_path in result['additions']:
                    result['additions'].remove(original_moved_path)

            # Finding for elements moved DOWN
            moved_path = _find_moved_path(removed, added, path)
            if moved_path:
                # Check if this moved path is present in additions (or its parent node)
                found = False
                for added_path in result['additions']:
                    if added_path == moved_path or moved_path.startswith(added_path):
                        found = True
                        break

                if not found:
                    continue

                original_moved_path = _join_paths(path + [removed])

                result['relocations'].append((original_moved_path, moved_path))
                result['removals'].append(original_moved_path)

                if removed in to_remove:
                    to_remove.remove(removed)

                # Remove from additions if it is present
                if moved_path in result['additions']:
                    result['additions'].remove(moved_path)

                # Remove from renames if original path for relocation is present
                for rename_index, renames in enumerate(result['renames']):
                    rename_from, rename_to = renames

                    # if is equal to origin element, we must to add destination element to additions
                    # and remove renames entry
                    if rename_from == original_moved_path:
                        result['additions'].append(rename_to)
                        result['renames'].pop(rename_index)
                        break

    # Store removals
    for elem in to_remove:
        result['removals'].append(_join_paths(path + [elem]))

    path.pop()

    return result


def is_children_with_same_order(first_el, second_el):
    # Let's consider only self hash for our children
    first_children = [i.self_hash for i in first_el.children_original_order]
    second_children = [i.self_hash for i in second_el.children_original_order]

    common_children = set(first_children) & set(second_children)

    if len(common_children) == 0:
        return True

    iterator = zip([i for i in first_el.children_original_order if i.self_hash in common_children],
                   [i for i in second_el.children_original_order if i.self_hash in common_children])

    for first_child, second_child in iterator:
        if first_child.self_hash != second_child.self_hash:
            return False

    return True


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
