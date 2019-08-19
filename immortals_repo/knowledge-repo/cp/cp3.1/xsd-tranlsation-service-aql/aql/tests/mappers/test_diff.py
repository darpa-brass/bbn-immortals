import entities

from mappers.tree import recalculate_hashes
from mappers.diff import compare
# from mappers.utils import tree_print

from pytest import fixture


@fixture
def basic_types():
    return {
        'boolean_type': entities.ElementType(name='xsd:boolean'),
        'token_type': entities.ElementType(name='xsd:token')
    }


def create_network_type(basic_types):
    network_children = [
        entities.Element(name='Owner', element_type=basic_types['token_type']),
        entities.Element(name='ReadOnly', element_type=basic_types['boolean_type']),
    ]

    return entities.ElementType(name='NetworkType', children=network_children)


def create_root_type(basic_types, network_type):
    # Root type
    root_children = [
        entities.Element(name='Network', element_type=network_type),
        entities.Element(name='Owner', element_type=basic_types['token_type']),
        entities.Element(name='ReadOnly', element_type=basic_types['boolean_type']),
        entities.Element(name='Service', element_type=basic_types['token_type']),
    ]

    return entities.ElementType(name='RootType', children=root_children)


def create_trees(root_type, new_root_type):
    root = entities.Element(name='Root', element_type=root_type)
    new_root = entities.Element(name='Root', element_type=new_root_type)

    # For now, they must be equal
    assert root == new_root

    return (root, new_root)


def others_empty(result, *exclude):
    # Utility to ensure other operations are empty
    for i, v in result.items():
        if i in exclude:
            continue

        assert v == []


@fixture()
def basic_trees(basic_types):
    first_network_type = create_network_type(basic_types)
    first_root_type = create_root_type(basic_types, first_network_type)

    new_network_type = create_network_type(basic_types)
    new_root_type = create_root_type(basic_types, new_network_type)

    return create_trees(first_root_type, new_root_type)


def test_removal_with_no_changes_must_return_empty(basic_trees):
    root, new_root = basic_trees

    result = compare(root, new_root)

    assert result['removals'] == []

    others_empty(result, 'removals')


def test_removal_with_one_level(basic_trees):
    root, new_root = basic_trees

    # Let's remove RootType->Owner
    del new_root.element_type.children[1]
    recalculate_hashes(new_root)
    recalculate_hashes(root)

    # Now, they must be different
    assert root != new_root

    result = compare(root, new_root)

    expected_removals = [
        '/Root/Owner',
    ]

    assert result['removals'] == expected_removals

    others_empty(result, 'removals')


def test_removal_two_levels(basic_trees):
    root, new_root = basic_trees

    # Let's remove RootType->Network->Owner
    del new_root.children[0].children[0]

    recalculate_hashes(new_root)
    recalculate_hashes(root)

    # Now, they must be different
    assert root != new_root

    result = compare(root, new_root)

    expected_removals = [
        '/Root/Network/Owner',
    ]

    assert result['removals'] == expected_removals

    others_empty(result, 'removals')


def test_rename_with_one_level_must_return_expected_operation(basic_trees):
    root, new_root = basic_trees

    # Change name of network node to Net
    new_root.children[0].name = 'Net'

    recalculate_hashes(new_root)
    recalculate_hashes(root)

    # Now, they must be different
    assert root != new_root

    result = compare(root, new_root)

    expected_renames = [
        ('/Root/Network', '/Root/Net'),
    ]

    assert result['renames'] == expected_renames

    others_empty(result, 'renames')


def test_rename_with_two_levels_must_return_expected_operation(basic_trees):
    root, new_root = basic_trees

    # Let's remove Root->Network->Person
    new_root.children[0].children[0].name = 'Person'

    recalculate_hashes(new_root)
    recalculate_hashes(root)

    # Now, they must be different
    assert root != new_root

    result = compare(root, new_root)

    expected_renames = [
        ('/Root/Network/Owner', '/Root/Network/Person'),
    ]

    assert result['renames'] == expected_renames

    others_empty(result, 'renames')


def test_rename_in_two_different_levels_must_return_expected_operation(basic_trees):
    root, new_root = basic_trees

    # Let's remove Root->Network->Person
    new_root.children[0].children[0].name = 'Person'

    # Change name of Root->ReadOnly
    new_root.children[2].name = 'DisableChanges'

    recalculate_hashes(new_root)
    recalculate_hashes(root)

    # Now, they must be different
    assert root != new_root

    result = compare(root, new_root)

    expected_renames = [
        ('/Root/Network/Owner', '/Root/Network/Person'),
        ('/Root/ReadOnly', '/Root/DisableChanges'),
    ]

    # As we are using sets, we need to sort our result to compare it.
    result['renames'].sort(key=lambda x: x[0])

    assert result['renames'] == expected_renames

    others_empty(result, 'renames')


def test_rename_with_similiar_name_must_return_expected_association(basic_trees, basic_types):
    root, new_root = basic_trees

    # Renaming other fields
    new_root.children[1].name = 'NewOwner'
    new_root.children[3].name = 'ServiceNew'

    # Two new fields with same type but different names to be used in renames
    root_name = entities.Element(name='Name', element_type=basic_types['token_type'])
    root_foo = entities.Element(name='Foo', element_type=basic_types['token_type'])
    root_operation = entities.Element(name='Operation', element_type=basic_types['token_type'])

    new_root_name = entities.Element(name='NewName', element_type=basic_types['token_type'])
    new_root_foo = entities.Element(name='Bar', element_type=basic_types['token_type'])
    new_root_operation = entities.Element(name='OperationNew', element_type=basic_types['token_type'])

    root.add_child(root_name)
    root.add_child(root_foo)
    root.add_child(root_operation)

    new_root.add_child(new_root_name)
    new_root.add_child(new_root_foo)
    new_root.add_child(new_root_operation)

    recalculate_hashes(new_root)
    recalculate_hashes(root)

    # Now, they must be different
    assert root != new_root

    result = compare(root, new_root)

    expected_renames = [
        ('/Root/Foo', '/Root/Bar'),
        ('/Root/Name', '/Root/NewName'),
        ('/Root/Operation', '/Root/OperationNew'),
        ('/Root/Owner', '/Root/NewOwner'),
        ('/Root/Service', '/Root/ServiceNew'),
    ]

    # As we are using sets, we need to sort our result to compare it.
    result['renames'].sort(key=lambda x: x[0])

    assert result['renames'] == expected_renames

    others_empty(result, 'renames')


