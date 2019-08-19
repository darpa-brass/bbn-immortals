from xml.dom import minidom

from pytest import fixture

from mappers.tree import build_tree
import entities


@fixture
def two_levels_dom(shared_datadir):
    with open(shared_datadir / 'two_levels.xsd', 'r') as xsd_file:
        return minidom.parseString(xsd_file.read())


def test_build_tree_must_return_expected_tree(two_levels_dom):
    tree = build_tree(two_levels_dom)

    # Basic types
    boolean_type = entities.ElementType(name='xsd:boolean')
    token_type = entities.ElementType(name='xsd:token')

    # Network type
    network_children = [
        entities.Element(name='ReadOnly', element_type=boolean_type),
        entities.Element(name='Owner', element_type=token_type),
    ]
    network_type = entities.ElementType(name='NetworkType', children=network_children)

    # Root type
    root_children = [
        entities.Element(name='ReadOnly', element_type=boolean_type),
        entities.Element(name='Owner', element_type=token_type),
        entities.Element(name='Network', element_type=network_type),
    ]

    root_type = entities.ElementType(name='RootType', children=root_children)

    root_element = entities.Element(name='Root', element_type=root_type)

    # Our tree must have our root element
    assert tree == root_element


def test_element_creation_with_expected_attrs(two_levels_dom):
    tree = build_tree(two_levels_dom)

    # Ordered values
    root_ro = tree.children[2]
    assert root_ro.name == 'ReadOnly'

    expected_attrs = {
        'default': 'true',
        'min_occurs': '1'
    }

    assert root_ro.attrs == expected_attrs

    network = tree.children[0]
    assert network.name == 'Network'

    network_ro = network.children[1]
    assert network_ro.name == 'ReadOnly'

    expected_attrs = {
        'default': 'false',
        'min_occurs': '0'
    }

    assert network_ro.attrs == expected_attrs