def test_additions_in_one_level_must_return_expected_operation(basic_trees, basic_types):
    root, new_root = basic_trees

    enabled = entities.Element(name='Enabled', element_type=basic_types['boolean_type'])
    new_root.add_child(enabled)

    recalculate_hashes(new_root)
    recalculate_hashes(root)

    # Now, they must be different
    assert root != new_root

    result = compare(root, new_root)

    expected_additions = [
        '/Root/Enabled'
    ]

    assert result['additions'] == expected_additions

    others_empty(result, 'additions')


def test_additions_in_two_levels_must_return_expected_operation(basic_trees, basic_types):
    root, new_root = basic_trees

    enabled = entities.Element(name='Enabled', element_type=basic_types['boolean_type'])
    new_root.children[0].add_child(enabled)

    recalculate_hashes(new_root)
    recalculate_hashes(root)

    # Now, they must be different
    assert root != new_root

    result = compare(root, new_root)

    expected_additions = [
        '/Root/Network/Enabled'
    ]

    assert result['additions'] == expected_additions

    others_empty(result, 'additions')


def test_relocation_with_one_level_must_return_expected_result(basic_trees, basic_types):
    root, new_root = basic_trees

    service = new_root.children[3]

    # Moving Root->Service to Root->Network->Service
    new_root.children[0].add_child(service)
    new_root.remove_child(service)

    recalculate_hashes(new_root)
    recalculate_hashes(root)

    # Now, they must be different
    assert root != new_root

    result = compare(root, new_root)

    expected_relocations = [
        ('/Root/Service', '/Root/Network/Service')
    ]

    expected_removals = [
        '/Root/Service'
    ]

    assert result['relocations'] == expected_relocations
    assert result['removals'] == expected_removals

    others_empty(result, 'relocations', 'removals')


def test_relocation_with_new_sub_element_must_return_expected_result(basic_trees, basic_types):
    root, new_root = basic_trees

    # Moving Root->Service to new node Root->Module->Service
    service = new_root.children[3]
    new_root.remove_child(service)

    module_children = [
        entities.Element(name='Owner', element_type=basic_types['token_type']),
        service
    ]

    module_type = entities.ElementType(name='ModuleType', children=module_children)

    module = entities.Element(name='Module', element_type=module_type)

    new_root.add_child(module)

    recalculate_hashes(new_root)
    recalculate_hashes(root)

    # Now, they must be different
    assert root != new_root

    result = compare(root, new_root)

    expected_relocations = [
        ('/Root/Service', '/Root/Module/Service')
    ]

    expected_additions = [
        '/Root/Module'
    ]

    expected_removals = [
        '/Root/Service'
    ]

    assert result['relocations'] == expected_relocations
    assert result['additions'] == expected_additions
    assert result['removals'] == expected_removals

    others_empty(result, 'relocations', 'additions', 'removals')


def test_circular_dependency_rename_tag_must_return_expected_result(basic_trees, basic_types):
    root, new_root = basic_trees
    network = new_root.children[0]

    # Rename Root->Network->ReadOnly attr
    network.children[1].name = 'OnlyRead'

    module_children = [
        entities.Element(name='Owner', element_type=basic_types['token_type']),
        network  # Adding a ciclic dependency here
    ]

    module_type = entities.ElementType(name='ModuleType', children=module_children)

    module = entities.Element(name='Module', element_type=module_type)

    network.add_child(module)

    recalculate_hashes(new_root)
    recalculate_hashes(root)

    expected_renames = [
        ('/Root/Network/ReadOnly', '/Root/Network/OnlyRead'),
    ]

    expected_additions = [
        '/Root/Network/Module',
    ]

    result = compare(root, new_root)

    assert result['renames'] == expected_renames
    assert result['additions'] == expected_additions

    others_empty(result, 'renames', 'additions')


def test_imediate_circular_dependency_rename_must_return_expected_result(basic_trees, basic_types):
    root, new_root = basic_trees

    # Circular dependency of network just inside network
    root_network = root.children[0]
    root_network.add_child(root_network)

    # Add a new module node inside network of our ORIGINAL tree
    root_module_type = entities.ElementType(name='ModuleType', children=[
        entities.Element(name='Owner', element_type=basic_types['token_type']),
    ])

    root_network.add_child(entities.Element(name='Module', element_type=root_module_type))

    # Add a new module node inside network of our NEW tree
    module_type = entities.ElementType(name='ModuleType', children=[
        entities.Element(name='Name', element_type=basic_types['token_type']),
    ])

    network = new_root.children[0]
    network.add_child(entities.Element(name='Module', element_type=module_type))

    # Adding a circular dependency
    network.add_child(network)

    recalculate_hashes(new_root)
    recalculate_hashes(root)

    result = compare(root, new_root)

    expected_renames = [
        ('/Root/Network/Module/Owner', '/Root/Network/Module/Name')
    ]

    assert result['renames'] == expected_renames
    others_empty(result, 'renames')
